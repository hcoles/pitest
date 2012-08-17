package org.pitest.classinfo;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.extension.TestClassIdentifier;
import org.pitest.functional.Option;
import org.pitest.mutationtest.MutationClassPaths;


public class CodeSourceTest {
  
  private CodeSource testee;
  
  @Mock
  private Repository repository;
  
  @Mock
  private MutationClassPaths classPath;
  
  @Mock
  private TestClassIdentifier testIdentifer;
  
  private ClassInfo foo;
  
  private ClassInfo bar;
  
  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    testee = new CodeSource(classPath, repository, testIdentifer);
    foo = makeClassInfo("Foo");
    bar = makeClassInfo("Bar");
  }
  
  @Test
  public void shouldIdentifyAllNonTestCodeOnClassPathWhenNoTestsPresent() {
    when(classPath.code()).thenReturn(Arrays.asList(foo.getName(), bar.getName()));
    assertEquals(Arrays.asList(foo,bar) , testee.getCode());
  }
  
  @Test
  public void shouldIdentifyAllNonTestCodeOnClassPathWhenTestsPresentOnCodePath() {
    when(testIdentifer.isATestClass(foo)).thenReturn(true);
    when(classPath.code()).thenReturn(Arrays.asList(foo.getName(), bar.getName()));

    assertEquals(Arrays.asList(bar) , testee.getCode());
  }
  
  @Test
  public void shouldIdentifyAllTestCodeOnTestPath() {
    when(testIdentifer.isATestClass(foo)).thenReturn(true);
    when(classPath.test()).thenReturn(Arrays.asList(foo.getName(), bar.getName()));

    assertEquals(Arrays.asList(foo) , testee.getTests());
  }
  
  @Test
  public void shouldProvideNamesOfNonTestClasses() {
    ClassInfo foo = makeClassInfo("Foo");
    ClassInfo bar = makeClassInfo("Bar");
    when(testIdentifer.isATestClass(foo)).thenReturn(true);
    when(classPath.code()).thenReturn(Arrays.asList(foo.getName(), bar.getName()));

    assertEquals(new HashSet<ClassName>(Arrays.asList(new ClassName("Bar"))) , testee.getCodeUnderTestNames());
  }
  
  @Test
  public void shouldMapTestsPostfixedWithTestToTesteeWhenTesteeExists() {
    when(this.repository.hasClass(new ClassName("com.example.Foo"))).thenReturn(true);
    assertEquals(new ClassName("com.example.Foo"),
        this.testee.findTestee("com.example.FooTest").value());
  }

  @Test
  public void shouldMapTestsPrefixedWithTestToTesteeWhenTesteeExists() {
    when(this.repository.hasClass(new ClassName("com.example.Foo"))).thenReturn(true);
    assertEquals(new ClassName("com.example.Foo"),
        this.testee.findTestee("com.example.TestFoo").value());
  }

  @Test
  public void shouldReturnNoneWhenNoTesteeExistsMatchingNamingConvention() {
    when(this.repository.hasClass(new ClassName("com.example.Foo"))).thenReturn(false);
    assertEquals(Option.none(), this.testee.findTestee("com.example.TestFoo"));
  }
  
  @Test
  public void shouldProvideDetailsOfRequestedClasses() {
    when(this.repository.fetchClass(ClassName.fromString("Foo"))).thenReturn(Option.some(foo));
    when(this.repository.fetchClass(ClassName.fromString("Unknown"))).thenReturn(Option.<ClassInfo>none());
    assertEquals(Arrays.asList(foo), this.testee.getClassInfo(Arrays.asList(ClassName.fromString("Foo"),ClassName.fromString("Unknown"))));
  }
  
  private ClassInfo makeClassInfo(String name) {
    ClassInfoBuilder data = new ClassInfoBuilder();
    data.id = new ClassIdentifier(1,new ClassName(name));
    ClassInfo ci = new ClassInfo(null,null,data);
    when(repository.fetchClass(ClassName.fromString(name))).thenReturn(Option.some(ci));
    return ci;
  }

}
