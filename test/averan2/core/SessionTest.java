package averan2.core;

import static averan2.core.Session.*;
import static averan2.core.Session.Stack.*;
import static averan2.core.Symbol.symbol;
import static averan2.core.Variable.variable;

import averan2.core.Module;
import averan2.core.Session;
import averan2.core.Variable;
import averan2.io.ConsoleOutput;
import averan2.io.SessionExporter;
import averan2.modules.Standard;

import multij.tools.Tools;

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
				
				deduce("recall", new Module().parametrize($X).suppose($X).conclude($X));
				{
					intros();
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
			
			deduce(Tools.getThisMethodName());
			{
				final Variable $X = variable("X");
				final Variable $Y = variable("Y");
				final Variable $Z = variable("Z");
				
				check(autoDeduce("transitivity_of_implication",
						$(forAll($X, $Y, $Z), $($($X, "->", $Y), "->", $($Y, "->", $Z), "->", $($X, "->", $Z))), 4));
			}
		} finally {
			SessionExporter.export(popSession(), new ConsoleOutput());
		}
	}
	
}
