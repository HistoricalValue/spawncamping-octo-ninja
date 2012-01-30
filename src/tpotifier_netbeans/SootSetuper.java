package tpotifier_netbeans;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class SootSetuper {
    static boolean PerformTransformations           = !false; public void SetPerformTransformations         (final boolean set) { PerformTransformations            = set; }
    static boolean withGetInstanceCall              = !false; public void SetWithGetInstanceCall            (final boolean set) { withGetInstanceCall               = set; }
    static boolean withObjoAssign                   = !false; public void SetWithObjoAssign                 (final boolean set) { withObjoAssign                    = set; }
    static boolean withOriginalOperationPreserved   = !false; public void SetWithOriginalOperationPreserved (final boolean set) { withOriginalOperationPreserved    = set; }
    static boolean withDiagnosticPrints             = false;  public void SetWithDiagnosticPrints           (final boolean set) { withDiagnosticPrints              = set; }
    static boolean withProxySetInstance             = false;  public void SetWithProxySetInstance           (final boolean set) { withProxySetInstance              = set; }
    //
    private boolean WithJimpleOutput                = !false; public void SetWithJimpleOutput               (final boolean set) { WithJimpleOutput                  = set; }
    private boolean WithClassOutput                 = !false; public void SetWithClassOutput                (final boolean set) { WithClassOutput                   = set; }
    private boolean WithDavaOutput                  = false;  public void SetWithDavaOutput                 (final boolean set) { WithDavaOutput                    = set; }
    //

    private SootSetuperInitialisation initialisation;
    //
    private void init (final TPOJimpleBodyTransformerOptions _tpoJimpleBodyTransformerOptions) {
        try {
            // Loggers first
            LogManager.getLogManager().readConfiguration(new ByteArrayInputStream(("\n"
                    + "java.util.logging.ConsoleHandler.level: ALL\n"
                    + "tpotifier_netbeans.level: ALL\n"
                    + "tpotifier_netbeans.handlers: java.util.logging.ConsoleHandler\n"
                    + "sample.level: WARNING\n"
                    + "sample.handlers: java.util.logging.ConsoleHandler\n"
                    + "sample.util.handlers: java.util.logging.ConsoleHandler\n"
            ).getBytes("iso8859-1")));
            LOG = Logger.getLogger(SootSetuper.class.getName());
        } catch (IOException ex) {
            throw new AssertionError(ex);
        } catch (SecurityException ex) {
            throw new AssertionError(ex);
        }

        initialisation = new SootSetuperInitialisation(_tpoJimpleBodyTransformerOptions);
        initialisation.Initialise();
    }

    private void cleanup () {
        initialisation.CleanUp();
        initialisation = null;
    }

    static _SootMode SootMode;

    final class MainArguments {
        MainArguments ( final TPOJimpleBodyTransformerOptions _tpoJimpleBodyTransformerOptions,
                        final String                          _mainClass,
                        final String                          _classToAnalyse)
        {
            tpoJimpleBodyTransformerOptions = _tpoJimpleBodyTransformerOptions;
            mainClass                       = _mainClass;
            classToAnalyse                  = _classToAnalyse;
        }

        final TPOJimpleBodyTransformerOptions tpoJimpleBodyTransformerOptions;
        final String                          mainClass;
        final String                          classToAnalyse;
    }
    private final Object SootThreadsMutex = new Object();
    private class SootStarter extends Thread {
        @Override
        public void start () {
//            LOG.log(Level.INFO, "starting thread {0}", this);
            super.start();
        }

        protected final MainArguments args;
        protected SootStarter (final MainArguments _args) { args = _args; }
    }
    private final class JimpleSootStarter extends SootStarter{
        JimpleSootStarter (final MainArguments _args) { super(_args); }
        @Override public void run(){if(WithJimpleOutput)synchronized(SootThreadsMutex){main0(args, "jimple");}}}
    private final class ClassSootStarter extends SootStarter{
        ClassSootStarter (final MainArguments _args) { super(_args); }
        @Override public void run(){if(WithClassOutput)synchronized(SootThreadsMutex){main0(args, "class");}}}
    private final class DavaSootStarter extends SootStarter{
        DavaSootStarter (final MainArguments _args) { super(_args); }
        @Override public void run(){if(WithDavaOutput)synchronized(SootThreadsMutex){main0(args, "dava");}}}
    public void main (  final MainArguments _args) throws InterruptedException {
        final JimpleSootStarter jss = new JimpleSootStarter(_args);
        final ClassSootStarter css = new ClassSootStarter(_args);
        final DavaSootStarter dss = new DavaSootStarter(_args);
        //
        // Serialising because of Singleton Keeper not being thread-safe
        // (or even multithreadedly usable)
        //
        dss.start();    css.start();    jss.start();
        //
        jss.join();     css.join();     dss.join();
    }
    public void main0 (
            final MainArguments _args
            , final String outputFormat
    ) {
        init(_args.tpoJimpleBodyTransformerOptions);

        try {
            final ArrayList<String> sootopts = new ArrayList<String>(30);
            switch (SootMode) {
                case App:
                    sootopts.add("-app");
                    break;
                case WholeProgramWithSpark:
                    sootopts.add("-whole-program");
                    sootopts.add("-phase-option"); sootopts.add("cg.spark");
                    sootopts.add(   ""
                                    + "enabled:true"
                                    + ",simple-edges-bidirectional:false"
                                    + "ignore-types:true"
                                    + ",on-fly-cg:true"
                                    + ",propagator:worklist"
                                    + ",set-impl:double"
                                    + ",double-set-old:hybrid"
                                    + ",double-set-new:hybrid"
                                    + ",lazy-pts:true"
//                                    + ",dump-html:true"
                                    );
                    break;
            }

            sootopts.add("-output-format"); sootopts.add(outputFormat);
//            sootopts.add("-dump-body"); sootopts.add("jtp");
//            sootopts.add("-throw-analysis"); sootopts.add("unit");
            sootopts.add("-exclude"); sootopts.add("java");
            sootopts.add("-trim-cfgs");
            sootopts.add("-main-class");
            sootopts.add(_args.mainClass);
            sootopts.add(_args.classToAnalyse);
//            sootopts.add("tpotifier_netbeans.SootSetuper");
//            sootopts.add("sample.util.P");
//            sootopts.add("sample.SharedMemoryTPO");

//            sootopts.add("-help");
//            sootopts.add("-phase-list");
//            sootopts.add("-phase-help"); sootopts.add("cg.spark");

            final String[] sootArgs = new String[sootopts.size()];
            sootopts.toArray(sootArgs);
            soot.Main.main(sootArgs);
        }
        finally {
            cleanup();
        }
    }
    private static Logger LOG = null;
}
