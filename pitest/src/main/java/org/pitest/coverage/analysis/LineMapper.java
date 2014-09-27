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
import org.pitest.functional.Option;
import org.pitest.mutationtest.engine.Location;
import org.pitest.mutationtest.engine.MethodName;

public class LineMapper implements LineMap {
	
	private final CodeSource source;
	
	public LineMapper(CodeSource source) {
		this.source = source;
	}

	public Map<BlockLocation, Set<Integer>> mapLines(ClassName clazz) {
		ControlFlowAnalyser cfa = new ControlFlowAnalyser();

	   Map<BlockLocation, Set<Integer>> map = new HashMap<BlockLocation, Set<Integer>>();
	   
		    Option<byte[]> maybeBytes = source.fetchClassBytes(clazz);
		    // classes generated at runtime eg by mocking frameworks
		    // will be instrumented but not available on the classpath
		    for ( byte[] bytes : maybeBytes ) {
	        ClassReader cr=new ClassReader(bytes);
	        ClassNode classNode=new ClassNode();
	        
	        //ClassNode is a ClassVisitor
	        cr.accept(classNode, ClassReader.EXPAND_FRAMES);
	          for ( Object m : classNode.methods ) {
	          MethodNode mn = (MethodNode) m;
	          Location l = Location.location(clazz, MethodName.fromString(mn.name),mn.desc);
	          List<Block> blocks = cfa.analyze(mn);
	          for ( int i = 0; i != blocks.size(); i ++ ) {
	            BlockLocation bl = new BlockLocation(l,i);
	            map.put(bl, blocks.get(i).getLines());
	          }
	              
	        }
		    }
		
		return map;
	}
	
}
