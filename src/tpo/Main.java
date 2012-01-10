package tpo;

import isi.net.http.ContentType;
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
import isi.util.html.Document;
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
			@SuppressWarnings({"fallthrough", "ConvertToStringSwitch"})
			public void Handle (final Response response, final Writer client, final Request request) throws IOException {
				response.SetStatus(Status.OK);
				final String style = "/hello bob", js = "/拉帮结伙";
				final String path = request.GetPath();
				if (path.equals(style)) {
					response.SetContentType(ContentType.Css);
					ServeFile("style", style, "style.css", "Css.java", SootHelpHtmlRenderer.CSS, client);
				}
				else
				if (path.equals(js)) {
					response.SetContentType(ContentType.Javascript);
					ServeFile("javascript", js, "script.js", "Script.java", SootHelpHtmlRenderer.Javascript, client);
				}
				else
				switch (path) {
					case "/stop": case "/giveup": case "/shutup":
						System.out.println("giving up through " + path);
						done.Assign(Boolean.TRUE);
					default:
						System.out.println("serving options");
						try {
							response.SetContentType(ContentType.Html);
							new SootHelpHtmlRenderer(client).WriteOptions(style, js);
						} catch (final SootOptionsParsingException ex) {
							Document.FromString(Throwables.toString(ex)).WriteTo(client);
						}
				}
			}
			
			private void ServeFile (
					final String		servingWhat,
					final String		servingPath,
					final String		filePathStr,
					final String		outFilePathStr,
					final String		ifNotFound,
					final Writer		client
				)
					throws IOException
			{
				System.out.println("serving " + servingWhat + " from " + servingPath);

				final Path filepath = Globals.cwd.resolve(filePathStr);
				if (Files.exists(filepath))
					try (final CharArrayWriter caw = new CharArrayWriter(1 << 19)) {
						try (final Reader cssfin = Files.newBufferedReader(filepath, Request.CHARSET)) {
						try (final Writer allouts = new MultiWriterDelegate(caw, client)) {
							Charstreams.transfuse(cssfin, allouts);
						}}
						try (final Writer csslitfout = Files.newBufferedWriter(Globals.cwd.resolve(outFilePathStr), Request.CHARSET)) {
						try (final Reader r = new CharArrayReader(caw.toCharArray())) {
							csslitfout.append(Strings.ToJavaLiteral(r));
						}}
					}
				else
					try (final Writer w = Files.newBufferedWriter(filepath, Request.CHARSET)) {
					try (final Writer allouts = new MultiWriterDelegate(client, w)) {
						allouts.append(ifNotFound);
					}}
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
