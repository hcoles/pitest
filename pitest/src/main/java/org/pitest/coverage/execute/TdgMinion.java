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
import org.junit.Test;
import java.util.stream.Collectors;
public class TdgMinion {
    private static final Logger LOG = Log.getLogger();
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
            

            // 读取classNames
            final int count = dis.readInt();
            LOG.fine(() -> "Expecting " + count + " tests classes from parent");
            final List<ClassName> classes = new ArrayList<>(count);
            for (int i = 0; i != count; i++) {
                classes.add(ClassName.fromString(dis.readString()));
            }
            LOG.fine(() -> "Tests classes received");
            LOG.fine(() -> "Tests classes received" + classes);
            // System.out.println("tdgminion receive classes : "  + classes);
            SafeDataOutputStream dos = new SafeDataOutputStream(s.getOutputStream());
            pipe = new TdgPipe(dos);
            for (ClassName n : classes) {
                try {
                    Method[] declaredMethods = Class.forName(n.toString()).getDeclaredMethods();
                    List<String> declaredMethodsList = Stream.of(declaredMethods).map(Method::getName).collect(Collectors.toList());
                    List<String> aMethods = Stream.of(declaredMethods).filter(m -> m.isAnnotationPresent(Test.class)).map(Method::getName).collect(Collectors.toList());
                    // System.out.println("tdgimpl getTests " + aMethods);
                    pipe.recordTestUnitsName(n.toString() , aMethods);
                } catch (Exception e) {
                    // TODO: handle exception
                    System.out.println("tdgimpl getTests classNotFound exception" + n);
                }
            }


            
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
            System.out.println("xixixixi");
        }
    }
}
