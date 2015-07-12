package averan3.core;

import static averan3.core.Session.*;
import static averan3.deductions.AutoDeduce.autoDeduce;
import static multij.tools.Tools.getThisMethodName;
import static org.junit.Assert.*;

import averan3.deductions.AutoDeduce3;
import averan3.deductions.Standard;
import averan3.io.ConsoleOutput;

import org.junit.Ignore;
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
			AutoDeduce3.deduceFundamentalPropositions();
			
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
				final Variable $y = new Variable("y");
				
				suppose($(forall($x, $y), rule($($x, " is real"), $($y, " is real"), $($($x, "+", $y), " is real"))));
			}
			
			assertTrue(autoDeduce($(rule($("0", " is real"), $($("0", "+", "0"), " is real")))));
			
			{
				final Variable $x = new Variable("x");
				final Variable $y = new Variable("y");
				final Variable $m = new Variable("m");
				final Variable $n = new Variable("n");
				final Variable $o = new Variable("o");
				
				suppose($(forall($x, $y, $m, $n, $o), rule($($x, IS_MATRIX, $m, "×", $n),
						$($y, IS_MATRIX, $n, "×", $o),
						$($($x, $y), IS_MATRIX, $m, "×", $o))));
			}
			
			{
				assertTrue(autoDeduce(rule($("a", IS_MATRIX, "i", "×", "j"),
						$("b", IS_MATRIX, "j", "×", "k"),
						$($("a", "b"), IS_MATRIX, "i", "×", "k"))));
			}
			
			{
				assertTrue(autoDeduce(rule($("a", IS_MATRIX, "i", "×", "j"),
						$("b", IS_MATRIX, "j", "×", "k"),
						$($("a", "b"), IS_MATRIX, new Variable("m?"), "×", new Variable("n?")))));
			}
		}, new ConsoleOutput());
	}
	
	@Ignore
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
				
				suppose($(forall($x, $y, $m, $n, $o), rule($($x, IS_MATRIX, $m, "×", $n),
						$($y, IS_MATRIX, $n, "×", $o),
						$($($x, $y), IS_MATRIX, $m, "×", $o))));
			}
			
			{
				assertTrue(autoDeduce(rule($("a", IS_MATRIX, "i", "×", "j"),
						$("b", IS_MATRIX, "j", "×", "k"),
						$("c", IS_MATRIX, "k", "×", "l"),
						$($("a", $("b", "c")), IS_MATRIX, new Variable("m?"), "×", new Variable("n?")))));
			}
		}, new ConsoleOutput());
	}
	
	@Ignore
	@Test
	public final void test7() {
		final String deductionName = this.getClass().getName() + "." + getThisMethodName();
		
		build(deductionName, () -> {
			include(Standard.DEDUCTION);
			
			{
				final Variable $x = new Variable("x");
				final Variable $y = new Variable("y");
				final Variable $m = new Variable("m");
				final Variable $n = new Variable("n");
				final Variable $o = new Variable("o");
				
				suppose($(forall($x, $y, $m, $n, $o), rule($($x, IS_MATRIX, $m, "×", $n),
						$($y, IS_MATRIX, $n, "×", $o),
						$($($x, $y), IS_MATRIX, $m, "×", $o))));
			}
			
			{
				assertTrue(autoDeduce(rule($("A", IS_MATRIX, "i", "×", "j"),
						$("B", IS_MATRIX, "j", "×", "k"),
						$("C", IS_MATRIX, "k", "×", "l"),
						$("D", IS_MATRIX, "l", "×", "m"),
						$("E", IS_MATRIX, "m", "×", "n"),
						$($($($("A", "B"), "C"), $("D", "E")), IS_MATRIX, new Variable("m?"), "×", new Variable("n?")))));
			}
		}, new ConsoleOutput());
	}
	
	@Ignore
	@Test
	public final void test8() {
		final String deductionName = this.getClass().getName() + "." + getThisMethodName();
		
		build(deductionName, new Runnable() {
			
			@Override
			public final void run() {
				AutoDeduce3.deduceFundamentalPropositions();
				
				deduce();
				{
					final Variable x = new Variable("x");
					
					suppose(x);
					
					assertSame(x, proof(-1).getProposition());
					
					apply("recall", name(-1));
					
					assertSame(x, proof(-1).getProposition());
					
					cancel();
				}
			}
			
		}, new ConsoleOutput());
	}
	
	public static final String IS_MATRIX = " is a matrix of size ";
	
}
