package tpo.soot;

import isi.net.http.helpers.Url;
import isi.util.StringBuilders;
import isi.util.html.Document;
import isi.util.html.Element;
import isi.util.html.ElementBuilder;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;
import tpo.soot.SootOption.Argument;
import tpo.soot.SootOption.EnumArgumentValue;

public class SootHelpHtmlRenderer {
	
	///////////////////////////////////////////////////////
	// state
	private final StringBuilder	extraJavascript = new StringBuilder(1 << 14),
								extraCss = new StringBuilder(1 << 14);
	private final GroupIdGenerator gidgen = new GroupIdGenerator();
	
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
		return SootHelpHtmlRendererConstants.StandardCSS + extraCss.toString();
	}
	
	public String Javascript () {
		return SootHelpHtmlRendererConstants.StandardJavascript + extraJavascript.toString();
	}
	
	///////////////////////////////////////////////////////
	//
	public void Reset () {
		ResetExtraCss();
		ResetExtraJavascript();
		gidgen.reset();
	}
	
	private void ResetExtraCss () {
		StringBuilders.reset(extraCss);
	}
	
	private void ResetExtraJavascript () {
		StringBuilders.reset(extraJavascript);
	}
	
	///////////////////////////////////////////////////////
	//
	@SuppressWarnings("FinalClass")
	private final static class GroupIdGenerator {
		private int seed = 0;
		@SuppressWarnings("ValueOfIncrementOrDecrementUsed")
		String next () { return "group_" + seed++; }
		void reset () { seed = 0; }
	}
	public SootHelpHtmlRenderer WriteOptions (
			final Writer sink,
			final String css,
			final String js)
		throws
			SootOptionsParsingException,
			IOException
	{
		Reset();
		
		final String bodyId = "bady";
		
		final ElementBuilder b = new ElementBuilder();
		final Element index = CreateIndexElement(b);
		final Document doc = CreateDocument(b, index, bodyId, css, js);
		
		for (final SootOptionGroup group: SootFacade.ListOfOptions())
			AppendElementsForOptionsGroup(b, group, doc, index);

		final List<String> subphasesIndecesElementsIds = new LinkedList<>();
		for (final SootPhaseOptions opt: SootFacade.ListOfPhases())
			AppendElementsForPhaseOption(b, opt, doc, index, subphasesIndecesElementsIds);
		
		doc.AddElement(CreateClosePopUpElementsButton(b, subphasesIndecesElementsIds));
		doc.AddElement(CreateJavascriptMainScriptElement(b, bodyId));
		doc.WriteTo(sink);
		return this;
	}

	private static Element CreateJavascriptMainScriptElement (
			final ElementBuilder	b,
			final String			bodyId) {
		final Element script = b.script();
		script.AddSubelement(b.html("$(\"" + bodyId + "\").onload = isi.Initialise;"));
		script.attr("type", "text/javascript");
		return script;
	}
	private Element CreateClosePopUpElementsButton (
			final ElementBuilder		b,
			final List<String>			subphasesIndecesElementsIds) {
		AddExtraJavascript("\n\nisi.HideAllSubphases = function HideAllSubphases () {");
		for (final String hiddenEl: subphasesIndecesElementsIds)
			AddExtraJavascript("\n\t$(\"")
					.AddExtraJavascript(hiddenEl)
					.AddExtraJavascript("\").style.display = \"none\";");
		AddExtraJavascript("\n}\n");
		
		return b.div(b.text("X")).SetId("closeButton");
	}
	
	private void AppendElementsForPhaseOption (
			final ElementBuilder		b,
			final SootPhaseOptions		opt,
			final Document				doc,
			final Element				index,
			final List<String>			subphasesIndecesElementsIds
			) {
		final String groupId = gidgen.next();
		final String groupIndexId = groupId + "_index";
		final String toggleGroupFunctionName = "ToggleGroup_" + groupIndexId;
		final String phaseName = opt.GetName();
		
		subphasesIndecesElementsIds.add(groupIndexId);
		
		ExtraJavascriptForPhaseOptions(groupIndexId, toggleGroupFunctionName);
		index.AddSubelement(b.li(b.a("#" + groupId, phaseName).attr("onclick", "isi." + toggleGroupFunctionName + "();")));

		doc.AddElement(b.h1(phaseName).SetId(groupId));
		doc.AddElement(b.p(soot.options.Options.v().getPhaseHelp(phaseName)).SetClass("description"));

		// list of subopts
		final Element subphaseOptionsIndexElement = CreatePopUpMenuElement(b).SetId(groupIndexId);
		doc.AddElement(subphaseOptionsIndexElement);	

		for (final SootPhaseOptions subphaseOptions: opt.GetSubphases())
			AppendElementForSubphaseOption(subphaseOptions, b, doc, subphaseOptionsIndexElement);
	}
	
	private void AppendElementForSubphaseOption (
			final SootPhaseOptions		subopt,
			final ElementBuilder		b,
			final Document				doc,
			final Element				suboptionsIndex) {
		final String subphaseName = subopt.GetName();

		if (!subopt.GetSubphases().isEmpty())
			throw new RuntimeException(subphaseName + " not without subphases");

		final String subgroupId = gidgen.next();

		suboptionsIndex.AddSubelement(b.li(b.a("#" + subgroupId, subphaseName)));

		doc.AddElement(b.h2(subphaseName).SetId(subgroupId))
				.AddElement(b.p(subopt.GetDescription()).SetClass("description"))
				.AddElement(b.p(soot.options.Options.v().getPhaseHelp(subphaseName)).SetClass("description"));
	}
	
	private void ExtraJavascriptForPhaseOptions (
			final String	groupIndexId,
			final String	toggleGroupFunctionName) {
		AddExtraJavascript("\nisi.")
				.AddExtraJavascript(toggleGroupFunctionName)
				.AddExtraJavascript(" = function ")
				.AddExtraJavascript(toggleGroupFunctionName)
				.AddExtraJavascript(" () { isi.ToggleSubphasePopUp(\"")
				.AddExtraJavascript(groupIndexId)
				.AddExtraJavascript("\"); }");
	}
	
	private void AppendElementsForOptionsGroup (
			final ElementBuilder	b,
			final SootOptionGroup	group,
			final Document			doc,
			final Element			index) {
		final String groupName = group.GetName();
		final String groupId = gidgen.next();

		doc.AddElement(b.h1(groupName).SetId(groupId));
		index.AddSubelement(b.li(b.a("#" + groupId, groupName)));

		final Element	tbody = b.tbody(),
						table = b.table(tbody).SetClass("options");
		doc.AddElement(table);

		for (final SootOption opt: group.GetOptions())
			tbody.AddSubelement(
					b.tr(
							b.td(OptionNamesElement(b, opt)).attr("class", "name"),
							b.td(OptionArgumentElement(b, opt.GetArgument())).attr("class", "argument"),
							b.td(opt.GetDescription()).attr("class", "description")
					).attr("class", "option"));		
	}
	
	private static Document CreateDocument (
			final ElementBuilder	b,
			final Element			index,
			final String			bodyId,
			final String			css,
			final String			js) {
		final Document doc = new Document("Soot options");

		doc.SetBodyId(bodyId);
		doc.AddElement(index);
		
		if (css != null)
			doc.SetStylesheet("/" + Url.EscapeUrl(css));
		if (js != null)
			doc.SetJavascript("/" + Url.EscapeUrl(js));
		
		return doc;
	}

	private static Element CreatePopUpMenuElement (final ElementBuilder b) {
		return b.ol().SetClass("menu", "popup");
	}
	private static Element CreateIndexElement (final ElementBuilder b) {
		return b.ol().SetId("index").SetClass("menu");
	}

	private static Element OptionNamesElement (final ElementBuilder b, final SootOption opt) {
		final Element ol = b.ol(opt.GetShortName());
		for (final String name: opt.GetLongNames())
			ol.AddSubelement(b.li(name));
		return ol;
	}
	
	private static Element OptionArgumentElement (final ElementBuilder b, final Argument arg) {
		Element result = null;
		switch (arg.GetType()) {
			case Enum:
				result = EnumOptionArgumentElement(b, arg);
				break;
			case List:
				result = ListOptionArgumentElement(b, arg);
				break;
			case None:
				result = NoneOptionArgumentElement(b, arg);
				break;
			default:
				assert false;
		}
		return result;
	}

	private static Element EnumOptionArgumentElement (final ElementBuilder b, final Argument arg) {
		final Element	tbody = b.tbody(),
						table = b.table(tbody);
		
		for (final EnumArgumentValue val: arg.GetValues())
			tbody.AddSubelement(
					b.tr(
							b.td(b.ol(val.GetNames())),
							b.td(val.GetDescription())
					));
		
		return table;
	}
	
	private static Element ListOptionArgumentElement (final ElementBuilder b, final Argument arg) {
		return b.ol(arg.GetNames());
	}
	
	private static Element NoneOptionArgumentElement (final ElementBuilder b, final Argument arg) {
		return b.text("");
	}

}
