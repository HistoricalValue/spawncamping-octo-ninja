package tpotifier_netbeans;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import soot.Body;
import soot.PatchingChain;
import soot.Unit;

public class TPOTransformer extends soot.BodyTransformer {

    public static final String PhaseName = "jtp.replaceTPOs";
    
    private static final long serialVersionUID = 237;

    private final soot.RefType ProxyType = soot.RefType.v("sample.SingletonTPO");
    
    @Override
    protected void internalTransform (final Body b, final String phaseName, final Map options) {
        final boolean contains_enabled = options.containsKey("enabled");
        final Object enabledValue = options.get("enabled");
        final boolean enabledIsTrue = ((String) enabledValue).equals("true");
        final boolean phaseNameIsCorrest = phaseName.equals(PhaseName);
        if (contains_enabled && enabledIsTrue) {
            final PatchingChain<Unit> units = b.getUnits();
            for (final Unit unit: units) {
            }
            logfine("{0}", b.getLocals());
        }
    }
    private static final Logger LOG = Logger.getLogger(TPOTransformer.class.getName());
    private static void logfine (final String msg, Object... args) {
        LOG.log(Level.FINE, msg, args);
    }
}
