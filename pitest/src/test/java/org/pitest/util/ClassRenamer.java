package org.pitest.util;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.RemappingClassAdapter;
import org.objectweb.asm.commons.SimpleRemapper;

@SuppressWarnings("deprecation")
public class ClassRenamer {
  
  public static byte[] rename(byte[] bytes, String oldName, String newName) {
    ClassReader classReader = new ClassReader(bytes);               
    ClassWriter classWriter = new ClassWriter(classReader, 0);              
    Remapper remapper = new SimpleRemapper(oldName, newName);   
  
    classReader.accept(new RemappingClassAdapter(classWriter, remapper), ClassReader.EXPAND_FRAMES);    
    
    return classWriter.toByteArray();
      
   }

}
