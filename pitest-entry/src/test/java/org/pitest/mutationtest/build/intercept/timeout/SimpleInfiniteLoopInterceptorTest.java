package org.pitest.mutationtest.build.intercept.timeout;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.pitest.bytecode.analysis.MethodMatchers.named;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;
import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.bytecode.analysis.MethodTree;
import org.pitest.classinfo.ClassName;
import org.pitest.classpath.ClassloaderByteArraySource;
import org.pitest.functional.Option;
import org.pitest.functional.predicate.True;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.gregor.GregorMutater;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.mutators.NonVoidMethodCallMutator;
import org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveIncrementsMutator;
import org.pitest.util.ResourceFolderByteArraySource;

public class SimpleInfiniteLoopInterceptorTest {
  ClassloaderByteArraySource    source = ClassloaderByteArraySource
      .fromContext();

  SimpleInfiniteLoopInterceptor testee = new SimpleInfiniteLoopInterceptor();


  @Test
  public void shouldFilterMutationsThatRemoveForLoopIncrement() {
    GregorMutater mutator = createMutator(RemoveIncrementsMutator.REMOVE_INCREMENTS_MUTATOR);
    List<MutationDetails> mutations = mutator.findMutations(ClassName.fromClass(MutateMyForLoop.class));
    assertThat(mutations).hasSize(2);
    
    testee.begin(forClass(MutateMyForLoop.class));
    Collection<MutationDetails> actual = testee.intercept(mutations, mutator);
    testee.end();
    
    assertThat(actual).hasSize(1);   
  }
  
  @Test
  public void shouldNotFilterMutationsInMethodsThatAppearToAlreadyHaveInfiniteLoops() {
    GregorMutater mutator = createMutator(RemoveIncrementsMutator.REMOVE_INCREMENTS_MUTATOR);
    // our analysis incorrectly identifies some loops as infinite - must skip these
    List<MutationDetails> mutations = mutator.findMutations(ClassName.fromClass(DontFilterMyAlreadyInfiniteLoop.class));
    assertThat(mutations).hasSize(1);
    
    testee.begin(forClass(DontFilterMyAlreadyInfiniteLoop.class));
    Collection<MutationDetails> actual = testee.intercept(mutations, mutator);
    testee.end();
    
    assertThat(actual).hasSize(1);   
  }
  
  @Test
  public void shouldFilterMutationsThatRemoveIteratorNextCalls() {
    GregorMutater mutator = createMutator(NonVoidMethodCallMutator.NON_VOID_METHOD_CALL_MUTATOR);
    List<MutationDetails> mutations = mutator.findMutations(ClassName.fromClass(MutateMyForEachLoop.class));
    assertThat(mutations).hasSize(3);
    
    testee.begin(forClass(MutateMyForEachLoop.class));
    Collection<MutationDetails> actual = testee.intercept(mutations, mutator);
    testee.end();
    
    assertThat(actual).hasSize(2);   
  }
  
  @Test
  public void shouldFindInfiniteLoopsInForLoopWithNoIncrement() {
    checkFiltered(HasForLoops.class, "infiniteNoIncrement");
  }
    
  @Test
  @Ignore("not implemented yet")
  public void shouldFindInfiniteLoopsInForLoopWithhNoConditional() {
    checkFiltered(HasForLoops.class, "infiniteNoConditional");
  }
    
  
  @Test
  public void shouldNotFindInfiniteLoopsInCodeWithNoLoops() {
    checkNotFiltered(HasForLoops.class, "noLoop");
  }
  
  @Test
  public void shouldNotFindInfiniteLoopsInValidForLoop() {
    checkNotFiltered(HasForLoops.class, "normalLoop");
  }
    
  @Test
  public void shouldNotFindInfiniteLoopsInForLoopWithNonConditionalIncrementInLoop() {
    checkNotFiltered(HasForLoops.class, "incrementInsideLoop");
  }
    
  @Test
  public void shouldNotFindInfiniteLoopsInForLoopWithConditionalIncrementInLoop() {
    checkNotFiltered(HasForLoops.class, "incrementInsideLoopConditionally");
  }
  
  @Test
  @Ignore("need thought")
  public void willFindInfiniteLoopsInForLoopWithConditionalReturn() {
    // although these loops are likely not infinite, pragmatically it is
    // worth avoiding mutating them as they are likely to be long running
    checkFiltered(HasForLoops.class, "returnsInLoop");
  }
  
  @Test
  @Ignore
  public void shouldNotFindInfiniteLoopsInForLoopWithConditionalBreak() {
    // works with javac, but eclipse makes forward jumps that we don't understand
    checkNotFiltered(HasForLoops.class, "brokenByBreak");
  }
  
  @Test
  public void shouldFindInfiniteLoopsInForLoopWithNoIncrementAndBranchedContents() {
    checkFiltered(HasForLoops.class, "infiniteMoreComplex");    
  }
  
  @Test
  public void shouldFindInfiniteForLoopsWhenOtherBranchedCodePresent() {
    checkFiltered(HasForLoops.class, "ifForInfiniteNoIncrement");  
  }
  
  @Test
  public void shouldNotFindInfiniteLoopsInWhileLoopWithIncrement() {
    checkNotFiltered(HasWhileLoops.class, "simpleWhile");
  }
  
  @Test
  public void shouldNotFindInfiniteLoopsInDoWhileLoopWithIncrement() {
    checkNotFiltered(HasWhileLoops.class, "simpleDoWhile");
  }
  
  @Test
  public void willNotFindInfiniteLoopsInInfiniteWhileLoop() {
    // would prefer it to filter
    checkNotFiltered(HasWhileLoops.class, "infiniteWhile"); 
  }
  
  @Test
  public void shouldNotFindInfiniteLoopInForEach() {
    checkNotFiltered(HasIteratorLoops.class, "forEach");
  }
  
  @Test
  public void shouldNotFindInfiniteLoopInHandCodedInteratorLoop() {
    checkNotFiltered(HasIteratorLoops.class, "iteratorLoop");
  }
  
  @Test
  public void shouldFindInfiniteLoopInIteratorLoopWithoutNext() {
    checkFiltered(HasIteratorLoops.class, "infiniteNoNextCall");
  }
  
  @Test
  public void shouldMatchRealInfiniteLoopFromJodaTimeMutants() {
    checkNotFiltered(ClassName.fromString("LocalDate"),"withPeriodAdded");
    checkFiltered(ClassName.fromString("LocalDateMutated"),"withPeriodAdded");
    checkFiltered(ClassName.fromString("MonthDayMutated"),"withPeriodAdded");
    checkFiltered(ClassName.fromString("BaseChronologyMutated"),"validate");
  }
  
  private void checkNotFiltered(Class<?> clazz, String method) {
    checkNotFiltered(ClassName.fromClass(clazz), method);
  }
  
  private void checkFiltered(Class<?> clazz, String method) {
    checkFiltered(ClassName.fromClass(clazz), method);
  }
  
  
  private void checkNotFiltered(ClassName clazz, String method) {
    boolean testedSomething = false;
    for (Compiler each : Compiler.values()) {
      Option<MethodTree> mt = parseMethodFromCompiledResource(clazz, each,
          method);
      for (MethodTree m : mt) {
        assertThat(SimpleInfiniteLoopInterceptor.INFINITE_LOOP
            .matches(m.instructions()))
                .describedAs("With " + each
                    + " compiler matched when it shouldn't " + toString(m))
                .isFalse();
        testedSomething = true;
      }

    }
    if (!testedSomething) {
      fail("No samples found for test");
    }
  }
  
  private void checkFiltered(ClassName clazz, String method) {
    boolean testedSomething = false;
    for (Compiler each : Compiler.values()) {
      Option<MethodTree> mt = parseMethodFromCompiledResource(clazz, each,
          method);
      for (MethodTree m : mt) {
        assertThat(SimpleInfiniteLoopInterceptor.INFINITE_LOOP
            .matches(m.instructions()))
                .describedAs("With " + each
                    + " compiler did not match as expected " + toString(m))
                .isTrue();
        testedSomething = true;
      }
    }
    if (!testedSomething) {
      fail("No samples found for test");
    }
  }
  
  private String toString(MethodTree mt) {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();

    TraceMethodVisitor mv = new TraceMethodVisitor(new Textifier());
    
    mt.rawNode().accept(mv);
    PrintWriter pw = new PrintWriter(bos);
    mv.p.print(pw);
    pw.close();
    
    return "Byte code is \n" + new String(bos.toByteArray());
  }

  private Option<MethodTree> parseMethodFromCompiledResource(ClassName clazz,
      Compiler compiler, String method) {
    ResourceFolderByteArraySource source = new ResourceFolderByteArraySource();
    Option<byte[]> bs = source.getBytes("loops/" + compiler.name() + "/" + clazz.getNameWithoutPackage().asJavaName());
    for (byte[] bytes : bs) {
      ClassTree tree = ClassTree.fromBytes(bytes);
      return tree.methods().findFirst(named(method)); 
    }
    return Option.none();
  }
  
  private ClassTree forClass(Class<?> clazz) {
    byte[] bs = source.getBytes(clazz.getName()).value();
    return ClassTree.fromBytes(bs);
  }

  private Collection<MethodMutatorFactory> asList(MethodMutatorFactory ...factories ) {
    return Arrays.asList(factories);
  }
  

  private GregorMutater createMutator(MethodMutatorFactory ...factories) {
    ClassloaderByteArraySource source = ClassloaderByteArraySource
        .fromContext();
    Collection<MethodMutatorFactory> mutators = asList(factories);
    return new GregorMutater(source, True.<MethodInfo> all(), mutators);
  }
}

class HasForLoops {
  
  public void noLoop() {
    int i = 0;
    if (i++ > 0) {
      System.out.println("" + i);
    }
  }
  
  public void normalLoop() {
    for (int i = 0; i != 10; i++) {
      System.out.println("" + i);
    }
  }
  
  public void incrementInsideLoop() {
    for (int i = 0; i != 10;) {
      System.out.println("" + i);
      i = i + 4;
    }
  }
  
  public void incrementInsideLoopConditionally() {
    for (int i = 0; i != 10;) {
      System.out.println("" + i);
      if ( i != 10 ) {
        i = i + 4;
      }
    }
  }
  
  public void infiniteNoIncrement() {
    for (int i = 0; i != 10;) {
      System.out.println("" + i);
    }
  }
  
  public void infiniteMoreComplex() {
    for (int i = 0; i != 10;) {
      System.out.println("" + i);
      if ( i != 7) {
        System.out.println("7 " + i);
      } else {
        continue;
      }
    }
  }
  
  public void ifForInfiniteNoIncrement(int j) {
    if (j > 11) {
      return;
    }
    
    for (int a = 0; a != 10; a++) {
      System.out.println("" + a);
    }
    
    for (int i = 0; i != 10;) {
      System.out.println("" + i);
    }
  }
  
  public void returnsInLoop() {
    int j = 0;
    for (int i = 0; i != 10;) {
      j = j + 1;
      if ( j > 10 ) {
        return;
      }
    }
  }
  
  public void brokenByBreak() {
    int j = 0;
    for (int i = 0; i != 10;) {
      if ( j > 10 ) {
        break;
      }
      j = j + 1;
    }
  }
  
  public void infiniteNoConditional() {
    for (int i = 0; ; i++) {
      System.out.println("" + i);
    }
  }
  
  public void infiniteAlwaysTrue() {
    for (int i = 0;true; i++) {
      System.out.println("" + i);
    }
  }
}

class HasWhileLoops {
  public void simpleWhile() {
    int i = 0;
    while (i != 10) {
      System.out.println("" + i);
      i = i + 1;
    }
  }
  
  public void simpleDoWhile() {
    int i = 0;
    do {
      System.out.println("" + i);
      i = i + 2;
    } while ( i != 10);
  }
  
  public void infiniteWhile() {
    while(true) {
      System.out.println("");
    }
  }
}

class HasIteratorLoops {
  public void forEach(List<String> ss) {
    for (String each : ss) {
      System.out.println(each);
    }
  }
  
  public void iteratorLoop(List<String> ss) {
    for(Iterator<String> it = ss.iterator(); it.hasNext(); ) {
      String s = it.next();
      System.out.println(s);
    }
  }
  
  public void infiniteNoNextCall(List<String> ss) {
    for(Iterator<String> it = ss.iterator(); it.hasNext(); ) {
      System.out.println(it);
    }
  }
  
}

class MutateMyForLoop {
  public int normalLoop(int j) {
    for (int i = 0; i != 10; i++) {
      System.out.println("" + i);
    }
    // but leave my increment alone
    return j++;
  }
}

class MutateMyForEachLoop {
  public void forEach(List<String> ss) {
    for (String each : ss) {
      System.out.println(each);
    }
  }
}


class DontFilterMyAlreadyInfiniteLoop {
  public int normalLoop(int j) {
    for (int i = 0; i != 10;) {
      System.out.println("" + i);
    }
    return j++;
  }
}

enum Compiler {
  eclipse, javac
}

