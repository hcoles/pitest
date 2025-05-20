package com.example.service;


import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ExampleService {

    public boolean doStuff(String s) {
        System.out.println("this survives");
        return s.equals("foo");
    }

}
