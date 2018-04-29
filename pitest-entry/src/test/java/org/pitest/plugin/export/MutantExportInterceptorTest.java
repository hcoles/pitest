package org.pitest.plugin.export;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.classinfo.ClassName;
import org.pitest.classpath.ClassloaderByteArraySource;
import org.pitest.mutationtest.engine.Mutant;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.gregor.GregorMutater;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.config.Mutator;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

public class MutantExportInterceptorTest {

  ClassloaderByteArraySource source = ClassloaderByteArraySource.fromContext();
  MutantExportInterceptor testee;
  FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix());
  GregorMutater mutator;

  @Before
  public void setUp() {
    final Collection<MethodMutatorFactory> mutators = Mutator.defaults();
    this.mutator = new GregorMutater(this.source, m -> true, mutators);
    this.testee = new MutantExportInterceptor(this.fileSystem, this.source, "target");
  }

  @Test
  public void shouldCreateAMutantsDirectoryForEachClass() {
    this.testee.begin(tree(Foo.class));
    this.testee.begin(tree(String.class));
    assertThat(this.fileSystem.getPath("target", "export", "org", "pitest", "plugin", "export", "Foo", "mutants")).exists();
    assertThat(this.fileSystem.getPath("target", "export", "java", "lang", "String", "mutants")).exists();
  }

  @Test
  public void shouldReturnMutantListUnmodified() {
    final Collection<MutationDetails> mutations = this.mutator.findMutations(ClassName.fromClass(VeryMutable.class));

    this.testee.begin(tree(VeryMutable.class));
    final Collection<MutationDetails> actual = this.testee.intercept(mutations, this.mutator);
    this.testee.end();

    assertThat(actual).isSameAs(mutations);
  }

  @Test
  public void shouldWriteMutantBytesToDisk() {
    final Collection<MutationDetails> mutations = executeFor(VeryMutable.class);

    final Mutant firstMutant = this.mutator.getMutation(mutations.iterator().next().getId());
    final Path shouldBeCreated = mutantBasePath(VeryMutable.class,0).resolve(ClassName.fromClass(VeryMutable.class).asJavaName() + ".class");
    assertThat(shouldBeCreated).hasBinaryContent(firstMutant.getBytes());
  }

  @Test
  public void shouldWriteMutantDetailsToDisk() {
    final Collection<MutationDetails> mutations = executeFor(VeryMutable.class);

    final MutationDetails firstMutant = mutations.iterator().next();
    final Path shouldBeCreated = mutantBasePath(VeryMutable.class,0).resolve("details.txt");
    assertThat(shouldBeCreated).hasContent(firstMutant.toString());
  }

  @Test
  public void shouldWriteDissasembledMutantBytecodeToDisk() {
    executeFor(VeryMutable.class);
    final Path shouldBeCreated = mutantBasePath(VeryMutable.class,0).resolve(ClassName.fromClass(VeryMutable.class).asJavaName() + ".txt");
    assertThat(shouldBeCreated).exists();
  }


  @Test
  public void shouldWriteDisassembledOriginalBytecodeToDisk() {
    executeFor(VeryMutable.class);
    final Path shouldBeCreated = classBasePath(VeryMutable.class).resolve(ClassName.fromClass(VeryMutable.class).asJavaName() + ".txt");
    assertThat(shouldBeCreated).exists();
  }


  private Collection<MutationDetails> executeFor(Class<?> clazz) {
    final Collection<MutationDetails> mutations = this.mutator.findMutations(ClassName.fromClass(clazz));

    this.testee.begin(tree(VeryMutable.class));
    this.testee.intercept(mutations, this.mutator);
    this.testee.end();
    return mutations;
  }


  private ClassTree tree(Class<?> clazz) {
    return ClassTree.fromBytes(this.source.getBytes(clazz.getName()).get());
  }

  private Path mutantBasePath(Class<?> clazz, int mutant) {
    return classBasePath(clazz).resolve("mutants").resolve(""+ mutant);
  }

  private Path classBasePath(Class<?> clazz) {
    final ClassName name = ClassName.fromClass(clazz);
    return this.fileSystem.getPath("target/export",name.asInternalName().split("/"));
  }
}


class Foo {

}


class VeryMutable {
  public int foo(int i) {
    for (int y = 0; y != i; y++) {
      System.out.println("" + (i * y));
    }
    return i + 2;
  }
}