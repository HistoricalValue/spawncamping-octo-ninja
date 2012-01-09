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
	
	///////////////////////////////////////////////////////
	//
	public final static String CSS = ""
			+ "body {\n"
			+ "	padding: 0;\n"
			+ "	margin: 0;\n"
			+ "}\n"
			+ "\n"
			+ "h1 {\n"
			+ "	text-align: center;\n"
			+ "	border-style: solid;\n"
			+ "	border-width: .3em 0;\n"
			+ "	color: #202020;\n"
			+ "	margin: 0 0 .3em 0;\n"
			+ "	padding: 0;\n"
			+ "}\n"
			+ "\n"
			+ "table.options {\n"
			+ "	border-collapse: collapse;\n"
			+ "	width: 80%;\n"
			+ "	margin: 0 auto 2em auto;\n"
			+ "}\n"
			+ "\n"
			+ ".options .name { width: 20%; }\n"
			+ ".options .argument { width: 30%; }\n"
			+ ".options .description { width: 50%; }\n"
			+ "\n"
			+ ".options td {\n"
			+ "	padding: .3em;\n"
			+ "	vertical-align: top;\n"
			+ "}\n"
			+ "\n"
			+ ".options ol {\n"
			+ "	list-style-type: none;\n"
			+ "	margin: 0;\n"
			+ "	padding: 0;\n"
			+ "}\n"
			+ "\n"
			+ ".options .name > ol > li:before {\n"
			+ "	content: \"-\";\n"
			+ "}\n"
			+ "\n"
			+ ".options .argument > ol {\n"
			+ "	margin: .3em;\n"
			+ "}\n"
			+ "\n"
			+ ".options .argument > ol > li {\n"
			+ "	display: inline;\n"
			+ "}\n"
			+ "\n"
			+ ".options .argument > ol > li:after {\n"
			+ "	content: \" \";\n"
			+ "}\n"
			+ "\n"
			+ ".options .argument {\n"
			+ "	padding: 0;\n"
			+ "}\n"
			+ ".options .argument > table {\n"
			+ "	margin: 0;\n"
			+ "	width: 100%;\n"
			+ "	border-collapse: collapse;\n"
			+ "}\n"
			+ "\n"
			+ "\n"
			+ "/****************************/\n"
			+ "\n"
			+ "body {\n"
			+ "	background-color: #404040;\n"
			+ "}\n"
			+ "\n"
			+ ".options > tbody > tr > td {\n"
			+ "	border: 1px solid #606060;\n"
			+ "	background-color: #808080;\n"
			+ "}\n"
			+ "\n"
			+ ".options .argument > table > tbody > tr > td {\n"
			+ "	border-style: dotted;\n"
			+ "	border-color: #505050;\n"
			+ "	border-width: 1px;\n"
			+ "}\n"
			+ "\n"
			+ ".options .name { font-family: \"consolas\", \"courier new\", monospace; }\n"
			+ "\n"
			+ ".options .name { background-color: #7A7E85; }\n"
			+ ".options .argument { background-color: #9A9394; }\n"
			+ ".options .description { background-color: #A7B6A4; }\n";
}
