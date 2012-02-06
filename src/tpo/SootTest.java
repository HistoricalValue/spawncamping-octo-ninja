package tpo;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SootTest implements Runnable {

	@Override
	public void run () {
		final ArrayList<String> a = new ArrayList<>(200);
		// General Options
		a.add("-whole-program"	);
//		a.add("-app"			);
		a.add("-verbose"		);
		// Input Options
//		a.add("-soot-classpath"	);	a.add(Paths.get("wd1", "classes").toString());
		a.add("-src-prec"		);	a.add("only-class"		);
		a.add("-main-class"		);	a.add("sample.Sample"	);
		// Output Options
		a.add("-output-dir"		);	a.add(Paths.get("wd1", "sooted").toString());
//		a.add("-dump-body"		);	a.add("jtp");
		a.add("-output-format"	);	a.add("jimple");
		// Processing Options
//		a.add("-exclude"		);	a.add("java");
		a.add("-exclude"		);	a.add("sample.SharedMemoryTPO");
		a.add("-exclude"		);	a.add("sample.Foo");
		// Application Mode Options
		a.add("-trim-cfgs"		);
		// input files
		a.add("sample.Sample"	);

		Loagger.log(Level.FINER, "[cmd]soot: {0}", a.toString());

		soot.Main.main(a.toArray(new String[a.size()]));
	}

	private final static Logger Loagger = Logger.getLogger(SootTest.class.getCanonicalName());
}
