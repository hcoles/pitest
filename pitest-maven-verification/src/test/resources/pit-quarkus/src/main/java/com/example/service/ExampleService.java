package com.example.service;


import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ExampleService {

    public boolean doStuff(String s) {
        return s.equals("foo");
    }

}
