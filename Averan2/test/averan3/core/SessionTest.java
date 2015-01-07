package averan3.core;

import static averan3.core.Composite.*;
import static averan3.core.Session.*;
import static net.sourceforge.aprog.tools.Tools.getThisMethodName;

import averan3.io.ConsoleOutput;

import org.junit.Test;

/**
 * @author codistmonk (creation 2015-01-06)
 */
public final class SessionTest {
	
	@Test
	public final void test1() {
		final String deductionName = this.getClass().getName() + "." + getThisMethodName();
		
		build(deductionName, () -> {
			deduce();
			{
				suppose($("a", IMPLIES, "b"));
				suppose($("a"));
				apply(name(-2), name(-1));
				conclude();
			}
			
			deduce($($("a", IMPLIES, "b"), IMPLIES, $("a", IMPLIES, "b")));
			{
				intros();
				apply(name(-2), name(-1));
				conclude();
			}
		}, new ConsoleOutput());
	}
	
	@Test
	public final void test2() {
		final String deductionName = this.getClass().getName() + "." + getThisMethodName();
		
		build(deductionName, () -> {
			deduce();
			{
				suppose($("a", IMPLIES, "b"));
				suppose($("b", IMPLIES, "c"));
				deduce($("a", IMPLIES, "c"));
				{
					intros();
					apply(name(-3), name(-1));
					apply(name(-3), name(-1));
					conclude();
				}
				conclude();
			}
		}, new ConsoleOutput());
	}
	
}
