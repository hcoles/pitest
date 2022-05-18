package org.pitest.mutationtest.build.intercept.logging;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.pitest.bytecode.ASMVersion;
import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.bytecode.analysis.MethodTree;
import org.pitest.functional.FCollection;
import org.pitest.mutationtest.build.InterceptorType;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;

public class LoggingCallsFilter implements MutationInterceptor {

  private final Set<String>   loggingClasses = new HashSet<>();
  private Set<Integer> lines;

  public LoggingCallsFilter(Collection<String> loggingClasses) {
    FCollection.mapTo(loggingClasses, correctFormat(), this.loggingClasses);
  }

  @Override
  public InterceptorType type() {
    return InterceptorType.FILTER;
  }

  @Override
  public void begin(ClassTree clazz) {
    this.lines = new HashSet<>();
    for (final MethodTree each : clazz.methods()) {
      findLoggingLines(each,this.lines);
    }
  }

  private void findLoggingLines(MethodTree each, Set<Integer> lines) {
    each.rawNode().accept(new LoggingLineScanner(lines, this.loggingClasses));
  }

  @Override
  public Collection<MutationDetails> intercept(
      Collection<MutationDetails> mutations, Mutater m) {
    return mutations.stream()
            .filter(isOnLoggingLine().negate())
            .collect(Collectors.toList());
  }

  private Predicate<MutationDetails> isOnLoggingLine() {
    return a -> LoggingCallsFilter.this.lines.contains(a.getClassLine().getLineNumber());
  }

  @Override
  public void end() {
    this.lines = null;
  }

  private static Function<String, String> correctFormat() {
    return a -> a.replace('.', '/');
  }

}

class LoggingLineScanner extends MethodVisitor {

  private final Set<Integer> lines;
  private final Set<String>   loggingClasses;
  private int                 currentLineNumber;

  LoggingLineScanner(final Set<Integer> lines, final Set<String> loggingClasses) {
    super(ASMVersion.ASM_VERSION);
    this.lines = lines;
    this.loggingClasses = loggingClasses;
  }

  @Override
  public void visitMethodInsn(final int opcode, final String owner,
      final String name, final String desc, boolean itf) {
    if (this.loggingClasses.stream().anyMatch(owner::startsWith)) {
      this.lines.add(this.currentLineNumber);
    }
  }

  @Override
  public void visitLineNumber(final int line, final Label start) {
    this.currentLineNumber = line;
  }

}
