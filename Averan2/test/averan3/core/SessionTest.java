package averan3.core;

import static averan3.core.Session.*;
import static averan3.deductions.Standard.*;
import static net.sourceforge.aprog.tools.Tools.getThisMethodName;
import static org.junit.Assert.assertTrue;

import averan3.deductions.Standard;
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
				suppose(rule("a", "b"));
				suppose($("a"));
				apply(name(-2), name(-1));
				conclude();
			}
			
			deduce(rule(rule("a", "b"), "a", "b"));
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
			include(Standard.DEDUCTION);
			
			deduce(rule(rule("a", "b"), rule("b", "c"), rule("a", "c")));
			{
				intros();
				assertTrue(autoDeduce());
				conclude();
			}
			
			deduce();
			{
				suppose(rule("a", "b"));
				suppose(rule("b", "c"));
				deduce(rule("a", "c"));
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
	
	@Test
	public final void test3() {
		final String deductionName = this.getClass().getName() + "." + getThisMethodName();
		
		build(deductionName, () -> {
			include(Standard.DEDUCTION);
			
			deduce(rule(rule("a", "b"), rule("b", "c"), rule("a", "c")));
			{
				assertTrue(autoDeduce());
				conclude();
			}
		}, new ConsoleOutput());
	}
	
	@Test
	public final void test4() {
		final String deductionName = this.getClass().getName() + "." + getThisMethodName();
		
		build(deductionName, () -> {
			include(Standard.DEDUCTION);
			
			deduce(rule(rule("a", "b"), rule("b", "c"), rule("c", "d"), rule("a", "d")));
			{
				assertTrue(autoDeduce());
				conclude();
			}
		}, new ConsoleOutput());
	}
	
	@Test
	public final void test5() {
		final String deductionName = this.getClass().getName() + "." + getThisMethodName();
		
		build(deductionName, () -> {
			include(Standard.DEDUCTION);
			
			{
				final Variable $x = new Variable("x");
				
				suppose($(forall($x), rule($($x, " is real"), $($($x, "+", $x), " is real"))));
			}
			
			assertTrue(autoDeduce($(rule($("0", " is real"), $($("0", "+", "0"), " is real")))));
			
			{
				final Variable $x = new Variable("x");
				final Variable $y = new Variable("y");
				final Variable $m = new Variable("m");
				final Variable $n = new Variable("n");
				final Variable $o = new Variable("o");
				
				suppose($(forall($x, $y, $m, $n, $o), rule($($x, " is matrix ", $m, "×", $n),
						$($y, " is matrix ", $n, "×", $o),
						$($($x, $y), " is matrix ", $m, "×", $o))));
			}
			
			{
				assertTrue(autoDeduce(rule($("a", " is matrix ", "i", "×", "j"),
						$("b", " is matrix ", "j", "×", "k"),
						$($("a", "b"), " is matrix ", "i", "×", "k"))));
			}
			
			{
				assertTrue(autoDeduce(rule($("a", " is matrix ", "i", "×", "j"),
						$("b", " is matrix ", "j", "×", "k"),
						$($("a", "b"), " is matrix ", new Variable("m?"), "×", new Variable("n?")))));
			}
		}, new ConsoleOutput());
	}
	
	@Test
	public final void test6() {
		final String deductionName = this.getClass().getName() + "." + getThisMethodName();
		
		build(deductionName, () -> {
			include(Standard.DEDUCTION);
			
			{
				final Variable $x = new Variable("x");
				final Variable $y = new Variable("y");
				final Variable $m = new Variable("m");
				final Variable $n = new Variable("n");
				final Variable $o = new Variable("o");
				
				suppose($(forall($x, $y, $m, $n, $o), rule($($x, " is matrix ", $m, "×", $n),
						$($y, " is matrix ", $n, "×", $o),
						$($($x, $y), " is matrix ", $m, "×", $o))));
			}
			
			{
				assertTrue(autoDeduce(rule($("a", " is matrix ", "i", "×", "j"),
						$("b", " is matrix ", "j", "×", "k"),
						$("c", " is matrix ", "k", "×", "l"),
						$($("a", $("b", "c")), " is matrix ", new Variable("m?"), "×", new Variable("n?")))));
			}
		}, new ConsoleOutput());
	}
	
}
