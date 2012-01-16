package tpo.soot;

import isi.util.logging.Loggers;
import static isi.util.Collections.map;
import static isi.util.Collections.select;
import isi.util.OnceSettable.GetUnsetOrSetSetException;
import isi.util.OnceSettable;
import isi.util.streams.StreamTokeniserTokenType;
import java.util.LinkedList;
import java.util.List;
import isi.util.Ref;
import isi.util.StreamTokenisers;
import isi.util.ValueMapper;
import isi.util.logging.AutoLogger;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import soot.G;
import tpo.soot.SootOption.EnumArgumentValue;
import tpo.soot.util.OutputCapturer;
import tpo.soot.util.StoringOutputCapturer;
import static isi.util.StringBuilders.isEmpty;
import static isi.util.StringBuilders.reset;
import static isi.util.Matchers.matches;
import static java.util.Objects.requireNonNull;

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
	
	public static List<SootPhaseOptions> ListOfPhases () throws SootOptionsParsingException {
		final Pattern NEW_GROUP = Pattern.compile("^  ? ? ?([\\w.-]+)\\s*(.*)$");
		final Matcher m = INDENT.matcher("");
		
		final List<SootPhaseOptions> options = new LinkedList<>();
		
		// group in the making info
		Ref<String> name = Ref.CreateRef(null);
		final StringBuilder bob = new StringBuilder(1 << 14);
		Ref<SootPhaseOptions> group = Ref.CreateRef(null), subgroup = Ref.CreateRef(null);
		Ref<Integer> buildingGroupType = Ref.CreateRef(-1);
		
		for (final String line: NL.split(soot.options.Options.v().getPhaseList())) {
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
	//
	
	public static List<SootOptionGroup> ListOfOptions () throws SootOptionsParsingException {
		final List<SootOptionGroup> groups = new LinkedList<>();
		final Matcher m = INDENT.matcher("");
		final Pattern GROUP_TITLE = Pattern.compile("^(.*):\\s*$");
		
		// building state
		boolean firstGroup = true, firstOption = true;
		String groupName = null;
		List<SootOption> group = new LinkedList<>();
		SootOption optionBeingBuilt = null;
		boolean startedEnumValues = false;
		
		for (final String line: NL.split(soot.options.Options.v().getUsage())) {
			m.usePattern(INDENT);
			m.reset(line);
			
			if (!m.matches())
				throw new SootOptionsParsingException(line);
			
			final int indentLength = m.group(1).length();
			switch (indentLength) {
				case 0: {	// new group or empty line
					if (line.isEmpty()) 
						{} // nade
					else {
						// finalise previous group being built
						if (!firstGroup) {
							groups.add(new SootOptionGroup(groupName, group));
							group.clear();
						}
						firstGroup = false;
						
						m.usePattern(GROUP_TITLE);
						if (!matches(m, GROUP_TITLE))
							throw new SootOptionsParsingException(line + "{{not-a-group-title-line}}");
						groupName = m.group(1);
					}
					break;
				}
				case 2: {	// new option
					// "finalise" previously being-built option
					if (!firstOption)
						group.add(requireNonNull(optionBeingBuilt));
					firstOption = false;
					
					//
					optionBeingBuilt = ParseOptionLine(line);
					startedEnumValues = false;
					break;
				}
				case 5: {	// new enum argument value
					optionBeingBuilt = optionBeingBuilt.AppendEnumValue(ParseEnumValueLine(line));
					startedEnumValues = true;
					break;
				}
				case 31: {	// description continuation
					if (startedEnumValues)
						throw new SootOptionsParsingException(line + "{{cannot-go-back-to-descrition-after-enum-values}}");
					
					final String description = line.substring(31);
					optionBeingBuilt = optionBeingBuilt.AppendDescription(description);
					break;
				}
				default:
					throw new SootOptionsParsingException(line + "{{unrecognisable-leading-whitespace}}");
			}
		}
		
		return groups;
	}
	
	private static SootOption ParseOptionLine (final String line) throws SootOptionsParsingException {
		final Pattern	OPTION_NAME = Pattern.compile("-([\\w-]+)"),
						ARGUMENT_NAME = Pattern.compile("([A-Z:]+)");
		final Matcher m = OPTION_NAME.matcher("");
		final StreamTokenizer tok = new StreamTokenizer(new StringReader(line));
		StreamTokenisers.SetStreamTokeniserWordMode(tok);
		
		// building state
		final OnceSettable<String> shortName = new OnceSettable<>();
		final List<String> longNames = new LinkedList<>();
		final ArrayList<OnceSettable<String>> argumentsNames = new ArrayList<>(2);
		argumentsNames.add(new OnceSettable<String>());
		argumentsNames.add(new OnceSettable<String>());
		int argumentIndex = -3000;
		final OnceSettable<Boolean> descriptionStarted = new OnceSettable<>();
		String description = null;
		
		try {
			for (StreamTokeniserTokenType tt = StreamTokeniserTokenType.valueOf(tok.nextToken()); tt != StreamTokeniserTokenType.EOF; tt = StreamTokeniserTokenType.valueOf(tok.nextToken())) {
				if (tt != StreamTokeniserTokenType.WORD)
					throw new SootOptionsParsingException(line + "{{@" + tok.toString() + "}}");

				final String token = tok.sval;

				if (matches(m, OPTION_NAME, token)) {	// option name
					if (descriptionStarted.IsSet())
						throw new SootOptionsParsingException(line + "{{option-name-after-description}}");
					final String name = m.group(1);
					
					if (!shortName.IsSet())
						shortName.Set(name);
					else
						longNames.add(name);
					
					argumentIndex = 0;
				}
				else
				if (matches(m, ARGUMENT_NAME, token)) {	// option argument name
					if (descriptionStarted.IsSet())
						throw new SootOptionsParsingException(line + "{{option-argument-name-after-description}}");
					if (!shortName.IsSet())
						throw new SootOptionsParsingException(line + "{{option-argument-name-without-specified-option}}");
					
					final OnceSettable<String> argumentName = argumentsNames.get(argumentIndex);
					if (argumentName.IsSet())
						if (!argumentName.Get().equals(token))
							throw new SootOptionsParsingException(line + "{{" + "different-argument-option-names " + argumentName.Get() + " " + token + "}}");
						else
							{}
					else
						argumentName.Set(token);
					
					++argumentIndex;
				}
				else {	// going to description
					descriptionStarted.Set(Boolean.TRUE);
					description = StreamTokenisers.GetLine(tok);
				}
			}
		}
		catch (final GetUnsetOrSetSetException | IOException ex) {
			throw new AssertionError("", ex);
		}
		
		final List<String> argumentsNamesStrings =
				select(
					map(
						argumentsNames,
						new ValueMapper<OnceSettable<String>, String>() {
							@Override
							public String map (final OnceSettable<String> v) {
								return v.GetIfSet();
							}
						}
					)
				);
		final SootOption.Argument argument = argumentsNamesStrings.isEmpty()?
				new SootOption.NoArgument() : new SootOption.ListArgument(argumentsNamesStrings);
				
		if (description == null)	// could happen for too long first option lines
			description = "";
		
		return new SootOption(shortName.GetIfSet(), longNames, argument, description);
	}
	
	private static SootOption.EnumArgumentValue ParseEnumValueLine (final String line) throws SootOptionsParsingException {
		final Matcher m = ENUM_VALUE.matcher(line);
		if (!m.matches())
			throw new SootOptionsParsingException(line + "{{unparsable-enum-value-line}}");
	
		// TODO check for "(default)" and mark default enum value
		try {
			return new EnumArgumentValue(m.group(2), StreamTokenisers.ReadAllWordTokensToTheEnd(StreamTokenisers.New(m.group(1))));
		} catch (final IOException ex) {
			throw new AssertionError("", ex);
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
	private static final int RootGroup = 347823;
	private static final int SubGroup = 38471;
	private static final Pattern	INDENT = Pattern.compile("^(\\s*).*$"),
									NL = Pattern.compile("\n"),
									WHITESPACE = Pattern.compile("\\s"),
									ENUM_VALUE = Pattern.compile("^\\s{5}(.{28})([A-Z].*)$");
	
	///////////////////////////////////////////////////////
	// Constructors
	
	private SootFacade () {
	}
}
