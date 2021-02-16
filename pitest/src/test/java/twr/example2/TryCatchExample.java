package twr.example2;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author Artem Khvastunov
 *
 * Tests use stored compiled binaries of this class. Source here for reference only.
 */
public class TryCatchExample {

    public static void main(String[] args) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            baos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
