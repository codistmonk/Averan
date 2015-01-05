package averan3.core;

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
	
	@Test
	public final void test() {
		final Deduction deduction = new Deduction(null, getThisMethodName(), null);
		
		try {
			final Symbol<String> x = deduction.introduce("X");
			
			deduction.new Supposition(null, x).conclude();
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
		final String indent2 = indent1 + '	';
		
		out.println(indent + "Deduce (" + deduction.getName() + ")");
		
		if (!deduction.getProtoparameters().isEmpty()) {
			out.println(indent2 + '∀' + join(",", deduction.getProtoparameters()));
		}
		
		for (final Proof proof : deduction.getProofs()) {
			out.println(indent1 + "(" + proof.getName() + ")");
			out.println(indent1 + proof);
			out.println(indent2 + proof.getProposition());
		}
		
		if (deduction.getGoal() != null) {
			out.println(indent + "Goal: " + deduction.getGoal());
		}
		
		out.println(indent + ".");
	}
	
}
