package tpo.soot;

import isi.util.html.Document;
import isi.util.html.Element;
import isi.util.html.ElementBuilder;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import tpo.soot.SootOption.Argument;
import tpo.soot.SootOption.ArgumentType;
import tpo.soot.SootOption.EnumArgumentValue;

public class SootHelpHtmlRenderer {
	
	///////////////////////////////////////////////////////
	// state
	private final Appendable sink;
	
	///////////////////////////////////////////////////////
	// constructors
	public SootHelpHtmlRenderer (final Appendable sink) {
		this.sink = Objects.requireNonNull(sink);
	}
	
	///////////////////////////////////////////////////////
	//
	public SootHelpHtmlRenderer WriteOptions (final String style) throws SootOptionsParsingException, IOException {
		final ElementBuilder b = new ElementBuilder();
		final Document doc = new Document("Soot options");
		if (style != null)
			doc.SetStylesheet(style);
		
		for (final SootOptionGroup group: SootFacade.ListOfOptions()) {
			doc.AddElement(b.h1(group.GetName()));
			
			final Element tbody = b.tbody(), table = b.table(tbody);
			doc.AddElement(table);
			table.attr("class", "options");
			
			for (final SootOption opt: group.GetOptions())
				tbody.AddSubelement(b.tr(
						b.td(OptionNameElement(b, opt)).attr("class", "name"),
						b.td(OptionArgumentElement(b, opt.GetArgument())).attr("class", "argument"),
						b.td(opt.GetDescription()).attr("class", "description")
						).attr("class", "option"));
		}

		doc.WriteTo(sink);
		
		return this;
	}
	
	private Element OptionNameElement (final ElementBuilder b, final SootOption opt) {
		final Element ol = b.ol_lis(opt.GetShortName());
		for (final String name: opt.GetLongNames())
			ol.AddSubelement(b.li(name));
		return ol;
	}
	
	private Element OptionArgumentElement (final ElementBuilder b, final Argument arg) {
		return
				arg.GetType() == ArgumentType.Enum? EnumOptionArgumentElement(b, arg):
				arg.GetType() == ArgumentType.List? ListOptionArgumentElement(b, arg):
				arg.GetType() == ArgumentType.None? NoneOptionArgumentElement(b, arg):
				null;
	}

	private Element EnumOptionArgumentElement (final ElementBuilder b, final Argument arg) {
		final Element tbody = b.tbody(), table = b.table(tbody);
		
		for (final EnumArgumentValue val: arg.GetValues())
			tbody.AddSubelement(b.tr(
					b.td(b.ol(val.GetNames())),
					b.td(val.GetDescription())
					));
		
		return table;
	}
	
	private Element ListOptionArgumentElement (final ElementBuilder b, final Argument arg) {
		Element result;
		
		final List<String> names = arg.GetNames();
		if (names.isEmpty())
			result = b.text("");
		else
			result = b.ol(names);
		
		return result;
	}
	
	private Element NoneOptionArgumentElement (final ElementBuilder b, final Argument arg) {
		return b.text("");
	}
}
