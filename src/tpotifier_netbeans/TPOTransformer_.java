package tpotifier_netbeans;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import sample.util.AssertionError;
import soot.Local;
import soot.PatchingChain;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.SootMethodRef;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.jimple.AddExpr;
import soot.jimple.AssignStmt;
import soot.jimple.CastExpr;
import soot.jimple.Constant;
import soot.jimple.EqExpr;
import soot.jimple.FieldRef;
import soot.jimple.GotoStmt;
import soot.jimple.IfStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InstanceOfExpr;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.NewArrayExpr;
import soot.jimple.NewExpr;
import soot.jimple.NullConstant;
import soot.jimple.StaticFieldRef;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.VirtualInvokeExpr;
import soot.jimple.spark.pag.LocalVarNode;
import soot.jimple.spark.pag.Node;
import soot.jimple.spark.pag.PAG;
import soot.util.Chain;
import soot.util.HashChain;



class Base_ic implements Base_icExpr {
    private VirtualInvokeExpr iie;
    private InstanceFieldRef ifr;
    Base_ic (final VirtualInvokeExpr _iie) { iie = _iie; ifr = null; }
    Base_ic (final InstanceFieldRef _ifr) { ifr = _ifr; iie = null; }

    @Override public void setBase (final Value base) {
        if (base == null)
            throw new NullPointerException();
        
        if (iie != null) {
            assert ifr == null;
            iie.setBase(base);

        }
        else
        if (ifr != null) {
            assert iie == null;
            ifr.setBase(base);
        }
        else {
            assert false;
            throw new AssertionError();
        }
    }
    @Override public Value getBase () {
        if (iie != null) {
            assert ifr == null;
            return iie.getBase();
        }
        if (ifr != null) {
            assert iie == null;
            return ifr.getBase();
        }
        throw new AssertionError(); // TODO handle
    }

    static private Base_ic aBase_ic = new Base_ic((VirtualInvokeExpr) null);
    static Base_ic Base_icFor (final VirtualInvokeExpr b) {
        aBase_ic.iie = b;
        aBase_ic.ifr = null;
        return aBase_ic;
    }
    static Base_ic Base_icFor (final InstanceFieldRef b) {
        aBase_ic.iie = null;
        aBase_ic.ifr = b;
        return aBase_ic;
    }
}










enum AlterationType { Insertion, Replacement, Removal, Appending }
final class Alteration {    
    final AlterationType    type;
    final Unit              point;
    final Chain<Unit>       patch;
    final int               id;
    Alteration (final AlterationType _type, final Unit _point, final Chain<Unit> _patch) {
        type    = _type;
        point   = _point;
        patch   = _patch;
        id      = ids++;
    }

    @Override public String toString () {
        final StringBuilder b = new StringBuilder(4096);
        b   .append("{")
                .append(",id="   ).append(id   )
                .append(",type=" ).append(type )
                .append(",point=").append(point)
                .append(",patch=").append(patch)
            .append("}");
        return b.toString();
    }
    //
    private static int ids = 0;
}










final class Transformer {
    private final static Logger     LOG = Logger.getLogger(Transformer.class.getName());
    //
    private static final RefType    ProxyType                       = RefType.v("sample.SharedMemoryTPO");
    private static final RefType    ValueType                       = RefType.v("sample.Foo");
    private static final RefType    ObjectType                      = RefType.v("java.lang.Object");
    private static final PrintStream    out                         = soot.G.v().out;
    //
    private final Scene             scene                           = Scene.v();
    private final Jimple            jimple                          = Jimple.v();
    //
    private final SootClass         ProxyClass                      = scene.getSootClass("sample.SharedMemoryTPO");
    //
    private final SootMethod        ProxyGetInstanceMethod          = ProxyClass.getMethodByName(TPOJimpleBodyTransformer.ProxyGetInstanceMethodName);
    private final SootMethod        ProxySetInstanceMethod          = ProxyClass.getMethodByName(TPOJimpleBodyTransformer.ProxySetInstanceMethodName);
    private final SootMethod        PrintlnMethod                   = RefType.v("sample.util.P").getSootClass().getMethodByName("println");
    private final SootMethodRef     ProxyGetInstanceMethodRef       = ProxyGetInstanceMethod.makeRef();
    private final SootMethodRef     ProxySetInstanceMethodRef       = ProxySetInstanceMethod.makeRef();
    private final SootMethodRef     PrintlnMethodRef                = PrintlnMethod.makeRef();
    //
    private final List<Alteration>  alterations                     = new LinkedList<Alteration>();
    private final IntConstant       IntConstantZero                 = IntConstant.v(0);
    //
    private final Set<SootClass>    classesNotToTransform           = new HashSet<SootClass>(1024);
    private final Set<SootMethod>   methodsNotToTransform           = new HashSet<SootMethod>(1024);
    private boolean                 doNotProcessStaticInits         = false;



    private boolean isAValueType (final Type type) {
        final boolean result = ValueType.equals(type);
        return result;
    }

    private boolean isAProxyType (final Type type) {
        final boolean result = ProxyType.equals(type);
        return result;
    }



    void setDoNotProcessStaticInitMethods (final boolean doNotProcess) {
        doNotProcessStaticInits = doNotProcess;
    }

    void setDoNotProcessStaticInitMethods () {
        setDoNotProcessStaticInitMethods(true);
    }

    boolean doesNotProcessStaticInitMethods () {
        return doNotProcessStaticInits;
    }

    void addClassNotToTransform (final String klassname) {
        final SootClass klass = scene.getSootClass(klassname);
        if (klass == null)
            throw new TPOTransformationException("Could not find class with name "
                    + klassname);
        if (classesNotToTransform.contains(klass))
            throw new TPOTransformationException("Class " + klass
                    + " already specified not to be transformed");
        classesNotToTransform.add(klass);
    }

    void addMethodNotToTransform (final String methstr) {
        final SootMethod meth = scene.getMethod(methstr);
        if (meth == null)
            throw new TPOTransformationException("Could not find method with signature "
                    + methstr);
        if (methodsNotToTransform.contains(meth))
            throw new TPOTransformationException("Method " + method
                    + " already specified not to be transformed");
        methodsNotToTransform.add(meth);
    }

    private boolean methodShouldBeTransformed (final SootMethod method) {
        final SootClass klass               = method.getDeclaringClass();
        final boolean   classNotToBeTransed = classesNotToTransform.contains(klass);
        final boolean   classNotInScene     = !klass.isInScene();
        final boolean   methodNotToBeTransed= methodsNotToTransform.contains(method);
        final boolean   methodIsStaticInit  = method.getName().equals("<clinit>");
        final boolean   doNotProcessStcInit = doesNotProcessStaticInitMethods();
        final boolean   mustNotTransformed  = false
                || classNotToBeTransed
                || classNotInScene
                || methodNotToBeTransed
                || methodIsStaticInit && doNotProcessStcInit
                ;
        final boolean   result              = !mustNotTransformed;
        return result;
    }



    private static boolean isAnyTaggable (final Iterable<Node> node) {
        return true; // TODO do
    }


    
    private static Collection<? extends Local> getTagged (  final Chain<Local>  locals,
                                                            final SootMethod    method)
    {
         Collection<Local> result;
         
        if (tpotifier_netbeans.SootSetuper.SootMode.equals(tpotifier_netbeans._SootMode.WholeProgramWithSpark)) {
            final PAG                       points  = (PAG) Scene.v().getPointsToAnalysis();
            final Map<Local, Iterable<Node[]>>
                                            aliases = new HashMap<Local, Iterable<Node[]>>(locals.size());
            
            for (final Local local: locals) {
                final LocalVarNode localVarNode = points.findLocalVarNode(local);
                if (localVarNode == null)
                    LOG.log(Level.WARNING, "Local {0} does not have a LocalVarNode from the points-to analysis", local);
                final Node[] allocnodes = points.allocInvLookup(localVarNode);
                final Node[] loadnodes  = points.loadInvLookup(localVarNode);
                final Node[] simplenodes= points.simpleInvLookup(localVarNode);
                final List<Node[]> subrs= new ArrayList<Node[]>(3);
                subrs.add(allocnodes); subrs.add(loadnodes); subrs.add(simplenodes);
                aliases.put(local, subrs);
            }
    
            final Set<Local> tagged = new HashSet<Local>(20);
            for (final Entry<Local, Iterable<Node[]>> entry: aliases.entrySet()) {
                final StringBuilder aliases_str_b = new StringBuilder(1024);
                {
                    final Iterator<Node[]> nodes_ite = entry.getValue().iterator();
                    Node[] nodes;
                    {
                        assert nodes_ite.hasNext();
                        nodes = nodes_ite.next();
                        aliases_str_b.append("AllocInv=");
                        aliases_str_b.append(java.util.Arrays.toString(nodes));
                    }
                    {
                        assert nodes_ite.hasNext();
                        nodes = nodes_ite.next();
                        aliases_str_b.append("LoadInv=");
                        aliases_str_b.append(java.util.Arrays.toString(nodes));
                    }
                    {
                        assert nodes_ite.hasNext();
                        nodes = nodes_ite.next();
                        aliases_str_b.append("SimpleInv=");
                        aliases_str_b.append(java.util.Arrays.toString(nodes));
                    }
                }
                
                final String aliases_str = aliases_str_b.toString();
                out.printf("%s: %s%n", entry.getKey(), aliases_str);
//                if (isAnyTaggable(entry.getValue()))
//                    tagged.add(entry.getKey());
            }

            result = java.util.Collections.unmodifiableCollection(locals);
        }
        else
            result = java.util.Collections.unmodifiableCollection(locals);

         // overwrite all
         result = new HashSet<Local>(locals.size());
         for (final Local local: locals)
             if (local.getType() instanceof RefType)
                 result.add(local);
         return result;
    }



    private static boolean localsContainLocalNamed (final Iterable<Local> locals, final String name) {
        for (final Local local: locals)
            if (name.equals(local.getName()))
                return true;
        return false;
    }

    private static String getUniqueLocalName (final Iterable<Local> locals, final String basename) {
        String result;
        for (int i = 0; localsContainLocalNamed(locals, result = basename + i); ++i)
            {}
        return result;
    }

    private static Local createAndAddLocal (final Jimple jimple, final Chain<Local> locals, final String basename, final Type type) {
        final String    name    = getUniqueLocalName(locals, basename);
        final Local     result  = jimple.newLocal(name, type);
        locals.add(result);
        return result;
    }

    private static Local createAndAddFlagLocal (final Jimple jimple, final Chain<Local> locals) {
        return createAndAddLocal(jimple, locals, "$larkness", soot.BooleanType.v());
    }



    //
    private final SootMethod                    method;
    private final Chain<Local>                  locals;
    private final PatchingChain<Unit>           units;
    private final Chain<Unit>                   units_nonpatching;
    private final Collection<? extends Local>   taggedLocals;
    private final Local                         isInstanceFlagLocal;
    private final EqExpr                        isInstExpr;
    private final Map<Type, Local>              holdersLocalsForType = new HashMap<Type, Local>(50);
    private final Map<Local, Type>              originalTypes = new HashMap<Local, Type>(50);
    private final Map<SootField, Type>          originalStaticFieldsTypes = new HashMap<SootField, Type>(50);



    Transformer (final JimpleBody _body) {
        method              = _body.getMethod();
        locals              = _body.getLocals();
        units               = _body.getUnits();
        units_nonpatching   = units.getNonPatchingChain();
        taggedLocals        = getTagged(locals, method);
        isInstanceFlagLocal = createAndAddFlagLocal(jimple, locals);
        isInstExpr          = jimple.newEqExpr(isInstanceFlagLocal, IntConstantZero);
    }

    private boolean tagged_contains (final Local local) {
        return taggedLocals.contains(local);
    }

    private static String getBasenameForType (final Type type) {
        if (type instanceof RefType) {
            final RefType reftype = (RefType) type;
            final String typestr = reftype.getClassName();
            final String result = "__" + typestr.substring(typestr.lastIndexOf(".") + 1);
            return result;
        }
        throw new TPOTransformationException("trying to get a tmp local for a non-ref type "
                + type + " : " + type.getClass());
    }

    private Local getLocalForType (final Type type) {
        final String basename = getBasenameForType(type);
        if (!holdersLocalsForType.containsKey(type))
            holdersLocalsForType.put(type, createAndAddLocal(jimple, locals, basename, type));
        return holdersLocalsForType.get(type);
    }

    private Type getOriginalTypeFor (final Local local) {
        Type result;
        if (originalTypes.containsKey(local)) {
            assert tagged_contains(local);
            result = originalTypes.get(local);
        }
        else
            result = local.getType();
        return result;
    }

    private void objectifyTaggedLocal (final Local tagged) {
        final Type type = tagged.getType();
        if (type instanceof RefType) {
            assert tagged_contains(tagged);
            assert !originalTypes.containsKey(tagged);
            originalTypes.put(tagged, tagged.getType());
            tagged.setType(ObjectType);
        }
        else
            throw new TPOTransformationException("Tagged of non-ref type passed as tagged for type change: "
                    + tagged + " : " + tagged.getClass() + " // type: "
                    + type + " : " + type.getClass());
    }



    void transform ( final JimpleBody body) {
        if (SootSetuper.PerformTransformations) {
            final SootMethod body_method = body.getMethod();
            if (methodShouldBeTransformed(body_method)) {
                //
                out.printf( "[%s]%n"
                        +   "locals = %s%n"
                        +   "tagged = %s%n",
                        method,
                        locals,
                        taggedLocals
                        );

                // Change the type of all tagged vars to Object
                for (final Local taggedLocal: taggedLocals)
                    objectifyTaggedLocal(taggedLocal);

                // Perform transformations on operations on tagged locals
                for (final Unit unit: units)
                    if (unit instanceof InvokeStmt)
                        produceInvokeStmtPatch((InvokeStmt) unit);
                    else
                        if (unit instanceof AssignStmt)
                            produceAssignStmtPatch((AssignStmt) unit);
                        else;

                applyAlterations();
            }
            else
                LOG.log(Level.FINE, "Ignoring method {0}", body_method);
        }
        else
            LOG.log(Level.FINE, "Not performing any transformations (not even for method {0})",
                    method);
    }



    private void produceAssignStmtPatch (final AssignStmt stmt) {
        final Value lhs = stmt.getLeftOp();
        final Value rhs = stmt.getRightOp();
        //
        final HashChain<Unit> lhsTaggedLocalPatching;
        final Stmt lastStmt;
        //
        boolean lhsIdentified;
        //
        // Object-ref-assign (o = v)
        if (tpotifier_netbeans.SootSetuper.withProxySetInstance && lhs instanceof Local && !(rhs instanceof NewExpr)) {
            final Local local = (Local) lhs;
            if (tagged_contains(local)) {
                final Local methResultHolderLocal = getLocalForType(getOriginalTypeFor(local));
                stmt.setLeftOp(methResultHolderLocal);
                //
                final Local proxyHolderLocal = getLocalForType(ProxyType);

                // Patch to be added at the end
                lhsTaggedLocalPatching = new HashChain<Unit>();

                // -1: methTmp = rhs // done by replacing lhs on the assign stmt
                // Make patch:
                // 0: flag = local instanceof ProxyT
                // 1: if flag == 0 goto 5
                // 2: proxyHolder = (ProxyT) local
                // 3: proxyHolder.SetInstance(methtmp)
                // 4: goto 6
                // 5: local = (LocalType) methtmp
                // 6: noop
                //
                // flag = instaceof
                lhsTaggedLocalPatching.add(jimple.newAssignStmt(
                        isInstanceFlagLocal,
                        jimple.newInstanceOfExpr(local, ProxyType)));
                //
                // ... then: proxyHolder = (ProxyType) local; proxyHolder.Set(methTmP)
                final Stmt assignToProxyHolder = jimple.newAssignStmt(
                        proxyHolderLocal,
                        jimple.newCastExpr(local, ProxyType));
                final Stmt invokeSetInstance = jimple.newInvokeStmt(
                        jimple.newVirtualInvokeExpr(
                                proxyHolderLocal,
                                ProxySetInstanceMethodRef,
                                methResultHolderLocal));
                //
                // ... else: local = (LocalType) methTmp
                final Stmt assignSetting = jimple.newAssignStmt(
                        local,
                        jimple.newCastExpr(
                                methResultHolderLocal,
                                getOriginalTypeFor(local)));
                //
                //
                final Stmt afterElse = jimple.newNopStmt();

                // if (instanceof)
                lhsTaggedLocalPatching.add(
                        jimple.newIfStmt(
                                jimple.newEqExpr(
                                        isInstanceFlagLocal,
                                        IntConstantZero),
                                assignSetting));
                lhsTaggedLocalPatching.add(assignToProxyHolder);
                lhsTaggedLocalPatching.add(invokeSetInstance);
                lhsTaggedLocalPatching.add(jimple.newGotoStmt(afterElse));
                lhsTaggedLocalPatching.add(assignSetting);
                lhsTaggedLocalPatching.add(afterElse);
            }
            else
                lhsTaggedLocalPatching = null;
            lhsIdentified = true;
        }
        else {
            lhsTaggedLocalPatching = null;
            lhsIdentified = false;
        }

        //
        // Field-write (o.x = v)
        if (lhs instanceof FieldRef) {
            assert !(lhs instanceof Local);
            assert rhs instanceof Local || rhs instanceof Constant;
            // Object-ref-assign (o = v)  must not have happened here
            assert lhs.equals(stmt.getLeftOp());
            produceFieldRefReplacementPatch(stmt, (FieldRef) lhs);
            lastStmt = stmt;
            lhsIdentified = true;
        }
        else
        // Field-read (... o.x ...)
        if (rhs instanceof FieldRef) {
            assert lhs instanceof Local;
            produceFieldRefReplacementPatch(stmt, (FieldRef) rhs);
            lastStmt = stmt;
            lhsIdentified = false;
        }
        else
        // Field-invoke (... o.m(...) ...)
        if (rhs instanceof InvokeExpr) {
            assert lhs instanceof Local;
            produceInvokeExprReplacementPatch(stmt, (InvokeExpr) rhs);
            lastStmt = stmt;
            lhsIdentified = false;
        }
        else
        // Suspicious cast ( ... (ProxyType) ValueTypeObj ... )
        if (rhs instanceof CastExpr) {
            assert lhs instanceof Local;
            // if rhs is a local (of ValueType) which is tagged
            // (could point to a proxy), and the cast type is ProxyType,
            // then this cast has to be eliminated
            final CastExpr cast = (CastExpr) rhs;
            final Value base = cast.getOp();
            if (base instanceof Local) {
                final Local     local               = (Local) base;
                final boolean   taggedContainsLocal = tagged_contains(local);
                final Type      castType            = cast.getCastType();
                final boolean   castTypeIsValueType = isAValueType(castType);
                final Type      baseType            = getOriginalTypeFor(local);
                final boolean   baseTypeIsProxyType = isAProxyType(baseType);
                if (taggedContainsLocal && castTypeIsValueType && baseTypeIsProxyType) {
                    final HashChain<Unit> patch = new HashChain<Unit>();
                    lastStmt = jimple.newAssignStmt(stmt.getLeftOp(), local);
                    patch.add(lastStmt);
                    addReplacementAlteration(stmt, patch);
                }
                else
                    lastStmt = stmt;
            }
            else
                throw new TPOTransformationException("base of cast expr is expected to be a Local. We got: "
                        + base + " : " + base.getClass());

            lhsIdentified = false;
        }
        else if (   rhs instanceof NewExpr          || rhs instanceof Local         ||
                    rhs instanceof InstanceOfExpr   || rhs instanceof NullConstant  ||
                    rhs instanceof NewArrayExpr     || rhs instanceof AddExpr
        ) {
        // it'sok
            lastStmt = stmt;
            lhsIdentified = false;
        }
        else
            throw new TPOTransformationException("What kind of assignment is dis!!! >8-| "
                    + stmt + " : " + stmt.getClass()
                    + " left: " + stmt.getLeftOp().getClass()
                    + "right: " + stmt.getRightOp().getClass());

        if (!lhsIdentified)
            if (lhs instanceof Local)
                {} // itsok
            else
                throw new TPOTransformationException("What kind of LHS IS DIS!!! "
                        + lhs + " : " + lhs.getClass());

        if (lhsTaggedLocalPatching != null)
            addAppendingAlteration(lastStmt, lhsTaggedLocalPatching);
    }

    private void producePatchForBaseReplacement (final Stmt jump2stmt, final Base_icExpr base_ic) {
        final Value base = base_ic.getBase();
        if (base instanceof Local) {
            final Local local = (Local) base;
            assert tagged_contains(local);
            final Local objHolder = getLocalForType(getOriginalTypeFor(local));
            base_ic.setBase(objHolder);
            final HashChain<Unit> patch = generatePatchForProxiedObjectOperation(local, objHolder, jump2stmt);
            final int altid =
                    tpotifier_netbeans.SootSetuper.withOriginalOperationPreserved ?
                            addInsertionAlteration(jump2stmt, patch) :
                            addReplacementAlteration(jump2stmt, patch);
            LOG.log(Level.FINE, "alteration id={0} produced by stmt: {1}",
                    new Object[]{altid, jump2stmt});
        }
        else
            throw new RuntimeException("base is expected to be a local. We got:"
                    + base + " : " + base.getClass());
    }

    private void produceFieldRefReplacementPatch (final Stmt jump2stmt, final FieldRef fr) {
//        assert jump2stmt.getUseBoxes().contains(jimple.newVariableBox(fr));
        if (fr instanceof InstanceFieldRef)
            producePatchForBaseReplacement(jump2stmt, Base_ic.Base_icFor((InstanceFieldRef) fr));
        else if (fr instanceof StaticFieldRef)
            {} // it'sok
        else
            throw new TPOTransformationException("What kind of Field Ref is dis!!: "
                    + fr + " : " + fr.getClass());
    }
     private void produceInvokeExprReplacementPatch (final Stmt jump2stmt, final InvokeExpr invokeExpr) {
//        assert jump2stmt.getUseBoxes().contains(jimple.newInvokeExprBox(invokeExpr));
        if (invokeExpr instanceof InstanceInvokeExpr) {
            if (invokeExpr instanceof VirtualInvokeExpr)
                producePatchForBaseReplacement(jump2stmt, Base_ic.Base_icFor((VirtualInvokeExpr) invokeExpr));
        }
        else if (invokeExpr instanceof StaticInvokeExpr)
            {} // itsok
        else
            throw new TPOTransformationException("What kind of invokation expression is dis!!: "
                    + invokeExpr + " : " + invokeExpr.getClass());
    }



    private void produceInvokeStmtPatch (final InvokeStmt stmt) {
        produceInvokeExprReplacementPatch(stmt, stmt.getInvokeExpr());
    }



    private void applyAlterations () {
        for (final Alteration alteration: alterations) {
            if (!units_nonpatching.contains(alteration.point))
                throw new TPOTransformationException("What alteration is dis!! >8-| "
                        + alteration + " : " + alteration.getClass());
            try {
                final AlterationType type = alteration.type;
                final Unit point = alteration.point;
                //
                final boolean doPatching = (
                        type == AlterationType.Appending    ||
                        type == AlterationType.Insertion    ||
                        type == AlterationType.Replacement
                        );
                final boolean doAppending = type == AlterationType.Appending;
                final boolean doRemovePoint = (
                        type == AlterationType.Removal  ||
                        type == AlterationType.Replacement
                        );
                //
                assert !(type == AlterationType.Removal) || alteration.patch == null;
                //
                if (doPatching)
                    if (doAppending)
                        units_nonpatching.insertAfter(alteration.patch, point);
                    else
                        units_nonpatching.insertBefore(alteration.patch, point);
                if (doRemovePoint)
                    units.remove(point);
            }
            catch (final Exception ex) {
                final String message = "Applying alteration \""
                        + alteration + "\" failed because of exception. Cause attached.";
                LOG.log(Level.SEVERE, message);
                throw new TPOTransformationException(message, ex);
            }
        }
    }



    /** @return new alteration's id */
    private int __addAlteration (final AlterationType type, final Unit point, final Chain<Unit> patch) {
        final Alteration alteration = new Alteration(type, point, patch);
        final boolean added = alterations.add(alteration);
        assert added;
        return alteration.id;
    }

    private int addReplacementAlteration (final Unit point, final Chain<Unit> patch) {
        if (point == null || patch == null)
            throw new NullPointerException();
        return __addAlteration(AlterationType.Replacement, point, patch);
    }

    private int addInsertionAlteration (final Unit point, final Chain<Unit> patch) {
        if (point == null || patch == null)
            throw new NullPointerException();
        return __addAlteration(AlterationType.Insertion, point, patch);
    }

    private int addRemovalAlteration (final Unit point) {
        if (point == null)
            throw new NullPointerException();
        return __addAlteration(AlterationType.Removal, point, null);
    }

    private int addAppendingAlteration (final Unit point, final Chain<Unit> patch) {
        if (point == null || patch == null)
            throw new NullPointerException();
        return __addAlteration(AlterationType.Appending, point, patch);
    }



    private HashChain<Unit> generatePatchForProxiedObjectOperation (final Local local, final Local objHolder, final Unit opOnObj) {
        //
        // printings
        // ---
        // printBeforeGetInstanceStmt       : println("before calling GetInstance for local ")
        // printBeforeAssignmentStmt        : println("before plain assignment for local")
        final Stmt printBeforeGetInstanceStmt               = tpotifier_netbeans.SootSetuper.withDiagnosticPrints ?
                jimple.newInvokeStmt(jimple.newStaticInvokeExpr(PrintlnMethodRef, StringConstant.v("Before calling GetInstance for local " + local))):
                jimple.newNopStmt();
        final Stmt printBeforeAssignmentStmt                = tpotifier_netbeans.SootSetuper.withDiagnosticPrints ?
                jimple.newInvokeStmt(jimple.newStaticInvokeExpr(PrintlnMethodRef, StringConstant.v("Befoer plain assignment for local " + local))):
                jimple.newNopStmt();
        final Stmt gotoPrintBeforeGetInstanceStmt           = jimple.newGotoStmt(printBeforeGetInstanceStmt);
        final Stmt gotoPrintBeforeAssignmentStmt            = jimple.newGotoStmt(printBeforeAssignmentStmt);
        //
        // larkness  = (local instanceof ProxyT)
        final InstanceOfExpr    flagExpr                    = jimple.newInstanceOfExpr(local, ProxyType);
        final AssignStmt        flagAssignmentStmt          = jimple.newAssignStmt(isInstanceFlagLocal, flagExpr);
        //
        // ... else -> objectHolder = proxy.getInstance()
        // proxyHolderAssignmentStmt        : proxyHolder = (ProxyType) local
        final Local             objectHolder                = getLocalForType(ObjectType);
        final Local             proxyHolder                 = getLocalForType(ProxyType);
        final Stmt              proxyHolderAssignmentStmt   = jimple.newAssignStmt(proxyHolder, jimple.newCastExpr(local, ProxyType));
        // (getInstExpr: proxyHolder.GetInstance())
        final Value             getInstExpr                 = tpotifier_netbeans.SootSetuper.withGetInstanceCall ?
                jimple.newVirtualInvokeExpr(proxyHolder, ProxyGetInstanceMethodRef) :
                NullConstant.v();
        // getInstanceResultAssignment      : objectHolder = proxyHolder.GetInstance()
        final Stmt              getInstanceResultAssignment = jimple.newAssignStmt(objectHolder, getInstExpr);
        // objaAsgndProxyStmt               : instHolder = (LocalType) objectHolder
        final Stmt              objaAsgndProxyStmt          = jimple.newAssignStmt(objHolder, jimple.newCastExpr(objectHolder, getOriginalTypeFor(objHolder)));
        // goto end-of-if (opOnObj)
        final GotoStmt          gotoOpOnObj                 = jimple.newGotoStmt(opOnObj);
        //
        // ... then ->
        // instHolder = local
        final Stmt              objoAsgndLocalStmt  = tpotifier_netbeans.SootSetuper.withObjoAssign ?
                jimple.newAssignStmt(objHolder, jimple.newCastExpr(local, getOriginalTypeFor(objHolder))):
                jimple.newNopStmt();
        //
        // if ...
        // if larkness == 0 (!local instanceof ProxyT) then holder = obj
        final IfStmt            ifInstOfStmt        = jimple.newIfStmt(isInstExpr, printBeforeAssignmentStmt);
        //
        // print $objo
        final Stmt              printObjo           = tpotifier_netbeans.SootSetuper.withDiagnosticPrints ?
                jimple.newInvokeStmt(jimple.newStaticInvokeExpr(PrintlnMethodRef, objHolder)):
                jimple.newNopStmt();
        //
        //
        // flagAssignmentStmt       : larkness = (local instanceof ProxyT)
        // ifInstOfStmt             : if larkness == 0 goto objoAsgndLocalStmt
        // objaAsgndProxyStmt       : objectHolder = proxy.getInstance()
        // gotoObjoInvoke           : goto opOnObj
        // objoAsgndLocalStmt       : objectHolder = local
        // opOnObj                  : ... [ f(objectHolder) ]
        final HashChain<Unit> patch = new HashChain<Unit>();
                                                            // === begin if-else
        patch.addLast(flagAssignmentStmt);                  // larkness = (local instanceof ProxyT)
        patch.addLast(ifInstOfStmt);                        // if larkness == 0 goto printBeforeAssignmentStmt
                                                            // === then
        patch.addLast(printBeforeGetInstanceStmt);          // println("before calling GetInstance for local ")
        patch.addLast(proxyHolderAssignmentStmt);           // proxyHolder = (ProxyType) local
        patch.addLast(getInstanceResultAssignment);         // objectHolder = proxyHolder.GetInstance()
        if (tpotifier_netbeans.SootSetuper.withDiagnosticPrints) {
            patch.addLast(jimple.newInvokeStmt(jimple.newStaticInvokeExpr(PrintlnMethodRef, StringConstant.v("Proxy getinstance() result"))));
            patch.addLast(jimple.newInvokeStmt(jimple.newStaticInvokeExpr(PrintlnMethodRef, objectHolder)));
        }
        patch.addLast(objaAsgndProxyStmt);                  // instHolder = (LocalType) objectHolder
        patch.addLast(jimple.newGotoStmt(printObjo));       // goto (after-else) print instHolder
                                                            // === else
        patch.addLast(printBeforeAssignmentStmt);           // println("before plain assignment for local")
        patch.addLast(objoAsgndLocalStmt);                  // instHolder = local
                                                            // === end if-else
        patch.addLast(printObjo);                           // println(instHolder)
//        patch.addLast(opOnObj);
        return patch;
    }
    
    private HashChain<Unit> generatePatchForProxiedObjectOperation(final Local local, final Local objHolder) {
        final Unit              noop    = jimple.newNopStmt();
        final HashChain<Unit>   patch   = generatePatchForProxiedObjectOperation(local, objHolder, noop);
                                patch.add(noop);
        return patch;
    }
}