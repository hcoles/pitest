package com.example;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class LoadsResourcesFromClassPathTest {

  @Test
  public void testReadsResource() {
    assertTrue(LoadsResourcesFromClassPath.loadResource());
  }

}
