package org.pitest.bytecode.analysis;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.pitest.sequence.Match;

import static org.pitest.bytecode.analysis.InstructionMatchers.opCode;


public class OpcodeMatchers {

    public static final Match<AbstractInsnNode> NOP = opCode(Opcodes.NOP);

    public static final Match<AbstractInsnNode> ACONST_NULL = opCode(Opcodes.ACONST_NULL);

    public static final Match<AbstractInsnNode> ICONST_M1 = opCode(Opcodes.ICONST_M1);

    public static final Match<AbstractInsnNode> ICONST_0 = opCode(Opcodes.ICONST_0);

    public static final Match<AbstractInsnNode> ICONST_1 = opCode(Opcodes.ICONST_1);

    public static final Match<AbstractInsnNode> ICONST_2 = opCode(Opcodes.ICONST_2);

    public static final Match<AbstractInsnNode> ICONST_3 = opCode(Opcodes.ICONST_3);

    public static final Match<AbstractInsnNode> ICONST_4 = opCode(Opcodes.ICONST_4);

    public static final Match<AbstractInsnNode> ICONST_5 = opCode(Opcodes.ICONST_5);

    public static final Match<AbstractInsnNode> LCONST_0 = opCode(Opcodes.LCONST_0);

    public static final Match<AbstractInsnNode> LCONST_1 = opCode(Opcodes.LCONST_1);

    public static final Match<AbstractInsnNode> FCONST_0 = opCode(Opcodes.FCONST_0);

    public static final Match<AbstractInsnNode> FCONST_1 = opCode(Opcodes.FCONST_1);

    public static final Match<AbstractInsnNode> FCONST_2 = opCode(Opcodes.FCONST_2);

    public static final Match<AbstractInsnNode> DCONST_0 = opCode(Opcodes.DCONST_0);

    public static final Match<AbstractInsnNode> DCONST_1 = opCode(Opcodes.DCONST_1);

    public static final Match<AbstractInsnNode> BIPUSH = opCode(Opcodes.BIPUSH);

    public static final Match<AbstractInsnNode> SIPUSH = opCode(Opcodes.SIPUSH);

    public static final Match<AbstractInsnNode> LDC = opCode(Opcodes.LDC);

    public static final Match<AbstractInsnNode> ILOAD = opCode(Opcodes.ILOAD);

    public static final Match<AbstractInsnNode> LLOAD = opCode(Opcodes.LLOAD);

    public static final Match<AbstractInsnNode> FLOAD = opCode(Opcodes.FLOAD);

    public static final Match<AbstractInsnNode> DLOAD = opCode(Opcodes.DLOAD);

    public static final Match<AbstractInsnNode> ALOAD = opCode(Opcodes.ALOAD);

    public static final Match<AbstractInsnNode> IALOAD = opCode(Opcodes.IALOAD);

    public static final Match<AbstractInsnNode> LALOAD = opCode(Opcodes.LALOAD);

    public static final Match<AbstractInsnNode> FALOAD = opCode(Opcodes.FALOAD);

    public static final Match<AbstractInsnNode> DALOAD = opCode(Opcodes.DALOAD);

    public static final Match<AbstractInsnNode> AALOAD = opCode(Opcodes.AALOAD);

    public static final Match<AbstractInsnNode> BALOAD = opCode(Opcodes.BALOAD);

    public static final Match<AbstractInsnNode> CALOAD = opCode(Opcodes.CALOAD);

    public static final Match<AbstractInsnNode> SALOAD = opCode(Opcodes.SALOAD);

    public static final Match<AbstractInsnNode> ISTORE = opCode(Opcodes.ISTORE);

    public static final Match<AbstractInsnNode> LSTORE = opCode(Opcodes.LSTORE);

    public static final Match<AbstractInsnNode> FSTORE = opCode(Opcodes.FSTORE);

    public static final Match<AbstractInsnNode> DSTORE = opCode(Opcodes.DSTORE);

    public static final Match<AbstractInsnNode> ASTORE = opCode(Opcodes.ASTORE);

    public static final Match<AbstractInsnNode> IASTORE = opCode(Opcodes.IASTORE);

    public static final Match<AbstractInsnNode> LASTORE = opCode(Opcodes.LASTORE);

    public static final Match<AbstractInsnNode> FASTORE = opCode(Opcodes.FASTORE);

    public static final Match<AbstractInsnNode> DASTORE = opCode(Opcodes.DASTORE);

    public static final Match<AbstractInsnNode> AASTORE = opCode(Opcodes.AASTORE);

    public static final Match<AbstractInsnNode> BASTORE = opCode(Opcodes.BASTORE);

    public static final Match<AbstractInsnNode> CASTORE = opCode(Opcodes.CASTORE);

    public static final Match<AbstractInsnNode> SASTORE = opCode(Opcodes.SASTORE);

    public static final Match<AbstractInsnNode> POP = opCode(Opcodes.POP);

    public static final Match<AbstractInsnNode> POP2 = opCode(Opcodes.POP2);

    public static final Match<AbstractInsnNode> DUP = opCode(Opcodes.DUP);

    public static final Match<AbstractInsnNode> DUP_X1 = opCode(Opcodes.DUP_X1);

    public static final Match<AbstractInsnNode> DUP_X2 = opCode(Opcodes.DUP_X2);

    public static final Match<AbstractInsnNode> DUP2 = opCode(Opcodes.DUP2);

    public static final Match<AbstractInsnNode> DUP2_X1 = opCode(Opcodes.DUP2_X1);

    public static final Match<AbstractInsnNode> DUP2_X2 = opCode(Opcodes.DUP2_X2);

    public static final Match<AbstractInsnNode> SWAP = opCode(Opcodes.SWAP);

    public static final Match<AbstractInsnNode> IADD = opCode(Opcodes.IADD);

    public static final Match<AbstractInsnNode> LADD = opCode(Opcodes.LADD);

    public static final Match<AbstractInsnNode> FADD = opCode(Opcodes.FADD);

    public static final Match<AbstractInsnNode> DADD = opCode(Opcodes.DADD);

    public static final Match<AbstractInsnNode> ISUB = opCode(Opcodes.ISUB);

    public static final Match<AbstractInsnNode> LSUB = opCode(Opcodes.LSUB);

    public static final Match<AbstractInsnNode> FSUB = opCode(Opcodes.FSUB);

    public static final Match<AbstractInsnNode> DSUB = opCode(Opcodes.DSUB);

    public static final Match<AbstractInsnNode> IMUL = opCode(Opcodes.IMUL);

    public static final Match<AbstractInsnNode> LMUL = opCode(Opcodes.LMUL);

    public static final Match<AbstractInsnNode> FMUL = opCode(Opcodes.FMUL);

    public static final Match<AbstractInsnNode> DMUL = opCode(Opcodes.DMUL);

    public static final Match<AbstractInsnNode> IDIV = opCode(Opcodes.IDIV);

    public static final Match<AbstractInsnNode> LDIV = opCode(Opcodes.LDIV);

    public static final Match<AbstractInsnNode> FDIV = opCode(Opcodes.FDIV);

    public static final Match<AbstractInsnNode> DDIV = opCode(Opcodes.DDIV);

    public static final Match<AbstractInsnNode> IREM = opCode(Opcodes.IREM);

    public static final Match<AbstractInsnNode> LREM = opCode(Opcodes.LREM);

    public static final Match<AbstractInsnNode> FREM = opCode(Opcodes.FREM);

    public static final Match<AbstractInsnNode> DREM = opCode(Opcodes.DREM);

    public static final Match<AbstractInsnNode> INEG = opCode(Opcodes.INEG);

    public static final Match<AbstractInsnNode> LNEG = opCode(Opcodes.LNEG);

    public static final Match<AbstractInsnNode> FNEG = opCode(Opcodes.FNEG);

    public static final Match<AbstractInsnNode> DNEG = opCode(Opcodes.DNEG);

    public static final Match<AbstractInsnNode> ISHL = opCode(Opcodes.ISHL);

    public static final Match<AbstractInsnNode> LSHL = opCode(Opcodes.LSHL);

    public static final Match<AbstractInsnNode> ISHR = opCode(Opcodes.ISHR);

    public static final Match<AbstractInsnNode> LSHR = opCode(Opcodes.LSHR);

    public static final Match<AbstractInsnNode> IUSHR = opCode(Opcodes.IUSHR);

    public static final Match<AbstractInsnNode> LUSHR = opCode(Opcodes.LUSHR);

    public static final Match<AbstractInsnNode> IAND = opCode(Opcodes.IAND);

    public static final Match<AbstractInsnNode> LAND = opCode(Opcodes.LAND);

    public static final Match<AbstractInsnNode> IOR = opCode(Opcodes.IOR);

    public static final Match<AbstractInsnNode> LOR = opCode(Opcodes.LOR);

    public static final Match<AbstractInsnNode> IXOR = opCode(Opcodes.IXOR);

    public static final Match<AbstractInsnNode> LXOR = opCode(Opcodes.LXOR);

    public static final Match<AbstractInsnNode> IINC = opCode(Opcodes.IINC);

    public static final Match<AbstractInsnNode> I2L = opCode(Opcodes.I2L);

    public static final Match<AbstractInsnNode> I2F = opCode(Opcodes.I2F);

    public static final Match<AbstractInsnNode> I2D = opCode(Opcodes.I2D);

    public static final Match<AbstractInsnNode> L2I = opCode(Opcodes.L2I);

    public static final Match<AbstractInsnNode> L2F = opCode(Opcodes.L2F);

    public static final Match<AbstractInsnNode> L2D = opCode(Opcodes.L2D);

    public static final Match<AbstractInsnNode> F2I = opCode(Opcodes.F2I);

    public static final Match<AbstractInsnNode> F2L = opCode(Opcodes.F2L);

    public static final Match<AbstractInsnNode> F2D = opCode(Opcodes.F2D);

    public static final Match<AbstractInsnNode> D2I = opCode(Opcodes.D2I);

    public static final Match<AbstractInsnNode> D2L = opCode(Opcodes.D2L);

    public static final Match<AbstractInsnNode> D2F = opCode(Opcodes.D2F);

    public static final Match<AbstractInsnNode> I2B = opCode(Opcodes.I2B);

    public static final Match<AbstractInsnNode> I2C = opCode(Opcodes.I2C);

    public static final Match<AbstractInsnNode> I2S = opCode(Opcodes.I2S);

    public static final Match<AbstractInsnNode> LCMP = opCode(Opcodes.LCMP);

    public static final Match<AbstractInsnNode> FCMPL = opCode(Opcodes.FCMPL);

    public static final Match<AbstractInsnNode> FCMPG = opCode(Opcodes.FCMPG);

    public static final Match<AbstractInsnNode> DCMPL = opCode(Opcodes.DCMPL);

    public static final Match<AbstractInsnNode> DCMPG = opCode(Opcodes.DCMPG);

    public static final Match<AbstractInsnNode> IFEQ = opCode(Opcodes.IFEQ);

    public static final Match<AbstractInsnNode> IFNE = opCode(Opcodes.IFNE);

    public static final Match<AbstractInsnNode> IFLT = opCode(Opcodes.IFLT);

    public static final Match<AbstractInsnNode> IFGE = opCode(Opcodes.IFGE);

    public static final Match<AbstractInsnNode> IFGT = opCode(Opcodes.IFGT);

    public static final Match<AbstractInsnNode> IFLE = opCode(Opcodes.IFLE);

    public static final Match<AbstractInsnNode> IF_ICMPEQ = opCode(Opcodes.IF_ICMPEQ);

    public static final Match<AbstractInsnNode> IF_ICMPNE = opCode(Opcodes.IF_ICMPNE);

    public static final Match<AbstractInsnNode> IF_ICMPLT = opCode(Opcodes.IF_ICMPLT);

    public static final Match<AbstractInsnNode> IF_ICMPGE = opCode(Opcodes.IF_ICMPGE);

    public static final Match<AbstractInsnNode> IF_ICMPGT = opCode(Opcodes.IF_ICMPGT);

    public static final Match<AbstractInsnNode> IF_ICMPLE = opCode(Opcodes.IF_ICMPLE);

    public static final Match<AbstractInsnNode> IF_ACMPEQ = opCode(Opcodes.IF_ACMPEQ);

    public static final Match<AbstractInsnNode> IF_ACMPNE = opCode(Opcodes.IF_ACMPNE);

    public static final Match<AbstractInsnNode> GOTO = opCode(Opcodes.GOTO);

    public static final Match<AbstractInsnNode> JSR = opCode(Opcodes.JSR);

    public static final Match<AbstractInsnNode> RET = opCode(Opcodes.RET);

    public static final Match<AbstractInsnNode> TABLESWITCH = opCode(Opcodes.TABLESWITCH);

    public static final Match<AbstractInsnNode> LOOKUPSWITCH = opCode(Opcodes.LOOKUPSWITCH);

    public static final Match<AbstractInsnNode> IRETURN = opCode(Opcodes.IRETURN);

    public static final Match<AbstractInsnNode> LRETURN = opCode(Opcodes.LRETURN);

    public static final Match<AbstractInsnNode> FRETURN = opCode(Opcodes.FRETURN);

    public static final Match<AbstractInsnNode> DRETURN = opCode(Opcodes.DRETURN);

    public static final Match<AbstractInsnNode> ARETURN = opCode(Opcodes.ARETURN);

    public static final Match<AbstractInsnNode> RETURN = opCode(Opcodes.RETURN);

    public static final Match<AbstractInsnNode> GETSTATIC = opCode(Opcodes.GETSTATIC);

    public static final Match<AbstractInsnNode> PUTSTATIC = opCode(Opcodes.PUTSTATIC);

    public static final Match<AbstractInsnNode> GETFIELD = opCode(Opcodes.GETFIELD);

    public static final Match<AbstractInsnNode> PUTFIELD = opCode(Opcodes.PUTFIELD);

    public static final Match<AbstractInsnNode> INVOKEVIRTUAL = opCode(Opcodes.INVOKEVIRTUAL);

    public static final Match<AbstractInsnNode> INVOKESPECIAL = opCode(Opcodes.INVOKESPECIAL);

    public static final Match<AbstractInsnNode> INVOKESTATIC = opCode(Opcodes.INVOKESTATIC);

    public static final Match<AbstractInsnNode> INVOKEINTERFACE = opCode(Opcodes.INVOKEINTERFACE);

    public static final Match<AbstractInsnNode> INVOKEDYNAMIC = opCode(Opcodes.INVOKEDYNAMIC);

    public static final Match<AbstractInsnNode> NEW = opCode(Opcodes.NEW);

    public static final Match<AbstractInsnNode> NEWARRAY = opCode(Opcodes.NEWARRAY);

    public static final Match<AbstractInsnNode> ANEWARRAY = opCode(Opcodes.ANEWARRAY);

    public static final Match<AbstractInsnNode> ARRAYLENGTH = opCode(Opcodes.ARRAYLENGTH);

    public static final Match<AbstractInsnNode> ATHROW = opCode(Opcodes.ATHROW);

    public static final Match<AbstractInsnNode> CHECKCAST = opCode(Opcodes.CHECKCAST);

    public static final Match<AbstractInsnNode> INSTANCEOF = opCode(Opcodes.INSTANCEOF);

    public static final Match<AbstractInsnNode> MONITORENTER = opCode(Opcodes.MONITORENTER);

    public static final Match<AbstractInsnNode> MONITOREXIT = opCode(Opcodes.MONITOREXIT);

    public static final Match<AbstractInsnNode> MULTIANEWARRAY = opCode(Opcodes.MULTIANEWARRAY);

    public static final Match<AbstractInsnNode> IFNULL = opCode(Opcodes.IFNULL);

    public static final Match<AbstractInsnNode> IFNONNULL = opCode(Opcodes.IFNONNULL);

}
