package org.pitest.bytecode;

import org.objectweb.asm.Opcodes;

public class ASMVersion {
  public static final int ASM_VERSION = Opcodes.ASM9;

  /**
   * Provide the asm version via a method call so third party plugins built against pitest
   * will receive the current asm version instead of one inlined at build time
   * @return asm version
   */
  public static int asmVersion() {
    return ASM_VERSION;
  }
}
