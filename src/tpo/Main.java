package tpo;

import isi.util.logging.AutoLogger;
import java.io.IOException;
import tpo.soot.SootFacade;
import tpo.soot.SootHelpHtmlRenderer;
import tpo.soot.SootOptionsParsingException;
import tpo.soot.util.OutputCapturer;

public class Main {
	
	public static void main (String[] args) throws IOException, SootOptionsParsingException, Exception {
		Loggers.Initialise();
		final OutputCapturer sootOutputCapturer = SootFacade.CaptureOutput(Globals.cwd.resolve("soot_out.txt"), true, false);
		new Main().Run();
		Loggers.CleanUp();
	}
	
	private void Run () throws IOException, SootOptionsParsingException, Exception {
		new SootHelpHtmlRenderer(new isi.util.streams.VoidWriter()).WriteOptions();
	}
	

	private Main () {
		L = new AutoLogger(Loggers.GetLogger(Main.class));
	}
	
	private final AutoLogger L;
}
