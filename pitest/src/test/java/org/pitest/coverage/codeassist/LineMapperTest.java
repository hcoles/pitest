package org.pitest.coverage.codeassist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pitest.classinfo.ClassName;
import org.pitest.classpath.CodeSource;
import org.pitest.coverage.BlockLocation;
import org.pitest.coverage.LineMap;
import org.pitest.coverage.analysis.LineMapper;
import org.pitest.functional.Option;
import org.pitest.mutationtest.engine.Location;
import org.pitest.mutationtest.engine.MethodName;

import com.example.coverage.execute.samples.simple.LastLineOfContructorCheck;
import com.example.coverage.execute.samples.simple.OneBlock;
import com.example.coverage.execute.samples.simple.ThreeBlocks;
import com.example.coverage.execute.samples.simple.ThreeMultiLineBlocks;

@RunWith(MockitoJUnitRunner.class)
public class LineMapperTest {

  @Mock
  CodeSource source;

  @Test
  public void shouldMapAllLinesWhenMethodContainsSingleBlock() throws Exception {
    Map<BlockLocation, Set<Integer>> actual = analyse(OneBlock.class);

    Location l = Location.location(ClassName.fromClass(OneBlock.class),
        MethodName.fromString("foo"), "()I");
    BlockLocation bl = new BlockLocation(l, 0);

    assertThat(actual.get(bl)).containsOnly(5);

  }

  @Test
  public void shouldMapAllLinesWhenMethodContainsThreeBlocks() throws Exception {
    Map<BlockLocation, Set<Integer>> actual = analyse(ThreeBlocks.class);

    Location l = Location.location(ClassName.fromClass(ThreeBlocks.class),
        MethodName.fromString("foo"), "(I)I");

    assertThat(actual.get(BlockLocation.blockLocation(l, 0))).containsOnly(5);
    assertThat(actual.get(BlockLocation.blockLocation(l, 1))).containsOnly(6);
    assertThat(actual.get(BlockLocation.blockLocation(l, 2))).containsOnly(8);
  }

  @Test
  public void shouldMapAllLinesWhenMethodContainsThreeMultiLineBlocks()
      throws Exception {
    Map<BlockLocation, Set<Integer>> actual = analyse(ThreeMultiLineBlocks.class);

    Location l = Location.location(
        ClassName.fromClass(ThreeMultiLineBlocks.class),
        MethodName.fromString("foo"), "(I)I");

    assertThat(actual.get(BlockLocation.blockLocation(l, 0))).contains(5, 6);
    assertThat(actual.get(BlockLocation.blockLocation(l, 1))).contains(7, 8);
    assertThat(actual.get(BlockLocation.blockLocation(l, 2))).contains(10, 11);
  }

  @Test
  public void shouldMapLinesWhenLinesSpanBlocks() throws Exception {

    Map<BlockLocation, Set<Integer>> actual = analyse(com.example.LineNumbersSpanBlocks.class);
    Location l = Location.location(
        ClassName.fromClass(com.example.LineNumbersSpanBlocks.class),
        MethodName.fromString("foo"), "(I)I");

    assertThat(actual.get(BlockLocation.blockLocation(l, 2))).containsOnly(12);
  }

  @Test
  public void shouldIncludeLastLinesConstructorsInBlock() throws Exception {
    Map<BlockLocation, Set<Integer>> actual = analyse(LastLineOfContructorCheck.class);
    Location l = Location.location(
        ClassName.fromClass(LastLineOfContructorCheck.class),
        MethodName.fromString("<init>"), "()V");

    assertThat(actual.get(BlockLocation.blockLocation(l, 0))).contains(6);
  }

  @Test
  public void shouldI() throws Exception {
    Map<BlockLocation, Set<Integer>> actual = analyse(ThreeBlocks2.class);
    Location l = Location.location(ClassName.fromClass(ThreeBlocks2.class),
        MethodName.fromString("foo"), "(I)I");
    assertThat(actual.get(BlockLocation.blockLocation(l, 0))).containsOnly(105);
    assertThat(actual.get(BlockLocation.blockLocation(l, 1))).containsOnly(106);
    assertThat(actual.get(BlockLocation.blockLocation(l, 2))).containsOnly(108);
  }

  static class ThreeBlocks2 {
    int foo(int i) {
      if (i > 30) {
        return 1;
      }
      return 2;
    }
  }

  private Map<BlockLocation, Set<Integer>> analyse(Class<?> clazz)
      throws ClassNotFoundException {
    when(this.source.fetchClassBytes(any(ClassName.class))).thenReturn(
        Option.some(ClassUtils.classAsBytes(clazz)));
    LineMap testee = new LineMapper(this.source);
    return testee.mapLines(ClassName.fromClass(clazz));
  }

}
