package tpo.soot;

import isi.util.Collections;
import java.util.List;

public class SootOptionGroup {
	
	///////////////////////////////////////////////////////
	// state
	private final String name;
	private final List<SootOption> options;
	
	///////////////////////////////////////////////////////
	//
	
	public String GetName () {
		return name;
	}
	
	@SuppressWarnings("ReturnOfCollectionOrArrayField")
	public List<SootOption> GetOptions () {
		return options;
	}
	
	///////////////////////////////////////////////////////
	// package
	///////////////////////////////////////////////////////
	
	///////////////////////////////////////////////////////
	// constructors
	SootOptionGroup (final String name, final Iterable<? extends SootOption> options) {
		this.name = name;
		this.options = Collections.unmodifiableList(options);
	}
}
