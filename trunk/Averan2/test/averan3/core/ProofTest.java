package averan3.core;

import static averan3.core.Composite.FORALL;
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
			
			deduction.new Supposition(null, c(x, IMPLIES, x)).conclude();
			deduction.new Supposition(null, x).conclude();
			deduction.new ModusPonens(null, deductionName + ".1", deductionName + ".2").conclude();
			
			deduction.conclude();
		} finally {
			export(deduction);
		}
	}
	
	@Test
	public final void test3() {
		final Deduction deduction = new Deduction(null, getThisMethodName(), null);
		
		try {
			final Symbol<String> p = deduction.introduce("P");
			
			{
				final Variable $E = new Variable("E");
				final Variable $F = new Variable("F");
				final Variable $X = new Variable("X");
				final Variable $Y = new Variable("Y");
				
				deduction.new Supposition(null,
						c(
								c(FORALL, $E),
								c(
										c(c(FORALL, $X), $E),
										"->",
										c(
												c(
														c(FORALL, $F, $Y),
														c(c($E, c(c($X, "=", $Y)), c()), "=", $F)
												),
												"->",
												$F
										)
								)
						)).conclude();
			}
			
			deduction.conclude();
		} finally {
			export(deduction);
		}
	}
	
	public static final Composite<Expression<?>> c(final Object... expressions) {
		final Composite<Expression<?>> result = new Composite<>();
		
		for (final Object element : expressions) {
			result.add(element instanceof Expression<?> ? (Expression<?>) element : new Symbol<>(element));
		}
		
		return result;
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
			out.println(indent1 + proof.getProposition().accept(TO_STRING));
			out.println(indent1 + proof);
		}
		
		if (deduction.getGoal() != null) {
			out.println(indent + "Goal: " + deduction.getGoal().accept(TO_STRING));
		}
		
		out.println(indent + ".");
	}
	
	public static final ToString TO_STRING = new ToString();
	
	/**
	 * @author codistmonk (creation 2015-01-05)
	 */
	public static final class ToString implements Expression.Visitor<String> {
		
		@Override
		public final String visit(final Symbol<?> symbol) {
			return symbol.toString();
		}
		
		@Override
		public final String visit(final Variable variable) {
			return variable.getName();
		}
		
		@Override
		public final String visit(final Composite<Expression<?>> composite) {
			if (1 < composite.size() && FORALL.implies(composite.get(0))) {
				final StringBuilder resultBuilder = new StringBuilder().append(FORALL);
				final int n = composite.size();
				
				for (int i = 1; i < n; ++i) {
					if (1 < i) {
						resultBuilder.append(',');
					}
					
					resultBuilder.append(composite.get(i).accept(this));
				}
				
				resultBuilder.append(' ');
				
				return resultBuilder.toString();
			}
			
			return "(" + join("", composite.stream().map(e -> e.accept(this)).toArray()) + ")";
		}
		
		private static final long serialVersionUID = 315809084289227049L;
		
	}
	
}
