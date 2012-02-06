package tpo;

import isi.util.Configuration;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import tpo.craps.KeyManager;

public class Globals {
	private KeyManager km = new KeyManager("patrick");
	private Configuration configuration = new Configuration();

	public KeyManager GetKeyManager () { return km; }
	@SuppressWarnings("ReturnOfCollectionOrArrayField")
	public Configuration GetConfiguration () { return configuration; }


	private static Globals instance;
	public static void CreateSingleton () { try { instance = new Globals(); } catch (final NoSuchAlgorithmException | KeyStoreException ex) { instance = null; } }
	public static void ForgetSingleton () { instance = null; }
	public static Globals GetSingleton () { return instance; }

	private Globals () throws NoSuchAlgorithmException, KeyStoreException  {
	}
}
