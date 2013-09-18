package org.pitest.mutationtest.verify;

import org.pitest.classpath.CodeSource;

public interface BuildVerifier {

  public void verify(CodeSource coverageDatabase);

}
