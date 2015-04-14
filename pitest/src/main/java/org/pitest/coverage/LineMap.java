package org.pitest.coverage;

import java.util.Map;
import java.util.Set;

import org.pitest.classinfo.ClassName;

public interface LineMap {

  Map<BlockLocation, Set<Integer>> mapLines(ClassName clazz);

}