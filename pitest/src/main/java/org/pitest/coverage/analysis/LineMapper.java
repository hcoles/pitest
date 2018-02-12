package org.pitest.coverage.analysis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.pitest.classinfo.ClassName;
import org.pitest.classpath.CodeSource;
import org.pitest.coverage.BlockLocation;
import org.pitest.coverage.LineMap;
import java.util.Optional;
import org.pitest.mutationtest.engine.Location;
import org.pitest.mutationtest.engine.MethodName;

public class LineMapper implements LineMap {

  private final CodeSource source;

  public LineMapper(final CodeSource source) {
    this.source = source;
  }

  @Override
  public Map<BlockLocation, Set<Integer>> mapLines(final ClassName clazz) {

    final Map<BlockLocation, Set<Integer>> map = new HashMap<>();

    final Optional<byte[]> maybeBytes = this.source.fetchClassBytes(clazz);
    // classes generated at runtime eg by mocking frameworks
    // will be instrumented but not available on the classpath
    if (maybeBytes.isPresent()) {
      final ClassReader cr = new ClassReader(maybeBytes.get());
      final ClassNode classNode = new ClassNode();

      cr.accept(classNode, ClassReader.EXPAND_FRAMES);
      for (final Object m : classNode.methods) {
        final MethodNode mn = (MethodNode) m;
        final Location l = Location.location(clazz,
            MethodName.fromString(mn.name), mn.desc);
        final List<Block> blocks = ControlFlowAnalyser.analyze(mn);
        for (int i = 0; i != blocks.size(); i++) {
          final BlockLocation bl = new BlockLocation(l, i);
          map.put(bl, blocks.get(i).getLines());
        }

      }
    }

    return map;
  }

}
