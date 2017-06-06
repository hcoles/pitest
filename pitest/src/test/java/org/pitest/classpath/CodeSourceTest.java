package org.pitest.classpath;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.classinfo.ClassInfo;
import org.pitest.classinfo.ClassInfoMother;
import org.pitest.classinfo.ClassName;
import org.pitest.classinfo.Repository;
import org.pitest.functional.Option;
import org.pitest.testapi.TestClassIdentifier;

public class CodeSourceTest {

  private CodeSource          testee;

  @Mock
  private Repository          repository;

  @Mock
  private ProjectClassPaths   classPath;

  @Mock
  private TestClassIdentifier testIdentifer;

  private ClassInfo           foo;

  private ClassInfo           bar;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    this.testee = new CodeSource(this.classPath, this.repository,
        this.testIdentifer);
    this.foo = makeClassInfo("Foo");
    this.bar = makeClassInfo("Bar");
  }

  @Test
  public void shouldIdentifyAllNonTestCodeOnClassPathWhenNoTestsPresent() {
    when(this.classPath.code()).thenReturn(
        Arrays.asList(this.foo.getName(), this.bar.getName()));
    assertEquals(Arrays.asList(this.foo, this.bar), this.testee.getCode());
  }

  @Test
  public void shouldIdentifyAllNonTestCodeOnClassPathWhenTestsPresentOnCodePath() {
    when(this.testIdentifer.isATestClass(this.foo)).thenReturn(true);
    when(this.classPath.code()).thenReturn(
        Arrays.asList(this.foo.getName(), this.bar.getName()));

    assertEquals(Arrays.asList(this.bar), this.testee.getCode());
  }

  @Test
  public void shouldIdentifyTestClassesOnTestPath() {
    when(this.testIdentifer.isATestClass(this.foo)).thenReturn(true);
    when(this.testIdentifer.isIncluded(any(ClassInfo.class))).thenReturn(true);
    when(this.classPath.test()).thenReturn(
        Arrays.asList(this.foo.getName(), this.bar.getName()));

    assertEquals(Arrays.asList(this.foo), this.testee.getTests());
  }

  @Test
  public void shouldOnlyIdentifyIncludedTestClassesOnTestPath() {
    when(this.testIdentifer.isATestClass(any(ClassInfo.class)))
        .thenReturn(true);
    when(this.testIdentifer.isIncluded(this.foo)).thenReturn(true);
    when(this.classPath.test()).thenReturn(
        Arrays.asList(this.foo.getName(), this.bar.getName()));

    assertEquals(Arrays.asList(this.foo), this.testee.getTests());
  }

  @Test
  public void shouldNotIdentifyExcludedTestClassesOnTestPath() {
    when(this.testIdentifer.isATestClass(any(ClassInfo.class)))
        .thenReturn(true);
    when(this.testIdentifer.isIncluded(any(ClassInfo.class))).thenReturn(false);
    when(this.classPath.test()).thenReturn(
        Arrays.asList(this.foo.getName(), this.bar.getName()));

    assertEquals(Collections.emptyList(), this.testee.getTests());
  }

  @Test
  public void shouldProvideNamesOfNonTestClasses() {
    final ClassInfo foo = makeClassInfo("Foo");
    final ClassInfo bar = makeClassInfo("Bar");
    when(this.testIdentifer.isATestClass(foo)).thenReturn(true);
    when(this.classPath.code()).thenReturn(
        Arrays.asList(foo.getName(), bar.getName()));

    assertEquals(new HashSet<ClassName>(Arrays.asList(ClassName.fromString("Bar"))),
        this.testee.getCodeUnderTestNames());
  }

  @Test
  public void shouldMapTestsPostfixedWithTestToTesteeWhenTesteeExists() {
    when(this.repository.hasClass(ClassName.fromString("com.example.Foo")))
    .thenReturn(true);
    assertEquals(ClassName.fromString("com.example.Foo"),
        this.testee.findTestee("com.example.FooTest").value());
  }

  @Test
  public void shouldMapTestsPrefixedWithTestToTesteeWhenTesteeExists() {
    when(this.repository.hasClass(ClassName.fromString("com.example.Foo")))
    .thenReturn(true);
    assertEquals(ClassName.fromString("com.example.Foo"),
        this.testee.findTestee("com.example.TestFoo").value());
  }

  @Test
  public void shouldReturnNoneWhenNoTesteeExistsMatchingNamingConvention() {
    when(this.repository.hasClass(ClassName.fromString("com.example.Foo")))
    .thenReturn(false);
    assertEquals(Option.<ClassName> none(),
        this.testee.findTestee("com.example.TestFoo"));
  }

  @Test
  public void shouldProvideDetailsOfRequestedClasses() {
    when(this.repository.fetchClass(ClassName.fromString("Foo"))).thenReturn(
        Option.some(this.foo));
    when(this.repository.fetchClass(ClassName.fromString("Unknown")))
    .thenReturn(Option.<ClassInfo> none());
    assertEquals(Arrays.asList(this.foo), this.testee.getClassInfo(Arrays
        .asList(ClassName.fromString("Foo"), ClassName.fromString("Unknown"))));
  }

  @Test
  public void shouldAllowClientsToRetrieveBytecode() {
    this.testee.fetchClassBytes(ClassName.fromString("Foo"));
    verify(this.repository).querySource(ClassName.fromString("Foo"));
  }

  private ClassInfo makeClassInfo(final String name) {
    final ClassInfo ci = ClassInfoMother.make(name);
    when(this.repository.fetchClass(ClassName.fromString(name))).thenReturn(
        Option.some(ci));
    return ci;
  }

}
