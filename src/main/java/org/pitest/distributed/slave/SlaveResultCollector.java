// Copyright 2010 Henry Coles
// Licensed under the Apache License, Version 2.0 (the "License"); 
// you may not use this file except in compliance with the License. 
// You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 
// Unless required by applicable law or agreed to in writing, 
// software distributed under the License is distributed on an "AS IS" BASIS, 
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
// See the License for the specific language governing permissions and limitations under the License. 

package org.pitest.distributed.slave;

import java.util.logging.Logger;

import org.pitest.Description;
import org.pitest.TestResult;
import org.pitest.distributed.ResultMessage;
import org.pitest.distributed.message.RunDetails;
import org.pitest.extension.ResultCollector;
import org.pitest.testunit.TestUnitState;

import com.hazelcast.core.ITopic;

public class SlaveResultCollector implements ResultCollector {

  private final static Logger         logger = Logger
                                                 .getLogger(SlaveResultCollector.class
                                                     .getName());

  private final RunDetails            run;
  private final ITopic<ResultMessage> resultsTopic;

  // private final MasterService master;

  public SlaveResultCollector(final RunDetails run,
      final ITopic<ResultMessage> resultsTopic) {
    this.run = run;
    this.resultsTopic = resultsTopic;
  }

  private void publish(final TestResult testResult) {
    this.resultsTopic.publish(new ResultMessage(this.run, testResult));

  }

  public void notifySkipped(final Description tu) {

    this.publish(new TestResult(tu, null, TestUnitState.NOT_RUN));
  }

  public void notifyStart(final Description tu) {
    this.publish(new TestResult(tu, null, TestUnitState.STARTED));
  }

  public void notifyEnd(final Description description, final Throwable t) {
    notifyEnd(new TestResult(description, t));
  }

  public void notifyEnd(final Description tu) {
    notifyEnd(new TestResult(tu, null));
  }

  public void notifyEnd(final TestResult testResult) {
    logger.info("Test complete " + testResult);
    this.publish(testResult);
  }

  public boolean shouldExit() {
    return false;
  }

};
