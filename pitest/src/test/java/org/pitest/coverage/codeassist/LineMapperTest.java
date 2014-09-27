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

@RunWith(MockitoJUnitRunner.class)
public class LineMapperTest {
  
  @Mock
  CodeSource source;
  
  static class OneBlock {
    int foo() {
      return 1;
    }
  }

	@Test
	public void shouldMapAllLinesWhenMethodContainsSingleBlock() throws Exception {
	  Map<BlockLocation, Set<Integer>> actual = analyse(OneBlock.class);

	  Location l = Location.location(ClassName.fromClass(OneBlock.class), MethodName.fromString("foo"), "()I");
	  BlockLocation bl = new BlockLocation(l,0);
	  
	  assertThat(actual.get(bl)).containsOnly(31);
	  
	}
	
  static class ThreeBlocks {
    int foo(int i) {
      if (i > 30 ) {
        return 1;
      }
      return 2;
    }
  }
	
	 @Test
	  public void shouldMapAllLinesWhenMethodContainsThreeBlocks() throws Exception {
	    Map<BlockLocation, Set<Integer>> actual = analyse(ThreeBlocks.class);

	    Location l = Location.location(ClassName.fromClass(ThreeBlocks.class), MethodName.fromString("foo"), "(I)I");
	    
	    assertThat(actual.get( BlockLocation.blockLocation(l,0))).containsOnly(48);
      assertThat(actual.get( BlockLocation.blockLocation(l,1))).containsOnly(49);
      assertThat(actual.get( BlockLocation.blockLocation(l,2))).containsOnly(51);
	  }
	 
	 @Test
	 public void shouldMapLinesWhenLinesSpanBlocks() throws Exception {
	   
     Map<BlockLocation, Set<Integer>> actual = analyse(com.example.LineNumbersSpanBlocks.class);
     Location l = Location.location(ClassName.fromClass(com.example.LineNumbersSpanBlocks.class), MethodName.fromString("foo"), "(I)I");

     assertThat(actual.get(BlockLocation.blockLocation(l,2))).containsOnly(11);
	 }

  private Map<BlockLocation, Set<Integer>> analyse(Class<?> clazz)
      throws ClassNotFoundException {
    when(source.fetchClassBytes(any(ClassName.class))).thenReturn(Option.some(ClassUtils.classAsBytes(clazz)));
	  LineMap testee = new LineMapper(source);
	  return testee.mapLines(ClassName.fromClass(clazz));
  }

}
