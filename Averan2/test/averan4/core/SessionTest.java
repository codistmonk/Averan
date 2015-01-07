package averan4.core;

import static averan4.core.Composite.*;
import static averan4.core.Session.*;
import static net.sourceforge.aprog.tools.Tools.getThisMethodName;

import averan4.io.ConsoleOutput;

import org.junit.Test;

/**
 * @author codistmonk (creation 2015-01-06)
 */
public final class SessionTest {
	
	@Test
	public final void test1() {
		final String deductionName = this.getClass().getName() + "." + getThisMethodName();
		
		build(deductionName, () -> {
			deduce("test1");
			{
				suppose($("a", IMPLIES, "b"));
				suppose($("a"));
				apply(name(-2), name(-1));
				conclude();
			}
			
			deduce("test2", $($("a", IMPLIES, "b"), IMPLIES, $("a", IMPLIES, "b")));
			{
				intros();
				apply(name(-2), name(-1));
				conclude();
			}
		}, new ConsoleOutput());
	}
	
}
