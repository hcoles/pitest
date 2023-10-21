package com.example.controller;

import com.example.service.ExampleService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("/example")
@ApplicationScoped
public class ExampleController {
    @Inject
    ExampleService service;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public boolean doStuff(String s) {
      System.out.println("Won't die");
      return service.doStuff(s);
    }
}
