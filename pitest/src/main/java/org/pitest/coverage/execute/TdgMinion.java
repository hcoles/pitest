package org.pitest.coverage.execute;

import org.pitest.util.SafeDataInputStream;
import org.pitest.util.Log;
import org.pitest.classinfo.ClassName;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Arrays;
import java.net.Socket;
import java.io.BufferedOutputStream;
import org.pitest.util.Log;
import java.util.logging.Logger;
import org.pitest.util.SafeDataOutputStream;
import org.pitest.util.ExitCode;
import org.pitest.util.Id;
import org.pitest.util.Verbosity;
import java.lang.reflect.Method;
import java.util.stream.Stream;
import java.util.Collection;
import org.junit.Test;
import java.util.stream.Collectors;
import org.pitest.mutationtest.execute.TdgMinionArgments;
import org.pitest.util.IsolationUtils;
import org.pitest.testapi.Configuration;
import org.pitest.classpath.ClassloaderByteArraySource;
import org.pitest.classpath.ClassloaderByteArraySource;
import org.pitest.testapi.execute.FindTestUnits;
import org.pitest.mutationtest.config.MinionSettings;
import org.pitest.testapi.TestUnit;
import org.pitest.mutationtest.config.ClientPluginServices;
import org.pitest.junit.JUnitCompatibleConfiguration; // for fix bug test
public class TdgMinion {
    private static final Logger LOG = Log.getLogger();
    private static List<TestUnit> findTestsForTestClasses(
        final ClassLoader loader, final Collection<ClassName> testClasses,
        final Configuration config) {
      final Collection<Class<?>> tcs = testClasses.stream().flatMap(ClassName.nameToClass(loader)).collect(Collectors.toList());
      final FindTestUnits finder = new FindTestUnits(config);
      return finder.findTestUnitsForAllSuppliedClasses(tcs);
            // return new ArrayList<TestUnit>();
    }
  
    public static void main(String[] args) {
        // LOG.setVerbose(Verbosity.VERBOSE);
        Socket s = null;
        TdgPipe pipe = null;
        ExitCode exitCode = ExitCode.OK;
        try {
            final int port = Integer.parseInt(args[0]);
            s = new Socket("localhost", port);
            s.setSoTimeout(10000);
            final SafeDataInputStream dis = new SafeDataInputStream(s.getInputStream());
            if (dis == null ) {
                LOG.fine("dis is null");    
            }
            
            // System.out.println("1111111111111111111111111111111111111111111111111111111111");
            // 读取classNames
            // final int count = dis.readInt();
            // LOG.fine(() -> "Expecting " + count + " tests classes from parent");
            // final List<ClassName> classes = new ArrayList<>(count);
            // for (int i = 0; i != count; i++) {
            //     classes.add(ClassName.fromString(dis.readString()));
            // }
            final TdgMinionArgments tdgargs = dis.read(TdgMinionArgments.class);
            
            // LOG.fine(() -> "Tests classes received");
            // LOG.fine(() -> "Tests classes received" + classes);
            // System.out.println("Tests classes received" + tdgargs.clazzes);
            // System.out.println("tdgminion receive classes : "  + classes);
            
            SafeDataOutputStream dos = new SafeDataOutputStream(s.getOutputStream());
            
            pipe = new TdgPipe(dos);
            
            
            
            
            final ClientPluginServices plugins = ClientPluginServices.makeForContextLoader();
            final MinionSettings factory = new MinionSettings(plugins);
            Configuration cfg =  factory.getTestFrameworkPlugin(tdgargs.pitConfig,
                            ClassloaderByteArraySource.fromContext());
            if (cfg instanceof JUnitCompatibleConfiguration) {
                System.out.println("JUnitCompatibleConfiguration");
            }
            final ClassLoader loader = IsolationUtils.getContextClassLoader();
            final List<TestUnit> tests = findTestsForTestClasses(loader,
                tdgargs.clazzes.stream().map(ClassName::fromString).collect(Collectors.toList()),  
                cfg);
            TdgTestMethodResult result = new TdgTestMethodResult(tests);
            // System.out.println("99999999999999999999999999999999999" + result.res);
            pipe.recordTestUnitsName(result);
            // System.out.println("99999999999999999999999999999999999" + result.res);
            
            dos.writeByte(Id.DONE);
            dos.writeInt(exitCode.getCode());
            dos.flush();
            // 读取classNames
            // final int count = dis.readInt();
            // LOG.fine(() -> "Expecting " + count + " tests classes from parent");
            // final List<ClassName> classes = new ArrayList<>(count);
            // for (int i = 0; i != count; i++) {
            //     classes.add(ClassName.fromString(dis.readString()));
            // }
            // LOG.fine(() -> "Tests classes received");

            // //创建输出流
            // pipe = new TdgPipe(new SafeDataOutputStream(s.getOutputStream()));

            // // 对于每一个className，用反射获取其用Test.class注解的方法名字集合
            // List<String> fakemethodsNames = new ArrayList<String>(Arrays.asList("123", "456"));
            // for (int i = 0; i != count; i++) {
            //     pipe.recordTestUnitsName("abc" + i , fakemethodsNames);
            // }
            // System.out.println("hahahah");
            // System.exit(exitCode.getCode());
        }

        catch(Exception e) {
            System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
        }
    }
}
