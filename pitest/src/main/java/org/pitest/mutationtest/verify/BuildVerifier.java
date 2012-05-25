package org.pitest.mutationtest.verify;

import org.pitest.classinfo.CodeSource;

public interface BuildVerifier {

  public void verify(CodeSource coverageDatabase);

}
