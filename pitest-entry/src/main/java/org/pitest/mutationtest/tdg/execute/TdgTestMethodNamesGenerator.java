package org.pitest.mutationtest.tdg.execute;


import org.pitest.util.SocketFinder;
import java.net.ServerSocket;

import org.pitest.help.PitHelpError;
import org.pitest.process.LaunchOptions;
import org.pitest.util.ExitCode;
import org.pitest.functional.FCollection;
import org.pitest.classinfo.ClassInfo;
import org.pitest.classpath.CodeSource;
import org.pitest.util.Log;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.Collection;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.io.File;
import org.pitest.process.ProcessArgs;
import org.pitest.util.PitError;
import org.pitest.util.Unchecked;
import org.pitest.help.PitHelpError;
import org.pitest.classinfo.ClassName;
import java.util.stream.Collectors;
import org.pitest.mutationtest.execute.TdgMinionArgments;
import org.pitest.coverage.execute.TdgTestMethodResult;
import org.pitest.testapi.TestUnit;
import org.pitest.coverage.execute.CoverageOptions;
public class TdgTestMethodNamesGenerator {
    private static final Logger    LOG = Log.getLogger();

    private final LaunchOptions    launchOptions;
    private final CodeSource       code;
    private final File             workingDir;
    private final CoverageOptions coverageOptions;
    public TdgTestMethodNamesGenerator(final LaunchOptions  launchOptions,
    final CodeSource       code, final File             workingDir,CoverageOptions coverageOptions) {
        this.launchOptions = launchOptions;
        this.code = code;
        this.workingDir = workingDir;
        this.coverageOptions = coverageOptions;
    }

    public Map<String, Set<String>> getClassMethodNames() {
        try {
            // System.out.println("TdgTestMethodNamesGenerator getClassMethodNames 42");
            // 获取测试类全类名
            
            Collection<ClassName> targets = this.code.getProjectClassPaths().code();
            List<String> tests = this.code.getProjectClassPaths().test().stream().filter(s -> !targets.contains(s)).map(ClassName::toString).collect(Collectors.toList());

            
            Map<String, Set<String>> classMethodNames = new HashMap<>();
        
            final Consumer<TdgTestMethodResult> handler = resultProcessor(classMethodNames);
            final SocketFinder sf = new SocketFinder();
            final ServerSocket socket = sf.getNextAvailableServerSocket();
            //为了支持junit5，传入testPluginConfig的参数，利用JunitRunner寻找方法名
            TdgMinionArgments tdgMinionArgments = new TdgMinionArgments(tests, coverageOptions.getPitConfig());
            final TdgProcess process = new TdgProcess(ProcessArgs
            .withClassPath(this.code.getClassPath()).andBaseDir(this.workingDir)
            .andLaunchOptions(this.launchOptions),
            socket, handler, tdgMinionArgments);

            process.start();

            final ExitCode exitCode = process.waitToDie();
// System.out.println("TdgTestMethodNamesGenerator getClassMethodNames 58");
            if (exitCode == ExitCode.TEST_PLUGIN_ISSUE) {
                LOG.severe("Pitest could not get any tests. Please check that you have installed the pitest plugin for your testing library (eg JUnit 5, TestNG). If your project uses JUnit 4 "
                        + "the plugin is automatically included, but a recent version of JUnit 4 must be on the classpath.");
                throw new PitError(
                    "Please check you have correctly installed the pitest plugin for your project's test library (JUnit 5, TestNG, JUnit 4 etc). ");
            } else if (!exitCode.isOk()) {
                LOG.severe("Tdg generator Minion exited abnormally due to "
                    + exitCode);
                    System.out.println("?????????????????????????????");
                throw new PitError("Tdg generation minion exited abnormally! (" + exitCode + ")");
            } else {
                LOG.fine("Tdg ggggggggggggggggggggggggggggggggggenerator Minion exited ok");
            }

            return classMethodNames;
        } 
        catch (final PitHelpError phe) {
            throw phe;
        }catch (Exception e) {
            throw Unchecked.translateCheckedException(e);
        }
        
    } 
    
    private static Function<ClassInfo, String> classInfoToName() {
        return a -> a.getName().asInternalName();
    }
    private Consumer<TdgTestMethodResult> resultProcessor(
      final Map<String, Set<String>> classMethodNames) {
    return new Consumer<TdgTestMethodResult>() {
      private final String[] spinner = new String[] { "\u0008/", "\u0008-",
          "\u0008\\", "\u0008|" };
      int i = 0;
 
      @Override
      public void accept(final TdgTestMethodResult tr) {
        if (classMethodNames != null) {
            // for (String testClass : tr.res.keySet()) {
            //     for (String methods : tr.res.get(testClass)) {
            //         System.out.println(testClass + " : " + methods);
            //     }
            // }
            classMethodNames.putAll(tr.res);
        }
            // Set<String> ss = new HashSet<>();
            // for (TestUnit unit : tr.tests) {
            //     unit.getDescription().getFirstTestClass()
            //     unit.
            // }
            

        System.out.printf("%s", this.spinner[this.i % this.spinner.length]);
        this.i++;
      }

    };
  }
}
