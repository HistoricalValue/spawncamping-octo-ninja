package tpotifier_netbeans;

public class Main {
    // Options API
    /** turn transformations on/off */
    public void SetPerformTransformations         (final boolean set) { sootSetuper.SetPerformTransformations(set); }
    /** produce instructions to "get" an object instance from a proxy object */
    public void SetWithGetInstanceCall            (final boolean set) { sootSetuper.SetWithGetInstanceCall(set); }
    /** produce instructions to reference-copy (assign) from an original object to the tmp-object-holder variable */
    public void SetWithObjoAssign                 (final boolean set) { sootSetuper.SetWithObjoAssign(set); }
    /** produce instructions to perform the original operation that was performed on the proxied object */
    public void SetWithOriginalOperationPreserved (final boolean set) { sootSetuper.SetWithOriginalOperationPreserved(set); }
    /** inject diagnostic prints about a) the instance acquired from a proxy object b) the object assigned to the tmp object holder in the end (before the original operation) */
    public void SetWithDiagnosticPrints           (final boolean set) { sootSetuper.SetWithDiagnosticPrints(set); }
    /** produce instructions to call "setinstance" whenever an object is assigned to a proxy object. This is experimental, does not work, and is off by design */
    public void SetWithProxySetInstance           (final boolean set) { sootSetuper.SetWithProxySetInstance(set); }
    //
    public void SetWithJimpleOutput               (final boolean set) { sootSetuper.SetWithJimpleOutput(set); }
    public void SetWithClassOutput                (final boolean set) { sootSetuper.SetWithClassOutput(set); }
    public void SetWithDavaOutput                 (final boolean set) { sootSetuper.SetWithDavaOutput(set); }
    //
    public void ExcludeClass                      (final String klass){ tpojbto.ExcludeClass(klass); }
    /** @param meth something like {@literal < some.Class : retType name ( arg1T, arg2T ) >} */
    public void ExcludeMethod                     (final String meth) { tpojbto.ExcludeMethod(meth); }
    //
    public void SetMainClass                      (final String mc)   { mainClass = mc; }
    public void SetClassToAnalyse                 (final String c2a)  { classToAnalyse = c2a; }
    //
    public void SetApplicationModeOn              ()                  { SootSetuper.SootMode = _SootMode.App; }
    public void SetWholeProgramWithSparkModeOn    ()                  { SootSetuper.SootMode = _SootMode.WholeProgramWithSpark; }
    //
    public void SetProxySuperclass                (final String psc)  { tpojbto.ProxyClass(psc); }
    /** A value class is a class which might be Proxy-ied. */
    public void AddValueClass                     (final String vc)   { tpojbto.ValueClass(vc); }
    //
    /** Output found in Jimple and Class formats under sootOutput/. A kind offer from the Soot framework.
     * @throws NullPointerException if main-class, class-to-be-analysed or execution mode has not been set
     */
    public void PerformTransformation () throws InterruptedException {
        if (mainClass == null)
            throw new NullPointerException("Main-class has not been set");
        if (classToAnalyse == null)
            throw new NullPointerException("Class-to-analyse has not been set");
        if (SootSetuper.SootMode == null)
            throw new NullPointerException("Execution mode has not been set");
        
        final SootSetuper.MainArguments args = sootSetuper. new MainArguments(
                tpojbto,
                mainClass,
                classToAnalyse
                );
        sootSetuper.main(args);
    }

    /**
     * Use this class' public API inside this class' main (or in another one).
     */
    public static void main (final String[] args) throws InterruptedException {
        // A sample main, transforming sample.Sample, excluding the proxy and
        // value types.
        // ---------------------------------------------------------------------
        Main mien = new Main();

        // Exclude the proxy and proxy value types
        mien.ExcludeClass("sample.SharedMemoryTPO");
        mien.ExcludeClass("sample.Foo");

        // Set execution mode
        mien.SetApplicationModeOn();
        
        // Set significant classes
        mien.SetMainClass("sample.Sample");
        mien.SetClassToAnalyse("sample.Sample");
        mien.SetProxySuperclass("sample.SharedMemoryTPO");
        mien.AddValueClass("sample.Foo");

        // DO IT!
        mien.PerformTransformation();
    }








    /////////////// Private -- Do not bother
    private final SootSetuper                       sootSetuper = new SootSetuper();
    private final TPOJimpleBodyTransformerOptions   tpojbto     = new TPOJimpleBodyTransformerOptions();
    private       String                            mainClass;
    private       String                            classToAnalyse;
}
