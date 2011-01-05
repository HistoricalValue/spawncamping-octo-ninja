package tpotifier_netbeans;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import sample.util.SingletonKeeper;
import sample.util.SingletonKeeper.CleanUpper;

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
                //
                //
                //
                SK.ConstructAll();
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
                final SingletonKeeper SK = SingletonKeeper.S();
                soot.PackManager.v().getPack("jtp").add(
                        new soot.Transform(
                                TPOTransformer.PhaseName,
                                (soot.BodyTransformer)SK.Get(TPOTransformer.class)
                        )
                );
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

    public static void main (final String[] args) {
        init();

        try {
            soot.Main.main(new String[]{
            "-app",
            "-validate",
//            "-whole-program",
            "-output-format", "jimple",
            "-trim-cfgs",
            "-phase-option", "cg", "enabled:true",
            "-phase-option", "cg.spark",
                    "enabled:true,"
                    + "verbose:true,"
                    + "propagator:worklist,"
                    + "simple-edges-bidirectional:false,"
                    + "on-fly-cg:true,"
                    + "set-impl:double,"
                    + "double-set-old:hybrid,"
                    + "double-set-new:hybrid",
            "sample.Sample"});
        }
        finally {
            cleanup();
        }
    }
    private static final Logger LOG = Logger.getLogger(Main.class.getName());

    private Main () {
    }

}
