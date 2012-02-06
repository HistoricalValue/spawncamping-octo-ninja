package tpo.util;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Handler;
import java.util.logging.LogRecord;


public class LogHandler extends Handler {

	///////////////////////////////////////////////////////

	private static class Formatter extends java.util.logging.Formatter {

		///////////////////////////////////////////////////

		private static SimpleDateFormat CreateDateFormat () {
			final SimpleDateFormat f =  (SimpleDateFormat) DateFormat.getDateTimeInstance();
			f.applyPattern("yyyy/MM/dd/HH:mm:ss:SSS");
			return f;
		}

		///////////////////////////////////////////////////
		// State
		private final Date d = new Date();
		private final SimpleDateFormat geekDateFormat = CreateDateFormat();
		private final StringBuffer bob = new StringBuffer(1 << 14);
		private final FieldPosition fp = new FieldPosition(0);

		///////////////////////////////////////////////////

		@Override
		public String format (final LogRecord record) {
			bob.delete(0, Integer.MAX_VALUE);
			d.setTime(record.getMillis());

			bob.append("[");
			geekDateFormat.format(d, bob, fp);
			return bob
					.append("]:")
					.append(record.getLoggerName())
					.append(":")
					.append(formatMessage(record))
					.toString();
		}
	}

	///////////////////////////////////////////////////////
	// State
	@SuppressWarnings("UseOfSystemOutOrSystemErr")
	private final PrintStream			out		= System.out;
	private final Formatter				f		= new Formatter();
	private final Charset				utf8	= Charset.forName("UTF-8");
	private final CharsetEncoder		enc		= utf8.newEncoder();
	private final CharBuffer			cbuf	= CharBuffer.allocate(1 << 14);
	private final ByteBuffer			buf		= ByteBuffer.allocate(1 << 14);
	private final WritableByteChannel	fout	=
			Files.newByteChannel(
					Paths.get("wd1", "log.txt"),
					StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING,
					StandardOpenOption.WRITE);

	///////////////////////////////////////////////////////

	@Override
	public void publish (final LogRecord record) {
		try {
			final String formattedRecord = f.format(record);
			out.println(formattedRecord);

			final int length = formattedRecord.length();
			for (int i = 0; i < length; ) {
				assert i <= length;
				cbuf.clear();
				cbuf.append(formattedRecord, i, Math.min(i + cbuf.capacity(), length));
				cbuf.flip();
				i += cbuf.remaining();

				buf.clear();

				for (CoderResult codingResult = enc.encode(cbuf, buf, false); codingResult == CoderResult.OVERFLOW; buf.clear(), codingResult = enc.encode(cbuf, buf, false)) {
					buf.flip();
					fout.write(buf);
				}
				buf.flip();
				fout.write(buf);
			}
		}
		catch (final IOException ioex) {
			throw new RuntimeException(ioex);
		}
	}

	///////////////////////////////////////////////////////

	@Override
	public void flush () {
		out.flush();
	}

	///////////////////////////////////////////////////////

	@Override
	public void close () {
		try {
			fout.close();
		} catch (final IOException ioex) {
			throw new RuntimeException(ioex);
		}
	}

	///////////////////////////////////////////////////////

	public LogHandler () throws IOException {
	}
}
