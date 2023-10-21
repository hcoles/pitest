package com.example;

import io.quarkus.test.junit.QuarkusTest;
import com.example.service.ExampleService;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
public class ExampleServiceTest {
    @Inject
    ExampleService service;


    @Test
    void doTrue() {
        assertTrue(service.doStuff("foo"));
    }

    @Test
    void doFalse() {
        assertFalse(service.doStuff("notfoo"));
    }
}
