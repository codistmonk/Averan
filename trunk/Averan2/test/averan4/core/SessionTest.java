package averan4.core;

import static averan4.core.Composite.*;
import static averan4.core.Session.*;
import static averan4.deductions.Standard.*;
import static net.sourceforge.aprog.tools.Tools.getThisMethodName;

import averan4.deductions.Standard;
import averan4.io.ConsoleOutput;

import org.junit.Test;

/**
 * @author codistmonk (creation 2015-01-06)
 */
public final class SessionTest {
	
	@Test
	public final void test1() {
		build(this.getClass().getName() + "." + getThisMethodName(), () -> {
			include(Standard.DEDUCTION);
			
			bind1("test1", "recall", $("toto"));
			
			bind1("test2", "symmetry_of_equality", $("toto"));
			
			deduce("test3");
			{
				suppose($("a", EQUALS, "b"));
				suppose($("a"));
				rewrite1(name(-1), name(-2));
				conclude();
			}
			
			deduce("test4");
			{
				suppose($("a", EQUALS, "b"));
				suppose($("b"));
				rewriteRight(name(-1), name(-2));
				conclude();
			}
		}, new ConsoleOutput());
	}
	
}
