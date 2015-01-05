package averan3.core;

import static averan3.core.Composite.IMPLIES;
import static java.util.Collections.nCopies;
import static net.sourceforge.aprog.tools.Tools.getThisMethodName;
import static net.sourceforge.aprog.tools.Tools.join;

import averan3.core.Proof.Deduction;

import java.io.PrintStream;

import org.junit.Test;

/**
 * @author codistmonk (creation 2015-01-04)
 */
public final class ProofTest {
	
	@Test(expected=IllegalStateException.class)
	public final void test1() {
		final Deduction deduction = new Deduction(null, getThisMethodName(), null);
		
		try {
			final Symbol<String> x = deduction.introduce("X");
			
			deduction.new Supposition(null, x).conclude();
			
			deduction.conclude();
		} finally {
			export(deduction);
		}
	}
	
	@Test
	public final void test2() {
		final String deductionName = getThisMethodName();
		final Deduction deduction = new Deduction(null, deductionName, null);
		
		try {
			final Symbol<String> x = new Symbol<>("X");
			
			deduction.new Supposition(null, new Composite<>().add(x).add(IMPLIES).add(x)).conclude();
			deduction.new Supposition(null, x).conclude();
			deduction.new ModusPonens(null, deductionName + ".1", deductionName + ".2").conclude();
			
			deduction.conclude();
		} finally {
			export(deduction);
		}
	}
	
	public static final void export(final Deduction deduction) {
		export(deduction, 0, System.out);
	}
	
	public static final void export(final Deduction deduction, final int level, final PrintStream out) {
		final String indent = join("", nCopies(level, '	'));
		final String indent1 = indent + '	';
		
		out.println(indent + "Deduce (" + deduction.getPropositionName() + ")");
		
		if (!deduction.getProtoparameters().isEmpty()) {
			out.println(indent1 + '∀' + join(",", deduction.getProtoparameters()));
		} else if (deduction.getRootParameters() != null) {
			out.println(indent1 + deduction.getRootParameters());
		}
		
		for (final Proof proof : deduction.getProofs()) {
			out.println(indent + "(" + proof.getPropositionName() + ")");
			out.println(indent1 + proof.getProposition());
			out.println(indent1 + proof);
		}
		
		if (deduction.getGoal() != null) {
			out.println(indent + "Goal: " + deduction.getGoal());
		}
		
		out.println(indent + ".");
	}
	
}
