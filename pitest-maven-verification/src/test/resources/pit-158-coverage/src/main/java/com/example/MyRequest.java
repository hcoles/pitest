package com.example;

import java.io.Serializable;
import com.google.common.base.Preconditions;

public class MyRequest implements Serializable {

   private static final long serialVersionUID = -3548858114709541512L;
   private Long userId;

   public Long getUserId() {
      return userId;
   }

   public void setUserId(Long userId) {
      this.userId = userId;
   }

   public void validate() throws IllegalStateException {
      Preconditions.checkState(userId != null);
   }

}