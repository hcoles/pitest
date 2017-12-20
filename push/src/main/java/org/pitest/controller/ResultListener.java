package org.pitest.controller;

import org.pitest.mutationtest.MutationResult;

public interface ResultListener {

  void report(MutationResult r);
  
}
