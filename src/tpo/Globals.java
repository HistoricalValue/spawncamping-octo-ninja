package tpo;

import java.io.PrintStream;
import java.nio.charset.Charset;

public class Globals {

	public static final Charset UTF8 = Charset.forName("UTF-8");
	public static final Cwd cwd = new Cwd("./wd");
	public static final PrintStream stdout = System.out;
	
	private Globals () {
	}
}
