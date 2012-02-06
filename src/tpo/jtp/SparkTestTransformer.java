package tpo.jtp;

import java.util.Formatter;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import soot.Body;
import soot.Local;
import soot.PatchingChain;
import soot.PointsToAnalysis;
import soot.Unit;
import soot.util.Chain;

public class SparkTestTransformer extends soot.BodyTransformer {

	///////////////////////////////////////////////////////
	// Public fields
	public static final String PhaseName = "jtp.isi_test00";

	@Override
	protected void internalTransform (final Body b, final String phaseName, final Map options) {
		if (PhaseName.equals(phaseName)) {
			final StringBuilder bob = new StringBuilder(1 << 14);
			final Formatter f = new Formatter(bob);
			
			final Chain<Local> locals = b.getLocals();
			
			final PointsToAnalysis pta = soot.Scene.v().getPointsToAnalysis();
			
		}
		else
			Loagger.log(Level.WARNING, "Trying to transform the body of an unknown phase ({0} vs {1})", new Object[] {PhaseName, phaseName});
	}

	///////////////////////////////////////////////////////
	// static utils
	private final static Logger Loagger = Logger.getLogger(SparkTestTransformer.class.getCanonicalName());
}
