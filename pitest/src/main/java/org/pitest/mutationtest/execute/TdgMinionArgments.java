package org.pitest.mutationtest.execute;

import java.util.Collection;
import java.io.Serializable;
import org.pitest.mutationtest.config.TestPluginArguments;



public class TdgMinionArgments implements Serializable{
    public  Collection<String> clazzes;
    public  TestPluginArguments         pitConfig;

    public TdgMinionArgments(Collection<String> clazzes, TestPluginArguments    pitConfig) {
        this.clazzes = clazzes;
        this.pitConfig = pitConfig;
    }
}
