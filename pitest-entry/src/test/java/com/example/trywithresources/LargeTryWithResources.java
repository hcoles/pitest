package com.example.trywithresources;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class LargeTryWithResources {
    public static void main(String[] args) {
        System.out.print("before");
        try (ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
             ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
             ByteArrayOutputStream baos3 = new ByteArrayOutputStream();
             ByteArrayOutputStream baos4 = new ByteArrayOutputStream()
             ) {
            baos1.flush();
            baos2.flush();
            baos3.flush();
            baos4.flush();
            System.out.println("bo!");
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.print("after");

    }
}
