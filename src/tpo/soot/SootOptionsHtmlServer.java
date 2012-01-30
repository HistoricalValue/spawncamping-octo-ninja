package tpo.soot;

import isi.net.http.ContentType;
import isi.net.http.Request;
import isi.net.http.Response;
import isi.net.http.Server;
import isi.net.http.Status;
import isi.util.Channels;
import isi.util.Charstreams;
import isi.util.IdGenerator;
import isi.util.Ref;
import isi.util.Runtime;
import isi.util.Strings;
import isi.util.Throwables;
import isi.util.charstreams.Encodings;
import isi.util.charstreams.MultiWriterDelegate;
import isi.util.html.Document;
import isi.util.logging.AutoLogger;
import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

public class SootOptionsHtmlServer {

	///////////////////////////////////////////////////////
	// state
	private final Server server;
	private final RequestHandler handler = new RequestHandler();

	///////////////////////////////////////////////////////
	// Constructors
	public SootOptionsHtmlServer () throws IOException {
		this(8000);
	}

	public SootOptionsHtmlServer (final int port) throws IOException {
		this(port, 1);
	}

	public SootOptionsHtmlServer (final int port, final int backlog) throws IOException {
		this(InetAddress.getLoopbackAddress(), port, backlog);
	}

	public SootOptionsHtmlServer (final InetAddress addr, final int port, final int backlog) throws IOException {
		this(new InetSocketAddress(addr, port), backlog);
	}

	public SootOptionsHtmlServer (final SocketAddress bindAddress, final int backlog) throws IOException {
		server = new Server(bindAddress, backlog, handler);
	}

	///////////////////////////////////////////////////////
	//
	public void ServeLoop () throws IOException {
		final Ref<Boolean> done = Ref.CreateRef(Boolean.FALSE);
		final SootHelpHtmlRenderer sootHelpHtmlRenderer = new SootHelpHtmlRenderer();

		handler.SetDone(done);
		handler.SetSootHelpHtmlRenderer(sootHelpHtmlRenderer);

		while (!done.Deref())
			server.Serve();
	}

	///////////////////////////////////////////////////////
	// static utils
	private static AutoLogger L () {
		return Runtime.GetCurrentLogger();
	}

	///////////////////////////////////////////////////////
	// private types
	private static class RequestHandler implements isi.net.http.RequestHandler {
		///////////////////////////////////////////////
		// Settable state
		private SootHelpHtmlRenderer sootHelpHtmlRenderer;
		private Ref<Boolean> done;
		///////////////////////////////////////////////
		// state
		private final IdGenerator requestIdGenerator = new IdGenerator("request:", "");
		private String requestId;
		private final Charset encodingOfChoice = Encodings.UTF8;

		///////////////////////////////////////////////
		//
		@Override
		@SuppressWarnings({"fallthrough", "ConvertToStringSwitch"})
		public void Handle (final Response response, final Request request, final boolean isDirect) throws IOException {
			requestId = requestIdGenerator.next();
			final Writer client = Channels.newUnclosableWriter(response, encodingOfChoice.newEncoder(), 256);

			final String stylePathName = "γεια σου μπόμπ", jsPathName = "拉&帮/结\\伙+=-";
			final String style = "/" + stylePathName, js = "/" + jsPathName;
			final String path = request.GetPath();

			if (path.equals(style)) {
				response.SetStatus(Status.OK);
				response.SetContentType(ContentType.Css);
				response.SetEncoding(encodingOfChoice);
				ServeFile("style", style, "style.css", "Css.java", sootHelpHtmlRenderer.Css(), client);
			}
			else
			if (path.equals(js)) {
				response.SetStatus(Status.OK);
				response.SetContentType(ContentType.Javascript);
				response.SetEncoding(encodingOfChoice);
				ServeFile("javascript", js, "script.js", "Script.java", sootHelpHtmlRenderer.Javascript(), client);
			}
			else
			switch (path) {
				case "/stop": case "/giveup": case "/shutup":
					L().i(requestId + ": giving up through " + path);
					done.Assign(Boolean.TRUE);
				case "/":
					L().i(requestId + ": serving options");
					try {
						response.SetStatus(Status.OK);
						response.SetContentType(ContentType.Html);
						response.SetEncoding(encodingOfChoice);
						sootHelpHtmlRenderer.WriteOptions(client, stylePathName, jsPathName);
					} catch (final SootOptionsParsingException ex) {
						Document.FromString(Throwables.toString(ex)).WriteTo(client);
					}
					break;
				default:
					L().w(requestId + ": Not found " + path);
					response.SetStatus(Status.NotFound);
			}

			client.flush();
			L().fff(requestId + ": DONE HANDLING");
		}

		///////////////////////////////////////////////
		//
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
			L().i(requestId + ": serving " + servingWhat + " from " + servingPath);

			final Path filepath = Runtime.GetCurrentCwd().resolve(filePathStr);
			if (Files.exists(filepath))
				try (final CharArrayWriter caw = new CharArrayWriter(1 << 19)) {
					try (final Reader cssfin = Files.newBufferedReader(filepath, Request.Encoding)) {
						final Writer allouts = new MultiWriterDelegate(caw, client);
						Charstreams.transfuse(cssfin, allouts);
						allouts.flush();
					}
					try (final Writer csslitfout = Files.newBufferedWriter(Runtime.GetCurrentCwd().resolve(outFilePathStr), Request.Encoding)) {
					try (final Reader r = new CharArrayReader(caw.toCharArray())) {
						csslitfout.append(Strings.ToJavaLiteral(r));
					}}
				}
			else
				try (final Writer w = Files.newBufferedWriter(filepath, Request.Encoding)) {
					final Writer allouts = new MultiWriterDelegate(client, w);
					allouts.append(ifNotFound);
					allouts.flush();
				}
		}

		///////////////////////////////////////////////
		//
		@Override
		public boolean ShouldHandleDirect (final Request request) {
			return false;
		}

		///////////////////////////////////////////////
		//
		public void SetSootHelpHtmlRenderer (final SootHelpHtmlRenderer sootHelpHtmlRenderer) {
			this.sootHelpHtmlRenderer = sootHelpHtmlRenderer;
		}

		public void SetDone (final Ref<Boolean> done) {
			this.done = done;
		}
	}
}
