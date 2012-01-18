package tpo.soot.util;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.file.Path;

public class StoringOutputCapturer extends OutputCapturer {

	///////////////////////////////////////////////////////
	///////////////////////////////////////////////////////

	///////////////////////////////////////////////////////
	// Constructors

	public StoringOutputCapturer (final Path path, final Charset encoding) {
		super(path, encoding);
		decoder = encoding.newDecoder();
	}

	///////////////////////////////////////////////////////
	//

	public String GetStoredContent () {
		try {
			return decoder.decode(ByteBuffer.wrap(baouts.GetBuf())).toString();
		} catch (final CharacterCodingException ex) {
			return "Decoding error: " + isi.util.Throwables.toString(ex);
		}
	}

	///////////////////////////////////////////////////////
	///////////////////////////////////////////////////////

	///////////////////////////////////////////////////////
	// state
	private final Baouts baouts = new Baouts(1 << 17);
	private final CharsetDecoder decoder;

	///////////////////////////////////////////////////////
	// types
	private final static class Baouts extends ByteArrayOutputStream {
		Baouts (final int capacity) {
			super(capacity);
		}

		byte[] GetBuf () {
			return buf;
		}
	}
}
