package tpotifier_netbeans;

import java.io.PrintStream;
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
import java.util.regex.Pattern;
import sample.util.AssertionError;
import soot.Body;
import soot.Local;
import soot.PatchingChain;
import soot.PointsToSet;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.SootMethodRef;
import soot.Type;
import soot.Unit;
import soot.UnitBox;
import soot.Value;
import soot.ValueBox;
import soot.jimple.AssignStmt;
import soot.jimple.CastExpr;
import soot.jimple.EqExpr;
import soot.jimple.FieldRef;
import soot.jimple.GotoStmt;
import soot.jimple.IdentityRef;
import soot.jimple.IfStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InstanceOfExpr;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.NewExpr;
import soot.jimple.Ref;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;
import soot.jimple.ThisRef;
import soot.jimple.VirtualInvokeExpr;
import soot.jimple.internal.InvokeExprBox;
import soot.jimple.internal.StmtBox;
import soot.jimple.spark.pag.GlobalVarNode;
import soot.jimple.spark.pag.LocalVarNode;
import soot.jimple.spark.pag.Node;
import soot.jimple.spark.pag.PAG;
import soot.jimple.spark.pag.VarNode;
import soot.util.Chain;
import soot.util.HashChain;


class Base_ic implements Base_icExpr {
    private VirtualInvokeExpr iie;
    private InstanceFieldRef ifr;
    Base_ic (final VirtualInvokeExpr _iie) { iie = _iie; ifr = null; }
    Base_ic (final InstanceFieldRef _ifr) { ifr = _ifr; iie = null; }

    @Override public void setBase (final Value base) {
        if (iie != null) iie.setBase(base); else
        if (ifr != null) ifr.setBase(base);
    }
    @Override public Value getBase () {
        if (iie != null) return iie.getBase(); else
        if (ifr != null) return ifr.getBase(); else
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

enum AlterationType { Insertion, Replacement, Removal }
final class Alteration {    
    final AlterationType type;
    final Unit point;
    final Chain<Unit> patch;
    final int id;
    Alteration (final AlterationType _type, final Unit _point, final Chain<Unit> _patch) {
        type = _type;
        point = _point;
        patch = _patch;
        id = ids++;
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
    private final Scene             scene                           = Scene.v();
    private final SootClass         ProxyClass                      = scene.getSootClass("sample.SharedMemoryTPO");
    private static final RefType    ProxyType                       = RefType.v("sample.SharedMemoryTPO");
    private static final RefType    ValueType                       = RefType.v("sample.Foo");
    private static final RefType    ObjectType                      = RefType.v("jaba.lang.Object");
    private final SootMethod        ProxyGetInstanceMethod          = ProxyClass.getMethodByName(TPOJimpleBodyTransformer.ProxyGetInstanceMethodName);
    private final SootMethod        ProxySetInstanceMethod          = ProxyClass.getMethodByName(TPOJimpleBodyTransformer.ProxySetInstanceMethodName);
    private final PrintStream       out                             = soot.G.v().out;
    private final Jimple            jimple                          = Jimple.v();
    private final List<Alteration>  alterations                     = new LinkedList<>();
    private final IntConstant       IntConstantZero                 = IntConstant.v(0);
    //
    private boolean isAValueType (final Type type) {
        final boolean result = ValueType.equals(type);
        return result;
    }
    private boolean isAProxyType (final Type type) {
        final boolean result = ProxyType.equals(type);
        return result;
    }

    private boolean isAnyTaggable (final Iterable<Node> node) {
        return true; // TODO do
    }

    private static Collection<? extends Local> getTagged (  final Chain<Local>  locals,
                                                            final SootMethod    method)
    {
        if (tpotifier_netbeans.Main.SootMode.equals(tpotifier_netbeans.Main._SootMode.WholeProgramWithSpark)) {
            final PAG                       points  = (PAG) Scene.v().getPointsToAnalysis();
            final Map<Local, Iterable<Node>>
                                            aliases = new HashMap<>(locals.size());
        //        final Iterator<?> varNodesObjectsIte = points.getVarNodeNumberer().iterator();
    //        if (varNodesObjectsIte.hasNext())
    //            for (Object varNodeObject = varNodesObjectsIte.next();
    //                 varNodesObjectsIte.hasNext();
    //                 varNodeObject = varNodesObjectsIte.next()
    //             ) {
    //                if (varNodeObject instanceof LocalVarNode) {
    //                    final LocalVarNode localVarNode = (LocalVarNode) varNodeObject;
    //                    final SootMethod nodeMethod = localVarNode.getMethod();
    //                    if (method.equals(nodeMethod)) {
    //                        final Object varObject = localVarNode.getVariable();
    //                        out.println(varObject);
    //                    }
    //                }
    //            }

    //        for (final Local local: locals) {
    //            final PointsToSet subshit = points.reachingObjects(local);
    //
    //            final LocalVarNode localVarNode = points.findLocalVarNode(local);
    //            final Node[] nodes = points.allocInvLookup(localVarNode);
    //            final List<Node> subresult = java.util.Arrays.asList(nodes);
    //            aliases.put(local, subresult);
    //        }
    //
    //        final Set<Local> tagged = new HashSet<>(20);
    //        for (final Entry<Local, Iterable<Node>> entry: aliases.entrySet()) {
    //            out.printf("%s: %s%n", entry.getKey(), entry.getValue());
    //            if (isAnyTaggable(entry.getValue()))
    //                tagged.add(entry.getKey());
    //        }

            return java.util.Collections.unmodifiableCollection(locals);
        }
        else
            return java.util.Collections.unmodifiableCollection(locals);
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
    private static Local createAndAddFlagLocal (final Jimple jimple, final Chain<Local> locals) {
        final String        flagName    = getUniqueLocalName(locals, "$larkness");
        final Local         result      = jimple.newLocal(flagName, soot.BooleanType.v());
        locals.add(result);
        return result;
    }
    private static Local createAndAddHolderLocal (final Jimple jimple, final Chain<Local> locals) {
        final String        localName   = getUniqueLocalName(locals, "$objo");
        final Local         result      = jimple.newLocal(localName, ProxyType);
        locals.add(result);
        return result;
    }
    private static Local createAndAddTmpHolderLocal (final Jimple jimple, final Chain<Local> locals) {
        final String        localName   = getUniqueLocalName(locals, "$methtmp");
        final Local         result      = jimple.newLocal(localName, ObjectType);
        locals.add(result);
        return result;
    }
    //
//    private final JimpleBody                    body;
    private final SootMethod                    method;
    private final Chain<Local>                  locals;
    private final PatchingChain<Unit>           units;
    private final Chain<Unit>                   units_nonpatching;
    private final Collection<? extends Local>   tagged;
    private final Local                         isInstanceFlagLocal;
    private final Local                         internalObjectHolder;
    private final Local                         tmpMethodResultHolder;
    private final EqExpr                        isInstExpr;
    Transformer (final JimpleBody _body) {
//        body                = _body;
        method              = _body.getMethod();
        locals              = _body.getLocals();
        units               = _body.getUnits();
        units_nonpatching   = units.getNonPatchingChain();
        tagged              = getTagged(locals, method);
        isInstanceFlagLocal = createAndAddFlagLocal(jimple, locals);
        internalObjectHolder= createAndAddHolderLocal(jimple, locals);
        tmpMethodResultHolder=createAndAddTmpHolderLocal(jimple, locals);
        isInstExpr          = jimple.newEqExpr(isInstanceFlagLocal, IntConstantZero);
    }
    private boolean tagged_contains (final Local local) {
        return tagged.contains(local);
    }


    void transform ( final JimpleBody body) {
        //
        out.printf( "[%s]%n"
                +   "locals = %s%n"
                +   "tagged = %s%n",
                method,
                locals,
                tagged
                );

        for (final Unit unit: units)
            if (unit instanceof InvokeStmt)
                produceInvokeStmtPatch((InvokeStmt) unit);
            else
            if (unit instanceof AssignStmt)
                produceAssignStmtPatch((AssignStmt) unit);

        applyAlterations();
    }

    private void produceAssignStmtPatch (final AssignStmt stmt) {
        final Value lhs = stmt.getLeftOp();
        final Value rhs = stmt.getRightOp();
        //
        final HashChain<Unit> lhsTaggedLocalPatching;
        //
        // Field-write (o.x = v)
        if (lhs instanceof Local) {
            final Local local = (Local) lhs;
            if (tagged_contains(local)) {
                stmt.setLeftOp(tmpMethodResultHolder);

                // Patch to be added at the end
                lhsTaggedLocalPatching = new HashChain<>();

                // Make patch
                //
                // flag = instaceof
                lhsTaggedLocalPatching.add(jimple.newAssignStmt(isInstanceFlagLocal,
                            jimple.newInstanceOfExpr(local, ProxyType)));
                // ... then: local.SetInstance(tmpResultHolder)
                final Stmt invokeSetting = jimple.newInvokeStmt(
                        jimple.newVirtualInvokeExpr(
                                local,
                                ProxySetInstanceMethod.makeRef(),
                                tmpMethodResultHolder));

                // ... else
                final Stmt assignSetting = jimple.newAssignStmt(local, tmpMethodResultHolder);

                //
                final Stmt afterElse = jimple.newNopStmt();

                // if (instanceof)
                lhsTaggedLocalPatching.add(jimple.newIfStmt(jimple.newEqExpr(isInstanceFlagLocal, IntConstantZero), assignSetting));
                lhsTaggedLocalPatching.add(invokeSetting);
                lhsTaggedLocalPatching.add(jimple.newGotoStmt(afterElse));
                lhsTaggedLocalPatching.add(assignSetting);
                lhsTaggedLocalPatching.add(afterElse);
            }
            else
                lhsTaggedLocalPatching = null;
        }
        else
            lhsTaggedLocalPatching = null;

        //
        if (lhs instanceof FieldRef) {
            assert !(lhs instanceof Local);
            assert rhs instanceof Local;
            produceFieldRefReplacementPatch(stmt, (FieldRef) lhs);
        }
        else
        if (rhs instanceof FieldRef) {
            assert lhs instanceof Local;
            produceFieldRefReplacementPatch(stmt, (FieldRef) rhs);
        }
        else
        if (rhs instanceof InvokeExpr) {
            assert lhs instanceof Local;
            if (rhs instanceof VirtualInvokeExpr)
                produceInvokeExprReplacementPatch(stmt, (VirtualInvokeExpr) rhs);
        }
        else if (rhs instanceof CastExpr) {
            assert lhs instanceof Local;
            // if rhs is a local which is tagged (could point to a proxy)
            // then this cast has to be eliminated
            final CastExpr cast = (CastExpr) rhs;
            final Value base = cast.getOp();
            if (base instanceof Local) {
                final Local local = (Local) base;
                final boolean taggedContainsLocal = tagged_contains(local);
                final Type castType = cast.getCastType();
                final boolean castTypeIsValueType = isAValueType(castType);
                final Type baseType = local.getType();
                final boolean baseTypeIsProxyType = isAProxyType(baseType);
                if (taggedContainsLocal && castTypeIsValueType && baseTypeIsProxyType) {
                    final HashChain<Unit> patch = new HashChain<>();
                    patch.add(jimple.newAssignStmt(lhs, local));
                    addReplacementAlteration(stmt, patch);
                }
            }
            else
                throw new TPOTransformationException("base of cast expr is expected to be a Local. We got: "
                        + base + " : " + base.getClass());
        }
        else if (rhs instanceof NewExpr || rhs instanceof Local || rhs instanceof InstanceOfExpr)
            {} // it'sok
        else
            throw new TPOTransformationException("What kind of assignment is dis!!! >8-| "
                    + stmt + " : " + stmt.getClass());

        if (lhsTaggedLocalPatching != null)
            addAppendingAlteration(stmt, lhsTaggedLocalPatching);
    }
    private void producePatchForBaseReplacement (final Stmt jump2stmt, final Base_icExpr base_ic) {
        final Value base = base_ic.getBase();
        if (base instanceof Local) {
            final Local local = (Local) base;
            assert tagged_contains(local);
            base_ic.setBase(internalObjectHolder);
            final HashChain<Unit> patch = generatePatchForProxiedObjectOperation(local, jump2stmt);
            final int altid = addInsertionAlteration(jump2stmt, patch);
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
        else
            throw new TPOTransformationException("What kind of invokation expression is dis!!: "
                    + invokeExpr + " : " + invokeExpr.getClass());
    }

    private void produceInvokeStmtPatch (final InvokeStmt stmt) {
        final InvokeExpr ie  = stmt.getInvokeExpr();
        produceInvokeExprReplacementPatch(stmt, ie);
    }

    private void applyAlterations () {
        for (final Alteration alteration: alterations) {
            try {
                final AlterationType type = alteration.type;
                final Unit point = alteration.point;
                //
                final boolean doPatching = type != AlterationType.Removal;
                final boolean doRemovePoint = type != AlterationType.Insertion;
                //
                assert !(type == AlterationType.Removal) || alteration.patch == null;
                //
                if (doPatching)
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
        return __addAlteration(AlterationType.Replacement, point, patch);
    }
    private int addInsertionAlteration (final Unit point, final Chain<Unit> patch) {
        return __addAlteration(AlterationType.Insertion, point, patch);
    }
    private int addRemovalAlteration (final Unit point) {
        return __addAlteration(AlterationType.Removal, point, null);
    }

    private HashChain<Unit> generatePatchForProxiedObjectOperation(final Local local, final Unit opOnObj) {
        //
        // larkness  = (local instanceof ProxyT)
        final InstanceOfExpr    flagExpr            = jimple.newInstanceOfExpr(local, ProxyType);
        final AssignStmt        flagAssignmentStmt  = jimple.newAssignStmt(isInstanceFlagLocal, flagExpr);
        //
        // ... else ->
        // objectHolder = proxy.getInstance()
        final VirtualInvokeExpr getInstExpr         = jimple.newVirtualInvokeExpr(local, ProxyGetInstanceMethod.makeRef());
        final AssignStmt        objaAsgndProxyStmt  = jimple.newAssignStmt(internalObjectHolder, getInstExpr);
        // goto end-of-if (opOnObj)
        final GotoStmt          gotoOpOnObj         = jimple.newGotoStmt(opOnObj);
        //
        // ... then ->
        // objectHolder = local
        final AssignStmt        objoAsgndLocalStmt  = jimple.newAssignStmt(internalObjectHolder, local);
        //
        // if ...
        // if larkness == 0 (!local instanceof ProxyT) then holder = obj
        final IfStmt            ifInstOfStmt        = jimple.newIfStmt(isInstExpr, objoAsgndLocalStmt);
        //
        //
        // flagAssignmentStmt       : larkness = (local instanceof ProxyT)
        // ifInstOfStmt             : if larkness == 0 goto objoAsgndLocalStmt
        // objaAsgndProxyStmt       : objectHolder = proxy.getInstance()
        // gotoObjoInvoke           : goto opOnObj
        // objoAsgndLocalStmt       : objectHolder = local
        // opOnObj                  : ... [ f(objectHolder) ]
        final HashChain<Unit> patch = new HashChain<>();
        patch.addLast(flagAssignmentStmt);
        patch.addLast(ifInstOfStmt);
        patch.addLast(objaAsgndProxyStmt);
        patch.addLast(gotoOpOnObj);
        patch.addLast(objoAsgndLocalStmt);
//        patch.addLast(opOnObj);
        return patch;
    }
    private HashChain<Unit> generatePatchForProxiedObjectOperation(final Local local) {
        final Unit noop = jimple.newNopStmt();
        final HashChain<Unit> patch = generatePatchForProxiedObjectOperation(local, noop);
        patch.add(noop);
        return patch;
    }
}