package tpo.soot;

import isi.util.StringBuilders;
import isi.util.html.Document;
import isi.util.html.Element;
import isi.util.html.ElementBuilder;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;
import tpo.soot.SootOption.Argument;
import tpo.soot.SootOption.ArgumentType;
import tpo.soot.SootOption.EnumArgumentValue;

public class SootHelpHtmlRenderer {
	
	///////////////////////////////////////////////////////
	// state
	private final StringBuilder	extraJavascript = new StringBuilder(1 << 14),
								extraCss = new StringBuilder(1 << 14);
	
	///////////////////////////////////////////////////////
	// constructors
	
	///////////////////////////////////////////////////////
	//
	public SootHelpHtmlRenderer AddExtraJavascript (final String script) {
		this.extraJavascript.append(script);
		return this;
	}
	
	public SootHelpHtmlRenderer AddExtraCss (final String style) {
		this.extraCss.append(style);
		return this;
	}
	
	public String Css () {
		return standardCSS + extraCss.toString();
	}
	
	public String Javascript () {
		return standardJavascript + extraJavascript.toString();
	}
	
	public void ResetExtraCss () {
		StringBuilders.reset(extraCss);
	}
	
	public void ResetExtraJavascript () {
		StringBuilders.reset(extraJavascript);
	}
	
	///////////////////////////////////////////////////////
	//
	public SootHelpHtmlRenderer WriteOptions (
			final Writer sink,
			final String style,
			final String js)
		throws
			SootOptionsParsingException,
			IOException
	{
		ResetExtraCss();
		ResetExtraJavascript();
		
		final ElementBuilder b = new ElementBuilder();
		final Document doc = new Document("Soot options");
		final Element index = b.ol().SetId("index").SetClass("menu");
		int groupIdSeed = 0;
		
		doc.Body().attr("onload", "isi.Initialise()");
		doc.AddElement(index);
		
		if (style != null)
			doc.SetStylesheet(style);
		if (js != null)
			doc.SetJavascript(js);
		
		for (final SootOptionGroup group: SootFacade.ListOfOptions()) {
			final String groupName = group.GetName();
			final String groupId = "group" + Integer.toString(groupIdSeed);
			++groupIdSeed;
			
			doc.AddElement(b.h1(groupName).SetId(groupId));
			index.AddSubelement(b.li(b.a("#" + groupId, groupName)));
			
			
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

		final List<String> subphasesIds = new LinkedList<>();
		for (final SootPhaseOptions opt: SootFacade.ListOfPhases()) {
			final String groupId = "group" + Integer.toString(groupIdSeed);
			final String groupIndexId = groupId + "_index";
			++groupIdSeed;
			final String phaseName = opt.GetName();
			
			index.AddSubelement(b.li(b.a("#" + groupId, phaseName).
					attr("onclick", "isi.ToggleGroup_" + groupIndexId + "()")));
			
			final String toggleGroupFunctionName = "ToggleGroup_" + groupIndexId;
			AddExtraJavascript("\nisi.")
					.AddExtraJavascript(toggleGroupFunctionName)
					.AddExtraJavascript(" = function ToggleGroup_")
					.AddExtraJavascript(groupIndexId)
					.AddExtraJavascript(" () { isi.ToggleSubphasePopUp(\"")
					.AddExtraJavascript(groupIndexId)
					.AddExtraJavascript("\"); }");
					
			
			doc.AddElement(b.h1(phaseName).SetId(groupId));
			doc.AddElement(b.p(soot.options.Options.v().getPhaseHelp(phaseName)));
			
			// list of subopts
			final Element suboptsEl = b.ol().SetId(groupIndexId).SetClass("menu", "popup");
			subphasesIds.add(groupIndexId);
			
			for (final SootPhaseOptions subopt: opt.GetSubphases()) {
				final String subphaseName = subopt.GetName();
				
				if (!subopt.GetSubphases().isEmpty())
					throw new RuntimeException(subphaseName + " not without subphases");
				
				final String subgroupId = groupId + "_" + Integer.toString(groupIdSeed);
				++groupIdSeed;
				
				suboptsEl.AddSubelement(b.li(b.a("#" + subgroupId, subphaseName)));
				
				doc.AddElement(b.h2(subphaseName).SetId(subgroupId))
						.AddElement(b.p(subopt.GetDescription()))
						.AddElement(b.p(soot.options.Options.v().getPhaseHelp(subphaseName)));
			}
			
			doc.AddElement(suboptsEl);
		}
		
		AddExtraJavascript("\n\nisi.HideAllSubphases = function HideAllSubphases () {");
		for (final String hiddenEl: subphasesIds)
			AddExtraJavascript("\n\t$(\"")
					.AddExtraJavascript(hiddenEl)
					.AddExtraJavascript("\").style.display = \"none\";");
		AddExtraJavascript("\n}\n");
		
		doc.AddElement(b.div(b.text("X")).SetId("closeButton"));
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
	private final static String standardCSS = ""
			+ "body {\n"
			+ "	padding: 0 0 10em 0;\n"
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
			+ "h1, table.options {\n"
			+ "	width: 80%;\n"
			+ "	left: 19%;\n"
			+ "	position: relative;\n"
			+ "}\n"
			+ "\n"
			+ "table.options {\n"
			+ "	border-collapse: collapse;\n"
			+ "	margin: 0 0 2em 0;\n"
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
			+ ".options .description { background-color: #A7B6A4; }\n"
			+ "\n"
			+ ".menu {\n"
			+ "	background-color: #202020;\n"
			+ "	font-family: \"verdana\", \"arial\", sans-serif;\n"
			+ "	font-size: 9px;\n"
			+ "	color: #707070;\n"
			+ "	width: 18%;\n"
			+ "	border: 1px solid #505050;\n"
			+ "	margin: 0;\n"
			+ "	padding: 0;\n"
			+ "	list-style-type: none;\n"
			+ "	max-height: 80%;\n"
			+ "	overflow-y: auto;\n"
			+ "}\n"
			+ "\n"
			+ ".menu > li {\n"
			+ "	border-style: solid;\n"
			+ "	border-color: #505050;\n"
			+ "	border-width: 3px 0 0 0;\n"
			+ "}\n"
			+ "\n"
			+ "\n"
			+ ".menu > li > * {\n"
			+ "	margin: 0;\n"
			+ "	padding: .7em .3em .5em .3em ;\n"
			+ "	color: inherit;\n"
			+ "	text-decoration: inherit;\n"
			+ "	display: block;\n"
			+ "}\n"
			+ ".menu > li:first-child {\n"
			+ "	border-top-width: 0;\n"
			+ "	margin-top: 0;\n"
			+ "}\n"
			+ ".menu > li:hover {\n"
			+ "	border-color: #406020;\n"
			+ "	background-color: #101010;\n"
			+ "	color: #708060;\n"
			+ "}\n"
			+ "\n"
			+ "#index {\n"
			+ "	position: fixed;\n"
			+ "	top: 1em;\n"
			+ "}\n"
			+ "\n"
			+ "div#closeButton {\n"
			+ "	position: fixed;\n"
			+ "	background-color: red;\n"
			+ "	color: white;\n"
			+ "	font-family: \"verdana\", sans-serif;\n"
			+ "	font-weight: bold;\n"
			+ "	padding: .3em;\n"
			+ "}\n"
			+ "\n"
			+ ".popup {\n"
			+ "	position: fixed;\n"
			+ "	top: 9.126em;\n"
			+ "	left: 9.126em;\n"
			+ "}\n"
			+ "\n";
	
	private static final String standardJavascript = ""
			+ "function $ (id)			{ return document.getElementById(id); }\n"
			+ "\n"
			+ "//\n"
			+ "isi = {};\n"
			+ "	isi.k = {};\n"
			+ "		isi.k.k		= document.createElement;\n"
			+ "		isi.k.div	= document.createElement.bind(\"div\"	);\n"
			+ "		isi.k.ol	= document.createElement.bind(\"ol\"	);\n"
			+ "		isi.k.li	= document.createElement.bind(\"li\"	);\n"
			+ "		isi.k.p		= document.createElement.bind(\"p\"	);\n"
			+ "//\n"
			+ "	isi.g = 0;\n"
			+ "	isi.G = function G () {\n"
			+ "		this.body = $(\"bady\");\n"
			+ "		this.index = $(\"index\");\n"
			+ "		this.indeces = this.index.getElementsByTagName(\"a\");\n"
			+ "		this.activeWindow = null;\n"
			+ "		this.closeButton = $(\"closeButton\");\n"
			+ "		this.closeButtonVisible = false;\n"
			+ "	}\n"
			+ "//////////\n"
			+ "	isi.RemoveElement = function RemoveElement (el) {\n"
			+ "		el.parentElement.removeChild(el);\n"
			+ "	}\n"
			+ "//////////\n"
			+ "	isi.ObjectToString = function ToString (anything) {\n"
			+ "		var result = [];\n"
			+ "		for (i in anything)\n"
			+ "			result.push(i + \":\" + anything[i]);\n"
			+ "		return result;\n"
			+ "	}\n"
			+ "//////////\n"
			+ "	isi.Initialise = function Initialise ()	{\n"
			+ "		isi.g = new isi.G();\n"
			+ "		isi.HideAllSubphases();\n"
			+ "		return alert(\"all loaded\");\n"
			+ "	}\n"
			+ "//////////\n"
			+ "	isi.ToggleSubphasePopUp = function ToggleSubphasePopUp (id) {\n"
			+ "		var g = isi.g;\n"
			+ "		var prevWindowId = g.activeWindow;\n"
			+ "		var w = id;\n"
			+ "	\n"
			+ "		g.activeWindow = id;\n"
			+ "	\n"
			+ "		if (prevWindowId)\n"
			+ "			$(prevWindowId).style.display = \"none\";\n"
			+ "\n"
			+ "		if (!g.closeButtonVisible)\n"
			+ "			g.closeButton.style.display = \"block\";\n"
			+ "\n"
			+ "		$(id).style.display = \"block\";\n"
			+ "	}\n"
			+ "\n";
}
