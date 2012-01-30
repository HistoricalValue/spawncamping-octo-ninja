package tpo.soot;

import isi.util.Collections;
import java.util.List;

import static java.util.Objects.requireNonNull;

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
		this.name = requireNonNull(name);
		this.options = Collections.newUnmodifiableList(requireNonNull(options));
	}
}
