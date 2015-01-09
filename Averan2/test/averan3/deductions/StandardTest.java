package averan3.deductions;

import static averan3.core.Composite.EQUALS;
import static averan3.core.Session.*;
import static averan3.deductions.Standard.*;
import static net.sourceforge.aprog.tools.Tools.getThisMethodName;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import averan3.core.Variable;
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
	
	@Test
	public final void test2() {
		final String deductionName = this.getClass().getName() + "." + getThisMethodName();
		
		build(deductionName, () -> {
			deduce("identity");
			{
				final Variable x = introduce("x");
				
				substitute($$(x, $(), $()));
				rewrite(name(-1), name(-1));
				conclude();
			}
			
			deduce("recall");
			{
				final Variable p = introduce("P");
				
				suppose(p);
				bind("identity", p);
				rewrite(name(-2), name(-1));
				conclude();
			}
			
			{
				final Variable $X = variable("X");
				final Variable $Y = variable("Y");
				
				suppose("left_introduction_of_conjunction",
						$(forall($X, $Y), rule($X, conjunction($X, $Y))));
			}
			
			{
				final Variable $X = variable("X");
				final Variable $Y = variable("Y");
				
				suppose("right_introduction_of_conjunction",
						$(forall($X, $Y), rule($Y, conjunction($X, $Y))));
			}
			
			{
				final Variable $X = variable("X");
				final Variable $Y = variable("Y");
				
				suppose("left_elimination_of_conjunction",
						$(forall($X, $Y), rule(conjunction($X, $Y), $X)));
			}
			
			{
				final Variable $X = variable("X");
				final Variable $Y = variable("Y");
				
				suppose("right_elimination_of_conjunction",
						$(forall($X, $Y), rule(conjunction($X, $Y), $Y)));
			}
			
			{
				final Variable $X = variable("X");
				final Variable $Y = variable("Y");
				
				assertTrue(autoDeduce("commutativity_of_conjunction",
						$(forall($X, $Y), rule(conjunction($X, $Y), conjunction($Y, $X)))));
			}
		}, new ConsoleOutput());
		
	}
	
	@Test
	public final void test3() {
		final String deductionName = this.getClass().getName() + "." + getThisMethodName();
		
		build(deductionName, () -> {
			deduce("identity");
			{
				final Variable x = introduce("x");
				
				substitute($$(x, $(), $()));
				rewrite(name(-1), name(-1));
				conclude();
			}
			
			deduce("recall");
			{
				final Variable p = introduce("P");
				
				suppose(p);
				bind("identity", p);
				rewrite(name(-2), name(-1));
				conclude();
			}
			
			{
				final Variable $X = variable("X");
				final Variable $Y = variable("Y");
				
				suppose("left_introduction_of_disjunction",
						$(forall($X, $Y), rule($X, disjunction($X, $Y))));
			}
			
			{
				final Variable $X = variable("X");
				final Variable $Y = variable("Y");
				
				suppose("right_introduction_of_disjunction",
						$(forall($X, $Y), rule($Y, disjunction($X, $Y))));
			}
			
			{
				final Variable $X = variable("X");
				final Variable $Y = variable("Y");
				final Variable $Z = variable("Z");
				
				suppose("elimination_of_disjunction",
						$(forall($X, $Y, $Z), rule(rule($X, $Z), rule($Y, $Z), rule(disjunction($X, $Y), $Z))));
			}
			
			{
				final Variable $X = variable("X");
				final Variable $Y = variable("Y");
				
				assertTrue(autoDeduce("commutativity_of_disjunction",
						$(forall($X, $Y), rule(disjunction($X, $Y), disjunction($Y, $X)))));
			}
		}, new ConsoleOutput());
		
	}
	
}
