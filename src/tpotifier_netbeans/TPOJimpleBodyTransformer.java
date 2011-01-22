package tpotifier_netbeans;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import soot.Body;
import soot.jimple.JimpleBody;

class TPOJimpleBodyTransformer extends soot.BodyTransformer {

    public static final String PhaseName = "jtp.replaceTPOs";
    public static final String ProxyGetInstanceMethodName = "GetInstance";
    public static final String ProxySetInstanceMethodName = "SetInstance";
    
    private static final long serialVersionUID = 237;

    private final TPOJimpleBodyTransformerOptions options;
    //
    TPOJimpleBodyTransformer (final TPOJimpleBodyTransformerOptions _options) {
        options = _options;
    }

    @Override
    protected void internalTransform (final Body b, final String phaseName, final Map options) {
        final boolean contains_enabled = options.containsKey("enabled");
        final Object enabledValue = options.get("enabled");
        final boolean enabledIsTrue = ((String) enabledValue).equals("true");
        final boolean phaseNameIsCorrect = phaseName.equals(PhaseName);
        final boolean bodyIsJimple = b instanceof soot.jimple.JimpleBody;
        if (contains_enabled && enabledIsTrue && phaseNameIsCorrect && bodyIsJimple) {
            final Transformer trans = new Transformer((JimpleBody)b);
            applyOptions(trans);
            trans.transform((JimpleBody) b);
        }
        else
            logfine("Transformation not applied to {0} because one of the "
                    + "following did not hold: contains_enabled={1}, enabled"
                    + "IsTrue={2}, phaseNameIsCorrect={3}, bodyIsJimple={4}",
                    b.getMethod().getName(), contains_enabled, enabledIsTrue,
                    phaseNameIsCorrect, bodyIsJimple);
    }
    private static final Logger LOG = Logger.getLogger(TPOJimpleBodyTransformer.class.getName());
    private static void logfine (final String msg, Object... args) {
        LOG.log(Level.FINE, msg, args);
    }

    private void applyOptions (final Transformer trans) {
        for (final String[] optPair: options.options) {
            final String key    = optPair[0];
            final String value  = optPair[1];

            if (key.equals(TPOJimpleBodyTransformerOptions.EXCLUDE_CLASS))
                trans.addClassNotToTransform(value);
            else
            if (key.equals(TPOJimpleBodyTransformerOptions.EXCLUDE_METHOD))
                trans.addMethodNotToTransform(value);
            else
                throw new TPOTransformationException("Unknow option value: "
                        + key);

            trans.setDoNotProcessStaticInitMethods();
        }
    }
}
