package org.pitest.mutationtest.decompilation;

import org.junit.Test;
import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.classinfo.ClassName;
import org.pitest.classpath.ClassloaderByteArraySource;
import org.pitest.functional.Option;
import org.pitest.util.ClassRenamer;

import static org.assertj.core.api.Assertions.assertThat;

public class SourceDiffTest {
  
  private Decompiler decomp = new Decompiler(new ClassloaderByteArraySource(SourceDiffTest.class.getClassLoader()));
  private Decompiler modDcomp = new Decompiler(new SubstitingClassByteArraySource());

  @Test
  public void shouldProduceNiceDescriptionWhenMethodCallRemoved() {
    String actual = createDiffForSample(DifferByMethodCall.class);
    
    assertThat(actual).containsOnlyOnce("Deleted");
    assertThat(actual).containsOnlyOnce("System.out.println(\"Method call\");");
  }
  
  @Test
  public void shouldProduceNiceDescriptionWhenMethodCallAdded() {
    String actual = createDiffForSample(DifferByAddingMethodCall.class);
    
    assertThat(actual).containsOnlyOnce("Inserted");
    assertThat(actual).containsOnlyOnce("System.out.println(\"Method call\");");
  }
  
  @Test
  public void shouldProduceNiceDescriptionWhenChangedBoundary() {
    String actual = createDiffForSample(DifferByBoundary.class);
    
    assertThat(actual).containsOnlyOnce("Changed");
    assertThat(actual).containsOnlyOnce("return i <= 10; to return i < 10;");
  }  

  private String createDiffForSample(Class<?> sample) {
    SourceDiff testee = new SourceDiff();
    ClassName input = ClassName.fromClass(sample);

    String actual = testee.describe(decomp.decompile(input), modDcomp.decompile(input));
    return actual;
  }

}

class DifferByMethodCall {
  public void foo() {
    System.out.println("Method call");
  }
}

class DifferByMethodCall_Mod {
  public void foo() {
 
  }
}


class DifferByAddingMethodCall {
  public void foo() {

  }
}

class DifferByAddingMethodCall_Mod {
  public void foo() {
    System.out.println("Method call");
  }
}


class DifferByBoundary {
  public boolean foo(int i) {
    return i <= 10;
  }
}

class DifferByBoundary_Mod {
  public boolean foo(int i) {
    return i < 10;
  }
}


class SubstitingClassByteArraySource implements ClassByteArraySource {
  ClassloaderByteArraySource child = new ClassloaderByteArraySource(SourceDiffTest.class.getClassLoader()); 

  @Override
  public Option<byte[]> getBytes(String clazz) {
    if (clazz.contains("DifferBy")) {
      String modifiedClass = clazz + "_Mod";
      return Option.some(ClassRenamer.rename(child.getBytes(modifiedClass).value(), modifiedClass, clazz));     
    } else {
      return child.getBytes(clazz);
    }
  }
}



