package tpo.craps;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import tpo.Globals;

public class KeyManager {

	///////////////////////////////////////////////////////
	// state
	private final KeyStore ks = KeyStore.getInstance("pkcs12");
	private final char[] keystorePasswd;

	///////////////////////////////////////////////////////
	//
	public KeyManager (
			final String storePasswd
		) throws
			NoSuchAlgorithmException,
			KeyStoreException
	{
		this.keystorePasswd = storePasswd.toCharArray();
	}

	///////////////////////////////////////////////////////
	//
	public boolean Load () throws IOException, NoSuchAlgorithmException, CertificateException  {
		final Path keysPath = isi.util.Runtime.GetCurrentCwd().resolve(Globals.GetSingleton().GetConfiguration().GetConfig().get("https.server.keys"));
		boolean loaded;

		if (Files.exists(keysPath))
			try (final InputStream ins = Files.newInputStream(keysPath, StandardOpenOption.READ)) {
				ks.load(ins, keystorePasswd);
				loaded = true;
			}
		else
			loaded = false;

		return loaded;
	}

	///////////////////////////////////////////////////////
	//
	@SuppressWarnings("ReturnOfCollectionOrArrayField")
	public char[] GetKeystorePassword () {
		return keystorePasswd;
	}

	public KeyStore GetKeystore () {
		return ks;
	}
}
