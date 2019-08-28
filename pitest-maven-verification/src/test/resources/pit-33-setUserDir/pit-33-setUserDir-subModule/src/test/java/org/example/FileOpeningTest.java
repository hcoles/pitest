package org.example;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertNotNull;

import java.io.File;
import org.junit.Test;

public class FileOpeningTest {
  
  @Test
  public void testOpenFileRelativeToWorkingDirectory() {
    SystemUnderTest sut = new SystemUnderTest();
    
    File file = new File("src/test/resources/fixture.file");
    assertTrue(file.exists());
    assertNotNull(sut.toString());    
  }
  
  
}