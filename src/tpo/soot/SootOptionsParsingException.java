package tpo.soot;

/**
 *
 * Thrown in case that soot options/phases output format changes and the
 * facade is not able to understand them.
 *
 * @author Amalia
 */
public class SootOptionsParsingException extends Exception {
	private static final long serialVersionUID = 1L;

	public SootOptionsParsingException (final String msg) {
		super(msg);
	}

}
