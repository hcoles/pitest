[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.pitest/pitest/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/org.pitest/pitest)
[![Build Status](https://travis-ci.org/hcoles/pitest.png?branch=master)](https://travis-ci.org/hcoles/pitest)

======

Pitest (aka PIT) is a state of the art mutation testing system for Java and the JVM.

Read all about it at http://pitest.org

## Releases

### 1.1.11-SNAPSHOT

Nothing yet

### 1.1.10

* #260 - Initial support for mutating Kotlin code
* #264 - Support for yatspec runner (thanks @theangrydev)
* Break build when more than `maxSurviving` mutants survive

### 1.1.9

* #132 - Allow analysis of only files touched in last commit (thanks Tomasz Luch)

### 1.1.8

* #239 - Provide a shortcut to set history files via maven
* #240 - Support for regexes (thanks sebi-hgdata)
* #243 - Use ephemeral ports to communicate with minions

### 1.1.7

* #196 - Raise minimum java version to 1.6
* #231 - Process hangs

### 1.1.6

* #10  - Add maven report goal (thanks jasonmfehr) 
* #184 - Remove undocumented project file feature
* #219 - Performance improvement for report generation (thanks tobiasbaum) 
* #190 - Allow custom properties for plugins

Note this release contains a known issue (#231). Please upgrade.

### 1.1.5

* Fix for #148 - Stackoverflow with TestNG data providers when using JMockit
* Fix for #56 - Not reporting junit incompatibilities
* Fix for #174 - Invalid linecoverage.xml with static initializers 
* Fix for #183 - Can't run GWTMockito tests
* Fix for #179 - Broken `includeLaunchClasspath=false` on Windows
* #173 - Read exclusions and groups from maven surefire config

### 1.1.4

* #157         - Support maven -DskipTests flag (thanks lkwg82)
* Fix for #163 - Should not include test tree in coverage threshold
* #166         - Allow classpath exclusions for maven plugin (thanks TomRK1089)
* #155         - Restore Java 5 compatibility
* Fix for #148 - Issue with JMockit + TestNG (thanks estekhin and KyleRogers)

### 1.1.3

* Fix for #158 - Tests incorrectly excluded from mutants
* Fix for #153 - SCM plugin broken for maven 2
* Fix for #152 - Does not work with IBM jdk

### 1.1.2

* Fix for #150 - line coverage under reported

### 1.1.1

* Block based coverage (fixes 79/131)
* End support for running on Java 5 (java 5 bytecode still supported)
* Skip flag for maven modules (#106)
* Stop declaring TestNG as a dependency 
* New parameter propagation mutator (thanks UrsMetz)

### 1.1.0

* Change scheme for identifying mutants (see https://github.com/hcoles/pitest/issues/125)
* Support alternate test apis via plugin system
* Report error when supplied mutator name does not match (thanks artspb)
* Report exit codes from coverage child process (thanks KyleRogers)
* Treat JUnit tests with ClassRule annotation as one unit (thanks devmop)

Please note that any stored history files or sonar results are invalidated by this release.

### 1.0.0

* Switch version numbering scheme
* Upgrade to ASM 5.0.2
* Fix for #114 - fails to run for java 8 when -parameters flag is set
* #99 Support additionalClasspathElements property in maven plugin (thanks artspb)
* #98 Do not mutate java 7 try with resources (thanks @artspb)
* #109 extended remove conditional mutator (thanks @vrthra)


### 0.33

* Move to Github
* Upgrade of ASM to support Java 8 bytecode (thanks to "iirekm") 
* Partial support for JUnit categories (thanks to "chrisr")
* New Remove Increments Mutator (thanks to Rahul Gopinath)
* Minor logging improvements (thanks to Kyle Rogers aka Stephan Penndorf)
* Fix for #92 - broken maven 2 support
* Fix for #75 - incorrectly ignored tests in classes with both @Ignore and @BeforeClass / @AfterClass

### 0.32

* restores java 7 compatibility
* new remove conditionals mutator
* support for mutating static initializers with TestNG
* properly isolate classpaths when running via Ant
* break builds on coverage threshold
* allow JVM to be specified 
* support user defined test selection strategies
* support user defined output format
* support user defined test prioritisation
* fix for issue blocking usage with [Robolectric](http://robolectric.org/)

Note, setup for Ant based projects changes in this release. See [ant setup](http://pitest.org/quickstart/ant/) for details on usage.

### 0.31

* Maven 2 compatibility restored
* Much faster line coverage calculation
* Fix for #78 - Error when PowerMockito test stores mock as member

This release also changes a number of internal implementation details, some of which may be of interest/importance to those maintaining tools that
integrate with PIT.

Mutations are now scoped internally as described in [https://groups.google.com/forum/#!topic/pitusers/E0-3QZuMYjE](https://groups.google.com/forum/#!topic/pitusers/E0-3QZuMYjE)

A new class (org.pitest.mutationtest.tooling.EntryPoint) has been introduced that removes some of the duplication that existed in the various ways of launching mutation analysis. 

### 0.30

* Support for parametrized [Spock](http://code.google.com/p/spock/) tests
* Support for [JUnitParams](http://code.google.com/p/junitparams/) tests
* Fix for #73 - JUnit parameterised tests calling mutee during setup failing during mutation phase
* Fix to #63 - ant task fails when empty options supplied
* Ability to override maven options from command line
* Ability to fail a build if it does not achieve a given mutation score
* Performance improvement when tests use @BeforeClass or @AfterClass annotations
* Slightly improved scheduling over multiple threads
* Improved maven multi project support
* Integration with source control for maven users


### 0.29

* Incremental analysis (--historyInputLocation and --historyOutputLocation)
* Inlined code detection turned on by default
* Quieter loggging by default
* Improved Java 7 support
* Upgrade of ASM from 3.3 to 4
* Fix for concurrency issues during coverage collection
* Fix for #53 - problems with snapshot junit versions
* Fix for #59 - duplicate dependencies set via maven


### 0.28

* Inlined finally block detection (--detectInlinedCode)
* New experimental switch statement mutator (contributed by Chris Rimmer)
* Do not mutate Groovy classes
* Fix for #33 - set user.dir to match surefire
* Fix for #43 - optionally suppress timestamped folders (--timestampedReports=true/false)
* Fix for #44 - concurrent modification exception when gathering coverage
* Fix for #46 - incorrect setting of flags by ant task
* Smaller memory footprint for main process
* Faster coverage gathering for large codebases
* Faster classpath scanning for large codebases
* Support for JUnit 3 suite methods
* Fixes for incorrect detection of JUnit 3 tests

**Known issue** - Fix for #33 may not resolve issue for maven 2 users.

Detection of Groovy code has not yet been tested with Groovy 2 which may generate substantially different
byte code to earlier versions.

### 0.27

* Much prettier reports
* Now avoids mutating assert statements
* Removed inScopeClasses option - use targetClasses and targetTests instead
* Fix for 100% CPU usage when child JVM crashes
* Fix for #35 #38 - experimental member variable mutator now corrects stack
* Fix for #39 - order of classpath elements now maintained when running from maven

**Upgrading users may need to modify their build due to removal of the inScopeClasses parameter**

### 0.26

* Ant support
* New experimental mutator for member variables 
* Fix for #12 #27 - no longer hangs when code under test launches non daemon threads
* Fix for #26 - now warns when no test library found on classpath
* Fix for #30 - now errors if mutated classes have no line or source debug
* Fix for #32 - now correctly handles of JUnit assumptions 

**Known issue** - The new member variable mutator may cause errors in synchronized errors. The mutator is
however disabled by default, and the generated errors are correctly handled by PIT.

### 0.25

* TestNG support (experimental)
* Fix for issue where mutations in nested classes not isolated from each other
* Fix for broken classpath isolation for projects using xstream
* Improved handling of JUnit parametrized tests
* Ability to limit mutations to specific classpath roots (--mutableCodePaths)
* Ability to add non launch classpath roots (--classPath) (experimental)
* Read configuration values from XML (experimental)
* Option to not throw error when no mutations found
* Consistent ordering of classes in HTML report
* Statistics written to console
* Classes no longer loaded during initial classpath scanning
* New syntax to easily enable all mutation operations

### 0.24

* JMockit support
* Option to output results in XML or CSV
* Fix for #11
* Improved INLINE_CONSTS mutator

### 0.23

* Fix for issue 7 - source files not located

### 0.22

* Upgrade of Xstream to 1.4.1 to enable OpenJDK 7 support
* Fix for #5 - corruption of newline character in child processes
* Ability to set child process launch arguments

### 0.21

* Significant performance improvements
* Support for powermock via both classloader (requires PowerMockIgnore annotation) and java agent
* Minor error reporting and usability improvements
* Fix for major defect around dependency analysis
* PIT dependencies no longer placed on classpath when running via maven
* Support for excluding certain classes or tests
* Support for verbose logging

### 0.20

* Limit number of mutations per class
* Upgrade xstream to 1.3.1
* Make available from maven central

### 0.19

* Built in enum methods now excluded from mutation
* Fixed bug around reporting of untested classes
* Support for excluding tests greater than a certain distance from class
* Support for excluding methods from mutation analysis
* Performance improvements
* Removed support for launching mutation reports from JUnit runner

### 0.18

* First public release

## Credits

Pitest is mainly the work of [me](https://twitter.com/0hjc) but has benefited from contributions from many others. 

Notable contributions not visible [here](https://github.com/hcoles/pitest/graphs/contributors) as they were made before this code was migrated to github include 

* Nicolas Rusconi - Ant Task
* Struan Kerr-Liddell - Improvements to html report
* Stephan Pendorf - Multiple improvments including improved mutators
 
Although PIT does not incorporate any code from the Jumble project (http://jumble.sourceforge.net/), the Jumble codebase was used as a guide when developing some aspects of PIT.

## Other stuff

The codebase is checked up on in a few places that give slower feedback than the github hooks.

[maven2 on IBM JDK check](https://hjc.ci.cloudbees.com/job/maven2_triangle_example/)

[Sonarqube analysis](http://nemo.sonarqube.org/dashboard/index/793182)


