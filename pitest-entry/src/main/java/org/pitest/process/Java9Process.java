package org.pitest.process;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Process for java 9+, using file to pass all parameters
 */
public class Java9Process implements WrappingProcess {

    private static final Map<Optional<String>, Path> CACHE = new ConcurrentHashMap<>();

    private final int         port;
    private final ProcessArgs processArgs;
    private final Class<?>    minionClass;
    private JavaProcess       process;

    public Java9Process(int port, ProcessArgs args, Class<?> minionClass) {
        this.port = port;
        this.processArgs = args;
        this.minionClass = minionClass;
    }

    public void start() throws IOException {
        String args =  "" + this.port;

        ProcessBuilder processBuilder = createProcessBuilder(
                this.processArgs.getJavaExecutable(),
                this.processArgs.getJvmArgs(),
                this.minionClass, args,
                this.processArgs.getJavaAgentFinder(),
                this.processArgs.getLaunchClassPath());


        configureProcessBuilder(processBuilder, this.processArgs.getWorkingDir(),
                this.processArgs.getEnvironmentVariables());

        Process process = processBuilder.start();
        this.process = new JavaProcess(process, this.processArgs.getStdout(),
                this.processArgs.getStdErr());
    }

    public boolean isAlive() {
        return process.isAlive();
    }

    private void configureProcessBuilder(ProcessBuilder processBuilder,
                                         File workingDirectory, Map<String, String> environmentVariables) {
        processBuilder.directory(workingDirectory);
        Map<String, String> environment = processBuilder.environment();

        environment.putAll(environmentVariables);
    }

    public void destroy() {
        this.process.destroy();
    }

    public JavaProcess getProcess() {
        return this.process;
    }

    private ProcessBuilder createProcessBuilder(String javaProc,
                                                List<String> unsanitisedArgs, Class<?> mainClass, String programArgs,
                                                JavaAgent javaAgent, String classPath) {

        List<String> fileArgs = createLaunchArgs(javaAgent, unsanitisedArgs, classPath);

        removeJacocoAgent(fileArgs);

        // All arguments are passed via a temporary file, thereby avoiding command line length limits
        Path argsFile = CACHE.computeIfAbsent(javaAgent.getJarLocation(), j -> createArgsFile(fileArgs));

        List<String> cmd = new ArrayList<>();
        cmd.add(javaProc);
        addLaunchJavaAgentsAndEnvironmentVariables(cmd);
        cmd.add("@" + argsFile.toFile().getAbsolutePath());

        cmd.add(mainClass.getName());
        cmd.add(programArgs);

        return new ProcessBuilder(cmd);
    }

    private List<String> removeArgFiles(List<String> args) {
        return args.stream()
                .map(String::trim)
                .filter(a -> !a.startsWith("@"))
                .collect(Collectors.toList());
    }


    private void removeJacocoAgent(List<String> cmd) {
        removeFromClassPath(cmd, line -> line.startsWith("-javaagent") && line.contains("jacoco"));
    }

    private static void removeFromClassPath(List<String> cmd, Predicate<String> match) {
        for (int i = cmd.size() - 1; i >= 0; i--) {
            if (match.test(cmd.get(i))) {
                cmd.remove(i);
            }
        }
    }

    private List<String> createLaunchArgs(JavaAgent agentJarLocator, List<String> args, String classPath) {

        // remove other arg files from the command line
        List<String> argsWithoutArgFiles = removeArgFiles(args);

        List<String> cmd = new ArrayList<>();

        cmd.add("-classpath");
        cmd.add(classPath.replace(" ", "\" \""));
        
        addPITJavaAgent(agentJarLocator, cmd);
        cmd.addAll(argsWithoutArgFiles);

        // We could pass the existing argfiles through as is, however
        // they may be designed for the module pass. As we don't properly
        // support the module path yet, the best we can do is strip
        // add-modules lines out. Since we're already creating an args file
        // we can append the existing files onto it with filtering.
        List<String> existing = findArgsFiles(args).stream()
                .flatMap(f -> readFile(f).stream())
                .filter(l -> !l.trim().startsWith("--add-modules"))
                .collect(Collectors.toList());


        cmd.addAll(existing);

        return cmd;
    }

    private static List<String> readFile(String f) {
        try {
            return Files.readAllLines(Path.of(f));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


    private Path createArgsFile(List<String> cmd) {
       try {
           // All files should be deleted on process exit, although some garbage may be left
           // if the process is killed. Files are however created in the system temp directory
           // so should be cleaned up on reboot
           String name = "pitest-args-";
           Path args = Files.createTempFile(name, ".args");
           args.toFile().deleteOnExit();
           Files.write(args, cmd);
           return args;
       } catch (IOException e) {
           throw new RuntimeException(e);
       }
    }

    private static void addPITJavaAgent(JavaAgent agentJarLocator,
                                        List<String> cmd) {
        final Optional<String> jarLocation = agentJarLocator.getJarLocation();
        jarLocation.ifPresent(l -> cmd.add("-javaagent:" + l));
    }

    private static void addLaunchJavaAgentsAndEnvironmentVariables(List<String> cmd) {
        RuntimeMXBean rt = ManagementFactory.getRuntimeMXBean();
        List<String> agents = rt.getInputArguments().stream()
                .filter(isJavaAgentParam().or(isEnvironmentSetting()))
                .collect(Collectors.toList());
        cmd.addAll(agents);
    }

    private static Predicate<String> isEnvironmentSetting() {
        return a -> a.startsWith("-D");
    }

    private static Predicate<String> isJavaAgentParam() {
        return a -> a.toLowerCase().startsWith("-javaagent");
    }

    private List<String> findArgsFiles(List<String> args) {
        return args.stream()
                .map(String::trim)
                .filter(a -> a.startsWith("@"))
                .map(a -> a.substring(1))
                .collect(Collectors.toList());
    }
}
