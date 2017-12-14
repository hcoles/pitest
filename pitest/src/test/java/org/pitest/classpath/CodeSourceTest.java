package org.pitest.classpath;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.classinfo.ClassInfo;
import org.pitest.classinfo.ClassInfoMother;
import org.pitest.classinfo.ClassName;
import org.pitest.classinfo.Repository;
import org.pitest.functional.Option;

public class CodeSourceTest {

  private CodeSource          testee;

  @Mock
  private Repository          repository;

  @Mock
  private ProjectClassPaths   classPath;

  private ClassInfo           foo;

  private ClassInfo           bar;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    this.testee = new CodeSource(this.classPath, this.repository);
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
  public void shouldIdentifyTestClassesOnTestPath() {
    when(this.classPath.test()).thenReturn(
        Arrays.asList(this.foo.getName(), this.bar.getName()));
    assertThat(this.testee.getTests()).containsExactly(foo, bar);
  }

  @Test
  public void shouldProvideNamesOfCodeClasses() {
    final ClassInfo foo = makeClassInfo("Foo");
    final ClassInfo bar = makeClassInfo("Bar");
    when(this.classPath.code()).thenReturn(
        Arrays.asList(foo.getName(), bar.getName()));

    assertThat(this.testee.getCodeUnderTestNames()).containsOnly(ClassName.fromString("Foo")
        , ClassName.fromString("Bar"));
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
