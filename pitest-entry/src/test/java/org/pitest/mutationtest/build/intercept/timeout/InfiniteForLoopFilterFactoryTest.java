package org.pitest.mutationtest.build.intercept.timeout;

import static org.assertj.core.api.Assertions.assertThat;
import static org.pitest.bytecode.analysis.MethodMatchers.forLocation;

import java.util.Collection;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.classinfo.ClassName;
import org.pitest.classpath.ClassloaderByteArraySource;
import org.pitest.mutationtest.engine.Location;
import org.pitest.mutationtest.engine.MethodName;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.gregor.GregorMutater;
import org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveIncrementsMutator;

public class InfiniteForLoopFilterFactoryTest extends InfiniteLoopBaseTest {
  ClassByteArraySource    source = ClassloaderByteArraySource.fromContext();

  InfiniteForLoopFilter testee = new InfiniteForLoopFilter();

  @Override
  InfiniteLoopFilter testee() {
    return testee;
  }
  
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
  public void shouldFindInfiniteLoopsInForLoopWithNoIncrement() {
    checkFiltered(HasForLoops.class, "infiniteNoIncrement");
  }
    
  @Test
  @Ignore("not implemented yet")
  public void shouldFindInfiniteLoopsInForLoopWithNoConditional() {
    checkFiltered(HasForLoops.class, "infiniteNoConditional");
  }
  
  @Test
  public void cannotFindInfiniteLoopsInForWhenCounterDeclaredElsewhere() {
    checkNotFiltered(HasForLoops.class, "infiniteDeclarationNotInFor");
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
  @Ignore("depends on compiler")
  public void mightTreatLoopsAsInifiniteDespitePotentialBreakByCondtional() {
    checkFiltered(HasForLoops.class, "incrementInsideLoopConditionally");
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
  public void shouldMatchRealInfiniteLoopFromJodaTimeMutants() {      
    Location l1 = Location.location(ClassName.fromString("org.joda.time.field.BaseDateTimeField")
        , MethodName.fromString("set")
        , "(Lorg/joda/time/ReadablePartial;I[II)[I");
    checkFiltered(ClassName.fromString("BaseDateTimeFieldMutated"),forLocation(l1));
    
    checkNotFiltered(ClassName.fromString("LocalDate"),"withPeriodAdded");
    checkFiltered(ClassName.fromString("LocalDateMutated"),"withPeriodAdded");
    
    checkNotFiltered(ClassName.fromString("MonthDay"),"withPeriodAdded");
    checkFiltered(ClassName.fromString("MonthDayMutated"),"withPeriodAdded");
    
    checkFiltered(ClassName.fromString("BaseChronologyMutated"),"validate");
    checkFiltered(ClassName.fromString("BaseChronologyMutated2"),"set");
    
    Location l = Location.location(ClassName.fromString("org.joda.time.MonthDay")
        , MethodName.fromString("withPeriodAdded")
        , "(Lorg/joda/time/ReadablePeriod;I)Lorg/joda/time/MonthDay;");
    checkFiltered(ClassName.fromString("MonthDayMutated2"),forLocation(l));
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
  
  public void infiniteDeclarationNotInFor(int size) {
    for (; size> 2; ) {
      System.out.println("" + size);
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


class DontFilterMyAlreadyInfiniteLoop {
  public int normalLoop(int j) {
    for (int i = 0; i != 10;) {
      System.out.println("" + i);
    }
    return j++;
  }
}


