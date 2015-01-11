package averan3.deductions;

import static averan3.core.Session.*;
import static java.lang.Math.min;
import static java.util.Collections.nCopies;
import static net.sourceforge.aprog.tools.Tools.cast;
import static net.sourceforge.aprog.tools.Tools.join;

import averan3.core.Composite;
import averan3.core.Expression;
import averan3.core.Proof;
import averan3.core.Variable;
import averan3.core.Proof.Deduction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.aprog.tools.IllegalInstantiationException;
import net.sourceforge.aprog.tools.Tools;

/**
 * @author codistmonk (creation 2015-01-11)
 */
public final class AutoDeduce2 {
	
	private AutoDeduce2() {
		throw new IllegalInstantiationException();
	}
		
	public static final boolean autoDeduce2(final Expression<?> goal, final int depth) {
		if (depth <= 0) {
			return false;
		}
		
		final String indent = join("", nCopies(recursionDepth(), "   "));
		
		for (final AutoDeduce2.Justification justification : justify(goal)) {
			Tools.debugPrint(indent, justification, goal);
			if (justification instanceof JustificationByApply) {
				deduce(goal);
				if (tryToApply((JustificationByApply) justification, goal, depth)) {
					Tools.debugPrint(indent, "SUCCEEDED", justification);
					conclude();
					return true;
				} else {
					cancel();
				}
			} else {
				deduce(goal);
				if (justification.justify(goal, depth) && (deduction().canConclude())) {
					Tools.debugPrint(indent, "SUCCEEDED", justification);
					conclude();
					return true;
				} else {
					cancel();
				}
			}
		}
		
		return false;
	}
	
	public static final boolean tryToApply(final AutoDeduce2.JustificationByApply j, final Expression<?> goal, final int depth) {
		if (depth <= 0) {
			return false;
		}
		
		Composite<Expression<?>> rule = proposition(j.getJustificationName());
		
		if (rule.getParameters() != null) {
			rule = rule.getContents();
		}
		
		final Expression<?> condition0 = rule.getCondition();
		
		for (int i = 0; i < j.depth; ++i) {
			rule = rule.getConclusion();
		}
		
		if (!rule.getConclusion().equals(goal.accept(Variable.RESET))) {
			throw new IllegalStateException();
		}
		
		final Expression<?> boundCondition0 = condition0.accept(Variable.BIND);
		final List<AutoDeduce2.Justification> justifications = justify(boundCondition0);
		
		for (final AutoDeduce2.Justification justification : justifications) {
			if (justification instanceof AutoDeduce2.JustificationByApply) {
				tryToApply((AutoDeduce2.JustificationByApply) justification, boundCondition0, depth - 1);
				
				if (deduction().canConclude()) {
					return true;
				}
				
				if (goalFromLastRule() != null) {
					if (tryToApply(new JustificationByApply(name(-1), 0), goalFromLastRule(), depth - 1)) {
						return true;
					}
				}
			} else {
				justification.justify(goal, 1);
				
				try {
					apply(j.getJustificationName(), name(-1));
				} catch (final Exception exception) {
					Tools.debugPrint(exception);
					Tools.debugPrint(justifications.size(), depth, Tools.getCallerMethodName());
					exception.printStackTrace();
					throw new RuntimeException(exception);
				}
				
				if (deduction().canConclude()) {
					return true;
				}
				
				if (goalFromLastRule() != null) {
					if (tryToApply(new JustificationByApply(name(-1), 0), goalFromLastRule(), depth - 1)) {
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	public static final Expression<?> goalFromLastRule() {
		Composite<Expression<?>> rule = cast(Composite.class, proof(-1).getProposition());
		
		if (rule == null) {
			return null;
		}
		
		if (rule.getParameters() != null) {
			rule = rule.getContents();
		}
		
		return rule.getConclusion();
	}
	
	public static final List<AutoDeduce2.Justification> justify(final Expression<?> goal) {
		final List<AutoDeduce2.Justification> result = new ArrayList<>();
		Deduction deduction = deduction();
		
		while (deduction != null) {
			final List<Proof> proofs = deduction.getProofs();
			
			for (int i = proofs.size() - 1; 0 <= i; --i) {
				final Proof proof = proofs.get(i);
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
				
				int depth = 0;
				
				while (composite != null) {
					final Expression<?> conclusion = composite.getConclusion();
					
					if (conclusion != null && conclusion.accept(Variable.RESET).equals(goal.accept(Variable.RESET))) {
						result.add(new JustificationByApply(proof.getPropositionName(), depth));
						break;
					}
					
					composite = cast(Composite.class, conclusion);
					++depth;
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
	public static final class JustificationByRecall extends AutoDeduce2.Justification {
		
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
	public static final class JustificationByApply extends AutoDeduce2.Justification {
		
		private final int depth;
		
		public JustificationByApply(final String name, final int depth) {
			super(name);
			this.depth = depth;
		}
		
		@Override
		public final boolean justify(final Expression<?> goal, final int depth) {
			if (true) {
				throw new RuntimeException();
			}
			
			if (depth <= 0) {
				return false;
			}
			
			Composite<Expression<?>> rule = proposition(this.getJustificationName());
			
			if (rule.getParameters() != null) {
				rule = rule.getContents();
			}
			
			final Expression<?> condition0 = rule.getCondition();
			
			for (int i = 0; i < this.depth; ++i) {
				rule = rule.getConclusion();
			}
			
			if (!rule.getConclusion().equals(goal.accept(Variable.RESET))) {
				throw new IllegalStateException();
			}
			
			final Expression<?> boundCondition0 = condition0.accept(Variable.BIND);
			
			for (final AutoDeduce2.Justification justification : AutoDeduce2.justify(boundCondition0)) {
				if (justification.justify(boundCondition0, min(1, depth - 1))) {
					Tools.debugPrint(proof(this.getJustificationName()).getProposition());
					Tools.debugPrint(proof(-1).getProposition());
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
	public static final class JustificationByBind extends AutoDeduce2.Justification {
		
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
	
}