package com.example.trywithresources;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class SimpleCloseCall {
    public static void main(String[] args) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try  {
            os.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                os.close();
            } catch(IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
