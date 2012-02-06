package tpo;

import isi.util.Runtime;
import isi.util.logging.AutoLogger;
import isi.util.logging.Loggers;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;

public class Main {

	public static void main (final String[] args) throws SecurityException, IOException, InterruptedException, NoSuchAlgorithmException, CertificateException, UnrecoverableEntryException, KeyStoreException {
		Runtime.PushDefault();
		Runtime.cd(args[0]);
		Loggers.Initialise();
		Globals.CreateSingleton();
		{
			TpoMain(args);
		//	Moin.CrapsMain(Arrays.copyOfRange(args, 1, args.length));
		}
		Globals.ForgetSingleton();
		Loggers.CleanUp();
		Runtime.PopRuntime();
		Runtime.PopRuntime();
	}

	public static void TpoMain (String[] args) throws SecurityException, IOException {
//		final OutputCapturer sootOutputCapturer = SootFacade.CaptureOutput(Runtime.GetCurrentCwd().resolve("soot_out.txt"), true, false);
		java.util.logging.LogManager.getLogManager().readConfiguration(new ByteArrayInputStream((""
				+ "handlers = tpo.util.LogHandler\n"
				+ ".level = ALL\n"
				+ "").getBytes()));
		new Main().Run();
	}

	private void Run () throws IOException {
//		new tpo.soot.SootOptionsHtmlServer().ServeLoop();
		new SootTest().run();
	}


	private Main () {
		L = Loggers.GetLogger(Main.class);
	}

	private final AutoLogger L;
}
