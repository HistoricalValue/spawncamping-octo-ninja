package tpotifier_netbeans;

import sample.util.SingletonKeeper;
import sample.util.SingletonKeeper.CleanUpper;
import soot.Pack;
import soot.PackManager;
import soot.Transform;
import tpotifier_netbeans.util.Initialisation;
import tpotifier_netbeans.util.InitialisationAction;

class TPOJimpleBodyTransformerConstructor implements SingletonKeeper.Constructor {
    private final TPOJimpleBodyTransformerOptions options;
    TPOJimpleBodyTransformerConstructor (final TPOJimpleBodyTransformerOptions _options) {
        options = _options;
    }
    @Override
    public Object Construct () {
        return new TPOJimpleBodyTransformer(options);
    }
}

class SingletonKeeperInitialisationAction implements InitialisationAction {
    private final TPOJimpleBodyTransformerOptions tpoJimpleBodyTransformerOptions;

    public SingletonKeeperInitialisationAction (final TPOJimpleBodyTransformerOptions _tpoJimpleBodyTransformerOptions) {
        tpoJimpleBodyTransformerOptions = _tpoJimpleBodyTransformerOptions;
    }

    @Override
    public String toString () {
        return "SingletonKeeper";
    }

    private final SingletonKeeper SK = SingletonKeeper.S();

    @Override
    public void Do () {
        final CleanUpper lazyMaid = CleanUpper.lazy;
        //
        SK.Register(TPOJimpleBodyTransformer.class,
                new TPOJimpleBodyTransformerConstructor(tpoJimpleBodyTransformerOptions),
                lazyMaid);
    }

    @Override
    public void UnDo () {
        SK.CleanUpAll();
    }
}

class SootInitialisationAction implements InitialisationAction {

    @Override
    public String toString () {
        return "Soot";
    }

    @Override
    public void Do () {
        final SingletonKeeper SK  = SingletonKeeper.S();
        final PackManager     PM  = soot.PackManager.v();
        final Pack            JTP = PM.getPack("jtp");
        
        final TPOJimpleBodyTransformer tpoJimpleTransformerSingletonInstance =
                SK.Get(TPOJimpleBodyTransformer.class);
        final Transform bodyTransformation =
                new Transform(  TPOJimpleBodyTransformer.PhaseName,
                                tpoJimpleTransformerSingletonInstance);

        JTP.add(bodyTransformation);
    }
    
    @Override
    public void UnDo () {
        soot.G.reset();
    }
}

class SootSetuperInitialisation extends Initialisation {
    
    SootSetuperInitialisation (final  TPOJimpleBodyTransformerOptions tpopojbto) {
        AddAction(new SingletonKeeperInitialisationAction(tpopojbto));
        AddAction(new SootInitialisationAction());
    }

}
