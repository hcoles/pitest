package com.example;

import com.example.controller.ExampleController;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import com.example.service.ExampleService;

import jakarta.inject.Inject;
import io.quarkus.test.junit.mockito.InjectMock;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;

@QuarkusTest
public class ExampleControllerTest {
    @Inject
    ExampleController controller;

    @InjectMock
    ExampleService service;

    @Test
    void doTrue() {
        Mockito.when(service.doStuff(anyString())).thenReturn(true);
        assertTrue(controller.doStuff("s"));
    }

    @Test
    void doFalse() {
        Mockito.when(service.doStuff(anyString())).thenReturn(false);
        assertFalse(controller.doStuff("s"));
    }


}