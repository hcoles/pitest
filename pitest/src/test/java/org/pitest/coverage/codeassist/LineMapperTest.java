package org.pitest.coverage.codeassist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.classinfo.ClassName;
import org.pitest.coverage.BlockLocation;
import org.pitest.coverage.LineMap;
import org.pitest.coverage.analysis.LineMapper;
import java.util.Optional;
import org.pitest.mutationtest.engine.Location;

import com.example.coverage.execute.samples.simple.LastLineOfContructorCheck;
import com.example.coverage.execute.samples.simple.OneBlock;
import com.example.coverage.execute.samples.simple.ThreeBlocks;
import com.example.coverage.execute.samples.simple.ThreeMultiLineBlocks;

@RunWith(MockitoJUnitRunner.class)
public class LineMapperTest {

  @Mock
  ClassByteArraySource source;

  @Test
  public void shouldMapAllLinesWhenMethodContainsSingleBlock() throws Exception {
    final Map<BlockLocation, Set<Integer>> actual = analyse(OneBlock.class);

    final Location l = Location.location(ClassName.fromClass(OneBlock.class),
        "foo", "()I");
    final BlockLocation bl = new BlockLocation(l, 0);

    assertThat(actual.get(bl)).containsOnly(5);

  }

  @Test
  public void shouldMapAllLinesWhenMethodContainsThreeBlocks() throws Exception {
    final Map<BlockLocation, Set<Integer>> actual = analyse(ThreeBlocks.class);

    final Location l = Location.location(ClassName.fromClass(ThreeBlocks.class),
        "foo", "(I)I");

    assertThat(actual.get(BlockLocation.blockLocation(l, 0))).containsOnly(5);
    assertThat(actual.get(BlockLocation.blockLocation(l, 1))).containsOnly(6);
    assertThat(actual.get(BlockLocation.blockLocation(l, 2))).containsOnly(8);
  }

  @Test
  public void shouldMapAllLinesWhenMethodContainsThreeMultiLineBlocks()
      throws Exception {
    final Map<BlockLocation, Set<Integer>> actual = analyse(ThreeMultiLineBlocks.class);

    final Location l = Location.location(
        ClassName.fromClass(ThreeMultiLineBlocks.class),
        "foo", "(I)I");

    assertThat(actual.get(BlockLocation.blockLocation(l, 0))).contains(5, 6);
    assertThat(actual.get(BlockLocation.blockLocation(l, 1))).contains(7);
    assertThat(actual.get(BlockLocation.blockLocation(l, 2))).contains(8,9);
    assertThat(actual.get(BlockLocation.blockLocation(l, 3))).contains(10);
    assertThat(actual.get(BlockLocation.blockLocation(l, 4))).contains(12,13);
    assertThat(actual.get(BlockLocation.blockLocation(l, 5))).contains(14);
  }

  @Test
  public void shouldMapLinesWhenLinesSpanBlocks() throws Exception {

    final Map<BlockLocation, Set<Integer>> actual = analyse(com.example.LineNumbersSpanBlocks.class);
    final Location l = Location.location(
        ClassName.fromClass(com.example.LineNumbersSpanBlocks.class),
        "foo", "(I)I");

    assertThat(actual.get(BlockLocation.blockLocation(l, 2))).containsOnly(12);
  }

  @Test
  public void shouldIncludeLastLinesConstructorsInBlock() throws Exception {
    final Map<BlockLocation, Set<Integer>> actual = analyse(LastLineOfContructorCheck.class);
    final Location l = Location.location(
        ClassName.fromClass(LastLineOfContructorCheck.class),
        "<init>", "()V");

    assertThat(actual.get(BlockLocation.blockLocation(l, 1))).contains(6);
  }

  @Test
  public void shouldI() throws Exception {
    final Map<BlockLocation, Set<Integer>> actual = analyse(ThreeBlocks2.class);
    final Location l = Location.location(ClassName.fromClass(ThreeBlocks2.class),
        "foo", "(I)I");
    assertThat(actual.get(BlockLocation.blockLocation(l, 0))).containsOnly(107);
    assertThat(actual.get(BlockLocation.blockLocation(l, 1))).containsOnly(108);
    assertThat(actual.get(BlockLocation.blockLocation(l, 2))).containsOnly(110);
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
    when(this.source.getBytes(anyString())).thenReturn(
        Optional.ofNullable(ClassUtils.classAsBytes(clazz)));
    final LineMap testee = new LineMapper(this.source);
    return testee.mapLines(ClassName.fromClass(clazz));
  }

}
