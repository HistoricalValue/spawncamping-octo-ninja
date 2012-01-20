package tpo;

import isi.util.logging.AutoLogger;
import isi.util.logging.Loggers;
import isi.util.Runtime;
import java.io.IOException;
import tpo.soot.SootFacade;
import tpo.soot.util.OutputCapturer;

public class Main {

	public static void main (final String[] args) throws SecurityException, IOException {
		Runtime.PushDefault();
		TpoMain(args);
		Runtime.PopRuntime();
	}

	public static void TpoMain (String[] args) throws SecurityException, IOException {
		Runtime.cd("wd");

		Loggers.Initialise();
		final OutputCapturer sootOutputCapturer = SootFacade.CaptureOutput(Runtime.GetCurrentCwd().resolve("soot_out.txt"), true, false);
		new Main().Run();
		Loggers.CleanUp();

		Runtime.PopRuntime();
	}

	private void Run () throws IOException {
		new tpo.soot.SootOptionsHtmlServer().ServeLoop();
	}


	private Main () {
		L = Loggers.GetLogger(Main.class);
	}

	private final AutoLogger L;
}
