package tpotifier_netbeans;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import sample.util.SingletonKeeper;
import sample.util.SingletonKeeper.CleanUpper;
import soot.Pack;
import soot.PackManager;
import soot.Scene;
import soot.Transform;

public class Main {

    private interface InitialisationAction {
        void Do ();
        void UnDo ();
    }
    private static class Initialisation {
        private final Deque<InitialisationAction> actions = new LinkedList<>();
        public void AddAction (final InitialisationAction action) {
            actions.push(action);
        }
        public void Initialise () {
            final Iterator<InitialisationAction> ite = actions.descendingIterator();
            while (ite.hasNext()) {
                final InitialisationAction action = ite.next();
                LOG.log(Level.INFO, "Performing initialisation action {0}", action);
                action.Do();
            }
            LOG.log(Level.INFO, "Done with initialisation actions");
        }
        public void CleanUp () {
            for (final InitialisationAction action: actions) {
                LOG.log(Level.INFO, "Performing cleanup {0}", action);
                action.UnDo();
            }
            LOG.log(Level.INFO, "Dan with cleanups");
        }
    }
    private static Initialisation initialisation;

    private static void init () {
        try {
            // Loggers first
            LogManager.getLogManager().readConfiguration(new ByteArrayInputStream(("\n"
                    + "java.util.logging.ConsoleHandler.level: ALL\n"
                    + "tpotifier_netbeans.level: ALL\n"
                    + "tpotifier_netbeans.handlers: java.util.logging.ConsoleHandler\n"
                    + "sample.level: ALL\n"
                    + "sample.hanlders: java.util.logging.ConsoleHandler\n"
                    + "sample.util.handlers: java.util.logging.ConsoleHandler\n"
            ).getBytes("iso8859-1")));
        } catch (IOException ex) {
            throw new AssertionError(ex);
        } catch (SecurityException ex) {
            throw new AssertionError(ex);
        }
        //
        //
        //
        initialisation = new Initialisation();
        //
        initialisation.AddAction(new InitialisationAction() {
            @Override public String toString () { return "SingletonKeeper"; }
            private final SingletonKeeper SK = SingletonKeeper.S();
            @Override
            public void Do () {
                final CleanUpper lazyMaid = new SingletonKeeper.CleanUpper() {
                    @Override public void CleanUp (final Object inst) {}
                };
                //
                SK.Register(TPOTransformer.class,
                        new SingletonKeeper.Constructor() {
                            @Override
                            public Object Construct () {
                                return new TPOTransformer();
                            }
                        }, lazyMaid);
            }
            @Override
            public void UnDo () {
                SK.CleanUpAll();
            }
        });
        //
        //
        initialisation.AddAction(new InitialisationAction() {
            @Override public String toString () { return "Soot"; }
            @Override
            public void Do () {
                final SingletonKeeper SK  = SingletonKeeper.S();
                final PackManager     PM  = soot.PackManager.v();
                final Pack            JTP = PM.getPack("jtp");
                JTP.add(new Transform(TPOTransformer.PhaseName, SK.Get(TPOTransformer.class)));
            }
            @Override
            public void UnDo () {}
        });
        //
        //
        //
        initialisation.Initialise();
    }

    private static void cleanup () {
        initialisation.CleanUp();
        initialisation = null;
    }

    public enum _SootMode {
        App, WholeProgramWithSpark
    }
    public static final _SootMode SootMode = _SootMode.
            App
//            WholeProgramWithSpark
            ;
    
    private static final Scene SootScene = Scene.v();
    private static void abc (final String className, final int level) {
        switch (SootMode) {
            case App:   SootScene.addBasicClass(className, level);  break;
            case WholeProgramWithSpark:                             break;
        }
    }
    private static void abc (final String className) {
        abc(className, soot.SootClass.SIGNATURES);
    }
    private static void abcb (final String className) {
        abc(className, soot.SootClass.BODIES);
    }
    private static void _addBasicClasses () {
        {
                abc("java.lang.Thread");
                abc("java.lang.ThreadGroup");
                abc("java.lang.ExceptionInInitializerError");
                abc("java.lang.RuntimeException");
                abc("java.lang.ClassNotFoundException");
                abc("java.lang.ArithmeticException");
                abc("java.lang.ArrayStoreException");
                abc("java.lang.ClassCastException");
                abc("java.lang.IllegalMonitorStateException");
                abc("java.lang.IndexOutOfBoundsException");
                abc("java.lang.ArrayIndexOutOfBoundsException");
                abc("java.lang.NegativeArraySizeException");
                abc("java.lang.NullPointerException");
                abc("java.lang.InstantiationError");
                abc("java.lang.InternalError");
                abc("java.lang.OutOfMemoryError");
                abc("java.lang.StackOverflowError");
                abc("java.lang.UnknownError");
                abc("java.lang.ThreadDeath");
                abc("java.lang.ClassCircularityError");
                abc("java.lang.ClassFormatError");
                abc("java.lang.IllegalAccessError");
                abc("java.lang.IncompatibleClassChangeError");
                abc("java.lang.LinkageError");
                abc("java.lang.VerifyError");
                abc("java.lang.NoSuchFieldError");
                abc("java.lang.AbstractMethodError");
                abc("java.lang.NoSuchMethodError");
                abc("java.lang.UnsatisfiedLinkError");
                abc("java.lang.Thread");
                abc("java.lang.Runnable");
                abc("java.lang.Cloneable");
                abc("java.io.Serializable");
                abc("java.lang.ref.Finalizer");
                abc("java.lang.ClassLoader");
                abc("java.security.PrivilegedActionException");
                abcb("java.lang.Object");
                abcb("java.lang.Object");
                abcb("java.lang.Class");
                abcb("java.lang.Void");
                abcb("java.lang.Boolean");
                abcb("java.lang.Byte");
                abcb("java.lang.Character");
                abcb("java.lang.Short");
                abcb("java.lang.Integer");
                abcb("java.lang.Long");
                abcb("java.lang.Float");
                abcb("java.lang.Double");
                abcb("java.lang.String");
                abcb("java.lang.StringBuffer");
                abcb("java.lang.Error");
                abcb("java.lang.AssertionError");
                abcb("java.lang.Throwable");
                abcb("java.lang.NoClassDefFoundError");
                abcb("java.lang.ExceptionInInitializerError");
                abcb("java.lang.RuntimeException");
                abcb("java.lang.ClassNotFoundException");
                abcb("java.lang.ArithmeticException");
                abcb("java.lang.ArrayStoreException");
                abcb("java.lang.ClassCastException");
                abcb("java.lang.IllegalMonitorStateException");
                abcb("java.lang.IndexOutOfBoundsException");
                abcb("java.lang.ArrayIndexOutOfBoundsException");
                abcb("java.lang.NegativeArraySizeException");
                abcb("java.lang.NullPointerException");
                abcb("java.lang.InstantiationError");
                abcb("java.lang.InternalError");
                abcb("java.lang.OutOfMemoryError");
                abcb("java.lang.StackOverflowError");
                abcb("java.lang.UnknownError");
                abcb("java.lang.ThreadDeath");
                abcb("java.lang.ClassCircularityError");
                abcb("java.lang.ClassFormatError");
                abcb("java.lang.IllegalAccessError");
                abcb("java.lang.IncompatibleClassChangeError");
                abcb("java.lang.LinkageError");
                abcb("java.lang.VerifyError");
                abcb("java.lang.NoSuchFieldError");
                abcb("java.lang.AbstractMethodError");
                abcb("java.lang.NoSuchMethodError");
                abcb("java.lang.UnsatisfiedLinkError");
                abcb("java.lang.Thread");
                abcb("java.lang.Runnable");
                abcb("java.lang.Cloneable");
                abcb("java.io.Serializable");
                abcb("java.lang.ref.Finalizer");
                abcb("java.lang.System");
                abcb("java.lang.ThreadGroup");
                abcb("java.lang.ClassLoader");
                abcb("java.security.PrivilegedActionException");
                abcb("sample.SharedMemoryTPO");
                abcb("sun.misc.VM");
                abcb("java.lang.Terminator");
                abcb("sun.misc.Version");
                abcb("java.io.BufferedInputStream");
                abcb("java.io.FileDescriptor");
                abcb("java.io.FileOutputStream");
                abcb("java.io.FileInputStream");
                abcb("java.security.ProtectionDomain");
            }
    }

    public static void main (final String[] args) {
        init();

        try {
            _addBasicClasses();

            final ArrayList<String> sootopts = new ArrayList<>(30);
            switch (SootMode) {
                case App:
//                    sootopts.add("-app");
                    break;
                case WholeProgramWithSpark:
                    sootopts.add("-whole-program");
                    sootopts.add("-phase-option"); sootopts.add("cg.spark");
                    sootopts.add(   ""
                                    + "enabled:true"
                                    + ",simple-edges-bidirectional:false"
                                    + ",on-fly-cg:true"
                                    + ",propagator:worklist"
                                    + ",set-impl:double"
                                    + ",double-set-old:hybrid"
                                    + ",double-set-new:hybrid"
                                    + ",lazy-pts:false"
//                                    + ",dump-html:true"
                                    );
                    break;
            }

//            sootopts.add("-validate");
            final String outputFormat =
                    "jimple"
//                     "dava"
                    ;
            sootopts.add("-output-format"); sootopts.add(outputFormat);
            sootopts.add("-dump-body"); sootopts.add("jtp");
//            sootopts.add("-throw-analysis"); sootopts.add("unit");
            sootopts.add("-exclude"); sootopts.add("java");
            sootopts.add("-trim-cfgs");
            sootopts.add("-main-class");
            sootopts.add("sample.Sample");
            sootopts.add("sample.Sample");
            //
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
    private static final Logger LOG = Logger.getLogger(Main.class.getName());

    private Main () {
    }

}
