package org.pitest.bytecode;

import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SignatureParser {

    /**
     * Extracts all types referenced in a field generic signature
     */
  public static Set<String> extractTypes(String signature) {
      if (signature == null) {
          return Collections.emptySet();
      }

      Set<String> types = new HashSet<>();
      SignatureReader r = new SignatureReader(signature);
      SignatureVisitor visitor = new SignatureVisitor(ASMVersion.asmVersion()) {
          @Override
          public void visitClassType(String name) {
              types.add(name);
          }
      };
      r.acceptType(visitor);
      return types;
  }
}
