package org.pitest.mutationtest.tdg.execute;


import org.pitest.util.CommunicationThread;
import java.util.function.Consumer;
import org.pitest.util.SafeDataInputStream;
import org.pitest.util.SafeDataOutputStream;
import org.pitest.util.Log;
import java.net.ServerSocket;
import java.util.Collection;
import java.util.Set;
import java.util.HashSet;
import org.pitest.util.ReceiveStrategy;
import org.pitest.util.Id;
import java.util.logging.Logger;
public class TdgCommunicationThread extends CommunicationThread{
    private static final Logger    LOG = Log.getLogger();
    

    private static class SendData implements Consumer<SafeDataOutputStream> {
        private  Collection<String> clazzes;

        SendData( Collection<String> clazzes) {
          this.clazzes = clazzes;
        }
    
        @Override
        public void accept(final SafeDataOutputStream dos) {
            LOG.info("Sending " + this.clazzes.size() + " test classes to minion");
            dos.writeInt(this.clazzes.size());
            for (final String tc : this.clazzes) {
            dos.writeString(tc);
            }
            dos.flush();
            LOG.info("Sent tests to minion");
        }
    }

    private static class Receive implements ReceiveStrategy {
        private  final Consumer<TdgResult> handler;
        
    
        Receive(Consumer<TdgResult> handler) {
          this.handler = handler;
        }
    
        @Override
        public void apply(final byte control, final SafeDataInputStream is) {
          switch (control) {
            case Id.OUTCOME:
                handleOutcome(is);
                break;
            case Id.DONE:
          }
          


        }

        private void handleOutcome(final SafeDataInputStream is) {
            final String className = is.readString();
            final int counter = is.readInt();
            Set<String> methodNames = new HashSet<String>();
            for (int i = 0; i < counter; i++) {
                methodNames.add(is.readString());
            }
            // System.out.println("receive ::::: " + className + methodNames);

            handler.accept(new TdgResult(className, methodNames));
        }
    }
    public TdgCommunicationThread(final ServerSocket socket,final Collection<String> clazzes, final Consumer<TdgResult> handler) {
        super(socket, new SendData(clazzes), new Receive(handler));
    }
}
