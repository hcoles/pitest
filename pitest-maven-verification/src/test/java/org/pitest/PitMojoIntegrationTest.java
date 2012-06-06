/*
 * Copyright 2011 Henry Coles and Stefan Penndorf
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, 
 * software distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package org.pitest;

import java.io.File;

import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.FileUtils;
import org.apache.maven.it.util.ResourceExtractor;
import org.junit.Before;
import org.junit.Test;


/**
 *
 *
 * @author Stefan Penndorf <stefan.penndorf@gmail.com>
 */
public class PitMojoIntegrationTest {

  private Verifier verifier;
  
  @Before
  public void setup() throws Exception {
    String tempDirPath = System.getProperty( "maven.test.tmpdir", System.getProperty( "java.io.tmpdir" ) );
    File tempDir = new File( tempDirPath, getClass().getSimpleName() );
    File testDir = new File( tempDir, "/pit-33-setUserDir" );
    FileUtils.deleteDirectory( testDir );
    String path = ResourceExtractor.extractResourcePath( getClass(), "/pit-33-setUserDir", tempDir, true ).getAbsolutePath();
    this.verifier = new Verifier(path);
    this.verifier.setAutoclean(false);

  }
  
  @Test
  public void shouldSetUserDirToArtefactWorkingDirectory() throws Exception {
    this.verifier.setDebug(true);
    this.verifier.executeGoal("test");
    this.verifier.executeGoal("org.pitest:pitest-maven:mutationCoverage");  
  }
  
  
}
