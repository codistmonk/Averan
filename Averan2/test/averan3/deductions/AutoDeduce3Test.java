package averan3.deductions;

import static averan3.core.Session.*;
import static java.lang.Math.min;
import static net.sourceforge.aprog.tools.Tools.cast;
import static net.sourceforge.aprog.tools.Tools.getThisMethodName;
import static org.junit.Assert.*;
import averan3.core.Composite;
import averan3.core.Proof.Deduction.Instance;
import averan3.core.Variable;
import averan3.core.Expression;
import averan3.core.Proof;
import averan3.core.Proof.Deduction;
import averan3.io.HTMLOutput;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.aprog.tools.Tools;

import org.junit.Test;

/**
 * @author codistmonk (creation 2015-01-11)
 */
public final class AutoDeduce3Test {
	
	@Test
	public final void test1() {
		final String deductionName = this.getClass().getName() + "." + getThisMethodName();
		
		build(deductionName, new Runnable() {
			
			@Override
			public final void run() {
				AutoDeduce3.deduceFundamentalPropositions();
				
				deduce();
				{
					suppose($("a"));
					assertTrue(autoDeduce($("a"), 1));
					conclude();
				}
				
				deduce();
				{
					final Variable $x = new Variable("x");
					
					suppose($(forall($x), $("p", $x)));
					assertTrue(autoDeduce($("p", "a"), 1));
					conclude();
				}
				
				deduce();
				{
					suppose($("p", "a"));
					assertTrue(autoDeduce($("p", new Variable("x")), 1));
					conclude();
				}
				
				deduce();
				{
					suppose(rule("a", "b"));
					assertTrue(autoDeduce(rule("a", "b"), 1));
					conclude();
				}
				
				deduce();
				{
					suppose($("a"));
					suppose(rule("a", "b"));
					assertTrue(autoDeduce($("b"), 2));
					conclude();
				}
				
				deduce();
				{
					final Variable $x = new Variable("x");
					final Variable $y = new Variable("y");
					
					suppose($(forall($x, $y), rule($($x, "&", $y), $x)));
					suppose($("a", "&", "b"));
					assertTrue(autoDeduce($("a"), 2));
					conclude();
				}
			}
			
		}, new HTMLOutput());
	}
	
	public static final boolean autoDeduce(final Expression<?> goal, final int depth) {
		if (depth <= 0) {
			return false;
		}
		
		for (final Justification  justification : justify(goal)) {
			Tools.debugPrint(justification);
			
			deduce();
			{
				justification.forward(goal, depth);
				
				breakpoint(7);
				
				if (proposition(-1).equals(goal.accept(Variable.RESET))) {
					conclude();
					
					return true;
				} else {
					cancel();
				}
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
				final Expression<?> proposition = proof.getProposition().accept(new Instance());
				
				if (proposition.equals(goal.accept(Variable.RESET))) {
					result.add(new JustificationByRecall(proof.getPropositionName()));
				}
				
				final Composite<Expression<?>> composite = cast(Composite.class, proposition);
				
				if (composite != null) {
					if (composite.getParameters() != null && composite.getContents().accept(Variable.RESET).equals(goal)) {
						result.add(new JustificationByBind(proof.getPropositionName()));
					}
					
					if (composite.getConclusion() != null && composite.getConclusion().accept(Variable.RESET).equals(goal)) {
						result.add(new JustificationByApply(proof.getPropositionName(), 0));
					}
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
		
		public abstract boolean forward(Expression<?> goal, int depth);
		
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
		public final boolean forward(final Expression<?> goal, final int depth) {
			apply("recall", this.getJustificationName());
			
			return true;
		}
		
		private static final long serialVersionUID = -8761694921382622419L;
		
	}
	
	/**
	 * @author codistmonk (creation 2015-01-11)
	 */
	public static final class JustificationByBind extends Justification {
		
		public JustificationByBind(final String name) {
			super(name);
		}
		
		@Override
		public final boolean forward(final Expression<?> goal, final int depth) {
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
	
	/**
	 * @author codistmonk (creation 2015-01-11)
	 */
	public static final class JustificationByApply extends Justification {
		
		private final int depth;
		
		public JustificationByApply(final String name, final int depth) {
			super(name);
			this.depth = depth;
		}
		
		@Override
		public final boolean forward(final Expression<?> goal, final int depth) {
			final Composite<Expression<?>> rule = proposition(this.getJustificationName());
			
			rule.getConclusion().equals(goal);
			
			if (autoDeduce(rule.getCondition().accept(Variable.BIND), depth - 1)) {
				apply(this.getJustificationName(), name(-1));
				
				return true;
			}
			
			return false;
		}
		
		private static final long serialVersionUID = -3626348322127777493L;
		
	}
	
}
