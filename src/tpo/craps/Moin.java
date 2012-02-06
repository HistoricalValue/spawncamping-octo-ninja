package tpo.craps;

import isi.util.Configuration;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Map;
import tpo.Globals;

public class Moin {

	@SuppressWarnings({"UseOfSystemOutOrSystemErr", "CallToThreadRun"})
	public static void CrapsMain (final String[] args)
			throws
			InterruptedException,
			IOException,
			NoSuchAlgorithmException,
			CertificateException,
			UnrecoverableEntryException,
			KeyStoreException
	{
		InitialiseConfiguration();
		Globals.GetSingleton().GetKeyManager().Load();
		final Options opts = new Options(args);

		final Thread server = CreateServerThread(args);
		final Thread client = CreateClientThread(args);

		if (opts.threaded) {
			if (opts.mode.isServer()) {
				server.start();
				Thread.sleep(2000);
			}
			if (opts.mode.isClient())
				client.start();

			server.join();
			client.join();
		}
		else
		if (opts.mode.isServer())
			server.run();
		else
		if (opts.mode.isClient())
			client.run();
		else
			System.out.println("Cannot be an unthreaded server-client");
	}

	private static Thread CreateClientThread (final String[] args) {
		return new Thread() {
			@Override
			public void run () {
				HttpsClient.Main(Arrays.copyOf(args, args.length));
			}
		};
	}

	private static Thread CreateServerThread (final String[] args) {
		return new Thread() {
			@Override
			public void run () {
				try {
					HttpsServer.Main(Arrays.copyOf(args, args.length));
				}
				catch (final Exception ex) {
					throw new RuntimeException(ex);
				}
			}
		};
	}

	private static void InitialiseConfiguration () {
		final Configuration configuration = Globals.GetSingleton().GetConfiguration();
		configuration.SetStoragePath(isi.util.Runtime.GetCurrentCwd().resolve("config.config"));
		final boolean loaded = configuration.LoadIFEmpty();
		final boolean failure = configuration.GetFailureReason() != null;
		assert !failure || !loaded;

		if (failure) { // set defaults
			final Map<String, String> config = configuration.GetConfig();
			config.put("https.server.keys", "C:\\tmp\\security\\kss");
		}
	}

	private static class Options {
		enum Mode {
			server(true, false),
			client(false, true),
			servent(false, false);

			private final boolean isServer;
			private final boolean isClient;
			Mode (final boolean isServer, final boolean isClient) {
				this.isServer = isServer;
				this.isClient = isClient;
			}

			public boolean isServer () { return isServer; }
			public boolean isClient () { return isClient; }
		};
		public final boolean threaded;
		public final Mode mode;

		Options (final String[] args) {
			threaded = Boolean.valueOf(args[0]);
			mode = Mode.valueOf(args[1]);
		}
	}

	private Moin () {
	}
}
