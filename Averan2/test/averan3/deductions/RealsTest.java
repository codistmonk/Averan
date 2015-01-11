package averan3.deductions;

import static averan3.core.Session.*;
import static averan3.deductions.Reals.*;
import static averan3.deductions.Standard.*;
import static net.sourceforge.aprog.tools.Tools.cast;
import static net.sourceforge.aprog.tools.Tools.getThisMethodName;
import static org.junit.Assert.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.aprog.tools.Tools;

import org.junit.Test;

import averan3.core.Composite;
import averan3.core.Expression;
import averan3.core.Proof;
import averan3.core.Proof.Deduction;
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
				
//				DEBUG = true;
//				assertTrue(autoDeduce(rule("a", equality("a", "b"), "b"), 2));
//				DEBUG = false;
				
				deduce(rule("a", equality("a", "b"), "b"));
				{
					assertTrue(autoDeduce2(goal(), 2));
					assertTrue(deduction().canConclude());
					cancel();
				}
				
				deduce(rule("a", equality("a", "b"), "b"));
				{
					introduce();
					assertTrue(autoDeduce2(goal(), 2));
					assertTrue(deduction().canConclude());
					cancel();
				}
				
				deduce(rule("a", equality("a", "b"), "b"));
				{
					introduce();
					introduce();
					assertTrue(autoDeduce2(goal(), 2));
					assertTrue(deduction().canConclude());
					cancel();
				}
			}
			
		}, new ConsoleOutput());
	}
	
	public static final boolean autoDeduce2(final Expression<?> goal, final int depth) {
		if (depth <= 0) {
			return false;
		}
		
		for (final Justification justification : justify(goal)) {
			Tools.debugPrint(justification);
			if (justification.justify(goal, depth) && (deduction().canConclude() || autoDeduce2(goal(), depth - 1))) {
				return true;
			}
		}
		
		return false;
	}
	
	public static final List<Justification> justify(final Expression<?> goal) {
		final List<Justification> result = new ArrayList<>();
		Deduction deduction = deduction();
		
		while (deduction != null) {
			final List<Proof> proofs = deduction.getProofs();
			
			for (int i = proofs.size() - 1; 0 <= i; --i) {
				final Proof proof = proofs.get(i);
				Tools.debugPrint(goal);
				if (proof.getProposition().accept(Variable.RESET).equals(goal.accept(Variable.RESET))) {
					result.add(new JustificationByRecall(proof.getPropositionName()));
				}
				
				Composite<Expression<?>> composite = cast(Composite.class, proof.getProposition());
				
				if (composite != null && composite.getParameters() != null) {
					if (composite.getContents().accept(Variable.RESET).equals(goal.accept(Variable.RESET))) {
						result.add(new JustificationByBind(proof.getPropositionName()));
					} else {
						composite = composite.getContents();
					}
				}
				
				while (composite != null) {
					final Expression<?> conclusion = composite.getConclusion();
					
					if (conclusion != null && conclusion.accept(Variable.RESET).equals(goal.accept(Variable.RESET))) {
						result.add(new JustificationByApply(proof.getPropositionName()));
						break;
					}
					
					composite = cast(Composite.class, conclusion);
				}
			}
			
			deduction = deduction.getParent();
		}
		
		
		return result;
	}
	
	/**
	 * @author codistmonk (creation 2015)
	 */
	public static abstract class Justification implements Serializable {
		
		private final String justificationName;
		
		protected Justification(final String name) {
			this.justificationName = name;
		}
		
		public final String getJustificationName() {
			return this.justificationName;
		}
		
		public abstract boolean justify(Expression<?> goal, int depth);
		
		@Override
		public final String toString() {
			return this.getClass().getSimpleName() + " (" + this.justificationName + ")";
		}
		
		private static final long serialVersionUID = 4949397716206009683L;
		
	}
	
	/**
	 * @author codistmonk (creation 2015-01-11)
	 */
	public static final class JustificationByRecall extends Justification {
		
		public JustificationByRecall(final String name) {
			super(name);
		}
		
		@Override
		public final boolean justify(final Expression<?> goal, final int depth) {
			apply("recall", this.getJustificationName());
			
			return true;
		}
		
		private static final long serialVersionUID = -8761694921382622419L;
		
	}
	
	/**
	 * @author codistmonk (creation 2015-01-11)
	 */
	public static final class JustificationByApply extends Justification {
		
		public JustificationByApply(final String name) {
			super(name);
		}
		
		@Override
		public final boolean justify(final Expression<?> goal, final int depth) {
			if (depth <= 0) {
				return false;
			}
			
			Composite<Expression<?>> rule = proposition(this.getJustificationName());
			
			if (rule.getParameters() != null) {
				rule = rule.getContents();
			}
			
			Tools.debugPrint(rule);
			
			rule.getConclusion().equals(goal.accept(Variable.RESET));
			
			final Expression<?> boundCondition = rule.getCondition().accept(Variable.BIND);
			
			Tools.debugPrint(boundCondition);
			
			for (final Justification justification : RealsTest.justify(boundCondition)) {
				if (justification.justify(boundCondition, depth - 1)) {
					apply(this.getJustificationName(), name(-1));
					return true;
				}
			}
			
			return false;
		}
		
		private static final long serialVersionUID = -3626348322127777493L;
		
	}
	
	/**
	 * @author codistmonk (creation 2015-01-11)
	 */
	public static final class JustificationByBind extends Justification {
		
		public JustificationByBind(final String name) {
			super(name);
		}
		
		@Override
		public final boolean justify(final Expression<?> goal, final int depth) {
			final Composite<?> block = proposition(this.getJustificationName());
			
			block.getContents().equals(goal.accept(Variable.RESET));
			
			final List<Expression<?>> values = new ArrayList<>();
			final Composite<Expression<?>> parameters = block.getParameters();
			final int n = parameters.getListSize();
			
			for (int i = 1; i < n; ++i) {
				final Variable parameter = (Variable) parameters.getListElement(i);
				
				values.add(parameter.getMatch());
			}
			
			bind(this.getJustificationName(), values.toArray(new Expression[n - 1]));
			
			return true;
		}
		
		private static final long serialVersionUID = -1870583995665152073L;
		
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
