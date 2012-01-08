package tpo;

import isi.net.http.Request;
import isi.net.http.RequestHandler;
import isi.net.http.Response;
import isi.net.http.Server;
import isi.net.http.Status;
import isi.util.Ref;
import isi.util.Throwables;
import isi.util.logging.AutoLogger;
import java.io.IOException;
import java.io.Writer;
import java.net.ServerSocket;
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
		final Server s = new Server(new ServerSocket(8000));
		final Ref<Boolean> done = Ref.CreateRef(Boolean.FALSE);
		s.AddHandler(new RequestHandler() {
			@Override
			public void Handle (final Response response, final Writer client, final Request request) throws IOException {
				response.SetStatus(Status.OK);
				
				switch (request.GetPath()) {
					case "/stop":
					case "/giveup":
					case "/shutup":
						done.Assign(Boolean.TRUE);
						client.write("GOODBYE!");
						break;
					default:
						try {
							new SootHelpHtmlRenderer(client).WriteOptions();
						} catch (final SootOptionsParsingException ex) {
							client.write(Throwables.toString(ex));
						}
				}
			}
		});
		
		L.fff(soot.options.Options.v().getUsage());
		while (!done.Deref())
			s.Serve();
	}
	

	private Main () {
		L = new AutoLogger(Loggers.GetLogger(Main.class));
	}
	
	private final AutoLogger L;
}
