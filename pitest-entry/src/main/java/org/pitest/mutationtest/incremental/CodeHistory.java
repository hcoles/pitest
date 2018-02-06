package org.pitest.mutationtest.incremental;

import java.math.BigInteger;

import org.pitest.classinfo.ClassName;
import java.util.Optional;
import org.pitest.mutationtest.MutationStatusTestPair;
import org.pitest.mutationtest.engine.MutationIdentifier;

public interface CodeHistory {

  Optional<MutationStatusTestPair> getPreviousResult(MutationIdentifier id);

  boolean hasClassChanged(ClassName className);

  boolean hasCoverageChanged(ClassName className, BigInteger currentCoverage);

}
