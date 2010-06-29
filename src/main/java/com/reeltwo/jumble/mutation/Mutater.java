// Taken unmodified from http://jumble.sourceforge.net/

// Embedded here as
// 1) There has been no binary release under the Apache 2 licence
//    (although all code is now available under that licence)
// 2) Release 1.1 is java 1.6 only
// 
// will hopefully remove this embedded file when jumble is next released

package com.reeltwo.jumble.mutation;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.Attribute;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantDouble;
import org.apache.bcel.classfile.ConstantFloat;
import org.apache.bcel.classfile.ConstantInteger;
import org.apache.bcel.classfile.ConstantLong;
import org.apache.bcel.classfile.ConstantNameAndType;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.ConstantString;
import org.apache.bcel.classfile.ConstantUtf8;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.classfile.Unknown;
import org.apache.bcel.generic.ACONST_NULL;
import org.apache.bcel.generic.ARETURN;
import org.apache.bcel.generic.ATHROW;
import org.apache.bcel.generic.ArithmeticInstruction;
import org.apache.bcel.generic.ArrayInstruction;
import org.apache.bcel.generic.BIPUSH;
import org.apache.bcel.generic.CPInstruction;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.DADD;
import org.apache.bcel.generic.DASTORE;
import org.apache.bcel.generic.DCMPG;
import org.apache.bcel.generic.DCONST;
import org.apache.bcel.generic.DDIV;
import org.apache.bcel.generic.DMUL;
import org.apache.bcel.generic.DNEG;
import org.apache.bcel.generic.DREM;
import org.apache.bcel.generic.DRETURN;
import org.apache.bcel.generic.DSTORE;
import org.apache.bcel.generic.DSUB;
import org.apache.bcel.generic.DUP;
import org.apache.bcel.generic.DUP2;
import org.apache.bcel.generic.FADD;
import org.apache.bcel.generic.FCMPG;
import org.apache.bcel.generic.FCONST;
import org.apache.bcel.generic.FDIV;
import org.apache.bcel.generic.FMUL;
import org.apache.bcel.generic.FNEG;
import org.apache.bcel.generic.FREM;
import org.apache.bcel.generic.FRETURN;
import org.apache.bcel.generic.FSUB;
import org.apache.bcel.generic.FieldInstruction;
import org.apache.bcel.generic.GETSTATIC;
import org.apache.bcel.generic.IADD;
import org.apache.bcel.generic.IAND;
import org.apache.bcel.generic.ICONST;
import org.apache.bcel.generic.IDIV;
import org.apache.bcel.generic.IFEQ;
import org.apache.bcel.generic.IFNE;
import org.apache.bcel.generic.IFNONNULL;
import org.apache.bcel.generic.IINC;
import org.apache.bcel.generic.IMUL;
import org.apache.bcel.generic.INEG;
import org.apache.bcel.generic.INVOKESPECIAL;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.INVOKEVIRTUAL;
import org.apache.bcel.generic.IOR;
import org.apache.bcel.generic.IREM;
import org.apache.bcel.generic.IRETURN;
import org.apache.bcel.generic.ISHL;
import org.apache.bcel.generic.ISHR;
import org.apache.bcel.generic.ISUB;
import org.apache.bcel.generic.IUSHR;
import org.apache.bcel.generic.IXOR;
import org.apache.bcel.generic.IfInstruction;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.LADD;
import org.apache.bcel.generic.LAND;
import org.apache.bcel.generic.LASTORE;
import org.apache.bcel.generic.LCONST;
import org.apache.bcel.generic.LDC;
import org.apache.bcel.generic.LDIV;
import org.apache.bcel.generic.LMUL;
import org.apache.bcel.generic.LNEG;
import org.apache.bcel.generic.LOOKUPSWITCH;
import org.apache.bcel.generic.LOR;
import org.apache.bcel.generic.LREM;
import org.apache.bcel.generic.LRETURN;
import org.apache.bcel.generic.LSHL;
import org.apache.bcel.generic.LSHR;
import org.apache.bcel.generic.LSTORE;
import org.apache.bcel.generic.LSUB;
import org.apache.bcel.generic.LUSHR;
import org.apache.bcel.generic.LXOR;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.NOP;
import org.apache.bcel.generic.POP;
import org.apache.bcel.generic.POP2;
import org.apache.bcel.generic.PUTFIELD;
import org.apache.bcel.generic.PUTSTATIC;
import org.apache.bcel.generic.ReturnInstruction;
import org.apache.bcel.generic.SIPUSH;
import org.apache.bcel.generic.Select;
import org.apache.bcel.generic.StackConsumer;
import org.apache.bcel.generic.StoreInstruction;
import org.apache.bcel.generic.TABLESWITCH;
import org.apache.bcel.generic.TargetLostException;
import org.apache.bcel.generic.Type;
import org.apache.bcel.util.ByteSequence;
import org.apache.bcel.util.Repository;
import org.apache.bcel.util.SyntheticRepository;

/**
 * Given a class file can either count the number of possible mutation points or
 * perform a mutations. Mutations can be specified by number or selected at
 * random.
 * 
 * @author Sean A. Irvine
 * @version $Revision: 672 $
 */
public class Mutater {

  /**
   * Maps -1 -&gt; 1; 0 -&gt; 1; 1 -&gt; 0; 2 -&gt; 3; 3 -&gt; 4; 4 -&gt; 5; 5
   * -&gt; -1. This mapping is careful to handle use as boolean cases correctly.
   */
  private static final int[]  ICONST_MAP             = new int[] { 1, 1, 0, 3,
      4, 5, -1                                      };

  /**
   * Table of mutatable instructions. If defined and not a NOP this gives the
   * mutated instruction to use.
   */
  private final Instruction[] mMutatable             = new Instruction[256];
  {
    this.mMutatable[Constants.IADD] = new ISUB();
    this.mMutatable[Constants.ISUB] = new IADD();
    this.mMutatable[Constants.IMUL] = new IDIV();
    this.mMutatable[Constants.IDIV] = new IMUL();
    this.mMutatable[Constants.IREM] = new IMUL();
    this.mMutatable[Constants.IAND] = new IOR();
    this.mMutatable[Constants.IOR] = new IAND();
    this.mMutatable[Constants.IXOR] = new IAND();
    this.mMutatable[Constants.ISHL] = new ISHR();
    this.mMutatable[Constants.ISHR] = new ISHL();
    this.mMutatable[Constants.IUSHR] = new ISHL();
    this.mMutatable[Constants.LADD] = new LSUB();
    this.mMutatable[Constants.LSUB] = new LADD();
    this.mMutatable[Constants.LMUL] = new LDIV();
    this.mMutatable[Constants.LDIV] = new LMUL();
    this.mMutatable[Constants.LREM] = new LMUL();
    this.mMutatable[Constants.LAND] = new LOR();
    this.mMutatable[Constants.LOR] = new LAND();
    this.mMutatable[Constants.LXOR] = new LAND();
    this.mMutatable[Constants.LSHL] = new LSHR();
    this.mMutatable[Constants.LSHR] = new LSHL();
    this.mMutatable[Constants.LUSHR] = new LSHL();
    this.mMutatable[Constants.FADD] = new FSUB();
    this.mMutatable[Constants.FSUB] = new FADD();
    this.mMutatable[Constants.FMUL] = new FDIV();
    this.mMutatable[Constants.FDIV] = new FMUL();
    this.mMutatable[Constants.FREM] = new FMUL();
    this.mMutatable[Constants.DADD] = new DSUB();
    this.mMutatable[Constants.DSUB] = new DADD();
    this.mMutatable[Constants.DMUL] = new DDIV();
    this.mMutatable[Constants.DDIV] = new DMUL();
    this.mMutatable[Constants.DREM] = new DMUL();
    this.mMutatable[Constants.IF_ACMPEQ] = new NOP();
    this.mMutatable[Constants.IF_ACMPNE] = new NOP();
    this.mMutatable[Constants.IF_ICMPEQ] = new NOP();
    this.mMutatable[Constants.IF_ICMPGE] = new NOP();
    this.mMutatable[Constants.IF_ICMPGT] = new NOP();
    this.mMutatable[Constants.IF_ICMPLE] = new NOP();
    this.mMutatable[Constants.IF_ICMPLT] = new NOP();
    this.mMutatable[Constants.IF_ICMPNE] = new NOP();
    this.mMutatable[Constants.IFEQ] = new NOP();
    this.mMutatable[Constants.IFGE] = new NOP();
    this.mMutatable[Constants.IFGT] = new NOP();
    this.mMutatable[Constants.IFLE] = new NOP();
    this.mMutatable[Constants.IFLT] = new NOP();
    this.mMutatable[Constants.IFNE] = new NOP();
    this.mMutatable[Constants.IFNONNULL] = new NOP();
    this.mMutatable[Constants.IFNULL] = new NOP();
  }

  /** Set of methods to be ignored (i.e. never mutated). */
  private Collection<String>  mIgnored;

  /** Should ICONST instructions be changed. */
  private boolean             mMutateInlineConstants = false;

  /** Should return instructions be changed. */
  private boolean             mMutateReturns         = false;

  /** Should IINC instructions be changed. */
  private boolean             mMutateIncrements      = false;

  /** Should store instructions be changed. */
  private boolean             mMutateStores          = false;

  /** Should NEG instructions be changed */
  private boolean             mMutateNegs            = false;

  /** Should SWITCH instructions be changed */
  private boolean             mMutateSwitch          = false;

  /** Should the constant pool be changed. */
  private boolean             mCPool                 = false;

  /** The most recent modification. */
  private String              mModification          = null;

  /** Count down for mutation to apply. */
  private int                 mCount                 = 0;

  // private Repository mRepository = null;
  private Repository          mRepository            = SyntheticRepository
                                                         .getInstance();

  public Mutater() {
    setIgnoredMethods(null);
  }

  public Mutater(final int count) {
    this();
    setMutationPoint(count);
  }

  public void setRepository(final Repository repository) {
    this.mRepository = repository;
  }

  public void setMutationPoint(final int count) {
    this.mCount = count;
    this.mModification = null;
  }

  /**
   * Sets whether mutations should be made in the constant pool.
   * 
   * @param v
   *          true to mutate the constant pool
   */
  public void setMutateCPool(final boolean v) {
    this.mCPool = v;
  }

  /**
   * Sets whether NEG instructions should be mutated.
   * 
   * @param mutateNegs
   *          flag indicating whether to mutate NEG instructions.
   */
  public void setMutateNegs(final boolean mutateNegs) {
    this.mMutateNegs = mutateNegs;
    final NOP nop = this.mMutateNegs ? new NOP() : null;
    this.mMutatable[Constants.INEG] = nop;
    this.mMutatable[Constants.DNEG] = nop;
    this.mMutatable[Constants.FNEG] = nop;
    this.mMutatable[Constants.LNEG] = nop;
  }

  /**
   * Sets whether SWITCH instructions should be mutated.
   * 
   * @param mutateSwitch
   *          flag indicating whether to mutate SWITCH instructions.
   */
  public void setMutateSwitch(final boolean mutateSwitch) {
    this.mMutateSwitch = mutateSwitch;
    final NOP nop = this.mMutateSwitch ? new NOP() : null;
    this.mMutatable[Constants.TABLESWITCH] = nop;
    this.mMutatable[Constants.LOOKUPSWITCH] = nop;
  }

  public void setMutateIncrements(final boolean v) {
    this.mMutateIncrements = v;
    if (this.mMutateIncrements) {
      this.mMutatable[Constants.IINC] = new NOP();
    } else {
      this.mMutatable[Constants.IINC] = null;
    }
  }

  /**
   * Set whether or not inline constants should be mutated.
   * 
   * @param v
   *          true for mutation of inline constants
   */
  public void setMutateInlineConstants(final boolean v) {
    this.mMutateInlineConstants = v;
    final NOP nop = this.mMutateInlineConstants ? new NOP() : null;
    this.mMutatable[Constants.ICONST_0] = nop;
    this.mMutatable[Constants.ICONST_1] = nop;
    this.mMutatable[Constants.ICONST_2] = nop;
    this.mMutatable[Constants.ICONST_3] = nop;
    this.mMutatable[Constants.ICONST_4] = nop;
    this.mMutatable[Constants.ICONST_5] = nop;
    this.mMutatable[Constants.ICONST_M1] = nop;
    this.mMutatable[Constants.FCONST_0] = nop;
    this.mMutatable[Constants.FCONST_1] = nop;
    this.mMutatable[Constants.FCONST_2] = nop;
    this.mMutatable[Constants.DCONST_0] = nop;
    this.mMutatable[Constants.DCONST_1] = nop;
    this.mMutatable[Constants.LCONST_0] = nop;
    this.mMutatable[Constants.LCONST_1] = nop;
    this.mMutatable[Constants.BIPUSH] = nop;
    this.mMutatable[Constants.SIPUSH] = nop;
  }

  /**
   * Set whether or not inline constants should be mutated.
   * 
   * @param v
   *          true for mutation of inline constants
   */
  public void setMutateStores(final boolean v) {
    this.mMutateStores = v;
    final NOP nop = this.mMutateStores ? new NOP() : null;
    this.mMutatable[Constants.PUTFIELD] = nop;
    this.mMutatable[Constants.PUTSTATIC] = nop;
    this.mMutatable[Constants.ASTORE] = nop;
    this.mMutatable[Constants.ASTORE_0] = nop;
    this.mMutatable[Constants.ASTORE_1] = nop;
    this.mMutatable[Constants.ASTORE_2] = nop;
    this.mMutatable[Constants.ASTORE_3] = nop;
    this.mMutatable[Constants.DSTORE] = nop;
    this.mMutatable[Constants.DSTORE_0] = nop;
    this.mMutatable[Constants.DSTORE_1] = nop;
    this.mMutatable[Constants.DSTORE_2] = nop;
    this.mMutatable[Constants.DSTORE_3] = nop;
    this.mMutatable[Constants.FSTORE] = nop;
    this.mMutatable[Constants.FSTORE_0] = nop;
    this.mMutatable[Constants.FSTORE_1] = nop;
    this.mMutatable[Constants.FSTORE_2] = nop;
    this.mMutatable[Constants.FSTORE_3] = nop;
    this.mMutatable[Constants.ISTORE] = nop;
    this.mMutatable[Constants.ISTORE_0] = nop;
    this.mMutatable[Constants.ISTORE_1] = nop;
    this.mMutatable[Constants.ISTORE_2] = nop;
    this.mMutatable[Constants.ISTORE_3] = nop;
    this.mMutatable[Constants.LSTORE] = nop;
    this.mMutatable[Constants.LSTORE_0] = nop;
    this.mMutatable[Constants.LSTORE_1] = nop;
    this.mMutatable[Constants.LSTORE_2] = nop;
    this.mMutatable[Constants.LSTORE_3] = nop;
    this.mMutatable[Constants.AASTORE] = nop;
    this.mMutatable[Constants.BASTORE] = nop;
    this.mMutatable[Constants.CASTORE] = nop;
    this.mMutatable[Constants.DASTORE] = nop;
    this.mMutatable[Constants.FASTORE] = nop;
    this.mMutatable[Constants.IASTORE] = nop;
    this.mMutatable[Constants.LASTORE] = nop;
    this.mMutatable[Constants.SASTORE] = nop;
    this.mMutatable[Constants.INVOKEINTERFACE] = nop;
    this.mMutatable[Constants.INVOKESPECIAL] = nop;
    this.mMutatable[Constants.INVOKESTATIC] = nop;
    this.mMutatable[Constants.INVOKEVIRTUAL] = nop;
  }

  /**
   * Set whether or not return values should be mutated.
   * 
   * @param v
   *          true to mutate return values
   */
  public void setMutateReturnValues(final boolean v) {
    this.mMutateReturns = v;
    final NOP nop = this.mMutateReturns ? new NOP() : null;
    this.mMutatable[Constants.ARETURN] = nop;
    this.mMutatable[Constants.DRETURN] = nop;
    this.mMutatable[Constants.FRETURN] = nop;
    this.mMutatable[Constants.IRETURN] = nop;
    this.mMutatable[Constants.LRETURN] = nop;
  }

  private static boolean checkAssertInstruction(final ConstantPoolGen cpg,
      final Instruction ins) {
    return (ins instanceof INVOKEVIRTUAL)
        && "desiredAssertionStatus".equals(((INVOKEVIRTUAL) ins)
            .getMethodName(cpg));
  }

  /**
   * Is this an instruction we know how to mutate? Needs the entire chain since
   * in rare cases we need to examine context to see if mutation is allowable.
   * Returns the number of mutation points in the instruction (this can be
   * bigger than 1 (e.g. switch statements).
   * 
   * @param ihs
   *          current instruction chain
   * @param offset
   *          position in chain
   * @param cpg
   *          constant pool
   * @return number of mutation points in the given instruction
   */
  private int isMutatable(final InstructionHandle[] ihs, final int offset,
      final ConstantPoolGen cpg) {
    final Instruction i = ihs[offset].getInstruction();

    // handle general mutability
    if (this.mMutatable[i.getOpcode()] == null) {
      return 0;
    }

    // handle special situation of .class invocations
    if ((i instanceof ICONST) && (offset < ihs.length - 1)) {
      final Instruction context = ihs[offset + 1].getInstruction();
      if ((context instanceof INVOKESTATIC)
          && "class".equals(((INVOKESTATIC) context).getMethodName(cpg))) {
        return 0;
      }
    }

    // handle special situation of .desiredAssertionStatus invocations
    // javac 1.5
    if (i instanceof ICONST) {
      if ((offset >= 2) && (((ICONST) i).getValue().intValue() == 1)
          && checkAssertInstruction(cpg, ihs[offset - 2].getInstruction())) {
        return 0;
      }
      if ((offset >= 4) && (((ICONST) i).getValue().intValue() == 0)
          && checkAssertInstruction(cpg, ihs[offset - 4].getInstruction())) {
        return 0;
      }
    }
    if ((i instanceof IFNE) && (offset >= 1)
        && checkAssertInstruction(cpg, ihs[offset - 1].getInstruction())) {
      return 0;
    }

    // we can only mutate invokes that return void
    if ((i instanceof InvokeInstruction)
        && (((InvokeInstruction) i).getReturnType(cpg) != Type.VOID)) {
      return 0;
    }

    // switch statements have multiple points
    if (i instanceof Select) {
      return ((Select) i).getMatchs().length;
    }

    // everything else 1 point
    return 1;
  }

  /**
   * Skip to the next valid instruction to examine. The primary reason for this
   * function is to attempt to skip over assertions and synthetics.
   */
  private static int skipAhead(final InstructionHandle[] ihs,
      final ConstantPoolGen cp, int j) {
    final Instruction i = ihs[j++].getInstruction();
    if (i instanceof GETSTATIC) {
      final GETSTATIC gs = (GETSTATIC) i;
      if (gs.getFieldName(cp).equals("$noassert")
          || gs.getFieldName(cp).equals("assert")
          || gs.getFieldName(cp).equals("$assertionsDisabled")) {
        // attempt to skip over a java 1.4 assert() statement
        // this works for code generated by jikes
        // $assertionsDisabled is used by javac 1.5
        // skip forwards to a ATHROW instruction, most likely it ends the assert
        while (!(ihs[j++].getInstruction() instanceof ATHROW)) {
          ; // do nothing
        }
      } else if (gs.getFieldName(cp).indexOf("class$") != -1) {
        // attempt to skip a ".class" reference (because it has a ifnonnull
        // test)
        if (ihs[j + 1].getInstruction() instanceof IFNONNULL) {
          j += 2;
        }
      }
    }

    return j;
  }

  /**
   * Set the names of all the methods to be ignored during mutation. Any method
   * named by a member of the given set will not be subject to mutation.
   * 
   * @param ignore
   *          Set of ignored methods
   */
  public void setIgnoredMethods(final Collection<String> ignore) {
    this.mIgnored = ignore == null ? new HashSet<String>() : ignore;
  }

  private boolean isMutatableMethod(final Method m) {
    // System.out.println(m.getName() + " " + mIgnored.contains(m.getName()));
    return (m != null) && !m.isNative() && !m.isAbstract() && !m.isSynthetic()
        && !this.mIgnored.contains(m.getName());
  }

  /** Records the first line in the code that uses a constant. */
  private int[] mConstantFirstRef = null;

  private void initConstantRef(final Method[] methods, final String className,
      final ConstantPoolGen cp) {
    if (this.mConstantFirstRef == null) {
      this.mConstantFirstRef = new int[cp.getSize()];
      Arrays.fill(this.mConstantFirstRef, -1);
      if (methods != null) {
        for (final Method m : methods) {
          if (isMutatableMethod(m)) {
            final InstructionList il = new MethodGen(m, className, cp)
                .getInstructionList();
            if (il != null) {
              final InstructionHandle[] ihs = il.getInstructionHandles();
              for (int j = 0; j < ihs.length; j++) {
                final Instruction ins = ihs[j].getInstruction();
                if (ins instanceof CPInstruction) {
                  // skip those which are messages for Assertion Error
                  if ((ins instanceof LDC) && (j + 1 < ihs.length)) {
                    final Instruction i2 = ihs[j + 1].getInstruction();
                    if ((i2 instanceof INVOKESPECIAL)
                        && ((INVOKESPECIAL) i2).getReferenceType(cp).toString()
                            .equals("java.lang.AssertionError")) {
                      continue;
                    }
                  }
                  final int index = ((CPInstruction) ins).getIndex();
                  if (this.mConstantFirstRef[index] == -1) {
                    this.mConstantFirstRef[index] = (m.getLineNumberTable() != null ? m
                        .getLineNumberTable().getSourceLine(
                            ihs[j].getPosition())
                        : 0);
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  /**
   * Test if a constant in the pool is mutatable.
   */
  private boolean isMutatable(final Constant c, final int i) {
    return (this.mConstantFirstRef[i] != -1)
        && (c != null)
        && ((c instanceof ConstantString) || (c instanceof ConstantLong)
            || (c instanceof ConstantInteger) || (c instanceof ConstantFloat) || (c instanceof ConstantDouble));
  }

  /**
   * Count the number of mutation points in the constant pool.
   */
  private int countMutationPoints(final Method[] methods,
      final String className, final ConstantPoolGen cp) {
    initConstantRef(methods, className, cp);
    int count = 0;
    for (int i = 0; i < cp.getSize(); i++) {
      if (isMutatable(cp.getConstant(i), i)) {
        count++;
      }
    }
    return count;
  }

  /**
   * Count number of mutation points in a method.
   */
  private int countMutationPoints(final Method m, final String className,
      final ConstantPoolGen cp) {
    // check this is a method that it makes sense to mutate
    if (!isMutatableMethod(m)) {
      return 0;
    }
    final InstructionList il = new MethodGen(m, className, cp)
        .getInstructionList();
    final InstructionHandle[] ihs = il.getInstructionHandles();
    int count = 0;
    for (int j = 0; j < ihs.length; j = skipAhead(ihs, cp, j)) {
      count += isMutatable(ihs, j, cp);
    }
    il.dispose();
    return count;
  }

  /*
   * Look for the special case of a synthetic class used to support a switch
   * statement on an Enum. These classes are just an automatically generated
   * mapping between Enum ordinals and values in the switch and it doesn't
   * really make sense for them to be unit tested.
   */
  private boolean isSwitchClass(final String cl, final ConstantPool cpool) {
    if (cl.indexOf('$') == -1) {
      return false;
    }
    for (int i = 0; i < cpool.getLength(); i++) {
      final Constant c = cpool.getConstant(i);
      if ((c instanceof ConstantNameAndType)
          && ((ConstantNameAndType) c).getName(cpool).startsWith("$SwitchMap")) {
        return true;
      }
    }
    return false;
  }

  /**
   * Compute the total number of possible mutation points in the class.
   */
  public int countMutationPoints(final String cl) {
    final String className = fixName(cl);
    final JavaClass clazz = lookupClass(className);

    if (clazz == null) {
      return -1;
    }
    // if is an interface, return -1 to distinguish from 0 point classes
    if (clazz.isInterface()) {
      return -1;
    }

    final Method[] methods = clazz.getMethods();
    final ConstantPool cpool = clazz.getConstantPool();

    // check for synthetic class used by Enums
    if (isSwitchClass(cl, cpool)) {
      return 0;
    }

    final ConstantPoolGen cp = new ConstantPoolGen(cpool);

    int count = this.mCPool ? countMutationPoints(methods, className, cp) : 0;
    for (final Method method : methods) {
      count += countMutationPoints(method, className, cp);
    }
    return count;
  }

  /** Mutate an ICONST instruction. */
  private static Instruction mutateICONST(final ICONST i,
      final ConstantPoolGen cp) {
    return new ICONST(ICONST_MAP[i.getValue().intValue() + 1]);
  }

  /** Mutate a FCONST instruction. */
  private static Instruction mutateFCONST(final FCONST i,
      final ConstantPoolGen cp) {
    final float v = i.getValue().floatValue();
    if (v == 0.0F) {
      return new FCONST(1.0F);
    } else {
      return new FCONST(0.0F);
    }
  }

  /** Mutate a DCONST instruction. */
  private static Instruction mutateDCONST(final DCONST i,
      final ConstantPoolGen cp) {
    final double v = i.getValue().doubleValue();
    if (v == 0.0) {
      return new DCONST(1.0);
    } else {
      return new DCONST(0.0);
    }
  }

  /** Mutate a LCONST instruction. */
  private static Instruction mutateLCONST(final LCONST i,
      final ConstantPoolGen cp) {
    final long v = i.getValue().longValue();
    if (v == 0L) {
      return new LCONST(1L);
    } else {
      return new LCONST(0L);
    }
  }

  /** Mutate a BIPUSH instruction. */
  private static Instruction mutateBIPUSH(final BIPUSH i,
      final ConstantPoolGen cp) {
    return new BIPUSH((byte) (i.getValue().byteValue() + 1));
  }

  /** Mutate a SIPUSH instruction. */
  private static Instruction mutateSIPUSH(final SIPUSH i,
      final ConstantPoolGen cp) {
    return new SIPUSH((byte) (i.getValue().shortValue() + 1));
  }

  /** Mutate an IINC instruction */
  private static Instruction mutateIINC(final IINC i, final ConstantPoolGen cp) {
    return new IINC(i.getIndex(), -i.getIncrement());
  }

  /**
   * Return a new integer instruction with the same parameter, but which differs
   * from the current instruction.
   */
  private Instruction mutateIntegerArithmetic(
      final ArithmeticInstruction current, final ConstantPoolGen cp) {
    return this.mMutatable[current.getOpcode()];
  }

  private InstructionList mutateRETURN(final ReturnInstruction ret,
      final InstructionFactory ifactory) {
    final InstructionList il = new InstructionList();
    if (ret instanceof IRETURN) {
      // maps 0->1 and anything else to 0, this is done without need
      // of any more stack space.
      final IFEQ ifeq = new IFEQ(null);
      il.append(ifeq);
      il.append(new ICONST(0));
      il.append(new IRETURN());
      il.append(new ICONST(1));
      ifeq.setTarget(il.getEnd());
    } else if (ret instanceof LRETURN) {
      // +1L
      il.append(new LCONST(1));
      il.append(new LADD());
    } else if (ret instanceof FRETURN) {
      // The following is complicated by the problem of NaNs. By default
      // the new value is -(x + 1), but this doesn't work for NaNs. But
      // for a NaN x != x is true, and we use this to detect them.
      il.append(new DUP());
      il.append(new DUP());
      il.append(new FCMPG());
      final IFEQ ifeq = new IFEQ(null);
      il.append(ifeq);
      il.append(new POP());
      il.append(new FCONST(0));
      il.append(new FCONST(1));
      ifeq.setTarget(il.getEnd());
      il.append(new FADD());
      il.append(new FNEG());
    } else if (ret instanceof DRETURN) {
      // The following is complicated by the problem of NaNs. By default
      // the new value is -(x + 1), but this doesn't work for NaNs. But
      // for a NaN x != x is true, and we use this to detect them.
      il.append(new DUP2());
      il.append(new DUP2());
      il.append(new DCMPG());
      final IFEQ ifeq = new IFEQ(null);
      il.append(ifeq);
      il.append(new POP2());
      il.append(new DCONST(0));
      il.append(new DCONST(1));
      ifeq.setTarget(il.getEnd());
      il.append(new DADD());
      il.append(new DNEG());
    } else if (ret instanceof ARETURN) {
      // if result is non-null make it null, otherwise hard case
      // for moment throw runtime exception
      final IFNONNULL ifnonnull = new IFNONNULL(null);
      il.append(ifnonnull);
      il.append(ifactory.createNew("java.lang.RuntimeException"));
      il.append(new DUP());
      il.append(ifactory.createInvoke("java.lang.RuntimeException", "<init>",
          Type.VOID, new Type[0], Constants.INVOKESPECIAL));
      il.append(new ATHROW());
      il.append(new ACONST_NULL());
      ifnonnull.setTarget(il.getEnd());
    }
    return il;
  }

  /**
   * Produce a human description of an instruction.
   * 
   * @param i
   *          the instruction
   * @return description
   */
  private String describe(final Instruction i) {
    if ((i instanceof IADD) || (i instanceof LADD) || (i instanceof FADD)
        || (i instanceof DADD)) {
      return "+";
    }
    if ((i instanceof ISUB) || (i instanceof LSUB) || (i instanceof FSUB)
        || (i instanceof DSUB)) {
      return "-";
    }
    if ((i instanceof IMUL) || (i instanceof LMUL) || (i instanceof FMUL)
        || (i instanceof DMUL)) {
      return "*";
    }
    if ((i instanceof IDIV) || (i instanceof LDIV) || (i instanceof FDIV)
        || (i instanceof DDIV)) {
      return "/";
    }
    if ((i instanceof IREM) || (i instanceof LREM) || (i instanceof FREM)
        || (i instanceof DREM)) {
      return "%";
    }
    if ((i instanceof IOR) || (i instanceof LOR)) {
      return "|";
    }
    if ((i instanceof IXOR) || (i instanceof LXOR)) {
      return "^";
    }
    if ((i instanceof IAND) || (i instanceof LAND)) {
      return "&";
    }
    if ((i instanceof ISHL) || (i instanceof LSHL)) {
      return "<<";
    }
    if ((i instanceof ISHR) || (i instanceof LSHR)) {
      return ">>";
    }
    if ((i instanceof IUSHR) || (i instanceof LUSHR)) {
      return ">>>";
    }
    if (i instanceof ICONST) {
      return ((ICONST) i).getValue().toString();
    }
    if (i instanceof FCONST) {
      return ((FCONST) i).getValue().toString() + "F";
    }
    if (i instanceof DCONST) {
      return ((DCONST) i).getValue().toString() + "D";
    }
    if (i instanceof LCONST) {
      return ((LCONST) i).getValue().toString() + "L";
    }
    if (i instanceof BIPUSH) {
      final byte b = ((BIPUSH) i).getValue().byteValue();
      if ((b >= ' ') && (b <= '~')) {
        return (b + " (" + (char) b + ")");
      }
      return "" + b;
    }
    if (i instanceof SIPUSH) {
      return ((SIPUSH) i).getValue().toString();
    }
    if (i instanceof ReturnInstruction) {
      return "changed return value (" + i.getName() + ")";
    }

    if (i instanceof IINC) {
      if (((IINC) i).getIncrement() >= 0) {
        return "+=";
      } else {
        return "-=";
      }
    }
    return "unknown";
  }

  /**
   * Return the most recent modification.
   * 
   * @return description of modification
   */
  public String getModification() {
    return this.mModification;
  }

  private double mutateDouble(final double current) {
    // treatment of reals is complicated by potential underflow and the
    // special values like the infinities
    return Double.isNaN(current) || Double.isInfinite(current) ? 0 : 2
        * current + (current >= 0 ? 1 : -1);
  }

  /**
   * Handle mutations of the constant pool.
   */
  private void mutateConstant(final String className, final ConstantPoolGen cp,
      final int i) {
    final Constant c = cp.getConstant(i);
    String mod = className + ":" + this.mConstantFirstRef[i] + ": CP[" + i
        + "] ";
    if (c instanceof ConstantString) {
      // in this case need to actually step to the UTF8 constant for the string
      final int index = ((ConstantString) c).getStringIndex();
      final ConstantUtf8 utf = (ConstantUtf8) cp.getConstant(index);
      final String current = utf.getBytes();
      if ("__jumble__".equals(current)) {
        cp.setConstant(index, new ConstantUtf8("__jumble__"));
        mod = mod + "\"" + current + "\" -> \"__jumble__\"";
      } else {
        cp.setConstant(index, new ConstantUtf8("___jumble___"));
        mod = mod + "\"" + current + "\" -> \"___jumble___\"";
      }
    } else if (c instanceof ConstantLong) {
      final long current = ((ConstantLong) c).getBytes();
      cp.setConstant(i, new ConstantLong(current + 1));
      mod = mod + current + " -> " + (current + 1);
    } else if (c instanceof ConstantInteger) {
      final int current = ((ConstantInteger) c).getBytes();
      cp.setConstant(i, new ConstantInteger(current + 1));
      mod = mod + current + " -> " + (current + 1);
    } else if (c instanceof ConstantFloat) {
      final float current = ((ConstantFloat) c).getBytes();
      final float newValue = (float) mutateDouble(current);
      cp.setConstant(i, new ConstantFloat(newValue));
      mod = mod + current + " -> " + newValue;
    } else if (c instanceof ConstantDouble) {
      final double current = ((ConstantDouble) c).getBytes();
      final double newValue = mutateDouble(current);
      cp.setConstant(i, new ConstantDouble(newValue));
      mod = mod + current + " -> " + newValue;
    }
    this.mModification = mod;
  }

  private int jumble(final Method[] methods, final int methodidx,
      final String className, final ConstantPoolGen cp, int count) {
    // check if modification is appropriate
    final Method m = methods[methodidx];
    if ((count < 0) || !isMutatableMethod(m)) {
      return count;
    }
    final MethodGen mg = new MethodGen(m, className, cp);
    final InstructionList il = mg.getInstructionList();
    final InstructionHandle[] ihs = il.getInstructionHandles();
    final InstructionFactory ifactory = new InstructionFactory(cp);

    for (int j = 0; j < ihs.length; j = skipAhead(ihs, cp, j)) {
      final Instruction i = ihs[j].getInstruction();
      final int points = isMutatable(ihs, j, cp);
      if ((points != 0) && ((count -= points) < 0)) {
        // note count is < -1 only for a few instructions like TABLESWITCH
        final int lineNumber = (m.getLineNumberTable() != null ? m
            .getLineNumberTable().getSourceLine(ihs[j].getPosition()) : 0);
        final StringBuffer mod = new StringBuffer(className).append(":")
            .append(lineNumber).append(": ");
        if (i instanceof IfInstruction) {
          ihs[j].setInstruction(((IfInstruction) i).negate());
          mod.append("negated conditional");
        } else if ((i instanceof INEG) || (i instanceof DNEG)
            || (i instanceof FNEG) || (i instanceof LNEG)) {
          ihs[j].setInstruction(new NOP());
          mod.append("removed negation");
        } else if (i instanceof ArithmeticInstruction) {
          // binary operand integer instruction
          final Instruction inew = mutateIntegerArithmetic(
              (ArithmeticInstruction) i, cp);
          ihs[j].setInstruction(inew);
          mod.append(describe(i) + " -> " + describe(inew));
        } else if (i instanceof ReturnInstruction) {
          il.insert(ihs[j], mutateRETURN((ReturnInstruction) i, ifactory));
          mod.append(describe(i));
        } else if (i instanceof StoreInstruction) {
          ihs[j]
              .setInstruction((i instanceof DSTORE) || (i instanceof LSTORE) ? new POP2()
                  : new POP());
          mod.append("removed local assignment");
        } else if (i instanceof InvokeInstruction) {
          mutateInvokeInstruction(cp, il, ihs, j, i, mod);
        } else if ((i instanceof PUTFIELD) || (i instanceof PUTSTATIC)) {
          final FieldInstruction fi = (FieldInstruction) i;
          final int size = fi.getFieldType(cp).getSize();
          final InstructionList lil = new InstructionList();
          lil.append(size > 1 ? new POP2() : new POP()); // pop the value
          lil.append(new POP()); // pop the object reference
          final InstructionHandle ins = ihs[j];
          il.insert(ins, lil);
          try {
            il.delete(ins);
          } catch (final TargetLostException e) {
            ; // too bad
          }
          mod.append("removed assignment to " + fi.getFieldName(cp));
        } else if ((i instanceof ArrayInstruction)
            && (i instanceof StackConsumer)) {
          // i.e. an array store
          final boolean big = (i instanceof DASTORE) || (i instanceof LASTORE);
          final InstructionList lil = new InstructionList();
          lil.append(big ? new POP2() : new POP()); // pop value
          lil.append(new POP2()); // pop index and array reference
          final InstructionHandle ins = ihs[j];
          il.insert(ins, lil);
          try {
            il.delete(ins);
          } catch (final TargetLostException e) {
            ; // too bad
          }
          mod.append("removed array assignment");
        } else if (i instanceof Select) {
          final Select select = (Select) i;
          final int index = -1 - count;
          // mutate by swapping target with default target, this is better than
          // swapping the case value itself because sometimes multiple cases
          // will branch to the same code
          final int[] matches = select.getMatchs();
          final InstructionHandle[] handles = select.getTargets();
          final InstructionHandle newDefHandle = handles[index];
          final InstructionHandle oldDefHandle = select.getTarget();
          if (newDefHandle == oldDefHandle) {
            // need to try harder to find a case we can swap with
            for (int k = 0; k < matches.length; k++) {
              if ((k != index) && (newDefHandle != handles[k])) {
                mod.append("switched case " + matches[index] + " with case "
                    + k);
                handles[index] = handles[k];
                handles[k] = newDefHandle;
                if (select instanceof TABLESWITCH) {
                  ihs[j].setInstruction(new TABLESWITCH(matches, handles,
                      oldDefHandle));
                } else {
                  ihs[j].setInstruction(new LOOKUPSWITCH(matches, handles,
                      oldDefHandle));
                }
                break;
              }
            }
            // still didn't find an option, just mutate the case value itself
            mod.append("switched case " + matches[index] + " -> "
                + ++matches[index]);
            if (select instanceof TABLESWITCH) {
              ihs[j].setInstruction(new TABLESWITCH(matches, handles,
                  oldDefHandle));
            } else {
              ihs[j].setInstruction(new LOOKUPSWITCH(matches, handles,
                  oldDefHandle));
            }
          } else {
            handles[index] = oldDefHandle;
            mod
                .append("switched case " + matches[index]
                    + " with default case");
            if (select instanceof TABLESWITCH) {
              ihs[j].setInstruction(new TABLESWITCH(matches, handles,
                  newDefHandle));
            } else {
              ihs[j].setInstruction(new LOOKUPSWITCH(matches, handles,
                  newDefHandle));
            }
          }
        } else {
          final Instruction inew;
          if (i instanceof ICONST) {
            inew = mutateICONST((ICONST) i, cp);
          } else if (i instanceof FCONST) {
            inew = mutateFCONST((FCONST) i, cp);
          } else if (i instanceof DCONST) {
            inew = mutateDCONST((DCONST) i, cp);
          } else if (i instanceof LCONST) {
            inew = mutateLCONST((LCONST) i, cp);
          } else if (i instanceof BIPUSH) {
            inew = mutateBIPUSH((BIPUSH) i, cp);
          } else if (i instanceof SIPUSH) {
            inew = mutateSIPUSH((SIPUSH) i, cp);
          } else if (i instanceof IINC) {
            inew = mutateIINC((IINC) i, cp);
          } else {
            inew = null;
          }
          if (inew != null) {
            ihs[j].setInstruction(inew);
            mod.append(describe(i) + " -> " + describe(inew));
          }
        }
        this.mModification = mod.toString();
        break;
      }
    }

    removeLVTTAttributeToFixLVTTClassLoadingError(mg);

    mg.setMaxStack(); // this is needed for the return mods
    methods[methodidx] = mg.getMethod();
    il.dispose();
    return count;
  }

  private void mutateInvokeInstruction(final ConstantPoolGen cp,
      final InstructionList il, final InstructionHandle[] ihs, final int j,
      final Instruction i, final StringBuffer mod) {
    final Type[] argTypes = ((InvokeInstruction) i).getArgumentTypes(cp);
    final InstructionList lil = new InstructionList();
    // Pop all the arguments (I'm assuming they are in reverse order
    for (int k = argTypes.length - 1; k >= 0; k--) {
      lil.append(argTypes[k].getSize() > 1 ? new POP2() : new POP());
    }
    final InstructionHandle ins = ihs[j];
    il.insert(ins, lil);
    try {
      il.delete(ins);
    } catch (final TargetLostException e) {
      ; // too bad
    }
    mod.append("removed method call to "
        + ((InvokeInstruction) i).getMethodName(cp));
  }

  private void removeLVTTAttributeToFixLVTTClassLoadingError(final MethodGen mg) {
    final Attribute[] attribs = mg.getCodeAttributes();
    for (final Attribute a : attribs) {
      if ((a instanceof Unknown)
          && ((Unknown) a).getName().equals("LocalVariableTypeTable")) {
        mg.removeCodeAttribute(a);
      }
    }
  }

  public JavaClass jumbler(final String cn) throws ClassNotFoundException {
    final JavaClass clazz = this.mRepository.loadClass(cn);
    return jumbler(clazz);
  }

  public JavaClass jumbler(final JavaClass clazz) {
    final JavaClass ret = clazz.copy();

    final Method[] methods = ret.getMethods();
    final ConstantPoolGen cp = new ConstantPoolGen(ret.getConstantPool());
    int count = this.mCount;
    if (this.mCPool) {
      // first deal with constant pool
      initConstantRef(methods, ret.getClassName(), cp);
      for (int i = 0; i < cp.getSize(); i++) {
        if (isMutatable(cp.getConstant(i), i) && (count-- == 0)) {
          mutateConstant(ret.getClassName(), cp, i);
        }
      }
    }
    for (int i = 0; i < methods.length; i++) {
      count = jumble(methods, i, ret.getClassName(), cp, count);
    }
    ret.setConstantPool(cp.getFinalConstantPool());
    return ret;
  }

  protected static String printClass(final JavaClass c) {
    final StringBuffer sb = new StringBuffer();
    try {
      final Method[] m = c.getMethods();
      for (final Method element : m) {
        sb.append(element.getName()).append("\n");
        final ByteSequence code = new ByteSequence(element.getCode().getCode());
        while (code.available() > 0) {
          sb.append("\t").append(Instruction.readInstruction(code))
              .append("\n");
        }
      }
    } catch (final Throwable e) {
      sb.append("Couldn't print class").append("\n");
    }
    return sb.toString();
  }

  /**
   * Gets the name of the method currently being mutated for the given class.
   * 
   * @param cl
   *          the name of the class to mutate
   * @return mutated method name
   */
  public String getMutatedMethodName(final String cl) {
    final String className = fixName(cl);
    final JavaClass clazz = lookupClass(className);

    if (clazz == null) {
      System.out.println("Error: could not retrieve " + className);
      return null;
    }

    final Method[] methods = clazz.getMethods();
    final ConstantPool cpool = clazz.getConstantPool();
    final ConstantPoolGen cp = new ConstantPoolGen(cpool);
    int count = this.mCPool ? countMutationPoints(methods, className, cp) : 0;
    for (final Method method : methods) {
      count += countMutationPoints(method, className, cp);

      // Once we have gone past the mutation point,
      // then we have found the mutated method
      if (this.mCount < count) {
        return method.getName() + method.getSignature();
      }
    }

    // If we get here, then something went wrong
    throw new RuntimeException("Invalid mutation point");
  }

  private JavaClass lookupClass(final String className) {
    try {
      final JavaClass clazz = this.mRepository.findClass(className);

      if (clazz == null) {
        return this.mRepository.loadClass(className);
      } else {
        return clazz;
      }
    } catch (final ClassNotFoundException ex) {
      return null;
    }
  }

  /**
   * Gets the mutation point, relative to the method being mutated. The method
   * is specified by <CODE>getMutatedMethodName(cl)</CODE>.
   * 
   * @param cl
   *          the class to to mutate.
   * @return the mutation point, relative to the mutated method.
   */
  public int getMethodRelativeMutationPoint(final String cl) {
    final String className = fixName(cl);
    final JavaClass clazz = lookupClass(className);

    if (clazz == null) {
      return -1;
    }

    final Method[] methods = clazz.getMethods();
    final ConstantPool cpool = clazz.getConstantPool();
    final ConstantPoolGen cp = new ConstantPoolGen(cpool);
    int count = this.mCPool ? countMutationPoints(methods, className, cp) : 0;
    for (final Method method : methods) {
      final int oldCount = count;
      count += countMutationPoints(method, className, cp);
      // Once we have gone past the mutation point,
      // then we have found the mutated method
      if (this.mCount < count) {
        return this.mCount - oldCount;
      }
    }

    // If we get here, then something went wrong
    throw new RuntimeException("Invalid mutation point");
  }

  /**
   * Lop off .class or .java from a string.
   * 
   * @param className
   *          name of the class
   * @return class name without extension
   */
  private static String fixName(final String className) {
    if (className.endsWith(".class")) {
      return className.substring(0, className.length() - 6).replace('/', '.');
    } else if (className.endsWith(".java")) {
      return className.substring(0, className.length() - 5).replace('/', '.');
    } else {
      return className.replace('/', '.');
    }
  }

}
