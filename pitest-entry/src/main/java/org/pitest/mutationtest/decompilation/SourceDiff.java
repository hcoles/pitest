package org.pitest.mutationtest.decompilation;

import java.util.List;

import difflib.Chunk;
import difflib.Delta;
import difflib.Delta.TYPE;
import difflib.DiffUtils;
import difflib.Patch;

class SourceDiff {
  
  String describe(List<String> orig, List<String> modified) {
    final Patch<String> patch = DiffUtils.diff(orig, modified);
    
    StringBuilder sb = new StringBuilder();
    for (Delta<String> each : patch.getDeltas()) {
      if (each.getType() == TYPE.DELETE) {
        sb.append("Deleted \n" + chunkToString(each.getOriginal()));
      }
      
      if (each.getType() == TYPE.INSERT) {
        sb.append("Inserted \n" + chunkToString(each.getRevised()));
      }
      
      if (each.getType() == TYPE.CHANGE) {
        sb.append("Changed <br/>" + chunkToString(each.getOriginal()) + " to " + chunkToString(each.getRevised()));
      }      
    }
    

    return sb.toString();
  }
  
  private String chunkToString(Chunk<String> original) {
    StringBuilder sb = new StringBuilder();
   for (String each : original.getLines()) {
     sb.append(each.trim());
   }
   return sb.toString();
  }


}
