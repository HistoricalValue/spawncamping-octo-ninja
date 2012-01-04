package tpo.soot;

import java.util.LinkedList;
import java.util.List;
import isi.util.Ref;
import java.util.Map;
import isi.util.logging.AutoLogger;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import soot.G;
import tpo.Loggers;
import tpo.soot.util.OutputCapturer;
import tpo.soot.util.StoringOutputCapturer;
import static isi.util.StringBuilders.isEmpty;
import static isi.util.StringBuilders.reset;

public class SootFacade {
	
	///////////////////////////////////////////////////////
	
	public static OutputCapturer CaptureOutput (final Path path, final boolean withStringStorage, final boolean duplicate) throws IOException {
		final OutputStream fout = Files.newOutputStream(path);
		
		try {
			G.out = new PrintStream(
					duplicate?
						new isi.util.streams.MultiplierOutputStream(fout, SootFacade.G.out):
						fout,
					true,
					utf8.name());
		} catch (final UnsupportedEncodingException ex) {
			throw new AssertionError("", ex);
		}
		
		return withStringStorage? new StoringOutputCapturer(path, utf8) : new OutputCapturer(path, utf8);
	}
	
	///////////////////////////////////////////////////////
	
	private final static int RootGroup = 347823;
	private final static int SubGroup = 38471;
	public static List<SootPhaseOptions> ListOfPhases () throws SootOptionsParsingException {
		final Pattern	NEW_GROUP = Pattern.compile("^  ? ? ?([\\w.-]+)\\s*(.*)$"),
						INDENT = Pattern.compile("^(\\s*).*$");
		final Matcher m = INDENT.matcher("");
		
		final List<SootPhaseOptions> options = new LinkedList<>();
		
		// group in the making info
		Ref<String> name = Ref.CreateRef(null);
		final StringBuilder bob = new StringBuilder(1 << 14);
		Ref<SootPhaseOptions> group = Ref.CreateRef(null), subgroup = Ref.CreateRef(null);
		Ref<Integer> buildingGroupType = Ref.CreateRef(-1);
		
		for (final String line: soot.options.Options.v().getPhaseList().split("\n")) {
			m.usePattern(INDENT);
			m.reset(line);
			
			if (!m.matches())
				throw new SootOptionsParsingException(line);
			
			switch (m.group(1).length()) {
				case 31:
				case 33: {	// (sub)group description continuation
					assert name.Deref() != null && !name.Deref().isEmpty() && !isEmpty(bob);
					bob.append(" ").append(line.substring(31));
					break;
				}
				case 1:
				case 4: {	// new (sub)group
					// finalise previous group building
					NewGroup(name, bob, group, subgroup, buildingGroupType, options);
					
					assert name.Deref() == null && isEmpty(bob) && subgroup.Deref() == null && buildingGroupType.Deref() == -1;
					
					buildingGroupType.Assign(m.group(1).length() == 1? RootGroup : m.group(1).length() == 4? SubGroup : -1);
					
					m.usePattern(NEW_GROUP);
					if (!m.matches())
						throw new SootOptionsParsingException(line);
					
					name.Assign(ExtractName(m));
					bob.append(ExtractDescription(m));
					
					break;
				}
				default:
					throw new SootOptionsParsingException(line);
			}
		}
		
		// done dealing with lines, finalise last group in building state
		NewGroup(name, bob, group, subgroup, buildingGroupType, options);
		
		return options;
	}
	
	private static String ExtractName (final MatchResult r) throws SootOptionsParsingException {
		final String name = r.group(1);
		if (name.isEmpty())
			throw new SootOptionsParsingException(r.group());
		return name;
	}
	
	private static String ExtractDescription (final MatchResult r) throws SootOptionsParsingException {
		final String desc = r.group(2);
		if (desc == null || desc.isEmpty())
			throw new SootOptionsParsingException(r.group());
		return desc;
	}
	
	private static void NewGroup (
			final Ref<String> nameRef,
			final StringBuilder bob,
			final Ref<SootPhaseOptions> groupRef,
			final Ref<SootPhaseOptions> subgroupRef,
			final Ref<Integer> buildingGroupTypeRef,
			final List<SootPhaseOptions> options)
		throws
			SootOptionsParsingException
	{
		if (nameRef.Deref() != null) {
			if (nameRef.Deref().isEmpty() || isEmpty(bob))
				throw new SootOptionsParsingException(nameRef.Deref() + ":" + bob.toString());
			
			assert subgroupRef.Deref() == null;
			subgroupRef.Assign(new SootPhaseOptions(nameRef.Deref(), bob.toString()));
			
			// install group
			switch (buildingGroupTypeRef.Deref()) {
				case RootGroup:
					groupRef.Assign(subgroupRef.Deref());
					options.add(groupRef.Deref());
					break;
				case SubGroup: 
					if (groupRef.Deref() == null)
						throw new SootOptionsParsingException("No group to add subgroup " + nameRef.Deref() + " to");
					groupRef.Deref().AddSubphase(subgroupRef.Deref());
					break;
				default:
					assert false;
			}
			// reset building state
			nameRef.Assign(null);
			reset(bob);
			subgroupRef.Assign(null);
			buildingGroupTypeRef.Assign(-1);
		}
	}

	///////////////////////////////////////////////////////
	// Package
	///////////////////////////////////////////////////////
	
	///////////////////////////////////////////////////////
	// Attributes
	
	static Charset UTF8 () {
		return utf8;
	}
	
	///////////////////////////////////////////////////////
	// Private
	///////////////////////////////////////////////////////
	
	///////////////////////////////////////////////////////
	// Static referers
	
	private static final G G = soot.G.v();
	private static final Charset utf8 = Charset.forName("UTF-8");
	
	///////////////////////////////////////////////////////
	// Static state
	private static final AutoLogger L = new AutoLogger(Loggers.GetLogger(SootFacade.class));
	
	///////////////////////////////////////////////////////
	// Constructors
	
	private SootFacade () {
	}
}
