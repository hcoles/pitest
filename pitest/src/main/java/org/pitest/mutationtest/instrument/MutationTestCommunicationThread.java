/*
 * Copyright 2011 Henry Coles
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
package org.pitest.mutationtest.instrument;

import java.util.Map;

import org.pitest.functional.SideEffect1;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.execute.SlaveArguments;
import org.pitest.mutationtest.instrument.ResultsReader.DetectionStatus;
import org.pitest.mutationtest.instrument.protocol.Id;
import org.pitest.util.CommunicationThread;
import org.pitest.util.ReceiveStrategy;
import org.pitest.util.SafeDataInputStream;
import org.pitest.util.SafeDataOutputStream;

public class MutationTestCommunicationThread extends CommunicationThread {

  static class SendData implements SideEffect1<SafeDataOutputStream> {
    private final SlaveArguments arguments;

    SendData(final SlaveArguments arguments) {
      this.arguments = arguments;
    }

    public void apply(final SafeDataOutputStream dos) {
      dos.write(this.arguments);
      dos.flush();
    }
  }

  static class Receive implements ReceiveStrategy {

    private final Map<MutationIdentifier, DetectionStatus> idMap;

    Receive(final Map<MutationIdentifier, DetectionStatus> idMap) {
      this.idMap = idMap;
    }

    public void apply(final byte control, final SafeDataInputStream is) {
      switch (control) {
      case Id.DESCRIBE:
        handleDescribe(is);
        break;
      case Id.REPORT:
        handleReport(is);
        break;
      }
    }

    private void handleReport(final SafeDataInputStream is) {
      final MutationIdentifier mutation = is.read(MutationIdentifier.class);
      final DetectionStatus value = is.read(DetectionStatus.class);
      this.idMap.put(mutation, value);
    }

    private void handleDescribe(final SafeDataInputStream is) {
      final MutationIdentifier mutation = is.read(MutationIdentifier.class);
      this.idMap.put(mutation, DetectionStatus.STARTED);
    }

  }

  private final Map<MutationIdentifier, DetectionStatus> idMap;

  public MutationTestCommunicationThread(final int port,
      final SlaveArguments arguments,
      final Map<MutationIdentifier, DetectionStatus> idMap) {
    super(port, new SendData(arguments), new Receive(idMap));
    this.idMap = idMap;
  }

  @Override
  public DetectionStatus getStatus(final MutationIdentifier id) {
    return this.idMap.get(id);
  }

}
