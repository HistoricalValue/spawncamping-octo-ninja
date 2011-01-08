package tpotifier_netbeans;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import soot.Body;
import soot.G;
import soot.Local;
import soot.PatchingChain;
import soot.PointsToSet;
import soot.Scene;
import soot.Unit;
import soot.jimple.JimpleBody;
import soot.jimple.spark.SparkTransformer;
import soot.jimple.spark.pag.PAG;
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

    private final static Pattern addBasicClassExceptionPatter = Pattern
            .compile("Scene");//.addBasicClass\\(([^,]+),([A-Z]+)\\);");
    private static void transform ( final JimpleBody body) {
        final Map<String, String> opts = new HashMap<>(20);
        opts.put("enabled", "true");
        opts.put("verbose", "true");
        opts.put("propagator", "worklist");
        opts.put("simple-edges-bidirectional", "false");
        opts.put("on-fly-cg", "false");
        opts.put("set-impl", "hybrid");
        opts.put("double-set-old", "hybrid");
        opts.put("double-set-new", "hybrid");
        try {
            SparkTransformer.v().transform("jtp.spark", opts);
        }
        catch (final RuntimeException ex) {
            final String msg = ex.getLocalizedMessage();
            final Matcher matcher = addBasicClassExceptionPatter.matcher(msg);
            final MatchResult matchResult = matcher.toMatchResult();
            final PrintStream out = G.v().out;
            final String group = matcher.group();
            out.println(group);
        }





        final PrintStream               out     = soot.G.v().out;
        final PAG                       points  = (PAG) Scene.v().getPointsToAnalysis();
        final Chain<Local>              locals  = body.getLocals();
        final Map<Local, Map<Unit, PointsToSet>>
                                        aliases = new HashMap<>(body.getLocalCount());
        final PatchingChain<Unit>       units   = body.getUnits();
        //
        out.printf( "[%s]%n"
                +   "locals = %s%n"
                +   "dereferences = %s%n"
                +   "simple sources = %s%n",
                body.getMethod(),
                locals,
                points.getDereferences(),
//                points.simpleSources()
                ""
                );
        for (final Local local: locals) {
            final Map<Unit, PointsToSet> subresult = new HashMap<>(20);
            for (final Unit unit: units) {
                final PointsToSet subreaching = points.reachingObjects(unit, local);
                if (!subreaching.isEmpty())
                    subresult.put(unit, subreaching);
            }
            aliases.put(local, subresult);
        }
        out.println(aliases);
    }
}
