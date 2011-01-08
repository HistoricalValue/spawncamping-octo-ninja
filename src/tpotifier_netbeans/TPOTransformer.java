package tpotifier_netbeans;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import soot.Body;
import soot.Local;
import soot.PatchingChain;
import soot.PointsToSet;
import soot.Scene;
import soot.Type;
import soot.Unit;
import soot.jimple.JimpleBody;
import soot.jimple.spark.pag.PAG;
import soot.util.Chain;

public class TPOTransformer extends soot.BodyTransformer {

    public static final String PhaseName = "jtp.replaceTPOs";
    
    private static final long serialVersionUID = 237;

    private static final soot.RefType ProxyType = soot.RefType.v("sample.SharedMemoryTPO");

    private static boolean isAProxyType (final Type type) {
        final boolean result = ProxyType.equals(type);
        return result;
    }

    private static boolean anyIsAProxyType (final Iterable<Type> types) {
        for (final Type type: types)
            if (isAProxyType(type))
                return true;
        return false;
    }
    
    @Override
    protected void internalTransform (final Body b, final String phaseName, final Map options) {
        final boolean contains_enabled = options.containsKey("enabled");
        final Object enabledValue = options.get("enabled");
        final boolean enabledIsTrue = ((String) enabledValue).equals("true");
        final boolean phaseNameIsCorrect = phaseName.equals(PhaseName);
        final boolean bodyIsJimple = b instanceof soot.jimple.JimpleBody;
        if (contains_enabled && enabledIsTrue && phaseNameIsCorrect && bodyIsJimple)
            transform((JimpleBody) b);
        else
            logfine("Transformation not applied to {0} because one of the "
                    + "following did not hold: contains_enabled={1}, enabled"
                    + "IsTrue={2}, phaseNameIsCorrect={3}, bodyIsJimple={4}",
                    b.getMethod().getName(), contains_enabled, enabledIsTrue,
                    phaseNameIsCorrect, bodyIsJimple);
    }
    private static final Logger LOG = Logger.getLogger(TPOTransformer.class.getName());
    private static void logfine (final String msg, Object... args) {
        LOG.log(Level.FINE, msg, args);
    }

    private final static Pattern addBasicClassExceptionPatter = Pattern
            .compile("Scene");//.addBasicClass\\(([^,]+),([A-Z]+)\\);");
    private static void transform ( final JimpleBody body) {
        final PrintStream               out     = soot.G.v().out;
        final PAG                       points  = (PAG) Scene.v().getPointsToAnalysis();
        final Chain<Local>              locals  = body.getLocals();
        final Map<Local, PointsToSet>
                                        aliases = new HashMap<>(body.getLocalCount());
        final PatchingChain<Unit>       units   = body.getUnits();
        //
        out.printf( "[%s]%n"
                +   "locals = %s%n",
                body.getMethod(),
                locals
                );
        for (final Local local: locals) {
            final PointsToSet subresult = points.reachingObjects(local);
            aliases.put(local, subresult);
        }

        final Set<Local> tagged = new HashSet<>(20);
        for (final Entry<Local, PointsToSet> entry: aliases.entrySet()) {
            out.printf("%s: %s%n", entry.getKey(), entry.getValue());
            if (anyIsAProxyType(entry.getValue().possibleTypes()))
                tagged.add(entry.getKey());
        }
        out.println(tagged);
    }
}
