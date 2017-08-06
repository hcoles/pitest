package org.pitest.mutationtest.build.intercept.logging;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.bytecode.analysis.MethodTree;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.prelude.Prelude;
import org.pitest.mutationtest.build.InterceptorType;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;

public class LoggingCallsFilter implements MutationInterceptor {
  
  private final Set<String>   loggingClasses = new HashSet<String>();
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
    lines = new HashSet<Integer>();
    for (MethodTree each : clazz.methods()) {
      findLoggingLines(each,lines);
    }
  }

  private void findLoggingLines(MethodTree each, Set<Integer> lines) {
    each.rawNode().accept(new LoggingLineScanner(lines, loggingClasses));
  }

  @Override
  public Collection<MutationDetails> intercept(
      Collection<MutationDetails> mutations, Mutater m) {
    return FCollection.filter(mutations, Prelude.not(isOnLoggingLine()));
  }

  private F<MutationDetails, Boolean> isOnLoggingLine() {
    return new  F<MutationDetails, Boolean>() {
      @Override
      public Boolean apply(MutationDetails a) {
        return lines.contains(a.getClassLine().getLineNumber());
      }  
    };
  }

  @Override
  public void end() {
    lines = null;
  }
  
  private static F<String, String> correctFormat() {
    return new F<String, String>() {
      @Override
      public String apply(String a) {
        return a.replace('.', '/');
      }
      
    };
  }

}

class LoggingLineScanner extends MethodVisitor {

  private final Set<Integer> lines;
  private final Set<String>   loggingClasses;
  private int                 currentLineNumber;

  LoggingLineScanner(final Set<Integer> lines, final Set<String> loggingClasses) {
    super(Opcodes.ASM6);
    this.lines = lines;
    this.loggingClasses = loggingClasses;
  }

  @Override
  public void visitMethodInsn(final int opcode, final String owner,
      final String name, final String desc, boolean itf) {
    if (FCollection.contains(this.loggingClasses, matches(owner))) {
      lines.add(currentLineNumber);
    }
  }
  
  private static F<String, Boolean> matches(final String owner) {
    return new F<String, Boolean>() {
      @Override
      public Boolean apply(final String a) {
        return owner.startsWith(a);
      }
    };
  }

  @Override
  public void visitLineNumber(final int line, final Label start) {
    this.currentLineNumber = line;
  }

}
