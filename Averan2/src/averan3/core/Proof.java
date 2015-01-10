package averan3.core;

import static averan3.core.Composite.EQUALS;
import static averan3.core.Composite.FORALL;
import static averan3.core.Composite.IMPLIES;
import static net.sourceforge.aprog.tools.Tools.cast;
import static net.sourceforge.aprog.tools.Tools.last;

import averan3.core.Expression.Visitor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import net.sourceforge.aprog.tools.Tools;

/**
 * @author codistmonk (creation 2015-01-04)
 */
public abstract class Proof implements Serializable {
	
	private final Deduction parent;
	
	private final String propositionName;
	
	private Expression<?> proposition;
	
	protected Proof(final Deduction parent, final String propositionName) {
		this.parent = parent;
		this.propositionName = propositionName != null ? propositionName : parent.getPropositionName() + "." + (parent.getProofs().size() + 1);
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
		
		this.proposition = proposition;
		
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
		
		private final Module module;
		
		private final List<Proof> proofs;
		
		private boolean hasParameters;
		
		private Expression<?> goal;
		
		private String conclusionMessage;
		
		public Deduction(final Deduction parent, final String name, final Expression<?> goal) {
			super(parent, name);
			this.module = new Module();
			this.proofs = new ArrayList<>();
			this.goal = goal;
		}
		
		public final Composite<Expression<?>> getRootParameters() {
			@SuppressWarnings("unchecked")
			final Composite<Expression<?>> composite = cast(Composite.class, this.module.getRoot());
			
			return composite != null && this.hasParameters ? composite.getParameters() : null;
		}
		
		public final List<Proof> getProofs() {
			return this.proofs;
		}
		
		public final Deduction include(final Deduction deduction, final Expression<?>... arguments) {
			final Composite<Expression<?>> deductionParameters = deduction.getRootParameters();
			final Expression.Substitution substitution = new Expression.Substitution();
			
			if (deductionParameters != null) {
				final int n = deductionParameters.size();
				
				if (n - 1 != arguments.length) {
					throw new IllegalArgumentException();
				}
				
				for (int i = 1; i < n; ++i) {
					substitution.bind(deductionParameters.get(i), arguments[i - 1]);
				}
			}
			
			deduction.getProofs().forEach(proof -> this.new Inclusion(proof, substitution).conclude());
			
			return this;
		}
		
		@SuppressWarnings("unchecked")
		public final <E extends Expression<?>> E introduce(final String parameterOrPropositionName) {
			final Composite<?> goal = cast(Composite.class, this.getGoal());
			
			if (goal != null) {
				if (goal.getParameters() != null && goal.getParameters().isList()) {
					final Variable parameter = (Variable) goal.getParameters().getListElement(1);
					final Variable result = this.newParameter(
							parameterOrPropositionName != null ? parameterOrPropositionName : parameter.getName());
					
					{
						final Composite<Expression<?>> newParameters = new Composite<>().append(FORALL);
						final int n = goal.getParameters().getListSize();
						
						for (int i = 2; i < n; ++i) {
							newParameters.append(goal.getParameters().getListElement(i));
						}
						
						final Expression<?> newContents = goal.getContents().accept(new Expression.Substitution().bind(parameter, result));
						
						this.goal = 1 < newParameters.getListSize() ? new Composite<>().add(newParameters).add(newContents) : newContents;
					}
					
					return (E) result;
				}
				
				if (goal.getCondition() != null) {
					this.new Supposition(parameterOrPropositionName, goal.getCondition()).conclude();
					this.goal = goal.getConclusion();
					
					return null;
				}
			}
			
			if (!this.getProofs().isEmpty()) {
				throw new IllegalStateException();
			}
			
			return (E) this.newParameter(parameterOrPropositionName);
		}
		
		public final void conclude(final String conclusionMessage) {
			this.conclusionMessage = conclusionMessage;
			this.conclude();
		}
		
		public final boolean canConclude() {
			return !(this.getProofs().isEmpty() || last(this.getProofs()) instanceof Supposition
					|| (this.getGoal() != null && !last(this.getProofs()).getProposition().equals(this.getGoal())));
		}
		
		@Override
		public final void conclude() {
			if (!this.canConclude()) {
				Tools.debugError(this.getProofs().size());
				if (!this.getProofs().isEmpty()) {
					Tools.debugError(last(this.getProofs()).getProposition());
				}
				Tools.debugError(this.getGoal());
				throw new IllegalStateException();
			}
			
			{
				final Deduction reduced = new Deduction(null, "", null);
				
				this.getProofs().forEach(proof -> {
					if (proof instanceof Supposition) {
						reduced.add(proof);
					}
				});
				
				reduced.add(last(this.getProofs()));
				
				if (this.hasParameters) {
					reduced.module.setRoot(new Composite<>().add(
							this.module.getRoot().get(0).accept(Variable.UNLOCK)).add(reduced.module.getRoot()));
				}
				
				this.setProposition(reduced.module.getRoot());
			}
		}
		
		public final Expression<?> getGoal() {
			return this.goal;
		}
		
		public final Proof findProof(final String name) {
			for (final Proof proof : this.getProofs()) {
				if (proof.getPropositionName().equals(name)) {
					return proof;
				}
			}
			
			return this.getParent() != null ? this.getParent().findProof(name) : null;
		}
		
		public final Proof findProof(final int index) {
			if (0 <= index) {
				throw new IllegalArgumentException();
			}
			
			final int i = this.getProofs().size() + index;
			
			return 0 <= i ? this.getProofs().get(i) :
				this.getParent() != null ? this.getParent().findProof(i) :
					null;
		}
		
		@SuppressWarnings("unchecked")
		public final <E extends Expression<?>> E instantiateProposition(final String name) {
			return (E) this.findProof(name).getProposition().accept(new Instance());
		}
		
		/**
		 * @author codistmonk (creation 2015-01-08)
		 */
		public static final class Instance implements Visitor<Expression<?>> {
			
			private final Map<Variable, Variable> variables = new IdentityHashMap<>();
			
			@Override
			public final Expression<?> visit(final Symbol<?> symbol) {
				return symbol;
			}
			
			@Override
			public final Expression<?> visit(final Variable variable) {
				return variable.isLocked() ? variable : this.variables.computeIfAbsent(variable, v -> new Variable(v.getName()));
			}
			
			@Override
			public final Expression<?> visit(final Composite<Expression<?>> composite) {
				final Composite<Expression<?>> newComposite = new Composite<>();
				boolean returnNewComposite = false;
				
				for (final Expression<?> element : composite) {
					final Expression<?> newElement = element.accept(this);
					
					newComposite.add(newElement);
					
					if (!returnNewComposite && element != newElement) {
						returnNewComposite = true;
					}
				}
				
				return returnNewComposite ? newComposite : composite;
			}
			
			private static final long serialVersionUID = 6918127691328544719L;
			
		}
		
		@Override
		public final String toString() {
			if (this.conclusionMessage != null) {
				return this.conclusionMessage;
			}
			
			Proof informativeProof = this;
			
			while (informativeProof instanceof Deduction && ((Deduction) informativeProof).getProofs().size() == 1) {
				informativeProof = ((Deduction) informativeProof).getProofs().get(0);
			}
			
			return this != informativeProof ? informativeProof.toString() :
				"By deduction in " + this.getProofs().size() + " steps (" + this.countSubsteps() + " substeps)";
		}
		
		public final int countSubsteps() {
			int result = this.getProofs().size();
			
			for (final Proof proof : this.getProofs()) {
				result += proof instanceof Deduction ? ((Deduction) proof).countSubsteps() : 1;
			}
			
			return result;
		}
		
		final void add(final Proof proof) {
			final Expression<?> proposition = proof.getProposition();
			
			if (proposition == null) {
				throw new IllegalArgumentException();
			}
			
			this.getProofs().add(proof);
			
			this.module.add(proposition);
		}
		
		private final Variable newParameter(final String name) {
			final Variable result = this.module.parametrize(name).lock();
			
			this.hasParameters = true;
			
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
				this.setProposition(this.proposition.accept(Variable.RESET));
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
				final Expression<?> condition = this.getParent().instantiateProposition(this.conditionName);
				Composite<Expression<?>> rule = this.getParent().instantiateProposition(this.ruleName);
				final Composite<Expression<?>> parameters = rule.getParameters();
				
				if (parameters != null) {
					rule = rule.getContents();
				}
				
				rule.accept(Variable.RESET);
				condition.accept(Variable.RESET);
				
				if (!rule.getCondition().equals(condition)) {
					Tools.debugError(rule.getCondition());
					Tools.debugError(condition);
					throw new IllegalArgumentException();
				}
				
				final Expression<?> conclusion = rule.getConclusion().accept(Variable.BIND);
				
				if (parameters != null) {
					@SuppressWarnings("unchecked")
					final Composite<Expression<?>> boundParameters = (Composite<Expression<?>>) parameters.accept(Variable.BIND);
					final int n = boundParameters.getListSize();
					final Composite<Expression<?>> newParameters = new Composite<>().append(FORALL);
					
					for (int i = 1; i < n; ++i) {
						final Variable parameter = cast(Variable.class, boundParameters.getListElement(i));
						
						if (parameter != null && parameter.getMatch() == null) {
							newParameters.append(parameter);
						}
					}
					
					if (1 < newParameters.getListSize()) {
						this.setProposition(new Composite<>().add(newParameters).add(conclusion));
					} else {
						this.setProposition(conclusion);
					}
				} else {
					this.setProposition(conclusion);
				}
				
				rule.accept(Variable.RESET);
			}
			
			@Override
			public final String toString() {
				return "By applying (" + this.ruleName + ") on (" + this.conditionName + ")";
			}
			
			private static final long serialVersionUID = -2333420406462704258L;
			
		}
		
		/**
		 * @author codistmonk (creation 2015-01-04)
		 */
		public final class Substitution extends Proof {
			
			private final Composite<Expression<?>> substitutionExpression;
			
			public Substitution(final String propositionName,
					final Composite<Expression<?>> substitutionExpression) {
				super(Deduction.this, propositionName);
				this.substitutionExpression = substitutionExpression;
			}
			
			@Override
			public final void conclude() {
				if (!(this.substitutionExpression.size() == 3
						&& this.substitutionExpression.get(1) instanceof Composite<?>
						&& this.substitutionExpression.get(2) instanceof Composite<?>)) {
					throw new IllegalArgumentException();
				}
				
				final Expression.Substitution substitution = new Expression.Substitution();
				
				{
					final Composite<Expression<?>> equalities = this.substitutionExpression.getEqualities();
					final int n = equalities.getListSize();
					
					for (int i = 0; i < n; ++i) {
						@SuppressWarnings("unchecked")
						final Composite<Expression<?>> equality = (Composite<Expression<?>>) equalities.getListElement(i);
						
						if (equality.getKey() == null || equality.getValue() == null) {
							throw new IllegalArgumentException();
						}
						
						substitution.bind(equality.getKey(), equality.getValue());
					}
				}
				
				{
					final Composite<Expression<?>> indices = this.substitutionExpression.getIndices();
					final int n = indices.getListSize();
					
					for (int i = 0; i < n; ++i) {
						@SuppressWarnings("unchecked")
						final Symbol<Integer> index = (Symbol<Integer>) indices.getListElement(i);
						substitution.at(index.getObject());
					}
				}
				
				this.setProposition(new Composite<>().add(this.substitutionExpression)
						.add(EQUALS).add(this.substitutionExpression.get(0).accept(substitution)));
			}
			
			@Override
			public final String toString() {
				final Expression<?> target = this.substitutionExpression.get(0);
				final Expression<?> equalities = this.substitutionExpression.get(1);
				final Expression<?> indices = this.substitutionExpression.get(2);
				
				return "By substitution in " + target
						+ " using " + equalities
						+ (indices.isEmpty() ? "" : " at indices " + indices);
				
			}
			
			private static final long serialVersionUID = 5765484578210551523L;
			
		}
		
		/**
		 * @author codistmonk (creation 2015-01-04)
		 */
		public final class Rewrite extends Proof {
			
			private final String targetName;
			
			private final Collection<String> equalityNames;
			
			private final Collection<Integer> indices;
			
			public Rewrite(final String propositionName, String targetName) {
				super(Deduction.this, propositionName);
				this.targetName = targetName;
				this.equalityNames = new LinkedHashSet<>();
				this.indices = new TreeSet<>();
			}
			
			public final Rewrite using(final String... equalityNames) {
				if (!this.indices.isEmpty()) {
					throw new IllegalStateException();
				}
				
				for (final String equalityName : equalityNames) {
					this.equalityNames.add(equalityName);
				}
				
				return this;
			}
			
			public final Rewrite at(final int... indices) {
				for (final int index : indices) {
					this.indices.add(index);
				}
				
				return this;
			}
			
			@Override
			public final void conclude() {
				final Expression<?> target = this.getParent().instantiateProposition(this.targetName);
				final Expression.Substitution substitution = new Expression.Substitution();
				
				for (final String equalityName : this.equalityNames) {
					final Composite<Expression<?>> equality = this.getParent().instantiateProposition(equalityName);
					
					if (equality == null || equality.getKey() == null || equality.getValue() == null) {
						Tools.debugError(equalityName, equality);
						throw new IllegalArgumentException();
					}
					
					substitution.bind(equality.getKey(), equality.getValue());
				}
				
				this.setProposition(target.accept(substitution.at(this.indices)));
			}
			
			@Override
			public final String toString() {
				return "By rewriting (" + this.targetName + ") using " + this.equalityNames
						+ (this.indices.isEmpty() ? "" : " at indices " + this.indices);
			}
			
			private static final long serialVersionUID = 6500801432673800012L;
			
		}
		
		/**
		 * @author codistmonk (creation 2015-01-06)
		 */
		public final class Binding extends Proof {
			
			private final String targetName;
			
			private final Expression<?>[] values;
			
			public Binding(final String propositionName,
					final String targetName, final Expression<?>... values) {
				super(Deduction.this, propositionName);
				this.targetName = targetName;
				this.values = values;
			}
			
			@Override
			public final void conclude() {
				final int n = this.values.length;
				final Composite<?> target = this.getParent().instantiateProposition(this.targetName);
				final Composite<Expression<?>> parameters = target.getParameters();
				final Composite<Expression<?>> newParameters = new Composite<>().append(FORALL);
				boolean useContentsOnly = true;
				
				target.accept(Variable.RESET);
				
				for (int i = 0; i < n; ++i) {
					final Variable parameter = (Variable) parameters.getListElement(i + 1);
					
					if (this.values[i] == null) {
						newParameters.append(parameter);
						useContentsOnly = false;
					} else {
						parameter.equals(this.values[i]);
					}
				}
				
				final Expression<?> newContents = target.getContents().accept(Variable.BIND);
				
				this.setProposition(useContentsOnly ? newContents :
					new Composite<>().add(newParameters).add(newContents));
				
				target.accept(Variable.RESET);
			}
			
			@Override
			public final String toString() {
				return "By binding in (" + this.targetName + ") with " + Arrays.toString(this.values);
			}
			
			private static final long serialVersionUID = 7752042237978618815L;
			
		}
		
		/**
		 * @author codistmonk (creation 2015-01-06)
		 */
		public final class Inclusion extends Proof {
			
			private final Proof included;
			
			private final Expression.Substitution specialization;
			
			public Inclusion(final Proof included, final Expression.Substitution specialization) {
				super(Deduction.this, included.getPropositionName());
				this.included = included;
				this.specialization = specialization;
			}
			
			public final Proof getIncluded() {
				return this.included;
			}
			
			@Override
			public final void conclude() {
				this.setProposition(this.included.getProposition().accept(this.specialization));
			}
			
			@Override
			public final String toString() {
				return "By inclusion from (" + this.included.getParent().getPropositionName() + ")"
						+ (this.specialization.getBindings().isEmpty() ? "" : " using " + this.specialization.getBindings());
			}
			
			private static final long serialVersionUID = -1808210996095205537L;
			
		}
		
		private static final long serialVersionUID = -4622604986554143041L;
		
		/**
		 * @author codistmonk (creation 2015-01-05)
		 */
		private static final class Module implements Serializable {
			
			private Expression<?> root;
			
			private Composite<Expression<?>> leaf;
			
			Module() {
				// package-private constructor to suppress access warning
			}
			
			public final Variable parametrize(final String parameterName) {
				if (parameterName == null) {
					throw new IllegalArgumentException();
				}
				
				final Variable result = new Variable(parameterName);
				
				if (this.root == null) {
					this.root = this.leaf = new Composite<>().add(new Composite<>().append(FORALL).append(result)).add(null);
				} else if (this.leaf.getParameters() != null && this.leaf.getContents() == null) {
					this.leaf.getParameters().append(result);
				} else {
					throw new IllegalStateException();
				}
				
				return result;
			}
			
			public final void setRoot(final Expression<?> root) {
				this.root = root;
			}
			
			public final Expression<?> getRoot() {
				return this.root;
			}
			
			public final void add(final Expression<?> proposition) {
				if (this.root == null) {
					this.root = proposition;
				} else if (this.leaf == null) {
					this.root = this.leaf = new Composite<>().add(this.root).add(IMPLIES).add(proposition);
				} else if (this.leaf.getParameters() != null) {
					if (this.leaf.getContents() == null) {
						this.leaf.removeLast();
						this.leaf.add(proposition);
					} else {
						this.addConclusion(proposition);
					}
				} else if (this.leaf.getCondition() != null) {
					if (this.leaf.getConclusion() == null) {
						throw new IllegalStateException();
					}
					
					this.addConclusion(proposition);
				} else {
					throw new IllegalStateException();
				}
			}
			
			private final void addConclusion(final Expression<?> proposition) {
				final Composite<Expression<?>> newLeaf = new Composite<>()
						.add(this.leaf.removeLast()).add(IMPLIES).add(proposition);
				this.leaf.add(newLeaf);
				this.leaf = newLeaf;
			}
			
			private static final long serialVersionUID = -7495741010510033390L;
			
		}
		
	}
	
}