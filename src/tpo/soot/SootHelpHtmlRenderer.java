package tpo.soot;

public class SootHelpHtmlRenderer {
	
	///////////////////////////////////////////////////////
	// state
	private final Appendable sink;
	
	///////////////////////////////////////////////////////
	// constructors
	public SootHelpHtmlRenderer (final Appendable sink) {
		this.sink = sink;
	}
	
	///////////////////////////////////////////////////////
	//
	public SootHelpHtmlRenderer WriteOptions () throws Exception {
		SootFacade.ListOfOptions();
		return this;
	}
	
	
}
