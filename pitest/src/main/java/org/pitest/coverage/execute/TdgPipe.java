package org.pitest.coverage.execute;
import org.pitest.util.SafeDataOutputStream;
import java.util.Collection;
import org.pitest.util.Id;
public class TdgPipe implements TdgProto {
    private final  SafeDataOutputStream dos;

    public TdgPipe(SafeDataOutputStream dos) {
        this.dos = dos;
    }

    public void recordTestUnitsName(String className, Collection<String> methodNames) {
        this.dos.writeByte(Id.OUTCOME);
        this.dos.writeString(className);
        this.dos.writeInt(methodNames.size());
        for (String methodName : methodNames) {
            this.dos.writeString(methodName);
        }
        this.dos.flush();
    }
}
