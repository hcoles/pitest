package com.example;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.example.MyRequest;

public class MyRequestTest {

   @Test
   public void testValidateEmpty() {
     try {
      MyRequest req = new MyRequest();
      req.validate();
      fail();
     } catch(IllegalStateException ex) {
       // pass
     }
   }
     
  @Test
   public void testValidateOk() {
      final Long userId = 99L;
      MyRequest req = new MyRequest();
      req.setUserId(userId);
      req.validate();

      assertEquals(userId, req.getUserId());
   }
}