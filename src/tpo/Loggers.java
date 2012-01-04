package tpo;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import isi.util.logging.Handler;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.LogManager;
import static tpo.Globals.UTF8;
import static tpo.Globals.cwd;

/**
 *
 * @author Amalia
 */
public class Loggers {
	
	///////////////////////////////////////////////////////

	public static Logger GetLogger (final String name) {
		if (loggers.containsKey(name))
			return loggers.get(name);
		
		final Logger logger = Logger.getLogger(name);
		final Object previous = loggers.put(name, logger);
		assert previous == null;
		
		logger.addHandler(handler);
		
		return logger;
	}
	
	public static <T> Logger GetLogger (final Class<T> klass) {
		return GetLogger(klass.getCanonicalName());
	}
	
	///////////////////////////////////////////////////////
	
	public static void Initialise () throws IOException {
		LogManager.getLogManager().readConfiguration(new ByteArrayInputStream(".handlers =\n.level = FINEST\n".getBytes(UTF8)));
		
		final Path outpath = cwd.resolve("out.html");
		Files.deleteIfExists(outpath);
		handler = new Handler(Files.newBufferedWriter(outpath, UTF8));
	}
	
	///////////////////////////////////////////////////////
	
	public static void CleanUp () {
	//	handler.close();
	}
	
	///////////////////////////////////////////////////////
	
	private Loggers () {
	}
	
	///////////////////////////////////////////////////////
	// state
	private static final Map<String, Logger> loggers = new HashMap<>(1000);
	private static Handler handler;
}
