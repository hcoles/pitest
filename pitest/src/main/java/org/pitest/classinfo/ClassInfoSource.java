package org.pitest.classinfo;

import java.util.Optional;

public interface ClassInfoSource {
  Optional<ClassInfo> fetchClass(ClassName name);
}
