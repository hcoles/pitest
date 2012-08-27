package org.pitest.bytecode.blocks;

public interface BlockCounter {

  void registerNewBlock();

  void registerFinallyBlockStart();

  void registerFinallyBlockEnd();

}
