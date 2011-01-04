package sample.util;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class SingletonKeeper {
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
    }
    
    private class SingletonEntry {
        private       Object        instance;
        private final Constructor   provider;
        private final CleanUpper    maid;
        private SingletonEntry (    final Constructor _provider,
                                    final CleanUpper  _maid) {
            provider = _provider;
            maid     = _maid;
        }
    }
    // Database of singletons
    private Map<Class<?>, SingletonEntry> singletons = new LinkedHashMap<>(1024);
    private List<SingletonEntry> maidQue = new LinkedList<>();
    private boolean underWork = false;

    public void ConstructAll () {
        if (underWork)
            throw new RuntimeException("Cannot do that now -- under maid work");
        if (maidQue.isEmpty()) {
            underWork = true;
            for (final Entry<Class<?>, SingletonEntry> entry: singletons.entrySet()) {
                final SingletonEntry    sentry  = entry.getValue();
                assert sentry.provider != null;
                assert sentry.instance == null;
                sentry.instance = sentry.provider.Construct();
                assert sentry.instance != null;
                final boolean maidAdded = maidQue.add(sentry);
                assert maidAdded;
            }
            underWork = false;
        }
        else
            throw new RuntimeException("The maid has not come yet.");
    }

    public void CleanUpAll () {
        if (underWork)
            throw new RuntimeException("Cannot do that now -- under maid work");

        if (maidQue.isEmpty())
            throw new RuntimeException("The maids has already been here");
        else {
            underWork = true;
            for (final SingletonEntry sentry: maidQue) {
                sentry.maid.CleanUp(sentry.instance);
                sentry.instance = null;
            }
            maidQue.clear();
            underWork = false;
        }
    }

    public boolean Register (final Class<?> klass, final Constructor ctor, final CleanUpper maid) {
        if (underWork)
            throw new RuntimeException("Cannot do that now -- under maid work.");

        if (maidQue.isEmpty()) {
            final SingletonEntry sentry = new SingletonEntry(ctor, maid);
            //
            final Object previous = singletons.put(klass, sentry);
            final boolean result = previous == null;
            //
            return result;
        }
        else
            throw new RuntimeException("Cannot register while it's a mess. The maids need to come");
    }

    public Object Get (final Class<?> name) {
        if (underWork)
            throw new RuntimeException("Cannot do that now -- under maid work.");

        if (maidQue.isEmpty())
            throw new RuntimeException("The maids have been here.");
        else {
            final SingletonEntry sentry = singletons.get(name);
            assert sentry != null;
            final Object result = sentry.instance;
            assert result != null;
            return result;
        }
    }
}
