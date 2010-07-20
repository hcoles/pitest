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

package org.pitest.distributed.master;

import java.util.concurrent.BlockingQueue;

import org.pitest.TestResult;
import org.pitest.distributed.ResultMessage;
import org.pitest.distributed.SharedNames;
import org.pitest.distributed.message.RunDetails;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.MessageListener;

public class MasterResultQueueListener implements
    MessageListener<ResultMessage> {

  private final BlockingQueue<TestResult> feedbackQueue;
  private final ITopic<ResultMessage>     resultsTopic;
  private final RunDetails                run;

  public MasterResultQueueListener(final RunDetails run,
      final HazelcastInstance hazelcast,
      final BlockingQueue<TestResult> feedbackQueue) {
    this.run = run;
    this.resultsTopic = hazelcast.getTopic(SharedNames.TEST_RESULTS);
    this.feedbackQueue = feedbackQueue;
  }

  public void start() {
    this.resultsTopic.addMessageListener(this);

  }

  public void stop() {
    this.resultsTopic.removeMessageListener(this);
  }

  public void onMessage(final ResultMessage message) {
    if (message.getRun().equals(this.run)) {
      this.feedbackQueue.add(message.getResult());
    }
  }

}
