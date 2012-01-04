package tpo.soot;

import isi.util.Predicate;
import isi.util.Predicates;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


public class SootPhaseOptions {
	
	private final String name, description;
	private final List<SootPhaseOptions> subphases, subphasesPublic;
	
	public SootPhaseOptions (final String name, final String description) {
		this.name = name;
		this.description = description;
		subphases = new LinkedList<>();
		subphasesPublic = Collections.unmodifiableList(subphases);
	}
	
	public SootPhaseOptions (final String name) {
		this(name, null);
	}
	
	///////////////////////////////////////////////////////
	
	public String GetName () {
		return name;
	}
	
	public String GetDescription () {
		return description;
	}

	@SuppressWarnings("ReturnOfCollectionOrArrayField")
	public List<SootPhaseOptions> GetSubphases () {
		return subphasesPublic;
	}
	
	///////////////////////////////////////////////////////
	
	@Override
	public String toString () {
		return
				name
				+ ": "
				+ description
				+ " ("
				+ Integer.toString(subphases.size())
				+ " subphases)";
	}
	
	///////////////////////////////////////////////////////
	
	/**
	 * 
	 * @param subphase
	 * @throws IllegalArgumentException if the given subphase is already contained
	 */
	public void AddSubphase (final SootPhaseOptions subphase) throws IllegalArgumentException {
		if (isi.util.Collections.find(subphases, Predicates.newEquality(subphase)) != null)
			throw new IllegalArgumentException(subphase.toString());
		
		subphases.add(subphase);
	}
}
