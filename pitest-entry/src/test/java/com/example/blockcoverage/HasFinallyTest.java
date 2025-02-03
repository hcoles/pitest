package com.example.blockcoverage;

import org.junit.Assert;
import org.junit.Test;

public class HasFinallyTest {
  @Test
  public void testHasInlinedFinallyBlock(){
    Assert.assertTrue(HasFinallyTestee.methodWithFinally(true));
  }

  @Test
  public void testHasInlinedFinallyBlockOtherBranch(){
    Assert.assertFalse(HasFinallyTestee.methodWithFinally(false));
  }
}
