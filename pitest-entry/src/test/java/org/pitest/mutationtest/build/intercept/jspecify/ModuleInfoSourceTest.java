package org.pitest.mutationtest.build.intercept.jspecify;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import org.junit.Test;
import org.pitest.classinfo.ClassName;
import org.pitest.classpath.ClassPath;
import org.pitest.classpath.ClassPathRoot;

public class ModuleInfoSourceTest {

  private static final byte[] FIRST_MODULE = {1};
  private static final byte[] SECOND_MODULE = {2};

  private final ClassPath classPath = new ClassPath(
      new StubRoot(Set.of("com.example.A"), FIRST_MODULE),
      new StubRoot(Set.of("com.example.B"), SECOND_MODULE),
      new StubRoot(Set.of("com.example.NoModule"), null));

  private final ModuleInfoSource underTest = ModuleInfoSource.fromClassPath(classPath);

  @Test
  public void findsModuleInfoFromRootContainingFirstClass() {
    assertThat(underTest.moduleInfoFor(ClassName.fromString("com.example.A")))
        .contains(FIRST_MODULE);
  }

  @Test
  public void findsModuleInfoFromRootContainingSecondClass() {
    assertThat(underTest.moduleInfoFor(ClassName.fromString("com.example.B")))
        .contains(SECOND_MODULE);
  }

  @Test
  public void doesNotUseModuleInfoFromOtherRoots() {
    assertThat(underTest.moduleInfoFor(ClassName.fromString("com.example.NoModule")))
        .isEmpty();
  }

  @Test
  public void returnsEmptyForUnknownClass() {
    assertThat(underTest.moduleInfoFor(ClassName.fromString("com.example.Unknown")))
        .isEmpty();
  }

  private static class StubRoot implements ClassPathRoot {
    private final Set<String> classes;
    private final byte[] moduleInfo;

    StubRoot(Set<String> classes, byte[] moduleInfo) {
      this.classes = classes;
      this.moduleInfo = moduleInfo;
    }

    @Override
    public InputStream getData(String name) {
      if (name.equals("module-info") && moduleInfo != null) {
        return new ByteArrayInputStream(moduleInfo);
      }
      return classes.contains(name) ? new ByteArrayInputStream(new byte[0]) : null;
    }

    @Override
    public URL getResource(String name) {
      return null;
    }

    @Override
    public Collection<String> classNames() {
      return classes;
    }

    @Override
    public Optional<String> cacheLocation() {
      return Optional.empty();
    }
  }
}
