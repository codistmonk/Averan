package averan2.core;

import static averan2.core.Session.*;
import static averan2.core.Session.Stack.*;
import static averan2.core.Symbol.symbol;
import static averan2.core.Variable.variable;
import averan2.core.Expression;
import averan2.core.Module;
import averan2.core.Session;
import averan2.core.Variable;
import averan2.io.ConsoleOutput;
import averan2.io.SessionExporter;
import averan2.modules.Standard;
import net.sourceforge.aprog.tools.Tools;

import org.junit.Test;

/**
 * @author codistmonk (creation 2014-12-27)
 */
public final class SessionTest {
	
	@Test
	public final void test1() {
		pushSession(new Session());
		
		try {
			deduce("test");
			{
				final Variable $X = variable("X");
				
				deduce("recall", new Module().suppose($X).conclude($X));
				{
					final Expression<?> x = introduce();
					
					introduce();
					
					substitute(x);
					rewrite(name(-1), name(-1));
					rewrite(name(-3), name(-1));
				}
				
				deduce();
				{
					suppose(symbol("Y"));
					apply("recall", name(-1));
					conclude();
				}
			}
		} finally {
			SessionExporter.export(popSession(), new ConsoleOutput());
		}
	}
	
	@Test
	public final void test2() {
		pushSession(new Session());
		
		try {
			include(Standard.MODULE);
			
			{
				final Variable $X = variable("X");
				final Variable $Y = variable("Y");
				final Variable $Z = variable("Z");
				
				deduce("transitivity_of_implication",
						$($($X, "->", $Y), "->", $($Y, "->", $Z), "->", $($X, "->", $Z)));
				{
					Tools.debugPrint(goal());
					final Expression<?> x = introduce();
					stop();
					introduce();
					final Expression<?> y = introduce();
//					introduce();
//					final Expression<?> x = introduce();
					
					
//				check(autoDeduce($($X, "->", $Z)));
				}
			}
			
			deduce("test");
			{
				suppose($("A", "->", "B"));
				suppose($("B", "->", "C"));
				
				check(autoDeduce($("A", "->", "C")));
			}
		} finally {
			SessionExporter.export(popSession(), new ConsoleOutput());
		}
	}
	
}
