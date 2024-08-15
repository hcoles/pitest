package org.pitest.mutationtest.execute;

import org.pitest.mutationtest.DetectionStatus;
import org.pitest.mutationtest.MutationStatusTestPair;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.util.Id;
import org.pitest.util.Log;
import org.pitest.util.ReceiveStrategy;
import org.pitest.util.SafeDataInputStream;

import java.util.Map;
import java.util.logging.Logger;

class Receive implements ReceiveStrategy {
    private static final Logger LOG = Log.getLogger();

    private final Map<MutationIdentifier, MutationStatusTestPair> idMap;

    Receive(final Map<MutationIdentifier, MutationStatusTestPair> idMap) {
        this.idMap = idMap;
    }

    @Override
    public void apply(final byte control, final SafeDataInputStream is) {
        switch (control) {
            case Id.DESCRIBE:
                handleDescribe(is);
                break;
            case Id.REPORT:
                handleReport(is);
                break;
            default:
                LOG.severe("Unknown control byte " + control);
        }
    }

    private void handleReport(final SafeDataInputStream is) {
        final MutationIdentifier mutation = is.read(MutationIdentifier.class);
        final MutationStatusTestPair value = is
                .read(MutationStatusTestPair.class);
        this.idMap.put(mutation, value);
        LOG.fine(mutation + " " + value);
    }

    private void handleDescribe(final SafeDataInputStream is) {
        final MutationIdentifier mutation = is.read(MutationIdentifier.class);
        this.idMap.put(mutation, MutationStatusTestPair.notAnalysed(1,
                DetectionStatus.STARTED,null));
    }

}