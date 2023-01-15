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
import org.pitest.mutationtest.execute.TdgMinionArgments;
import org.pitest.coverage.execute.TdgTestMethodResult;
public class TdgCommunicationThread extends CommunicationThread{
    private static final Logger    LOG = Log.getLogger();
    

    private static class SendData implements Consumer<SafeDataOutputStream> {
        private  TdgMinionArgments tdgMinionArgments;

        SendData( TdgMinionArgments tdgMinionArgments) {
          this.tdgMinionArgments = tdgMinionArgments;
        }
    
        @Override
        public void accept(final SafeDataOutputStream dos) {
            // LOG.info("Sending " + this.clazzes.size() + " test classes to minion");
            // dos.writeInt(this.clazzes.size());
            // for (final String tc : this.clazzes) {
            // dos.writeString(tc);
            // }
            dos.write(this.tdgMinionArgments);
            dos.flush();
            // LOG.info("Sent TdgMinionArgments to minion");
        }
    }

    private static class Receive implements ReceiveStrategy {
        private  final Consumer<TdgTestMethodResult> handler;
        
    
        Receive(Consumer<TdgTestMethodResult> handler) {
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
            // final String className = is.readString();
            TdgTestMethodResult testsResult = is.read(TdgTestMethodResult.class);
            // System.out.println("receive ::::: " + testsResult.res);

            handler.accept(testsResult);
        }
    }
    public TdgCommunicationThread(final ServerSocket socket,final TdgMinionArgments tdgMinionArgments, final Consumer<TdgTestMethodResult> handler) {
        super(socket, new SendData(tdgMinionArgments), new Receive(handler));
    }
}
