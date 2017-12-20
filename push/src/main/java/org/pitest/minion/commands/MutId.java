package org.pitest.minion.commands;

import java.beans.ConstructorProperties;

public class MutId {
  private final String  clazz;
  private final String method;
  private final String     methodDesc;
  private final int index;
  private final String operator;
  
  @ConstructorProperties({"clazz", "method", "methodDesc", "index", "operator"}) 
  public MutId(String clazz, String method, String methodDesc, int index,
      String operator) {
    this.clazz = clazz;
    this.method = method;
    this.methodDesc = methodDesc;
    this.index = index;
    this.operator = operator;
  }

  public String getClazz() {
    return clazz;
  }

  public String getMethod() {
    return method;
  }

  public String getMethodDesc() {
    return methodDesc;
  }

  public int getIndex() {
    return index;
  }

  public String getOperator() {
    return operator;
  }

  @Override
  public String toString() {
    return "MutId [clazz=" + clazz + ", method=" + method + ", methodDesc="
        + methodDesc + ", index=" + index + ", operator=" + operator + "]";
  }
  
  
  
}
