package org.pitest.mutationtest.execute;

import org.pitest.util.SafeDataOutputStream;

import java.util.function.Consumer;

class SendData implements Consumer<SafeDataOutputStream> {
    private final MinionArguments arguments;

    SendData(final MinionArguments arguments) {
        this.arguments = arguments;
    }

    @Override
    public void accept(final SafeDataOutputStream dos) {
        dos.write(this.arguments);
        dos.flush();
    }
}
