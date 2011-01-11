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
import soot.Body;
import soot.Local;
import soot.PatchingChain;
import soot.PointsToSet;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.AssignStmt;
import soot.jimple.EqExpr;
import soot.jimple.IfStmt;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InstanceOfExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.Stmt;
import soot.jimple.VirtualInvokeExpr;
import soot.jimple.spark.pag.GlobalVarNode;
import soot.jimple.spark.pag.LocalVarNode;
import soot.jimple.spark.pag.Node;
import soot.jimple.spark.pag.PAG;
import soot.jimple.spark.pag.VarNode;
import soot.util.Chain;

public class TPOTransformer extends soot.BodyTransformer {

    public static final String PhaseName = "jtp.replaceTPOs";
    public static final String ProxyGetInstanceMethodName = "GetInstance";
    
    private static final long serialVersionUID = 237;
    
    @Override
    protected void internalTransform (final Body b, final String phaseName, final Map options) {
        final boolean contains_enabled = options.containsKey("enabled");
        final Object enabledValue = options.get("enabled");
        final boolean enabledIsTrue = ((String) enabledValue).equals("true");
        final boolean phaseNameIsCorrect = phaseName.equals(PhaseName);
        final boolean bodyIsJimple = b instanceof soot.jimple.JimpleBody;
        if (contains_enabled && enabledIsTrue && phaseNameIsCorrect && bodyIsJimple)
            new Transformer((JimpleBody)b).transform((JimpleBody) b);
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

    private static final class Transformer {
        private final Scene         scene                   = Scene.v();
        private final SootClass     ProxyClass              = scene.getSootClass("sample.SharedMemoryTPO");
        private static final RefType ProxyType              = RefType.v("sample.SharedMemoryTPO");
        private final SootMethod    ProxyGetInstanceMethod  = ProxyClass.getMethodByName(ProxyGetInstanceMethodName);
        private final RefType       ObjectType              = RefType.v("java.lang.Object");
        //
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
                ;
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
        private final static Pattern addBasicClassExceptionPatter = Pattern
                .compile("Scene");//.addBasicClass\\(([^,]+),([A-Z]+)\\);");
        private final PrintStream out = soot.G.v().out;
        private final Jimple jimple = Jimple.v();
        private final List<Unit[]> insertions = new LinkedList<>();
        //
        private final JimpleBody                    body;
        private final SootMethod                    method;
        private final Chain<Local>                  locals;
        private final PatchingChain<Unit>           units;
        private final Collection<? extends Local>   tagged;
        private final Local                         isInstanceFlagLocal;
        private final Local                         internalObjectHolder;
        Transformer (final JimpleBody _body) {
            body                = _body;
            method              = _body.getMethod();
            locals              = _body.getLocals();
            units               = _body.getUnits();
            tagged              = getTagged(locals, method);
            isInstanceFlagLocal = createAndAddFlagLocal(jimple, locals);
            internalObjectHolder= createAndAddHolderLocal(jimple, locals);
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

            for (final Unit unit: units) {
                final String unitstr = unit.toString();
                if (unit instanceof InvokeStmt)
                    transformInstanceInvoke((InvokeStmt) unit);
            }

            for (final Unit[] insertionPair: insertions)
                units.insertBefore(insertionPair[0], insertionPair[1]);
        }

        private void transformInstanceInvoke (final InvokeStmt invokeStmt)
        {
            final String stmtstr = invokeStmt.toString();
            final InvokeExpr expr = invokeStmt.getInvokeExpr();
            if (expr instanceof VirtualInvokeExpr) {
                final VirtualInvokeExpr vie = (VirtualInvokeExpr) expr;
                final Value base = vie.getBase();
                if (base instanceof Local) {
                    final Local local = (Local) base;
                    if (tagged_contains(local)) {
                        // objectHolder = local
                        final AssignStmt        objoAsgndLocalStmt  = jimple.newAssignStmt(internalObjectHolder, local);
                        //
                        // objectHolder = proxy.getInstance()
                        final VirtualInvokeExpr getInstExpr         = jimple.newVirtualInvokeExpr(local, ProxyGetInstanceMethod.makeRef());
                        final AssignStmt        objaAsgndProxyStmt  = jimple.newAssignStmt(internalObjectHolder, getInstExpr);
                        //
                        // objectHolder.originalMethod(..)
                        final VirtualInvokeExpr objoInvokeExpr      = jimple.newVirtualInvokeExpr(  internalObjectHolder,
                                                                                                    vie.getMethodRef(),
                                                                                                    vie.getArgs());
                        final InvokeStmt        objoInvokeStmt      = jimple.newInvokeStmt(objoInvokeExpr);
                        //
                        // larkness  = (local instanceof ProxyT)
                        final InstanceOfExpr    flagExpr            = jimple.newInstanceOfExpr(local, ProxyType);
                        final AssignStmt        flagAssignmentStmt  = jimple.newAssignStmt(isInstanceFlagLocal, flagExpr);
                        //
                        // if larkness == 0 (!local instanceof ProxyT)
//                        final EqExpr            isInstExpr          = jimple.newEqExpr(isInstanceFlagLocal, );
//                        IfStmt                  ifInstOfStmt        = jimple.newIfStmt(isInstanceFlagLocal, objoAsgndLocalStmt);
                        //
                        //
//                        insertions.add(new Unit[]{ifInstOfStmt      , invokeStmt});
                        insertions.add(new Unit[]{flagAssignmentStmt, invokeStmt});
                        insertions.add(new Unit[]{objoInvokeStmt    , flagAssignmentStmt});
                        insertions.add(new Unit[]{objaAsgndProxyStmt, objoInvokeStmt});
                        insertions.add(new Unit[]{objoAsgndLocalStmt, objaAsgndProxyStmt});
                    }
                }
                else
                    throw new RuntimeException("Local expected");
            }
        }
    }
}
