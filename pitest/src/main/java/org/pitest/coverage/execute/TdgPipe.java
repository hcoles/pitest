package org.pitest.coverage.execute;
import org.pitest.util.SafeDataOutputStream;
import java.util.Collection;
import org.pitest.util.Id;

public class TdgPipe {
    private final  SafeDataOutputStream dos;

    public TdgPipe(SafeDataOutputStream dos) {
        this.dos = dos;
    }

    public void recordTestUnitsName(TdgTestMethodResult testsResult) {
        this.dos.writeByte(Id.OUTCOME);
        this.dos.write(testsResult);
        this.dos.flush();
    }
}
