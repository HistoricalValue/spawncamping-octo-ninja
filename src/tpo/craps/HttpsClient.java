package tpo.craps;

import isi.util.Stringifier;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;

import static isi.util.logging.Loggers.L;

public class HttpsClient {

	public static void Main (final String[] args) { try {
		final URI uri = new URI("https", null, "localhost", 8000, null, null, null);
		final URL url = uri.toURL();
		final HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

		connection.setUseCaches(false);
		//
		connection.setInstanceFollowRedirects(false);

		L().fff(""
				+ "\n[Request Properties]: " + ToString(connection.getRequestProperties())
				+ "\n[Request Method]    : " + ToString(connection.getRequestMethod())
				+ "\n[Class]             : " + ToString(connection.getClass()));

		L().fff("\n\n connecting to " + connection.getURL() + " ... ");

		connection.connect();
		L().fff("\n"
				+ "\ncontent-length  : " + Long.toString(connection.getContentLengthLong())
				+ "\ncontent-type    : " + connection.getContentType()
				+ "\n[Header Fields] : " + ToString(connection.getHeaderFields())
				+ "\n[Cipher Suit]   : " + connection.getCipherSuite());

		connection.getInputStream();

	}
	catch (final URISyntaxException | IOException ex) { throw new RuntimeException(ex); }
	}

	private static String ToString (final Object o) {
		return new Stringifier().ToString(o);
	}

	private HttpsClient () {
	}

}
