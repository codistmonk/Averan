package averan3.deductions;

import static averan3.core.Session.*;
import static averan3.deductions.Reals.*;
import static averan3.deductions.Standard.*;
import static net.sourceforge.aprog.tools.Tools.getThisMethodName;
import static org.junit.Assert.*;

import org.junit.Test;

import averan3.core.Variable;
import averan3.io.ConsoleOutput;

/**
 * @author codistmonk (creation 2015-01-08)
 */
public final class RealsTest {
	
	@Test
	public final void test1() {
		assertNotNull(Reals.DEDUCTION);
	}
	
	@Test
	public final void test2() {
		final String deductionName = this.getClass().getName() + "." + getThisMethodName();
		
		build(deductionName, new Runnable() {
			
			@Override
			public final void run() {
//				setupIdentitySymmetryRecall();
				
				{
					final Variable $P = new Variable("P");
					
					suppose("recall", $$(forall($P), rule($P, $P)));
				}
				
				{
					final Variable $X = variable("X");
					final Variable $Y = variable("Y");
					
					suppose("left_elimination_of_equality",
							$(forall($X, $Y), rule($X, equality($X, $Y), $Y)));
				}
				
				DEBUG = true;
				assertTrue(autoDeduce(rule("a", equality("a", "b"), "b"), 2));
				DEBUG = false;
			}
			
		}, new ConsoleOutput());
	}
	
	@Test
	public final void test3() {
		final String deductionName = this.getClass().getName() + "." + getThisMethodName();
		
		build(deductionName, new Runnable() {
			
			@Override
			public final void run() {
				setupIdentitySymmetryRecall();
				
				{
					final Variable $X = variable("X");
					final Variable $Y = variable("Y");
					
					deduce("left_elimination_of_equality",
							$(forall($X, $Y), rule($X, equality($X, $Y), $Y)));
					{
						intros();
						rewrite(name(-2), name(-1));
						conclude();
					}
				}
				
				{
					final Variable $X = variable("X");
					final Variable $Y = variable("Y");
					
					suppose("left_elimination_of_conjunction",
							$(forall($X, $Y), rule(conjunction($X, $Y), $X)));
					
					suppose("right_elimination_of_conjunction",
							$(forall($X, $Y), rule(conjunction($X, $Y), $Y)));
				}
				
				{
					final Variable $X = variable("X");
					final Variable $m = variable("m");
					final Variable $n = variable("n");
					final Variable $i = variable("i");
					final Variable $j = variable("j");
					
					suppose("definition_of_matrices",
							$(forall($X, $m, $n), $(realMatrix($X, $m, $n), "=", conjunction(
									nonzeroNatural($m),
									nonzeroNatural($n),
									$(forall($i, $j), rule(natural($i, $m), natural($j, $n),
											real(matrixElement($X, $i, $j))))))));
					
					deduce("type_of_matrix_rows",
							$(forall($X, $m, $n), rule(realMatrix($X, $m, $n), nonzeroNatural($m))));
					{
						final Variable x = introduce();
						final Variable m = introduce();
						final Variable n = introduce();
						
						intros();
						
						bind("definition_of_matrices", x, m, n);
						apply("left_elimination_of_equality", name(-2));
						apply(name(-1), name(-2));
						apply("left_elimination_of_conjunction", name(-1));
						
						assertTrue(deduction().canConclude());
						
						cancel();
					}
					
					deduce("type_of_matrix_rows",
							$(forall($X, $m, $n), rule(realMatrix($X, $m, $n), nonzeroNatural($m))));
					{
						intros();
						
//						apply("left_elimination_of_equality", name(-1));
						
						DEBUG = true;
						check(autoDeduce(3));
						DEBUG = false;
						
						conclude();
					}
				}
			}
			
		}, new ConsoleOutput());
	}
	
}
