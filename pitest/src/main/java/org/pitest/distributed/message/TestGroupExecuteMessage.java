/*
 * Copyright 2010 Henry Coles
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
 * See the License for the specific language governing permissions and limitations under the License. 
 */

package org.pitest.distributed.message;

import java.io.Serializable;

public class TestGroupExecuteMessage implements Serializable {

  private static final long serialVersionUID = 1L;

  private final RunDetails  run;
  private final long        id;
  private final String      xml;

  public TestGroupExecuteMessage(final RunDetails run, final long id,
      final String xml) {
    this.xml = xml;
    this.id = id;
    this.run = run;
  }

  public String getXML() {
    return this.xml;
  }

  public RunDetails getRun() {
    return this.run;
  }

  public long getId() {
    return this.id;
  }

}
