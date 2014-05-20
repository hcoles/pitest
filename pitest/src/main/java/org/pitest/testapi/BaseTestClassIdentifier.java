package org.pitest.testapi;

import org.pitest.classinfo.ClassInfo;

public abstract class BaseTestClassIdentifier implements TestClassIdentifier {

  public boolean isIncluded(ClassInfo a) {
    return true;
  }
  
}
