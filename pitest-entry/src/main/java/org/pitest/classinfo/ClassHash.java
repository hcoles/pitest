package org.pitest.classinfo;

import java.math.BigInteger;

public interface ClassHash {

    ClassIdentifier getId();

    ClassName getName();

    BigInteger getDeepHash();

    HierarchicalClassId getHierarchicalId();

}
