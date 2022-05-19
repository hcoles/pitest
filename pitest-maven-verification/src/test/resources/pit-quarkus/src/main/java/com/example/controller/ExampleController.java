package com.example.controller;

import com.example.service.ExampleService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

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
