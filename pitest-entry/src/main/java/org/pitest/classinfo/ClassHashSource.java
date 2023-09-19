package org.pitest.classinfo;

import java.util.Optional;

public interface ClassHashSource {
    Optional<ClassHash> fetchClassHash(ClassName name);
}
