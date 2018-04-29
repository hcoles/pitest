package org.pitest.mutationtest.verify;

import org.pitest.classpath.CodeSource;

public interface BuildVerifier {

  void verify(CodeSource coverageDatabase);

}
