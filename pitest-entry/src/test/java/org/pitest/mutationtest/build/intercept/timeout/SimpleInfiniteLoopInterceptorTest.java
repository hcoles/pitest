package org.pitest.mutationtest.build.intercept.timeout;

import static org.assertj.core.api.Assertions.assertThat;
import static org.pitest.bytecode.analysis.MethodMatchers.named;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
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
import org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveIncrementsMutator;
import org.pitest.util.ResourceFolderByteArraySource;

public class SimpleInfiniteLoopInterceptorTest {
  ClassloaderByteArraySource    source = ClassloaderByteArraySource
      .fromContext();

  SimpleInfiniteLoopInterceptor testee = new SimpleInfiniteLoopInterceptor();
  GregorMutater                 mutator;

  @Before
  public void setUp() {
    ClassloaderByteArraySource source = ClassloaderByteArraySource
        .fromContext();
    Collection<MethodMutatorFactory> mutators = asList(
        RemoveIncrementsMutator.REMOVE_INCREMENTS_MUTATOR);
    mutator = new GregorMutater(source, True.<MethodInfo> all(), mutators);
  }
  
  @Test
  public void shouldFilterMutationsThatRemoveForLoopIncrement() {
    List<MutationDetails> mutations = mutator.findMutations(ClassName.fromClass(MutateMyForLoop.class));
    assertThat(mutations).hasSize(2);
    
    testee.begin(forClass(MutateMyForLoop.class));
    Collection<MutationDetails> actual = testee.intercept(mutations, mutator);
    testee.end();
    
    assertThat(actual).hasSize(1);   
  }
  
  @Test
  public void shouldFindInfiniteLoopsInForLoopWithNoIncrement() {
    checkFiltered(HasForLoops.class, "infiniteNoIncrement");
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
  public void shouldNotFindInfiniteLoopsInForLoopWithConditionalReturn() {
    checkNotFiltered(HasForLoops.class, "returnsInLoop");
  }
  
  @Test
  public void shouldNotFindInfiniteLoopsInForLoopWithConditionalBreak() {
    checkNotFiltered(HasForLoops.class, "brokenByBreak");
  }
  
  @Test
  public void shouldFindInfiniteLoopsInForLoopWithNoIncrementAndBranchedContents() {
    checkFiltered(HasForLoops.class, "infiniteMoreComplex");    
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
  
  private void checkNotFiltered(Class<?> clazz, String method) {
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
  
  private void checkFiltered(Class<?> clazz, String method) {
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



  private Option<MethodTree> parseMethodFromCompiledResource(Class<?> clazz,
      Compiler compiler, String method) {
    ResourceFolderByteArraySource source = new ResourceFolderByteArraySource();
    Option<byte[]> bs = source.getBytes("loops/" + compiler.name() + "/" + ClassName.fromClass(clazz).getNameWithoutPackage().asJavaName());
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

class MutateMyForLoop {
  public int normalLoop(int j) {
    for (int i = 0; i != 10; i++) {
      System.out.println("" + i);
    }
    // but leave my increment alone
    return j++;
  }
}

enum Compiler {
  eclipse, javac
}

