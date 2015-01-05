package averan3.core;

import static averan3.core.Composite.IMPLIES;
import static net.sourceforge.aprog.tools.Tools.cast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import averan3.core.Expression.Substitution;

/**
 * @author codistmonk (creation 2015-01-04)
 */
public abstract class Proof implements Serializable {
	
	private final Deduction parent;
	
	private final String name;
	
	private Expression<?> proposition;
	
	protected Proof(final Deduction parent, final String name) {
		this.parent = parent;
		this.name = name != null ? name : parent.getName() + "." + (parent.getProofs().size() + 1);
	}
	
	public final Deduction getParent() {
		return this.parent;
	}
	
	public final String getName() {
		return this.name;
	}
	
	public final Expression<?> getProposition() {
		return this.proposition;
	}
	
	protected final void addToDeduction(final Expression<?> proposition) {
		if (this.getProposition() != null || proposition == null) {
			throw new IllegalStateException();
		}
		
		this.proposition = proposition;
		
		this.getParent().add(this);
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
		
		private final Collection<Symbol<String>> protoparameters;
		
		private Expression<?> goal;
		
		public Deduction(final Deduction parent, final String name, final Expression<?> goal) {
			super(parent, name);
			this.proofs = new ArrayList<>();
			this.protoparameters = new LinkedHashSet<>();
			this.goal = goal;
			// primitive module operations: parametrize, suppose, apply, substitute
			// primitive deduction operations: introduce, deduce, conclude
			// standard tactics: recall, bind, rewrite, rewriteRight, autoDeduce
		}
		
		public final Collection<Symbol<String>> getProtoparameters() {
			return this.protoparameters;
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
					final Symbol<String> result = new Symbol<String>(parameterOrPropositionName != null ? parameterOrPropositionName : parameter.getName());
					
					if (!this.getProtoparameters().add(result)) {
						throw new IllegalArgumentException();
					}
					
					this.goal = this.goal.accept(new Expression.Substitution().bind(new Variable(parameter.getName(), parameter), result));
					
					return (E) result;
				}
				
				if (goal.getCondition() != null) {
					this.new Supposition(parameterOrPropositionName, goal.getCondition()).conclude();
					this.goal = goal.getConclusion();
					
					return null;
				}
			}
			
			{
				final Symbol<String> result = new Symbol<String>(parameterOrPropositionName);
				
				if (parameterOrPropositionName == null || !this.getProtoparameters().add(result)) {
					throw new IllegalArgumentException();
				}
				
				return (E) result;
			}
		}
		
		@Override
		public final void conclude() {
			if (!this.getProtoparameters().isEmpty()) {
				final Substitution substitution = new Substitution();
				
				for (final Symbol<String> protoparameter : this.getProtoparameters()) {
					substitution.bind(protoparameter, new Variable(protoparameter.toString()));
				}
				
				this.root = this.root.accept(substitution);
				this.getProtoparameters().clear();
			}
			
			if (this.getParent() != null) {
				// TODO extract proposition from root
				this.addToDeduction(this.root);
			}
		}
		
		public final Expression<?> getGoal() {
			return this.goal;
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
				this.addToDeduction(this.proposition);
			}
			
			@Override
			public final String toString() {
				return "Suppose";
			}
			
			private static final long serialVersionUID = -2449310594857640213L;
			
		}
		
		private static final long serialVersionUID = -4622604986554143041L;
		
	}
	
}