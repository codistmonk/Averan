package averan3.deductions;

import static averan3.core.Composite.EQUALS;
import static averan3.core.Session.*;
import static averan3.deductions.Standard.*;
import static net.sourceforge.aprog.tools.Tools.getThisMethodName;

import org.junit.Test;

import averan3.deductions.Standard;
import averan3.io.ConsoleOutput;

/**
 * @author codistmonk (creation 2015-01-07)
 */
public final class StandardTest {
	
	@Test
	public final void test1() {
		final String deductionName = this.getClass().getName() + "." + getThisMethodName();
		
		build(deductionName, () -> {
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
