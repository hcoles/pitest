package org.pitest.mutationtest.build.intercept.timeout;

import static org.assertj.core.api.Assertions.assertThat;
import static org.pitest.bytecode.analysis.MethodMatchers.named;

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
  public void shouldNotFindInfiniteLoopsInCodeWithNoLoops() {
    checkNotFiltered(HasForLoops.class, Compiler.javac, "noLoop");
    checkNotFiltered(HasForLoops.class, Compiler.eclipse, "noLoop");
  }

  @Test
  public void shouldNotFindInfiniteLoopsInValidForLoopJavaC() {
    checkNotFiltered(HasForLoops.class, Compiler.javac, "normalLoop");
  }
  
  @Test
  public void shouldNotFindInfiniteLoopsInValidForLoopEclipse() {
    checkNotFiltered(HasForLoops.class, Compiler.eclipse, "normalLoop");
  }
  
  @Test
  public void shouldNotFindInfiniteLoopsInForLoopWithNonConditionalIncrementInLoopJavac() {
    checkNotFiltered(HasForLoops.class, Compiler.javac, "incrementInsideLoop");
  }
  
  @Test
  public void shouldNotFindInfiniteLoopsInForLoopWithNonConditionalIncrementInLoopEclipse() {
    checkNotFiltered(HasForLoops.class, Compiler.eclipse, "incrementInsideLoop");
  }
  
  @Test
  public void shouldNotFindInfiniteLoopsInForLoopWithConditionalIncrementInLoopJavac() {
    checkNotFiltered(HasForLoops.class, Compiler.javac, "incrementInsideLoopConditionally");
  }
  
  @Test
  public void shouldNotFindInfiniteLoopsInForLoopWithConditionalIncrementInLoopEclipse() {
    checkNotFiltered(HasForLoops.class, Compiler.eclipse, "incrementInsideLoopConditionally");
  }
  
  @Test
  public void shouldFindInfiniteLoopsInForLoopWithNoIncrementJavac() {
    checkFiltered(HasForLoops.class, Compiler.javac, "infiniteNoIncrement");
  }
  
  @Test
  public void shouldFindInfiniteLoopsInForLoopWithNoIncrementEclipse() {
    checkFiltered(HasForLoops.class, Compiler.eclipse, "infiniteNoIncrement");
  }
  
  @Test
  public void shouldFindInfiniteLoopsInForLoopWithNoIncrementAndBranchedContents() {
    checkFiltered(HasForLoops.class, Compiler.javac, "infiniteMoreComplex");
    checkFiltered(HasForLoops.class, Compiler.eclipse, "infiniteMoreComplex");    
  }
  
  @Test
  public void shouldNotFindInfiniteLoopsInWhileLoopWithIncrement() {
    checkNotFiltered(HasWhileLoops.class, Compiler.javac, "simpleWhile");
    checkNotFiltered(HasWhileLoops.class, Compiler.eclipse, "simpleWhile");
  }
  
  @Test
  public void shouldNotFindInfiniteLoopsInDoWhileLoopWithIncrement() {
    checkNotFiltered(HasWhileLoops.class, Compiler.javac, "simpleDoWhile");
    checkNotFiltered(HasWhileLoops.class, Compiler.eclipse, "simpleDoWhile");
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

  private void checkFiltered(Class<?> clazz, Compiler compiler, String method) {
    MethodTree mt = parseMethodFromCompiledResource(clazz, compiler, method);
    assertThat(SimpleInfiniteLoopInterceptor.INFINITE_LOOP.matches(mt.instructions()))
    .describedAs("Did not match as expected " + toString(mt))
    .isTrue();
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

  private void checkNotFiltered(Class<?> clazz, Compiler compiler, String method) {
    MethodTree mt = parseMethodFromCompiledResource(clazz, compiler, method);
    assertThat(SimpleInfiniteLoopInterceptor.INFINITE_LOOP.matches(mt.instructions()))
    .describedAs("Matched when it shouldn't " + toString(mt))
    .isFalse();
  }

  private MethodTree parseMethodFromCompiledResource(Class<?> clazz,
      Compiler compiler, String method) {
    ResourceFolderByteArraySource source = new ResourceFolderByteArraySource();
    byte[] bs = source.getBytes("loops/" + compiler.name() + "/" + ClassName.fromClass(clazz).getNameWithoutPackage().asJavaName()).value();
    ClassTree tree = ClassTree.fromBytes(bs);
    return tree.methods().findFirst(named(method)).value();
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
  javac, eclipse
}

