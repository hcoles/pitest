package org.pitest.mutationtest.build;

import static org.pitest.functional.prelude.Prelude.printWith;

import java.io.File;
import java.util.Collection;

import org.pitest.classinfo.ClassName;
import org.pitest.functional.SideEffect1;
import org.pitest.functional.prelude.Prelude;
import org.pitest.mutationtest.MutationConfig;
import org.pitest.mutationtest.TimeoutLengthStrategy;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.execute.MutationTestProcess;
import org.pitest.mutationtest.execute.MinionArguments;
import org.pitest.process.ProcessArgs;
import org.pitest.testapi.Configuration;
import org.pitest.util.Log;
import org.pitest.util.SocketFinder;

public class WorkerFactory {

  private final String                classPath;
  private final File                  baseDir;
  private final Configuration         pitConfig;
  private final TimeoutLengthStrategy timeoutStrategy;
  private final boolean               verbose;
  private final MutationConfig        config;

  public WorkerFactory(final File baseDir, final Configuration pitConfig,
      final MutationConfig mutationConfig,
      final TimeoutLengthStrategy timeoutStrategy, final boolean verbose,
      final String classPath) {
    this.pitConfig = pitConfig;
    this.timeoutStrategy = timeoutStrategy;
    this.verbose = verbose;
    this.classPath = classPath;
    this.baseDir = baseDir;
    this.config = mutationConfig;
  }

  public MutationTestProcess createWorker(
      final Collection<MutationDetails> remainingMutations,
      final Collection<ClassName> testClasses) {
    final MinionArguments fileArgs = new MinionArguments(remainingMutations,
        testClasses, this.config.getEngine(), this.timeoutStrategy,
        Log.isVerbose(), this.pitConfig);

    final ProcessArgs args = ProcessArgs.withClassPath(this.classPath)
        .andLaunchOptions(this.config.getLaunchOptions())
        .andBaseDir(this.baseDir).andStdout(captureStdOutIfVerbose())
        .andStderr(printWith("stderr "));

    final SocketFinder sf = new SocketFinder();
    final MutationTestProcess worker = new MutationTestProcess(
        sf.getNextAvailableServerSocket(), args, fileArgs);
    return worker;
  }

  private SideEffect1<String> captureStdOutIfVerbose() {
    if (this.verbose) {
      return Prelude.printWith("stdout ");
    } else {
      return Prelude.noSideEffect(String.class);
    }

  }

}
