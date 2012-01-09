package tpo;

import isi.net.http.Request;
import isi.net.http.RequestHandler;
import isi.net.http.Response;
import isi.net.http.Server;
import isi.net.http.Status;
import isi.util.Charstreams;
import isi.util.Ref;
import isi.util.Strings;
import isi.util.Throwables;
import isi.util.charstreams.MultiWriterDelegate;
import isi.util.logging.AutoLogger;
import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
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
			@SuppressWarnings("fallthrough")
			public void Handle (final Response response, final Writer client, final Request request) throws IOException {
				response.SetStatus(Status.OK);
				final String style = "/hello bob";
				final String path = request.GetPath();
				if (path.equals(style)) {
					System.out.println("serving style");
					final Path csspath = Globals.cwd.resolve("style.css");
					if (Files.exists(csspath))
						try (final CharArrayWriter caw = new CharArrayWriter(1 << 19)) {
							try (final Reader cssfin = Files.newBufferedReader(csspath, Request.CHARSET)) {
							try (final Writer allouts = new MultiWriterDelegate(caw, client)) {
								Charstreams.transfuse(cssfin, allouts);
							}}
							try (final Writer csslitfout = Files.newBufferedWriter(Globals.cwd.resolve("Css.java"), Request.CHARSET)) {
							try (final Reader r = new CharArrayReader(caw.toCharArray())) {
								csslitfout.append(Strings.ToJavaLiteral(r));
							}}
						}
					else
						try (final Writer w = Files.newBufferedWriter(csspath, Request.CHARSET)) {
						try (final Writer allouts = new MultiWriterDelegate(client, w)) {
							allouts.append(SootHelpHtmlRenderer.CSS);
						}}
				}
				else
				switch (path) {
					case "/stop":
					case "/giveup":
					case "/shutup":
						System.out.println("giving up");
						done.Assign(Boolean.TRUE);
					default:
						System.out.println("serving options");
						try {
							new SootHelpHtmlRenderer(client).WriteOptions(style);
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
