package tpo;

import isi.util.logging.AutoLogger;
import java.io.IOException;
import java.util.List;
import tpo.soot.SootFacade;
import tpo.soot.SootOptionsParsingException;
import tpo.soot.SootPhaseOptions;
import tpo.soot.util.OutputCapturer;

public class Main {
	
	public static void main (String[] args) throws IOException, SootOptionsParsingException {
		Loggers.Initialise();
		final OutputCapturer sootOutputCapturer = SootFacade.CaptureOutput(Globals.cwd.resolve("soot_out.txt"), true, false);
		new Main().Run();
		Loggers.CleanUp();
	}
	
	private void Run () throws IOException, SootOptionsParsingException {
		L.fff(soot.options.Options.v().getPhaseList());
		L.i(AppendAllOptionsTo(SootFacade.ListOfPhases(), new StringBuilder(1 << 17)));
	}
	
	private StringBuilder AppendAllOptionsTo (final List<SootPhaseOptions> opts, final StringBuilder bob) {
		return AppendAllOptionsTo(opts, bob, 0);
	}
	private StringBuilder AppendAllOptionsTo (final List<SootPhaseOptions> opts, final StringBuilder bob, final int level) {
		for (final SootPhaseOptions opt: opts)
			AppendOptionsTo(opt, bob, level);
		return bob;
	}
	private StringBuilder AppendOptionsTo (final SootPhaseOptions opt, final StringBuilder bob, final int level) {
		return AppendAllOptionsTo(
				opt.GetSubphases(),
				bob.append(
					String.format(
						"%-"
							+ Integer.toString(level + 1)
							+ "s%-"
							+ Integer.toString(31)
							+ "s%s\n",
						"",
						opt.GetName(),
						opt.GetDescription())),
				level + 4);
	}

	private Main () {
		L = new AutoLogger(Loggers.GetLogger(Main.class));
	}
	
	private final AutoLogger L;
}
