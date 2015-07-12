package averan.draft3.core;

import static averan.draft3.core.Composite.IMPLIES;
import static averan.draft3.core.Session.*;
import static multij.tools.Tools.getThisMethodName;
import averan.draft3.core.Session;
import averan.draft3.core.Symbol;
import averan.draft3.core.Variable;
import averan.draft3.core.Proof.Deduction;
import averan.draft3.io.ConsoleOutput;

import org.junit.Test;

/**
 * @author codistmonk (creation 2015-01-04)
 */
public final class ProofTest {
	
	@Test(expected=IllegalStateException.class)
	public final void test1() {
		final String deductionName = this.getClass().getName() + "." + getThisMethodName();
		final Deduction deduction = new Deduction(null, deductionName, null);
		
		try {
			final Variable x = deduction.introduce("X");
			
			deduction.new Supposition(null, x).conclude();
			
			deduction.conclude();
		} finally {
			export(deduction);
		}
	}
	
	@Test
	public final void test2() {
		final String deductionName = this.getClass().getName() + "." + getThisMethodName();
		final Deduction deduction = new Deduction(null, deductionName, null);
		
		try {
			final Symbol<String> x = new Symbol<>("X");
			
			deduction.new Supposition(null, $(x, IMPLIES, x)).conclude();
			deduction.new Supposition(null, x).conclude();
			deduction.new ModusPonens(null, deductionName + ".1", deductionName + ".2").conclude();
			
			deduction.conclude();
		} finally {
			export(deduction);
		}
	}
	
	@Test
	public final void test3() {
		final String deductionName = this.getClass().getName() + "." + getThisMethodName();
		final Deduction deduction = new Deduction(null, deductionName, null);
		
		try {
			{
				final Variable $E = new Variable("E");
				final Variable $F = new Variable("F");
				final Variable $X = new Variable("X");
				final Variable $Y = new Variable("Y");
				
				deduction.new Supposition("bind1",
						$(
								forall($E),
								$(
										$(forall($X), $E),
										IMPLIES,
										$(
												$(
														forall($F, $Y),
														$($($E, list($($X, "=", $Y)), list()), "=", $F)
												),
												IMPLIES,
												$F
										)
								)
						)).conclude();
			}
			
			{
				final Deduction subDeduction = new Deduction(deduction, "recall", null);
				{
					final Variable p = subDeduction.introduce("P");
					
					subDeduction.new Supposition(null, p).conclude();
					final String suppositionName = subDeduction.findProof(-1).getPropositionName();
					subDeduction.new Substitution(null, $$(p, $(), $())).conclude();
					final String identityName = subDeduction.findProof(-1).getPropositionName();
					subDeduction.new Rewrite(null, identityName).using(identityName).conclude();
					subDeduction.new Rewrite(null, suppositionName).using(identityName).conclude();
					
					subDeduction.conclude();
				}
			}
			
			deduction.conclude();
		} finally {
			export(deduction);
		}
	}
	
	public static final void export(final Deduction deduction) {
		final boolean debug = false;
		
		if (debug) {
			Session.export(deduction, new ConsoleOutput());
		} else {
			Session.export(deduction, Output.NOP);
		}
	}
	
}
