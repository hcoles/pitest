package org.pitest.util;

import org.junit.Test;
import java.io.File;
import static org.junit.Assert.assertNotNull;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.StringWriter;

public class AgentLoaderTest {
    @Test
    public void findToolsJarTest() {
        File f = AgentLoader.findToolsJar();
        assertNotNull(f);
    }

    @Test
    public void loadAndRunJdepsTest() throws Exception{
        
        List<String> args = new ArrayList<String>(Arrays.asList("-v", "-filter", "java.*|sun.*","-cp","/home/zipeng/biye/test1/haha/target/test-classes:/home/zipeng/biye/test1/haha/target/classes","/home/zipeng/biye/test1/haha/target/test-classes ", "/home/zipeng/biye/test1/haha/target/classes"));
        StringWriter output = AgentLoader.loadAndRunJdeps(args);
        assertNotNull(output);
        System.out.println(output.toString());
    }
}
