package averan3.core;

import static averan3.core.Composite.FORALL;
import static averan3.core.Composite.IMPLIES;
import static net.sourceforge.aprog.tools.Tools.cast;
import static net.sourceforge.aprog.tools.Tools.last;
import averan3.core.Expression.Substitution;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import net.sourceforge.aprog.tools.Pair;

/**
 * @author codistmonk (creation 2015-01-04)
 */
public abstract class Proof implements Serializable {
	
	private final Deduction parent;
	
	private final String propositionName;
	
	private Expression<?> proposition;
	
	protected Proof(final Deduction parent, final String name) {
		this.parent = parent;
		this.propositionName = name != null ? name : parent.getPropositionName() + "." + (parent.getProofs().size() + 1);
	}
	
	public final Deduction getParent() {
		return this.parent;
	}
	
	public final String getPropositionName() {
		return this.propositionName;
	}
	
	public final Expression<?> getProposition() {
		return this.proposition;
	}
	
	protected final void setProposition(final Expression<?> proposition) {
		if (this.getProposition() != null || proposition == null) {
			throw new IllegalStateException();
		}
		
		this.proposition = this.getParent() == null ? proposition : proposition.accept(this.getParent().getProtoparameterSubstitution());
		
		if (this.getParent() != null) {
			this.getParent().add(this);
		}
	}
	
	public abstract void conclude();
	
	private static final long serialVersionUID = -5949193213742615021L;
	
	/**
	 * @author codistmonk (creation 2015-01-04)
	 */
	public static final class Deduction extends Proof {
		
		private Expression<?> root;
		
		private Composite<Expression<?>> leaf;
		
		private final List<Proof> proofs;
		
		private final Substitution protoparameterSubstitution;
		
		private final Collection<Symbol<String>> protoparameters;
		
		private Expression<?> goal;
		
		public Deduction(final Deduction parent, final String name, final Expression<?> goal) {
			super(parent, name);
			this.proofs = new ArrayList<>();
			this.protoparameterSubstitution = new Substitution();
			this.protoparameters = new LinkedHashSet<>();
			this.goal = goal;
			
			if (parent != null) {
				this.protoparameterSubstitution.getBindings().addAll(
						parent.getProtoparameterSubstitution().getBindings());
			}
			// primitive module operations: suppose, apply, substitute
			// primitive deduction operations: introduce, deduce, conclude
			// standard tactics: recall, bind, rewrite, rewriteRight, autoDeduce
		}
		
		public final Substitution getProtoparameterSubstitution() {
			return this.protoparameterSubstitution;
		}
		
		public final Collection<Symbol<String>> getProtoparameters() {
			return this.protoparameters;
		}
		
		public final Composite<Expression<?>> getRootParameters() {
			@SuppressWarnings("unchecked")
			final Composite<Expression<?>> composite = cast(Composite.class, this.root);
			
			return composite != null ? composite.getParameters() : null;
		}
		
		public final List<Proof> getProofs() {
			return this.proofs;
		}
		
		@SuppressWarnings("unchecked")
		public final <E extends Expression<?>> E introduce(final String parameterOrPropositionName) {
			final Composite<?> goal = cast(Composite.class, this.getGoal());
			
			if (goal != null) {
				if (goal.getParameters() != null) {
					final Variable parameter = (Variable) goal.getParameters().get(1);
					final Symbol<String> result = this.newParameter(parameterOrPropositionName);
					
					this.goal = this.goal.accept(new Expression.Substitution().bind(new Variable(parameter.getName(), parameter), result));
					
					return (E) result;
				}
				
				if (goal.getCondition() != null) {
					this.new Supposition(parameterOrPropositionName, goal.getCondition()).conclude();
					this.goal = goal.getConclusion();
					
					return null;
				}
			}
			
			if (this.root != null) {
				throw new IllegalStateException();
			}
			
			return (E) this.newParameter(parameterOrPropositionName);
		}
		
		@Override
		public final void conclude() {
			if (this.getProofs().isEmpty() || last(this.getProofs()) instanceof Supposition) {
				throw new IllegalStateException();
			}
			
			if (!this.getProtoparameters().isEmpty()) {
				final Substitution substitution = new Substitution();
				final Composite<Expression<?>> parameters = new Composite<>().add(FORALL);
				
				for (final Symbol<String> protoparameter : this.getProtoparameters()) {
					final Variable parameter = new Variable(protoparameter.toString());
					
					parameters.add(parameter);
					substitution.bind(protoparameter, parameter);
				}
				
				this.root = new Composite<>().add(parameters).add(this.root.accept(substitution));
				this.getProtoparameters().clear();
			}
			
			// TODO extract proposition from root
			this.setProposition(this.root.accept(this.getProtoparameterSubstitution()));
		}
		
		public final Expression<?> getGoal() {
			return this.goal;
		}
		
		@SuppressWarnings("unchecked")
		public final <E extends Expression<?>> E findProposition(final String name) {
			for (int i = this.getProofs().size() - 1; 0 <= i; --i) {
				final Proof proof = this.getProofs().get(i);
				
				if (proof.getPropositionName().equals(name)) {
					return (E) proof.getProposition();
				}
			}
			
			return this.getParent() != null ? this.getParent().findProposition(name) : null;
		}
		
		final void add(final Proof proof) {
			final Expression<?> proposition = proof.getProposition();
			
			if (proposition == null) {
				throw new IllegalArgumentException();
			}
			
			this.getProofs().add(proof);
			
			if (this.root == null) {
				this.root = proposition;
			} else if (this.leaf == null) {
				this.root = this.leaf = new Composite<>().add(this.root).add(IMPLIES).add(proposition);
			} else if (this.leaf.getParameters() != null && this.leaf.getContents() == null) {
				this.leaf.removeLast();
				this.leaf.add(proposition);
			} else if (this.leaf.getCondition() != null) {
				if (this.leaf.getConclusion() == null) {
					throw new IllegalStateException();
				}
				
				final Composite<Expression<?>> newLeaf = new Composite<>()
						.add(this.leaf.removeLast()).add(IMPLIES).add(proposition);
				this.leaf.add(newLeaf);
				this.leaf = newLeaf;
			} else {
				throw new IllegalStateException();
			}
		}
		
		private final Symbol<String> newParameter(final String name) {
			final Symbol<String> result = new Symbol<>(name);
			
			if (name == null || !this.getProtoparameters().add(result)) {
				throw new IllegalArgumentException();
			}
			
			{
				for (final Iterator<Pair<Expression<?>, Expression<?>>> i =
						this.getProtoparameterSubstitution().getBindings().iterator(); i.hasNext();) {
					if (result.implies(i.next().getFirst())) {
						i.remove();
					}
				}
				
				this.getProtoparameterSubstitution().bind(result, new Variable(name));
			}
			
			return result;
		}
		
		/**
		 * @author codistmonk (creation 2015-01-04)
		 */
		public final class Supposition extends Proof {
			
			private final Expression<?> proposition;
			
			public Supposition(final String propositionName, final Expression<?> proposition) {
				super(Deduction.this, propositionName);
				this.proposition = proposition;
			}
			
			@Override
			public final void conclude() {
				this.setProposition(this.proposition);
			}
			
			@Override
			public final String toString() {
				return "By supposition";
			}
			
			private static final long serialVersionUID = -2449310594857640213L;
			
		}
		
		/**
		 * @author codistmonk (creation 2015-01-04)
		 */
		public final class ModusPonens extends Proof {
			
			private final String ruleName;
			
			private final String conditionName;
			
			public ModusPonens(final String propositionName, final String ruleName, final String conditionName) {
				super(Deduction.this, propositionName);
				this.ruleName = ruleName;
				this.conditionName = conditionName;
			}
			
			@Override
			public final void conclude() {
				final Expression<?> condition = this.getParent().findProposition(this.conditionName);
				Composite<Expression<?>> rule = this.getParent().findProposition(this.ruleName);
				
				if (rule.getParameters() != null) {
					rule = rule.getContents();
				}
				
				rule.accept(Variable.RESET);
				
				if (!rule.getCondition().equals(condition)) {
					throw new IllegalArgumentException();
				}
				
				this.setProposition(rule.getConclusion().accept(Variable.BIND));
				
				rule.accept(Variable.RESET);
			}
			
			@Override
			public final String toString() {
				return "By applying (" + this.ruleName + ") on (" + this.conditionName + ")";
			}
			
			private static final long serialVersionUID = -2333420406462704258L;
			
		}
		
		private static final long serialVersionUID = -4622604986554143041L;
		
	}
	
}