package twr.example1;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author Artem Khvastunov
 *
 * Tests use stored compiled binaries of this class. Source here for reference only.
 */
public class TryExample {

    public static void main(String[] args) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            baos.flush();
        }
    }
}
