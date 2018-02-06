package org.pitest.plugin.export;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceClassVisitor;
import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.classinfo.ClassName;
import org.pitest.mutationtest.build.InterceptorType;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.engine.Mutant;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;

public class MutantExportInterceptor implements MutationInterceptor {

  private final String     outDir;
  private final FileSystem fileSystem;
  private final ClassByteArraySource source;

  private Path             mutantsDir;
  private ClassName        currentClass;

  public MutantExportInterceptor(FileSystem fileSystem,
      ClassByteArraySource source, String outDir) {
    this.fileSystem = fileSystem;
    this.outDir = outDir;
    this.source = source;
  }

  @Override
  public InterceptorType type() {
    return InterceptorType.REPORT;
  }

  @Override
  public void begin(ClassTree clazz) {
    this.currentClass = clazz.name();
    final String[] classLocation = ("export." + clazz.name().asJavaName())
        .split("\\.");
    final Path classDir = this.fileSystem.getPath(this.outDir, classLocation);
    this.mutantsDir = classDir.resolve("mutants");
    try {
      Files.createDirectories(this.mutantsDir);
      writeBytecodeToDisk(this.source.getBytes(clazz.name().asJavaName()).get(), classDir);
    } catch (final IOException e) {
      throw new RuntimeException("Couldn't create direectory for " + clazz, e);
    }
  }

  @Override
  public Collection<MutationDetails> intercept(
      Collection<MutationDetails> mutations, Mutater m) {

    final List<MutationDetails> indexable = new ArrayList<>(mutations);

    try {
      for (int i = 0; i != indexable.size(); i++) {
        exportMutantDetails(m, indexable, i);
      }
    } catch (final IOException ex) {
      throw new RuntimeException("Error exporting mutants for report", ex);
    }

    return mutations;
  }

  private void exportMutantDetails(Mutater m, List<MutationDetails> indexable,
      int i) throws IOException {
    final MutationDetails md = indexable.get(i);
    final Path mutantFolder = this.mutantsDir.resolve("" + i);
    Files.createDirectories(mutantFolder);

    final Mutant mutant = m.getMutation(md.getId());

    writeMutantToDisk(mutant, mutantFolder);
    writeBytecodeToDisk(mutant.getBytes(), mutantFolder);
    writeDetailsToDisk(md, mutantFolder);
  }

  private void writeMutantToDisk(Mutant mutant, Path mutantFolder) throws IOException {
    final Path outFile = mutantFolder.resolve(this.currentClass.asJavaName() + ".class");
    Files.write(outFile, mutant.getBytes(), StandardOpenOption.CREATE);
  }


  private void writeBytecodeToDisk(final byte[] clazz, Path folder) throws IOException {
      final ClassReader reader = new ClassReader(clazz);
      final CharArrayWriter buffer = new CharArrayWriter();
      reader.accept(new TraceClassVisitor(null, new Textifier(), new PrintWriter(
          buffer)), ClassReader.EXPAND_FRAMES);
      final Path outFile = folder.resolve(this.currentClass.asJavaName() + ".txt");
      Files.write(outFile, Collections.singleton(buffer.toString()), StandardCharsets.UTF_8, StandardOpenOption.CREATE);
  }

  private void writeDetailsToDisk(MutationDetails md,
      Path mutantFolder) throws IOException  {
    final Path outFile = mutantFolder.resolve("details.txt");
    Files.write(outFile, Collections.singleton(md.toString()), StandardCharsets.UTF_8, StandardOpenOption.CREATE);
  }

  @Override
  public void end() {

  }

}
