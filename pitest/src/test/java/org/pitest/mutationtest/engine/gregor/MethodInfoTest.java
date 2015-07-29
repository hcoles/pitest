/*
 * Copyright 2011 Henry Coles
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.pitest.mutationtest.engine.gregor;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class MethodInfoTest {

  private static final int       NON_STATIC_MODIFIER    = 0;
  private static final int       NON_SYNTHETIC_MODIFIER = 0;
  private static final String    VOID_RETURN            = "()V";
  private static final String    STRING_RETURN          = "()Ljava/lang/String;";
  private static final int       STATIC_MODIFIER        = Opcodes.ACC_STATIC;
  private static final String    NO_PARAMETERS          = "()V";
  private static final String    ONE_PARAMETER          = "(Ljava/lang/String;)V";
  private static final int       SYNTHETIC_MODIFIER     = Opcodes.ACC_SYNTHETIC;
  private static final ClassInfo ENUMERATION_CLASS      = new ClassInfo(0, 0,
      "", "",
      "java/lang/Enum",
      new String[0]);
  private final MethodInfo       methodInfo             = new MethodInfo();

  @Test
  public void isVoidShouldReturnTrueWhenMethodIsVoid() {
    final MethodInfo testee = this.methodInfo.withMethodDescriptor(VOID_RETURN);
    assertThat(testee.isVoid(), is(true));
  }

  @Test
  public void isVoidShouldReturnFalseWhenMethodIsNotVoid() {
    final MethodInfo testee = this.methodInfo
        .withMethodDescriptor(STRING_RETURN);
    assertThat(testee.isVoid(), is(false));
  }

  @Test
  public void isStaticShouldReturnTrueWhenMethodIsStatic() {
    final MethodInfo testee = this.methodInfo.withAccess(STATIC_MODIFIER);
    assertThat(testee.isStatic(), is(true));
  }

  @Test
  public void isStaticShouldReturnFalseWhenMethodIsNotStatic() {
    final MethodInfo testee = this.methodInfo.withAccess(NON_STATIC_MODIFIER);
    assertThat(testee.isStatic(), is(false));
  }

  @Test
  public void takesNoParametersShouldReturnTrueWhenMethodTakesNoParameters() {
    final MethodInfo testee = this.methodInfo
        .withMethodDescriptor(NO_PARAMETERS);
    assertThat(testee.takesNoParameters(), is(true));
  }

  @Test
  public void takesNoParametersShouldReturnFalseWhenMethodTakesOneParameter() {
    final MethodInfo testee = this.methodInfo
        .withMethodDescriptor(ONE_PARAMETER);
    assertThat(testee.takesNoParameters(), is(false));
  }

  @Test
  public void isConstructorShouldReturnTrueWhenMethodIsConstructor() {
    final MethodInfo testee = this.methodInfo.withMethodName("<init>");
    assertThat(testee.isConstructor(), is(true));
  }

  @Test
  public void isConstructorShouldReturnTrueWhenMethodIsRegularMethod() {
    final MethodInfo testee = this.methodInfo.withMethodName("toString");
    assertThat(testee.isConstructor(), is(false));
  }

  @Test
  public void isSyntheticShouldReturnTrueWhenSyntheticAccessFlagSet() {
    final MethodInfo testee = this.methodInfo.withAccess(SYNTHETIC_MODIFIER);
    assertThat(testee.isSynthetic(), is(true));
  }

  @Test
  public void isSyntheticShouldReturnFalseWhenNoSyntheticAccessFlagSet() {
    final MethodInfo testee = this.methodInfo
        .withAccess(NON_SYNTHETIC_MODIFIER);
    assertThat(testee.isSynthetic(), is(false));
  }

  @Test
  public void isStaticInitializerShouldReturnTrueWhenMethodIsStaticInitializer() {
    final MethodInfo testee = this.methodInfo.withMethodName("<clinit>");
    assertThat(testee.isStaticInitializer(), is(true));
  }

  @Test
  public void getReturnTypeReturnsCorrectReturnType() {
    final MethodInfo testee = this.methodInfo
        .withMethodDescriptor(STRING_RETURN);
    assertThat(testee.getReturnType(), is(Type.getType(String.class)));
  }

  @Test
  public void getDescriptionReturnsQualifiedMethodName() {
    final String EXAMPLE_CLASS_NAME = "org.pitest.Example";
    final ClassInfo EXAMPLE_CLASS_INFO = new ClassInfo(0, 0,
        EXAMPLE_CLASS_NAME, "", "", new String[0]);
    final String EXAMPLE_METHOD_NAME = "myMethod";
    final String QUALIFIED_METHOD_NAME = EXAMPLE_CLASS_NAME + "::"
        + EXAMPLE_METHOD_NAME;

    final MethodInfo testee = this.methodInfo.withOwner(EXAMPLE_CLASS_INFO)
        .withMethodName(EXAMPLE_METHOD_NAME);

    assertThat(testee.getDescription(), is(QUALIFIED_METHOD_NAME));
  }

  @Test
  public void isGeneratedEnumMethodReturnsTrueIfMethodIsEnumValuesMethod() {
    final MethodInfo testee = this.methodInfo.withOwner(ENUMERATION_CLASS)
        .withAccess(STATIC_MODIFIER).withMethodName("values")
        .withMethodDescriptor(NO_PARAMETERS);

    assertThat(testee.isGeneratedEnumMethod(), is(true));
  }

  @Test
  public void isGeneratedEnumMethodReturnsTrueIfMethodIsEnumValueOfMethod() {
    final MethodInfo testee = this.methodInfo.withOwner(ENUMERATION_CLASS)
        .withAccess(STATIC_MODIFIER).withMethodName("valueOf")
        .withMethodDescriptor("(Ljava/lang/String;)Lorg/pitest/MyEnum;");

    assertThat(testee.isGeneratedEnumMethod(), is(true));
  }

  @Test
  public void isGeneratedEnumMethodReturnsTrueIfMethodIsStaticInitializerInEnum() {
    final MethodInfo testee = this.methodInfo.withOwner(ENUMERATION_CLASS)
        .withAccess(STATIC_MODIFIER).withMethodName("<clinit>");

    assertThat(testee.isGeneratedEnumMethod(), is(true));
  }

  @Test
  public void isGeneratedEnumMethodReturnsFalseForRegularEnumMethod() {
    final MethodInfo testee = this.methodInfo.withOwner(ENUMERATION_CLASS)
        .withMethodName("getOwner");

    assertThat(testee.isGeneratedEnumMethod(), is(false));
  }

  @Test
  public void isGeneratedEnumMethodReturnsFalseForNonEnumClasses() {
    final ClassInfo EXAMPLE_CLASS_INFO = new ClassInfo(0, 0,
        "org/pitest/Example", "", "java/lang/Object", new String[0]);
    final MethodInfo testee = this.methodInfo.withOwner(EXAMPLE_CLASS_INFO)
        .withAccess(STATIC_MODIFIER).withMethodName("values")
        .withMethodDescriptor(NO_PARAMETERS);

    assertThat(testee.isGeneratedEnumMethod(), is(false));
  }

}
