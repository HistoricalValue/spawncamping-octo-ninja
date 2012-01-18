package sample.util;

import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SingletonKeeper {
    private static final Logger LOG = Logger.getLogger(SingletonKeeper.class.getName());

    // self singleton
    private static SingletonKeeper me;
    public static SingletonKeeper S () {
        if (me == null)
            me = new SingletonKeeper();
        return me;
    }

    public interface Constructor {
        Object Construct ();
    }
    public interface CleanUpper {
        void CleanUp (Object inst);
        CleanUpper lazy =   new CleanUpper() {
                                @Override
                                public void CleanUp (final Object o) {
                                }
                            };
    }

    private class SingletonEntry {
        private Object              instance;
        private final Constructor   provider;
        private final CleanUpper    maid;
        private SingletonEntry (    final Constructor _provider,
                                    final CleanUpper  _maid) {
            provider = _provider;
            maid     = _maid;
        }
        private boolean             beingInitialised = false;
        //
        private void checkInitialised () {
            if (!IsInitialised())
                throw new RuntimeException("Not constructed");
        }
        private void checkNotInitialised () {
            if (IsInitialised())
                throw new RuntimeException("Already constructed");
        }
        public void Construct () {
            checkNotInitialised();

            assert beingInitialised;
            instance = provider.Construct();
            beingInitialised = false;

            assert IsInitialised();
        }
        public void Destroy () {
            checkInitialised();
            maid.CleanUp(instance);
            instance = null;
            assert !IsInitialised();
        }
        public Object GetInstance () {
            checkInitialised();
            return instance;
        }
        public boolean IsInitialised ()
            { return instance != null; }
        public boolean IsBeingInitialised ()
            { return beingInitialised; }
        public void SetBeingInitialised ()
            { beingInitialised = true; }
    }
    // Database of singletons
    private Map<Class<?>, SingletonEntry> singletons = new LinkedHashMap<Class<?>, SingletonEntry>(1024);
    private List<SingletonEntry> maidQue = new LinkedList<SingletonEntry>();
    private Deque<SingletonEntry> raisingPile = new LinkedList<SingletonEntry>();

    public void CleanUpAll () {
        for (final SingletonEntry sentry: maidQue)
            sentry.Destroy();
        maidQue.clear();
    }

    public boolean Register (final Class<?> klass, final Constructor ctor, final CleanUpper maid) {
        final SingletonEntry sentry = new SingletonEntry(ctor, maid);
        //
        final Object previous = singletons.put(klass, sentry);
        final boolean result = previous == null;
        //
        return result;
    }

    public <T> T Get (final Class<? extends T> klass) {
        final SingletonEntry sentry = singletons.get(klass);
        if (sentry == null)
            throw new RuntimeException("No such singleton registered");

        if (!sentry.IsInitialised())
            __raise(klass, sentry);

        assert sentry.IsInitialised();
        final Object result = sentry.GetInstance();
        assert result != null;
        assert klass.isInstance(result);
        @SuppressWarnings("unchecked") T reslut = (T) result;
        return reslut;
    }

    ////////////////////////////////////////////////////

    private void __raise (final Class<?> klass, final SingletonEntry sentry) {
        LOG.log(Level.INFO, "on-demand creating singleton {0}", klass);

        raisingPile.add(sentry);

        if (sentry.IsBeingInitialised())
            throw new RuntimeException("Cyclic dependency: " +
                    raisingPile);

        sentry.SetBeingInitialised();
        sentry.Construct();

        final SingletonEntry lastRaised = raisingPile.removeLast();
        assert lastRaised == sentry;
    }
}
