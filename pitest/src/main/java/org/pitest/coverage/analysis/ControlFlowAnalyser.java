package org.pitest.coverage.analysis;

import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.DRETURN;
import static org.objectweb.asm.Opcodes.FRETURN;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.LRETURN;
import static org.objectweb.asm.Opcodes.RETURN;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

public class ControlFlowAnalyser {
  
  private static int LIKELY_NUMBER_OF_LINES_PER_BLOCK = 7;

  public List<Block> analyze(MethodNode mn) {
    List<Block> blocks = new ArrayList<Block>(mn.instructions.size());
    
    Set<AbstractInsnNode> jumpTargets = findJumpTargets(mn.instructions);
    
    // not managed to construct bytecode to show need for this
    // as try catch blocks usually have jumps at their boundaries anyway.
    // so possibly useless, but here for now. Because fear.
    addtryCatchBoundaries(mn,jumpTargets);

    Set<Integer> blockLines = smallSet();
    int lastLine = Integer.MIN_VALUE;
    
    final int lastInstruction = mn.instructions.size() - 1;
    
    int blockStart = 0;
    for (int i = 0; i != mn.instructions.size(); i++ ) {

      AbstractInsnNode ins = mn.instructions.get(i);

      if (ins instanceof LineNumberNode){
    	  LineNumberNode lnn = (LineNumberNode) ins;
    	  blockLines.add(lnn.line);
       lastLine = lnn.line;
      } else if (jumpTargets.contains(ins) && blockStart != i) {
        blocks.add(new Block(blockStart, i - 1,blockLines));
        blockStart = i;
        blockLines = smallSet();
      } else if (endsBlock(ins)) {
        blocks.add(new Block(blockStart, i,blockLines));
        blockStart = i + 1;
        blockLines = smallSet();
      } else if ( lastLine != Integer.MIN_VALUE && isInstruction(ins)) {
        blockLines.add(lastLine);
      }
    }
    
    // this will not create a block if the last block contains only a single 
    // instruction.
    // In the case of the hanging labels that eclipse compiler seems to generate this is desirable.
    // Not clear if this will create problems in other scenarios
    if ( blockStart != lastInstruction) {
      blocks.add(new Block(blockStart, lastInstruction, blockLines));
    }

    return blocks;

  }
  
  private static HashSet<Integer> smallSet() {
    return new HashSet<Integer>(LIKELY_NUMBER_OF_LINES_PER_BLOCK);
  }

  private boolean isInstruction(AbstractInsnNode ins) {
    return !(ins instanceof LabelNode || ins instanceof FrameNode); 
  }
  
  private void addtryCatchBoundaries(
      MethodNode mn, Set<AbstractInsnNode> jumpTargets) {
   for ( Object each : mn.tryCatchBlocks) {
     TryCatchBlockNode tcb = (TryCatchBlockNode) each;
     jumpTargets.add(tcb.handler);
   }
  }

  private boolean endsBlock(AbstractInsnNode ins) {
    return (ins instanceof JumpInsnNode) || isReturn(ins);
  }
  
  

  private boolean isReturn(AbstractInsnNode ins) {
    int opcode = ins.getOpcode();
    switch ( opcode ) {
    case RETURN:
    case ARETURN:
    case DRETURN:
    case FRETURN:
    case IRETURN:
    case LRETURN:
    case ATHROW:
    return true;
    }
    
    return false;

  }

  @SuppressWarnings("unchecked") // asm jar has no generics info
  private Set<AbstractInsnNode> findJumpTargets(InsnList instructions) {
    Set<AbstractInsnNode> jumpTargets = new HashSet<AbstractInsnNode>();
    ListIterator<?> it = instructions.iterator();
    while ( it.hasNext() ) {
      Object o = it.next();
      if ( o instanceof JumpInsnNode ) {
        jumpTargets.add( ((JumpInsnNode)o).label );
      } else if ( o instanceof TableSwitchInsnNode) {
        TableSwitchInsnNode twn = (TableSwitchInsnNode) o;
        jumpTargets.add(twn.dflt);
        jumpTargets.addAll(twn.labels);
      } else if (o instanceof LookupSwitchInsnNode) {
        LookupSwitchInsnNode lsn = (LookupSwitchInsnNode) o;
        jumpTargets.add(lsn.dflt);
        jumpTargets.addAll(lsn.labels);
      }
    }
    return jumpTargets;
  }

}


