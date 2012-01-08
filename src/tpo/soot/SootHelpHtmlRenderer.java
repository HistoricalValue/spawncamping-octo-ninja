package tpo.soot;

import isi.util.html.Document;
import isi.util.html.Element;
import isi.util.html.ElementBuilder;
import java.io.IOException;
import java.util.List;
import tpo.soot.SootOption.EnumArgumentValue;

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
	public SootHelpHtmlRenderer WriteOptions () throws SootOptionsParsingException, IOException {
		final Document doc = new Document("Soot options");
		final ElementBuilder b = new ElementBuilder();
		
		for (final SootOptionGroup group: SootFacade.ListOfOptions()) {
			doc.AddElement(b.h1(group.GetName()));
			
			final Element ol = new Element("ol");
			
			for (final SootOption opt: group.GetOptions())
				ol.AddSubelement(b.li(OptionElement(opt)));
			
			doc.AddElement(ol);
		}
		
		doc.WriteTo(sink);
		
		return this;
	}

	private Element OptionElement (final SootOption opt) {
		final ElementBuilder b = new ElementBuilder();
		final Element tr = b.tr(b.td(opt.GetShortName()));
		final Element argument = ArgumentElement(opt.GetArgument());
		
		tr.AddSubelement(b.td(argument));
		
		return b.table(b.tbody(tr));
	}
	
	private Element ArgumentElement (final SootOption.Argument arg) {
		Element elem;
		final ElementBuilder b = new ElementBuilder();
		
		switch (arg.GetType()) {
			case List:
				elem = b.ol();
				for (final String name: arg.GetNames())
					elem.AddSubelement(b.li(name));
				break;
			case Enum:
				final Element tbody = b.tbody();
				elem = b.table(tbody);
				
				int maxnames = 0;
				for (final EnumArgumentValue val: arg.GetValues()) {
					final int size = val.GetNames().size();
					if (size > maxnames)
						maxnames = size;
				}
				
				for (final EnumArgumentValue val: arg.GetValues()) {
					final List<String> names = val.GetNames();
					final Element tr = b.tr();
					Element last = null;
					int columns = maxnames + 1;
					for (final String name: names) {
						last = b.td(name);
						tr.AddSubelement(last);
						--columns;
					}
					assert columns > 0;
					if (columns > 1)
						last.attr("colspan", Integer.toString(columns));
					tr.AddSubelement(b.td("???"));
					tbody.AddSubelement(tr);
				}
				break;
			case None:
				elem = b.text("");
				break;
			default:
				throw new AssertionError();
		}
		
		return elem;
	}
}
