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
import org.pitest.functional.predicate.True;
import org.pitest.mutationtest.engine.Mutant;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.gregor.GregorMutater;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
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
    Collection<MethodMutatorFactory> mutators = Mutator.defaults();
    mutator = new GregorMutater(source, True.<MethodInfo>all(), mutators);
    testee = new MutantExportInterceptor(fileSystem, source, "target");
  }

  @Test
  public void shouldCreateAMutantsDirectoryForEachClass() {
    testee.begin(tree(Foo.class));
    testee.begin(tree(String.class));
    assertThat(fileSystem.getPath("target", "export", "org", "pitest", "plugin", "export", "Foo", "mutants")).exists();
    assertThat(fileSystem.getPath("target", "export", "java", "lang", "String", "mutants")).exists();
  }

  @Test
  public void shouldReturnMutantListUnmodified() {
    Collection<MutationDetails> mutations = mutator.findMutations(ClassName.fromClass(VeryMutable.class));
   
    testee.begin(tree(VeryMutable.class));
    Collection<MutationDetails> actual = testee.intercept(mutations, mutator);
    testee.end();
    
    assertThat(actual).isSameAs(mutations);
  }

  @Test
  public void shouldWriteMutantBytesToDisk() {
    Collection<MutationDetails> mutations = executeFor(VeryMutable.class);
    
    Mutant firstMutant = mutator.getMutation(mutations.iterator().next().getId());
    Path shouldBeCreated = mutantBasePath(VeryMutable.class,0).resolve(ClassName.fromClass(VeryMutable.class).asJavaName() + ".class");
    assertThat(shouldBeCreated).hasBinaryContent(firstMutant.getBytes());
  }

  @Test
  public void shouldWriteMutantDetailsToDisk() {
    Collection<MutationDetails> mutations = executeFor(VeryMutable.class);
    
    MutationDetails firstMutant = mutations.iterator().next();
    Path shouldBeCreated = mutantBasePath(VeryMutable.class,0).resolve("details.txt");
    assertThat(shouldBeCreated).hasContent(firstMutant.toString());
  }
  
  @Test
  public void shouldWriteDissasembledMutantBytecodeToDisk() {
    executeFor(VeryMutable.class);
    Path shouldBeCreated = mutantBasePath(VeryMutable.class,0).resolve(ClassName.fromClass(VeryMutable.class).asJavaName() + ".txt");
    assertThat(shouldBeCreated).exists();
  }
  

  @Test
  public void shouldWriteDisassembledOriginalBytecodeToDisk() {
    executeFor(VeryMutable.class);
    Path shouldBeCreated = classBasePath(VeryMutable.class).resolve(ClassName.fromClass(VeryMutable.class).asJavaName() + ".txt");
    assertThat(shouldBeCreated).exists();
  }
  
  
  private Collection<MutationDetails> executeFor(Class<?> clazz) {
    Collection<MutationDetails> mutations = mutator.findMutations(ClassName.fromClass(clazz));
    
    testee.begin(tree(VeryMutable.class));
    testee.intercept(mutations, mutator);
    testee.end();
    return mutations;
  }
  

  private ClassTree tree(Class<?> clazz) {
    return ClassTree.fromBytes(source.getBytes(clazz.getName()).value());
  }
  
  private Path mutantBasePath(Class<?> clazz, int mutant) {
    return classBasePath(clazz).resolve("mutants").resolve(""+ mutant);
  }
  
  private Path classBasePath(Class<?> clazz) {
    ClassName name = ClassName.fromClass(clazz);
    return fileSystem.getPath("target/export",name.asInternalName().split("/"));
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