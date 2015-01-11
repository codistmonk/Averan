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
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

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
					suppose($("c"));
					suppose(rule("c", "d"));
					assertTrue(autoDeduce($("d"), 2));
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
				if (justification instanceof JustificationByApply) {
					String ruleName = justification.getJustificationName();
					final List<Expression<?>> conditions = ((JustificationByApply) justification).getConditionsFor(goal);
					final List<Justification>[] conditionJustifications =
							conditions.stream().map(AutoDeduce3Test::justify).toArray(List[]::new);
					final int n = conditions.size();
					final int[] indices = new int[n];
					
					Tools.debugPrint(conditions);
					Tools.debugPrint(proposition(justification.getJustificationName()));
					Tools.debugPrint(goal);
					
					for (int i = 0; i < n; ++i) {
						boolean ok = false;
						
						for (; indices[i] < conditionJustifications[i].size(); ++indices[i]) {
							ok = autoDeduce(conditions.get(i), 1);
						}
						
						if (ok) {
							apply(ruleName, name(-1));
							ruleName = name(-1);
						} else {
							Tools.debugPrint("TODO");
							
							return false;
						}
					}
				} else {
					justification.forward(goal, depth);
				}
				
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
	
	public static final Iterable<Expression<?>[]> possibleUnifications(final Expression<?>... expressions) {
		return new Iterable<Expression<?>[]>() {
			
			@Override
			public final Iterator<Expression<?>[]> iterator() {
				final int n = expressions.length;
				
				return new Iterator<Expression<?>[]>() {
					
					private final Expression<?>[] result = expressions.clone();
					
					@Override
					public final boolean hasNext() {
						// TODO Auto-generated method stub
						return false;
					}
					
					@Override
					public Expression<?>[] next() {
						// TODO Auto-generated method stub
						return null;
					}
					
				};
			}
			
		};
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
				
				Composite<Expression<?>> composite = cast(Composite.class, proposition);
				
				if (composite != null) {
					if (composite.getParameters() != null) {
						if (composite.getContents().accept(Variable.RESET).equals(goal)) {
							result.add(new JustificationByBind(proof.getPropositionName()));
						} else {
							composite = cast(Composite.class, composite.getContents());
						}
					}
					
					int depth = 0;
					
					while (composite != null && composite.getConclusion() != null) {
						if (composite.getConclusion().accept(Variable.RESET).equals(goal)) {
							Tools.debugPrint(composite);
							Tools.debugPrint(goal);
							result.add(new JustificationByApply(proof.getPropositionName(), depth));
							break;
						}
						
						composite = cast(Composite.class, composite.getConclusion());
						++depth;
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
		
		public final List<Expression<?>> getConditionsFor(final Expression<?> goal) {
			final List<Expression<?>> result = new ArrayList<>();
			Composite<Expression<?>> rule = proposition(this.getJustificationName());
			
			if (rule.getParameters() != null) {
				rule = rule.getContents();
			}
			
			for (int i = 0; i < this.depth; ++i) {
				result.add(rule.getCondition());
				rule = rule.getConclusion();
			}
			
			result.add(rule.getCondition());
			rule.getConclusion().equals(goal);
			
			final int n = result.size();
			
			for (int i = 0; i < n; ++i) {
				result.set(i, result.get(i).accept(Variable.BIND));
			}
			
			return result;
		}
		
		@Override
		public final boolean forward(final Expression<?> goal, final int depth) {
			throw new RuntimeException();
		}
		
		private static final long serialVersionUID = -3626348322127777493L;
		
	}
	
}
