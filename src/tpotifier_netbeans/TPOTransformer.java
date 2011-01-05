package tpotifier_netbeans;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import soot.Body;
import soot.Local;
import soot.PointsToAnalysis;
import soot.PointsToSet;
import soot.Scene;
import soot.jimple.JimpleBody;
import soot.util.Chain;

public class TPOTransformer extends soot.BodyTransformer {

    public static final String PhaseName = "jtp.replaceTPOs";
    
    private static final long serialVersionUID = 237;

    private static final soot.RefType ProxyType = soot.RefType.v("sample.SharedMemoryTPO");
    
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

    private static void transform ( final JimpleBody body) {
        final PrintStream out = soot.G.v().out;
        final PointsToAnalysis points = Scene.v().getPointsToAnalysis();
        final Chain<Local> locals = body.getLocals();
        final Map<Local, PointsToSet> aliases = new HashMap<>(body.getLocalCount());
        out.printf("locals = %s%n", locals);
        for (final Local local: locals) {
            if (ProxyType.equals(local.getType()))
                aliases.put(local, points.reachingObjects(local));
        }
        out.println(aliases);
    }
}
