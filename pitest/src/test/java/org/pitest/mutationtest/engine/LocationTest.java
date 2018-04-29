package org.pitest.mutationtest.engine;

import static org.junit.Assert.assertEquals;
import static org.pitest.mutationtest.engine.Location.location;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.pitest.classinfo.ClassName;

import nl.jqno.equalsverifier.EqualsVerifier;

public class LocationTest {

  @Test
  public void shouldSortInConsistantOrder() {
    final Location a = location(ClassName.fromString("A"),
        MethodName.fromString("A"), "A");
    final Location b = location(ClassName.fromString("AA"),
        MethodName.fromString("A"), "A");
    final Location c = location(ClassName.fromString("A"),
        MethodName.fromString("AA"), "A");
    final Location d = location(ClassName.fromString("A"),
        MethodName.fromString("AA"), "AA");
    final List<Location> ls = Arrays.asList(a, b, c, d);
    Collections.sort(ls);
    assertEquals(Arrays.asList(a, c, d, b), ls);
  }

  @Test
  public void shouldObeyHashcodeEqualsContract() {
    EqualsVerifier.forClass(Location.class).verify();
  }

}
