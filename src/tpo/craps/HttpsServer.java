package tpo.craps;

import isi.net.http.BufferedResponse;
import isi.net.http.ContentType;
import isi.net.http.Request;
import isi.net.http.RequestParser;
import isi.net.http.Response;
import isi.net.http.Status;
import isi.util.Ref;
import isi.util.Strings;
import isi.util.charstreams.Encodings;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;
import tpo.Globals;

import static isi.util.logging.Loggers.L;

public class HttpsServer {

	public static void Main (final String[] args) {
		try {
			if (false)
				HttpsServerHerong.main(args);
			else
				Main0(args);
		} catch (IOException | InterruptedException | NoSuchAlgorithmException | KeyStoreException | UnrecoverableKeyException | KeyManagementException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	@SuppressWarnings("SleepWhileInLoop")
	public static void Main0 (final String[] args) throws IOException, InterruptedException, NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException, KeyManagementException {
		final KeyStore ks = Globals.GetSingleton().GetKeyManager().GetKeystore();
		
		final KeyManagerFactory kmf = KeyManagerFactory.getInstance("PKIX");
		kmf.init(ks, "sandy1".toCharArray());
		
		final TrustManagerFactory tmf = TrustManagerFactory.getInstance("PKIX");
		tmf.init(ks);
		
		final SSLContext ssl = SSLContext.getInstance("SSLv3");
		ssl.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
//		PrintSSLContextInfo(ssl);
		
		final SSLServerSocketFactory serverSocketFactory = ssl.getServerSocketFactory();

		final SSLServerSocket server = (SSLServerSocket) serverSocketFactory.createServerSocket(8000, 5, InetAddress.getLoopbackAddress());
		L().fff(Strings.ToString(server.getSupportedProtocols()));
//		server.setEnabledCipherSuites(new String[] {"SSL_RSA_WITH_3DES_EDE_CBC_SHA"});
//		server.setEnabledProtocols(new String[] {"SSLv3", "TLSv1.2"});
//		server.setEnableSessionCreation(true);
//		server.setUseClientMode(false);
//		server.setNeedClientAuth(false);
//		server.setWantClientAuth(true);

		while (true) {
			L().fff("waiting wwaiting for a victim");
			try (final SSLSocket client = (SSLSocket) server.accept();) {
				final Ref<Boolean> handshakeDone = Ref.CreateRef(Boolean.FALSE);
				client.addHandshakeCompletedListener(new HandshakeCompletedListener() {
					@Override
					public void handshakeCompleted (final HandshakeCompletedEvent e) {
						handshakeDone.Assign(Boolean.TRUE);
						L().fff("Handshake done with " + e.getCipherSuite());
					}
				});
				final SSLSession session = client.getSession();
				try (
						InputStream ins = client.getInputStream();
						Reader insr = new InputStreamReader(ins, Request.Encoding);
						Response resp = new BufferedResponse(Channels.newChannel(client.getOutputStream()));
				) {
					resp.SetContentType(ContentType.Plaintext);
					resp.SetStatus(Status.OK);
					resp.SetEncoding(Encodings.UTF8);
					resp.write(ByteBuffer.wrap(("Hello Nio. This is you\n\n\n\nand this is me\n\n\n\n"
							+ new RequestParser(insr).Parse().toString()
							).getBytes(Encodings.UTF8)));
				}
				catch (Exception ex) {
					throw new IOException(ex);
				}
			}
			catch (final IOException ex) {
				L().fff("closing client because of " + ex.toString());
			}
		}
	}
	
	@SuppressWarnings("UseOfSystemOutOrSystemErr")
	private static void PrintSSLContextInfo (final SSLContext ssl) {
		final SSLParameters params = ssl.getSupportedSSLParameters();
		final Provider provider = ssl.getProvider();
		System.out.printf("--- SSL Context ---\n"
				+ "Protocol                                      : %s\n"
				+ "Provider                                      : %s\n"
				+ "Supported Cipher Suits                        : %s\n"
				+ "Supported End-Point Indentification Algorithm : %s\n"
				+ "Supported Need Client Auth                    : %s\n"
				+ "Supported Want Client Auth                    : %s\n",
				ssl.getProtocol(),
				ssl.getProvider().getName() + ", " + Double.toString(ssl.getProvider().getVersion()) + ", " + provider.getInfo(),
				Strings.ToString(params.getCipherSuites()),
				params.getEndpointIdentificationAlgorithm(),
				Boolean.toString(params.getNeedClientAuth()),
				Boolean.toString(params.getWantClientAuth()));
	}

	private HttpsServer () {
	}

	
	
	
	
	
	
	
	public static class HttpsServerHerong {
		public static void main (final String[] args) {
			mainisid(args);
		}
		/**
		* HttpsHello.java
		* Copyright (c) 2005 by Dr. Herong Yang
		*/
		public static void mainorig(String[] args) {
			String ksName = "C:\\tmp\\security\\kss";
			char ksPass[] = "patrick".toCharArray();
			char ctPass[] = "sandy1".toCharArray();
			try {
				KeyStore ks = KeyStore.getInstance("PKCS12");
				ks.load(new FileInputStream(ksName), ksPass);
				KeyManagerFactory kmf = 
				KeyManagerFactory.getInstance("PKIX"); // "SunX509");
				kmf.init(ks, ctPass);
				SSLContext sc = SSLContext.getInstance("SSLv3");
				sc.init(kmf.getKeyManagers(), null, null);
				SSLServerSocketFactory ssf = sc.getServerSocketFactory();
				SSLServerSocket s 
					= (SSLServerSocket) ssf.createServerSocket(8000);
				System.out.println("Server started:");
				printServerSocketInfo(s);
				// Listening to the port
				SSLSocket c = (SSLSocket) s.accept();
				printSocketInfo(c);
				BufferedWriter w = new BufferedWriter(
					new OutputStreamWriter(c.getOutputStream()));
				BufferedReader r = new BufferedReader(
					new InputStreamReader(c.getInputStream()));
				String m = r.readLine();
				w.write("HTTP/1.0 200 OK");
				w.newLine();
				w.write("Content-Type: text/html");
				w.newLine();
				w.newLine();
				w.write("<html><body>Hello world!</body></html>");
				w.newLine();
				w.flush();
				w.close();
				r.close();
				c.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		public static void mainisid(String[] args) {
			String ksName = "C:\\tmp\\security\\kss";
			char ksPass[] = "patrick".toCharArray();
			char ctPass[] = "sandy1".toCharArray();
			try {
				KeyStore ks = KeyStore.getInstance("PKCS12");
				ks.load(new FileInputStream(ksName), ksPass);
				KeyManagerFactory kmf = 
				KeyManagerFactory.getInstance("SunX509");
				kmf.init(ks, ctPass);
				SSLContext sc = SSLContext.getInstance("SSLv3");
				sc.init(kmf.getKeyManagers(), null, null);
				SSLServerSocketFactory ssf = sc.getServerSocketFactory();
				SSLServerSocket s 
					= (SSLServerSocket) ssf.createServerSocket(8000);
				System.out.println("Server started:");
				printServerSocketInfo(s);
				// Listening to the port
				SSLSocket c = (SSLSocket) s.accept();
				printSocketInfo(c);
				BufferedWriter w = new BufferedWriter(
					new OutputStreamWriter(c.getOutputStream()));
				BufferedReader r = new BufferedReader(
					new InputStreamReader(c.getInputStream()));
				String m = r.readLine();
				w.write("HTTP/1.0 200 OK");
				w.newLine();
				w.write("Content-Type: text/html");
				w.newLine();
				w.newLine();
				w.write("<html><body>Hello world!</body></html>");
				w.newLine();
				w.flush();
				w.close();
				r.close();
				c.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		private static void printSocketInfo(SSLSocket s) {
			System.out.println("Socket class: "+s.getClass());
			System.out.println("   Remote address = "
				+s.getInetAddress().toString());
			System.out.println("   Remote port = "+s.getPort());
			System.out.println("   Local socket address = "
				+s.getLocalSocketAddress().toString());
			System.out.println("   Local address = "
				+s.getLocalAddress().toString());
			System.out.println("   Local port = "+s.getLocalPort());
			System.out.println("   Need client authentication = "
				+s.getNeedClientAuth());
			SSLSession ss = s.getSession();
			System.out.println("   Cipher suite = "+ss.getCipherSuite());
			System.out.println("   Protocol = "+ss.getProtocol());
		}
		private static void printServerSocketInfo(SSLServerSocket s) {
			System.out.println("Server socket class: "+s.getClass());
			System.out.println("   Socker address = "
				+s.getInetAddress().toString());
			System.out.println("   Socker port = "
				+s.getLocalPort());
			System.out.println("   Need client authentication = "
				+s.getNeedClientAuth());
			System.out.println("   Want client authentication = "
				+s.getWantClientAuth());
			System.out.println("   Use client mode = "
				+s.getUseClientMode());
		}

		private HttpsServerHerong () {
		}
	}
}
