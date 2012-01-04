package tpo;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author Amalia
 */
public class Cwd {
	
	///////////////////////////////////////////////////////
	// state
	private Path cwd;
	
	///////////////////////////////////////////////////////
	// constructors
	public Cwd (final Path cwd) {
		this.cwd = cwd;
	}
	
	public Cwd (final String cwd) {
		this(Paths.get(cwd));
	}
	
	///////////////////////////////////////////////////////
	//
	
	public Path GetPath () {
		return cwd;
	}
	
	///////////////////////////////////////////////////////
	// Path proxy
	public Path resolve (final String path) {
		return cwd.resolve(path);
	}
}
