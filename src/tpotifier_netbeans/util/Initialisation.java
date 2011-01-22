package tpotifier_netbeans.util;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Initialisation {

    private final Deque<InitialisationAction> actions = new LinkedList<InitialisationAction>();

    protected void AddAction (final InitialisationAction action) {
        actions.push(action);
    }

    public void Initialise () {
        final Iterator<InitialisationAction> ite = actions.descendingIterator();
        while (ite.hasNext()) {
            final InitialisationAction action = ite.next();
            LOG.log(Level.INFO, "Performing initialisation action {0}", action);
            action.Do();
        }
        LOG.log(Level.INFO, "Done with initialisation actions");
    }

    public void CleanUp () {
        for (final InitialisationAction action : actions) {
            LOG.log(Level.INFO, "Performing cleanup {0}", action);
            action.UnDo();
        }
        LOG.log(Level.INFO, "Dan with cleanups");
    }
    private static final Logger LOG = Logger.getLogger(Initialisation.class.getName());
}
