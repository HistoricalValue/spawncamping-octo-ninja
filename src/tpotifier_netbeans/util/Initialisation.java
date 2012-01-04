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
            LOG.log(Level.INFO, "Initialising: {0}", action);
            action.Do();
        }
        LOG.log(Level.INFO, "Done initialising");
    }

    public void CleanUp () {
        for (final InitialisationAction action : actions) {
            LOG.log(Level.INFO, "Cleaning up: {0}", action);
            action.UnDo();
        }
        LOG.log(Level.INFO, "Dan cleaning up");
    }
    private static final Logger LOG = Logger.getLogger(Initialisation.class.getName());
}
