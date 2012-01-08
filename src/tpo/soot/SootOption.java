package tpo.soot;

import isi.util.Collections;
import isi.util.Iterators;
import isi.util.collections.ExternalIteratorIterable;
import isi.util.collections.IterableConcatenationIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SootOption {
	
	///////////////////////////////////////////////////////
	//
	@SuppressWarnings("PublicInnerClass")
	public enum ArgumentType {
		None, List, Enum
	}
	
	@SuppressWarnings("PublicInnerClass")
	public interface Argument {
		ArgumentType GetType ();
		List<String> GetNames ();
		List<EnumArgumentValue> GetValues ();
	}
	
	@SuppressWarnings({"FinalClass", "PublicInnerClass"})
	public static final class NoArgument implements Argument {

		NoArgument () {
		}
		
		@Override
		public ArgumentType GetType () {
			return ArgumentType.None;
		}

		@Override
		public List<String> GetNames () {
			throw new UnsupportedOperationException("no argument");
		}

		@Override
		public List<EnumArgumentValue> GetValues () {
			throw new UnsupportedOperationException("no argument");
		}
	}
	
	@SuppressWarnings({"PublicInnerClass", "FinalClass"})
	public static final class ListArgument implements Argument {

		private final List<String> names;
		
		ListArgument (final List<String> names) {
			this.names = Collections.unmodifiableList(new ArrayList<>(names));
		}
		ListArgument (final String... names) {
			this(Arrays.asList(names));
		}

		@Override
		@SuppressWarnings("ReturnOfCollectionOrArrayField")
		public List<String> GetNames () {
			return names;
		}

		@Override
		public ArgumentType GetType () {
			return ArgumentType.List;
		}

		@Override
		public List<EnumArgumentValue> GetValues () {
			throw new UnsupportedOperationException("string argument -- single name");
		}
		
	}
	
	@SuppressWarnings({"PublicInnerClass", "FinalClass"})
	public static final class EnumArgument implements Argument {
		
		private final List<EnumArgumentValue> values;
		
		EnumArgument (final Iterable<? extends EnumArgumentValue> values) {
			this.values = Collections.unmodifiableList(values);
		}

		@Override
		public ArgumentType GetType () {
			return ArgumentType.Enum;
		}

		@Override
		public List<String> GetNames () {
			throw new UnsupportedOperationException("enum argument -- multiple values, no name list");
		}

		@Override
		@SuppressWarnings("ReturnOfCollectionOrArrayField")
		public List<EnumArgumentValue> GetValues () {
			return values;
		}
		
	}

	@SuppressWarnings({"PublicInnerClass", "FinalClass"})
	public static final class EnumArgumentValue {
		private final List<String> names;
		
		EnumArgumentValue (final Iterable<? extends String> names) {
			this.names = Collections.unmodifiableList(names);
		}
		
		@SuppressWarnings("ReturnOfCollectionOrArrayField")
		public List<String> GetNames () {
			return names;
		}
	}
	
	///////////////////////////////////////////////////////
	//
	public SootOption AppendDescription (final String extra) {
		return new SootOption(shortName, longNames, argument, description + extra);
	}
	
	public SootOption AppendEnumValue (final EnumArgumentValue enumValue) {
		Argument newArgument;
		switch (argument.GetType()) {
			case None:
			case List:
				newArgument = new EnumArgument(Iterators.SingleItem(enumValue));
				break;
			case Enum:
				newArgument = new EnumArgument(Iterators.Concatenate(argument.GetValues(), enumValue));
				break;
			default:
				throw new AssertionError();
		}
		return new SootOption(shortName, longNames, newArgument, description);
	}
	
	///////////////////////////////////////////////////////
	//
	public String GetShortName () {
		return shortName;
	}
	
	public Argument GetArgument () {
		return argument;
	}
	
	///////////////////////////////////////////////////////
	// state
	private final String shortName, description;
	private final List<String> longNames;
	private final Argument argument;
	
	///////////////////////////////////////////////////////
	// package
	///////////////////////////////////////////////////////
	
	///////////////////////////////////////////////////////
	// constructors
	SootOption (final String shortName, final Iterable<? extends String> longNames, final Argument argument, final String description) {
		this.shortName = shortName;
		this.longNames = isi.util.Collections.unmodifiableList(longNames);
		this.argument = argument;
		this.description = description;
	}
			
}
