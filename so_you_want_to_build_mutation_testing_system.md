# So you want to build a mutation testing system

## Introduction

There have been a lot mutation testing systems, but very few of them have seen successful use in industry.

This document is a set of notes that might be helpful for anyone thinking of implementing a mutation testing system for another language.

It represents some of the things we learnt while creating pitest. The choices made by pitest are not necessarily the best choices for your system. Some of these choices are appropriate only because of the particular quirks of Java and the JVM, and some of them are simply the first idea that we had.

## Things to consider

Generating mutations is just one part of what a mutation testing system must do, but it is important to think about the broader picture.

Conceptually the main things a mutation testing system needs to do are:

* Generate mutants
* Insert the mutants into a running program
* Select tests to run against the mutant
* Run the tests
* Detect (and reliably terminate) any infinite loops
* Report the results

It is important to think carefully about how insertion and infinite loop termination will work upfront, as well as the more obvious problem of generation.

## How pitest solves things

### Very high level architecture

Pitest has one main controlling process. As it analyses mutations it creates a number of child processes (or minions) that do the work of running tests against each mutant.

The code under test is never loaded in the main process.

Part of pitest must share a JVM with the code under test within each minion. This part of pitest has minimal dependencies. The dependencies it does have are relocated to new packages so that they do not conflict with any dependencies the code under test may have.

### Generating mutants

Pitest generates mutants by manipulating the bytecode. This is very fast, but has some drawbacks. It can sometimes be hard to explain to the programmer that the mutation is, and occasionally the mutation doesn't map back to a mistake the programmer could actually make. We call these **junk** mutations.

When pitest mutates bytecode generated from Java source it doesn't generate many junk mutations as most bytecode instructions map back predictably to a source code construct. If you feed it bytecode generated from Scala source it will produce a lot of junk because the Scala compiler generates lots of code to support the language features.

Generating mutants is a two stage process.

First the main process scans the bytecode passed to it and creates a list of the possible mutations it could create.

Each mutant is identified by the combination of:

* The mutation operator that could create it
* The class + method signature
* The index to the bytecode instruction that is mutated

This information takes up very little space in memory, so the main process can hold a list of all possible mutations even for very large programs.

The bytecode for the mutant is never generated in the main process.

### Inserting mutants

Each minion is given a list of mutation identifiers it must analyse (currently this is pushed to it on creation, but it would be preferable for the minion to pull the information when it is available to do work).

The minion then generates the bytecode for the mutant (this is very quick) and inserts that bytecode into itself using Java's instrumentation api.

This insertion scheme is very efficient because:

1. Nothing is written to disk (io is expensive)
2. Multiple mutants can be analysed in one minion (process startup is expensive)

The one drawback of this scheme is that code in static initializers is not re-run. Any mutants in static initializers would appear to have no effect. To mitigate this pitest tried to avoid mutating this code.

### Selecting tests

Before any mutants are generated pitest inserts coverage probes into the code to be mutated and runs all the available tests. It then generates a map that shows which lines of code are executed by each individual test.

Only tests that execute the instruction that has been mutated are run against a mutant.

Pitest previously tried other selection schemes including

1. Running test based on naming conventions
2. Using static analyses to work out which methods a test called

But coverage based selection has proven to be much more efficient without requiring and conventions to be followed when writing tests.

It does have one drawback however - code in static initializers (i.e. that is run only when a class is loaded) will appear to be executed only by the first test to cause that class to load.

### Infinite loop termination

If pitest detects that a mutants has created an infinite loop, or caused a minion to become unresponsive for any other reason (e.g. memory exhaustion) it kills the child process into which it was inserted.

This is quite a heavy weight solution, but since it is impossible to reliably kill a thread in Java it is the only one that works reliably. Earlier versions of pitest did try using threads and classloaders along with escape instructions added to the code under test, but this proved unreliable.

The separate JVM processes also robustly ensure that any state in the JVM (e.g. static variables) cannot cause one mutation to effect the result of a different mutation that was analysed in a different JVM.

In order to kill a process pitest must first decide if an infinite loop has occurred.

This is done based on timings. The normal execution time of each test is recorded during the coverage stage. If the test takes x times as long (plus a fudge factor) then the mutant is considered to have caused an infinite loop.

This scheme introduces a certain degree of non-determinism as timings might be affected by other processes running on the same machine.

A better scheme is to insert probes into the code and count how many times they are called when the test is run against the unumated code. If the probe is hit significantly more times when the mutant is present the process can be killed.

## Other thoughts and considerations

### Early exit

A very basic 50% performance improvement is to make sure you stop analysing a mutation as soon as a test has failed.

Academic systems tend to keep running so that a matrix can be produced of which tests kills which mutant. Although this is nice to have it is expensive to produce - stopping early should at least be an option.

### Test splitting

There is a lot of complicated code in pitest that splits tests up into the smallest possible individually executable units. The default in Java is for all the tests defined in single test to be run as one (at least for JUnit and TestNG).

If pitest didn't split tests this way, it would have to execute all tests in a class even if the first of them failed. But the worthiness of such extra complexity strongly depends on how tests are written and how fast each test runs.

## Other solutions

### Naive implementations

The simplest implementation of a mutation testing system solves the generation and insertion problems by

1. Compiling mutant version of the program to disk
2. Launching a new process for each mutant

For Java this approach is unworkable as starting a new JVM and loading the required classes for anything but a toy program takes several seconds. Disk space can also become an issue if a binary of the entire program must be generated.

Most mutation testing system that have been abandoned after a few months or years of work have taken this approach.

### AST manipulation

Manipulating an AST would be a sensible way to create mutations with several advantages

1. The mutants can be easily described to the user via diffs etc
2. No junk mutations will be created

Depending on the tech stack the cost of creating the mutants from the AST is likely to be higher than manipulating bytecode. Whether this difference in cost is significant is probably language dependent, but generally the cost of generating mutants is less significant than the cost of analysing them.

For this approach to work well it is probably highly desirable that your tech stack can compile from memory to memory (i.e. no need to write the mutated AST or the compiled program to disk).

This will of course depend on whether your insertion mechanism requires the executable code to be written to disk.

### Mutant schemata

Mutant schemata are programs where mutants can be enabled/disabled by setting flags.

Something like

```java
class MutateMe {
  public static boolean MUTANT_1;
  public static boolean MUTANT_2;
  public static boolean MUTANT_3;

  public boolean aMethod(int i) {
    if (MUTANT_1) {
      return i > 10;
    } else if (MUTANT_2) {
      return i == 10;
    } else if (MUTANT_3) {
      return i != 10;
    } else {
      // original code
      return i < 10;
    }
  }

}

```

So all mutants are contained in a single version of the code.

Schemata have some nice properties

1. Insertion is cheap (just toggle a flag)
2. Only one compile cycle to produce many mutants (if you are mutating the AST)
3. A single linked binary can contain all mutants

But will result in large classes - you may hit language limits.


