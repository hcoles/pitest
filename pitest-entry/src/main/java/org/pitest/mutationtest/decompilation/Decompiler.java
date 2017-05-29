package org.pitest.mutationtest.decompilation;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.classinfo.ClassName;
import org.pitest.functional.Option;
import org.pitest.util.Unchecked;

import com.strobel.assembler.metadata.Buffer;
import com.strobel.assembler.metadata.ITypeLoader;
import com.strobel.assembler.metadata.MetadataSystem;
import com.strobel.assembler.metadata.TypeReference;
import com.strobel.decompiler.DecompilationOptions;
import com.strobel.decompiler.ITextOutput;
import com.strobel.decompiler.PlainTextOutput;
import com.strobel.decompiler.languages.Languages;

public class Decompiler {

  private final ClassByteArraySource source;
  
  public Decompiler(ClassByteArraySource source) {
   this.source = source; 
  }
  
  public List<String> decompile(ClassName className, byte[] bytes) {
    final MetadataSystem metadataSystem = new MetadataSystem(
        new MutantTypeLoader(source, className, bytes));

    metadataSystem.setEagerMethodLoadingEnabled(false);

    return decompile(metadataSystem, className);
  }
  
  public List<String> decompile(ClassName className) {
    final MetadataSystem metadataSystem = new MetadataSystem(
        new ClassByteArraySourceLoader(source));
    return decompile(metadataSystem, className);
  }
  
  
  private List<String> decompile(final MetadataSystem metadataSystem,
      ClassName clazz) {

    TypeReference type = metadataSystem.lookupType(clazz.asInternalName());
    if (type != null) {
      return decompileResolvedType(type);
    }
    
    return null;
  }

  private List<String> decompileResolvedType(TypeReference type) {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    final Writer writer = new OutputStreamWriter(bos, Charset.forName("UTF-8"));
    ITextOutput output = new PlainTextOutput(writer);
    DecompilationOptions options = new DecompilationOptions();
    Languages.java().decompileType(type.resolve(), output, options);

    try {
      writer.flush();

      String src = new String(bos.toByteArray(), Charset.forName("UTF-8"));

      return Arrays.asList(src.split("\n"));

    } catch (IOException e) {
      throw Unchecked.translateCheckedException(e);
    }
  }

}

 
class MutantTypeLoader implements ITypeLoader {

  private final ClassByteArraySource source;
  private final ClassName               target;
  private final byte[] targetBytes;

  MutantTypeLoader(ClassByteArraySource source, ClassName target, byte[] bytes) {
    this.source = source;
    this.target = target;
    this.targetBytes = bytes;
  }

  @Override
  public boolean tryLoadType(String internalName, Buffer buffer) {

    Option<byte[]> bytes = resolve(internalName);
    if (bytes.hasSome()) {
      byte[] bs = bytes.value();
      buffer.reset(bs.length);
      buffer.putByteArray(bs, 0, bs.length);
      buffer.position(0);
      return true;
    }

    return false;

  }

  private Option<byte[]> resolve(String name) {
    if (target.equals(ClassName.fromString(name))) {
      return Option.some(targetBytes);
    }

    return source.getBytes(name);
  }
  
}

class ClassByteArraySourceLoader implements ITypeLoader {

  private final ClassByteArraySource source;

  ClassByteArraySourceLoader(ClassByteArraySource source) {
    this.source = source;
  }

  @Override
  public boolean tryLoadType(String internalName, Buffer buffer) {
    Option<byte[]> bytes = source.getBytes(internalName);
    if (bytes.hasSome()) {
      byte[] bs = bytes.value();
      buffer.reset(bs.length);
      buffer.putByteArray(bs, 0, bs.length);
      buffer.position(0);
      return true;
    }

    return false;
  }

}
