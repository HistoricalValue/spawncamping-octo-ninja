package tpotifier_netbeans;

import java.util.ArrayList;

@SuppressWarnings("ClassWithoutLogger")
class TPOJimpleBodyTransformerOptions {
    public static final String EXCLUDE_CLASS = "exclude-class";
    public static final String EXCLUDE_METHOD= "exclude-method";
    public static final String PROXY_CLASS   = "proxy-superclass";
    public static final String VALUE_CLASS   = "value-class";

    final ArrayList<String[]> options = new ArrayList<String[]>(20);

    void ExcludeClass (final String className) {
        options.add(new String[] { EXCLUDE_CLASS, className } );
    }

    /**
     *
     * @param methodSig something like {@literal < some.Class : retType name ( arg1T, arg2T) >}
     */
    void ExcludeMethod (final String methodSig) {
        options.add(new String[] { EXCLUDE_METHOD, methodSig } );
    }

    void ProxyClass (final String proxyClass) {
        options.add(new String[] { PROXY_CLASS, proxyClass } );
    }

    void ValueClass (final String valueClass) {
        options.add(new String[] { VALUE_CLASS, valueClass} );
    }
}
