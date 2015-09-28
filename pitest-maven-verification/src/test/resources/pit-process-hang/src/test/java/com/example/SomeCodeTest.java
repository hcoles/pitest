package com.example;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.example.SomeCode;

public class SomeCodeTest {

   @Test
   public void testStuff() {
      SomeCode req = new SomeCode();
      req.generateLotsOfOutput();
   }
     
}
