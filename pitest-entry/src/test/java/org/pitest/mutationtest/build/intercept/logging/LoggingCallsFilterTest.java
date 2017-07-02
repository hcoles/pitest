package org.pitest.mutationtest.build.intercept.logging;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.assertj.core.api.Condition;
import org.junit.Before;
import org.junit.Test;
import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.classinfo.ClassName;
import org.pitest.classpath.ClassloaderByteArraySource;
import org.pitest.functional.predicate.True;
import org.pitest.mutationtest.build.InterceptorType;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.gregor.GregorMutater;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.config.Mutator;

public class LoggingCallsFilterTest {
  
  LoggingCallsFilter testee = new LoggingCallsFilter(Collections.singleton("java/util/logging"));
  
  Mutater mutator;
  
  @Before
  public void setUp() {
    ClassloaderByteArraySource source = ClassloaderByteArraySource.fromContext();
    Collection<MethodMutatorFactory> mutators = Mutator.defaults();
    mutator = new GregorMutater(source, True.<MethodInfo>all(), mutators);
  }

  @Test
  public void shouldDeclareTypeAsFilter() {
    assertThat(testee.type()).isEqualTo(InterceptorType.FILTER);
  }
  
  @Test
  public void shouldLeaveMutantsNotOnLoggingLinesUntouched() {
    ClassName clazz = ClassName.fromClass(DoesNotLog.class);
    List<MutationDetails> input = mutator.findMutations(clazz);
    Collection<MutationDetails> actual = analyseWithTestee(DoesNotLog.class);
    
    assertThat(actual).containsExactlyElementsOf(input);
  }
  
  @Test
  public void shouldFilterMutantsOnSameLineAsLoggingCall() {
    Collection<MutationDetails> actual = analyseWithTestee(Logs.class);
    assertThat(actual).isEmpty();
  }
  
  @Test
  public void shouldNotFilterMutantsOnLinesOtherThanLoggingLine() {
    Collection<MutationDetails> actual = analyseWithTestee(LogsAndDoesNot.class);
    assertThat(actual).doNotHave(mutantsIn("logs"));
    assertThat(actual).haveAtLeast(1, mutantsIn("noLog"));
    assertThat(actual).haveExactly(3, mutantsIn("both"));
  }
  
  private Condition< MutationDetails> mutantsIn(final String name) {
  return new  Condition< MutationDetails>("mutants in the methed " + name) {
    @Override
    public boolean matches(MutationDetails value) {
        return value.getId().getLocation().getMethodName().name().equals(name);
    }
  };
  }

  private Collection<MutationDetails> analyseWithTestee(Class<?> clazz) {
    ClassName name = ClassName.fromClass(clazz);
    testee.begin(treeFor(clazz));
    List<MutationDetails> input = mutator.findMutations(name);
    return testee.intercept(input, mutator);
  }

  ClassTree treeFor(Class<?> clazz) {
    ClassloaderByteArraySource source = ClassloaderByteArraySource.fromContext();
    return ClassTree.fromBytes(source.getBytes(clazz.getName()).value());
  }
  
}

class DoesNotLog {
  public int foo(int i) {
    return i++;
  }
}

class Logs {
  private static final Logger LOGGER = Logger.getLogger(Logs.class.getName());
  public void foo(int i) {
    LOGGER.log(Level.INFO, "lot " + " of " + "string " + "conact " + i);
  }
}

class LogsAndDoesNot {
  private static final Logger LOGGER = Logger.getLogger(Logs.class.getName());
  
  public void logs(int i) {
    LOGGER.log(Level.INFO, "lot " + " of " + "string " + "conact " + i);
  }
  
  public int noLog(int i) {
    return i++;
  }
  
  public int both(int i) {
    i = i + 42;
    
    LOGGER.log(Level.INFO, "lot " + " of " + "string " + "conact " + i);
    return i++;
  }
}
