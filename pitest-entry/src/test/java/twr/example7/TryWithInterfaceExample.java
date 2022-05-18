package twr.example7;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;

/**
 * @author Artem Khvastunov
 * Tests use stored compiled binaries of this class. Source here for reference only.
 */
public class TryWithInterfaceExample {

    public static void main(String[] args) throws IOException {
        try (Closeable os = new ByteArrayOutputStream()) {
            ((Flushable) os).flush();
        }
    }
}
