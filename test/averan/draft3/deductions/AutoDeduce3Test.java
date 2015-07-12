package averan.draft3.deductions;

import static averan.draft3.core.Session.*;
import static java.util.Collections.nCopies;
import static java.util.stream.Collectors.toList;
import static multij.tools.Tools.cast;
import static multij.tools.Tools.getThisMethodName;
import static multij.tools.Tools.ignore;
import static multij.tools.Tools.join;
import static org.junit.Assert.*;
import averan.draft3.core.Composite;
import averan.draft3.core.Expression;
import averan.draft3.core.Proof;
import averan.draft3.core.Variable;
import averan.draft3.core.Proof.Deduction;
import averan.draft3.core.Proof.FreeVariablePreventsConclusionException;
import averan.draft3.core.Proof.Deduction.Instance;
import averan.draft3.core.Proof.Deduction.Supposition;
import averan.draft3.deductions.AutoDeduce3;
import averan.draft3.io.HTMLOutput;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import multij.tools.Tools;

import org.junit.Ignore;
import org.junit.Test;

/**
 * @author codistmonk (creation 2015-01-11)
 */
@Ignore
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
	
	@Test
	public final void test2() {
		final String deductionName = this.getClass().getName() + "." + getThisMethodName();
		
		build(deductionName, new Runnable() {
			
			@Override
			public final void run() {
				AutoDeduce3.deduceFundamentalPropositions();
				
				deduce();
				{
					final Variable $x = new Variable("x");
					final Variable $y = new Variable("y");
					
					suppose($(forall($x, $y), rule($($x, "&", $y), $x)));
					suppose($(forall($x, $y), rule($($x, "&", $y), $y)));
					assertTrue(autoDeduce($(forall($x, $y), rule($($x, "&", $y), $x)), 1));
					conclude();
				}
				
				{
					final Variable $x = new Variable("x");
					final Variable $y = new Variable("y");
					
					suppose("left_elim", $(forall($x, $y), rule($($x, "&", $y), $x)));
					suppose("right_elim", $(forall($x, $y), rule($($x, "&", $y), $y)));
					suppose("intro", $(forall($x, $y), rule($x, $y, $($x, "&", $y))));
				}
				
				deduce($(rule($("a", "&", "b"), $("b", "&", "a"))));
				{
					intros();
					assertTrue(autoDeduce(goal(), 3));
					conclude();
				}
				
				{
					final Variable $x = new Variable("x");
					final Variable $y = new Variable("y");
					
					deduce($(forall($x), rule($($x, "&", $y), $($y, "&", $x))));
					{
						intros();
						assertTrue(autoDeduce(goal(), 3));
						conclude();
					}
				}
			}
			
		}, new HTMLOutput());
	}
	
	
	@Test
	public final void test3() {
		final String deductionName = this.getClass().getName() + "." + getThisMethodName();
		
		build(deductionName, new Runnable() {
			
			@Override
			public final void run() {
				AutoDeduce3.deduceFundamentalPropositions();
				
				deduce();
				{
					final Variable $x = new Variable("x");
					final Variable $y = new Variable("y");
					
					suppose("left_intro", $(forall($x, $y), rule($x, $($x, "|", $y))));
					assertTrue(autoDeduce(rule(new Variable("z"), $("a", "|", "b")), 1));
					conclude();
				}
				
//				{
//					final Variable $x = new Variable("x");
//					final Variable $y = new Variable("y");
//					final Variable $z = new Variable("z");
//					
////					suppose("left_intro", $(forall($x, $y), rule($x, $($x, "|", $y))));
//					suppose("right_intro", $(forall($x, $y), rule($y, $($x, "|", $y))));
////					suppose("elim", $(forall($x, $y, $z), rule(rule($x, $z), rule($y, $z), $($x, "|", $y), $z)));
//				}
//				
//				Tools.debugPrint("###########################");
//				Tools.debugPrint("###########################");
//				Tools.debugPrint("###########################");
//				Tools.debugPrint("###########################");
//				Tools.debugPrint("###########################");
//				
//				deduce($(rule("a", $("b", "|", "a"))));
//				{
//					intros();
//					assertTrue(autoDeduce(goal(), 2));
//					conclude();
//				}
			}
			
		}, new HTMLOutput());
	}
	
	public static final boolean autoDeduce(final Expression<?> goal, final int depth) {
		return autoDeduce(goal, depth, null);
	}
	
	public static final boolean autoDeduce(final Expression<?> goal, final int depth, final String[] recall) {
		if (depth <= 0) {
			return false;
		}
		
		final String indent = join("", nCopies(recursionDepth(), "   "));
		
		for (final Justification  justification : justify(goal)) {
			Tools.debugPrint(indent, justification);
			
			deduce();
			{
				if (justification instanceof JustificationByApply) {
					if (false) {
						continue;
					}
					String ruleName = justification.getJustificationName();
					final List<Expression<?>> conditions = ((JustificationByApply) justification).getConditionsFor(goal);
					final List<Justification>[] conditionJustifications =
							conditions.stream().map(AutoDeduce3Test::justify).toArray(List[]::new);
					final int n = conditions.size();
					final int[] indices = new int[n];
					
					Tools.debugPrint(indent, conditions);
					Tools.debugPrint(indent, Arrays.stream(conditionJustifications).map(List::size).collect(toList()));
					Tools.debugPrint(indent, proposition(justification.getJustificationName()));
					Tools.debugPrint(indent, goal, depth);
					final String[] rcl = new String[1];
					
					for (int i = 0; i < n; ++i) {
						boolean ok = false;
						rcl[0] = null;
						
						for (; !ok && indices[i] < conditionJustifications[i].size(); ++indices[i]) {
//							Tools.debugPrint(indent, i, indices[i]);
							ok = autoDeduce(conditions.get(i), depth - 1, rcl);
						}
						
						if (ok) {
							apply(ruleName, rcl[0] != null ? rcl[0] : name(-1));
							ruleName = name(-1);
						} else if (--i < 0) {
							break;
//						} else {
//							Tools.debugPrint("TODO");
//							
//							return false;
						}
						
//						Tools.debugPrint(indent, i);
					}
				} else if (justification instanceof JustificationByRecall && recall != null) {
					recall[0] = justification.getJustificationName();
					cancel();
					return true;
				} else {
					try {
						justification.forward(goal, depth);
					} catch (final FreeVariablePreventsConclusionException exception) {
						ignore(exception);
					}
				}
				
				if (proposition(-1).equals(goal.accept(Variable.RESET))) {
					concludeOrSimplify();
					
					return true;
				} else {
					cancel();
				}
			}
		}
		
		return false;
	}
	
	public static final void concludeOrSimplify() {
		if (false) {
			conclude();
			return;
		}
		
		final Deduction deduction = deduction();
		final List<Proof> proofs = deduction.getProofs();
		
		if (proofs.isEmpty()) {
			cancel();
			
			return;
		}
		
		if (!deduction.canConclude()) {
			throw new IllegalStateException();
		}
		
		if (proofs.size() == 1) {
			cancel();
			
			proofs.get(0).copyFor(deduction(), deduction.getPropositionName()).conclude();
		} else {
			conclude();
		}
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
					Tools.debugPrint(composite.getContents());
					if (composite.getParameters() != null) {
						if (composite.getContents().accept(Variable.RESET).equals(goal.accept(Variable.RESET))) {
							Tools.debugPrint(composite);
							result.add(new JustificationByBind(proof.getPropositionName()));
						} else {
							composite = cast(Composite.class, composite.getContents());
						}
					}
					
					int depth = 0;
					
					Tools.debugPrint(proof.getPropositionName(), composite, goal);
					while (composite != null && composite.getConclusion() != null) {
						if (composite.getConclusion().accept(Variable.RESET).equals(goal.accept(Variable.RESET))) {
							result.add(new JustificationByApply(proof.getPropositionName(), depth));
							break;
						}
						
						composite = cast(Composite.class, composite.getConclusion());
						Tools.debugPrint(proof.getPropositionName(), composite, goal);
						++depth;
					}
					
				}
				
				proposition.accept(Variable.RESET);
				goal.accept(Variable.RESET);
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
			final Proof lastProof = proof(-1);
			
			if (lastProof == null || lastProof instanceof Supposition || !lastProof.getPropositionName().equals(this.getJustificationName())) {
				apply("recall", this.getJustificationName());
			}
			
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
