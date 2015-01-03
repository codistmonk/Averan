package averan2.core;

import static averan2.core.Composite.composite;
import static averan2.core.Equality.equality;
import static averan2.core.Substitution.ANY_INDEX;
import static net.sourceforge.aprog.tools.Tools.cast;
import static net.sourceforge.aprog.tools.Tools.join;
import static net.sourceforge.aprog.tools.Tools.lastIndex;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import net.sourceforge.aprog.tools.Tools;

/**
 * @author codistmonk (creation 2014-12-26)
 */
public final class Module implements Expression<Composite<?>> {
	
	private final Module context;
	
	private final Composite<Variable> parameters;
	
	private final Composite<Expression<?>> propositions;
	
	private final IndexedMap<String, Integer> propositionIds;
	
	private final List<Proof> proofs;
	
	public Module() {
		this(null);
	}
	
	public Module(final Module context) {
		this.context = context;
		this.parameters = new Composite<>();
		this.propositions = new Composite<>();
		this.propositionIds = new IndexedMap<>();
		this.proofs = new ArrayList<>();
	}
	
	public final Composite<Variable> getParameters() {
		return this.parameters;
	}
	
	public final Module parametrize(final Variable variable) {
		this.getParameters().getElements().add(variable);
		
		return this;
	}
	
	public final Module canonicalize() {
		Module last = this.getPropositions().isEmpty() ? null : cast(Module.class, this.getPropositions().last());
		
		while (last != null && last.getParameters().isEmpty()) {
			final Module fact = removeLast(this.getPropositions().getElements());
			
			this.getPropositionIds().remove(this.getPropositionIds().last());
			
			if (fact.getPropositionIds().size() != fact.getPropositions().size()) {
				this.getPropositions().getElements().addAll(fact.getPropositions().getElements());
			} else {
				for (final Map.Entry<String, Integer> id : fact.getPropositionIds().entrySet()) {
					this.addFact(id.getKey(), fact.getPropositions().get(id.getValue()), fact.getProof(id.getKey()));
				}
			}
			
			last = this.getPropositions().isEmpty() ? null : cast(Module.class, this.getPropositions().last());
		}
		
		return this;
	}
	
	public final <E extends Expression<?>> E findProposition(final String name) {
		if (name == null) {
			return null;
		}
		
		Integer index = this.getPropositionIds().get(name);
		
		if (index != null) {
			return this.getPropositions().get(index);
		}
		
		return this.getContext() != null ? this.getContext().findProposition(name) : null;
	}
	
	public final Module getContext() {
		return this.context;
	}
	
	@Override
	public final int size() {
		return 2;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public final Composite<? extends Expression<?>> get(final int index) {
		switch (index) {
		case 0:
			return this.getParameters();
		case 1:
			return this.getPropositions();
		}
		
		return null;
	}
	
	@Override
	public final <V> V accept(Expression.Visitor<V> visitor) {
		return visitor.visit(this);
	}
	
	public final String getPropositionName(final int index) {
		if (0 <= index) {
			throw new IllegalArgumentException("Expected negative value but got: " + index);
		}
		
		int i = index;
		
		for (int j = this.getPropositionIds().size() - 1; 0 <= j; --j, ++i) {
			if (i == -1) {
				return this.getPropositionIds().get(j);
			}
		}
		
		return this.getContext() != null ? this.getContext().getPropositionName(i) : null;
	}
	
	public final <E extends Expression<?>> E getProposition(final int index) {
		if (0 <= index) {
			throw new IllegalArgumentException("Expected negative value but got: " + index);
		}
		
		int i = index;
		
		for (int j = this.getPropositions().size() - 1; 0 <= j; --j, ++i) {
			if (i == -1) {
				return this.getPropositions().get(j);
			}
		}
		
		return this.getContext() != null ? this.getContext().getProposition(i) : null;
	}
	
	public final Composite<Expression<?>> getPropositions() {
		return this.propositions;
	}
	
	public final IndexedMap<String, Integer> getPropositionIds() {
		return this.propositionIds;
	}
	
	public final Proof getProof(final String factName) {
		return this.getProofs().get(this.getPropositionIds().get(factName));
	}
	
	@Override
	public final boolean implies(final Expression<?> expression) {
		if (this == expression) {
			return true;
		}
		
		final Module that = cast(this.getClass(), expression);
		
		return that != null && this.canonicalize().getPropositions().implies(that.canonicalize().getPropositions());
	}
	
	@Override
	public final int hashCode() {
		this.canonicalize();
		
		return this.getPropositions().hashCode();
	}
	
	@Override
	public final boolean equals(final Object object) {
		final Variable variable = cast(Variable.class, object);
		
		if (variable != null) {
			return variable.equals(this);
		}
		
		final Module that = cast(this.getClass(), object);
		
		return that != null && this.canonicalize().getPropositions().equals(that.canonicalize().getPropositions());
	}
	
	@Override
	public final String toString() {
		return formatParameters(this.getParameters()) + formatPropositions("->", this.getPropositions());
	}
	
	public final Module suppose(final Expression<?> condition) {
		return this.addCondition(null, condition);
	}
	
	public final Module conclude(final Expression<?> fact) {
		return this.addFact(null, fact, null);
	}
	
	final Module addCondition(final String name, final Expression<?> proposition) {
		return this.addProposition(name, proposition).addProof(null);
	}
	
	final Module addFact(final String name, final Expression<?> proposition, final Proof proof) {
		return this.addProposition(name, proposition).addProof(proof);
	}
	
	final List<Proof> getProofs() {
		return this.proofs;
	}
	
	private final Module addProposition(final String name, final Expression<?> proposition) {
		if (this.findProposition(name) != null) {
			throw new IllegalArgumentException("Duplicate proposition name: " + name);
		}
		
		this.getPropositionIds().put(name, this.getPropositions().size());
		this.getPropositions().getElements().add(proposition);
		
		return this;
	}
	
	private final Module addProof(final Proof proof) {
		this.proofs.add(proof);
		
		return this;
	}
	
	/**
	 * @author codistmonk (creation 2014-12-26)
	 */
	public abstract class Proof implements Serializable {
		
		private final String factName;
		
		protected Proof(final String factName) {
			this.factName = factName;
		}
		
		public final String getFactName() {
			return this.factName;
		}
		
		public abstract Proof apply();
		
		@SuppressWarnings("unchecked")
		protected final <P extends Proof> P addFactToContext(final Expression<?> proposition) {
			Module.this.addFact(this.getFactName(), proposition, this);
			
			return (P) this;
		}
		
		private static final long serialVersionUID = 5282247624876197313L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-12-27)
	 */
	public final class ProofByApply extends Proof {
		
		private final String moduleName;
		
		private final String conditionName;
		
		public ProofByApply(final String factName, final String moduleName, final String conditionName) {
			super(factName);
			this.moduleName = moduleName;
			this.conditionName = conditionName;
		}
		
		public final String getModuleName() {
			return this.moduleName;
		}
		
		public final String getConditionName() {
			return this.conditionName;
		}
		
		@Override
		public final ProofByApply apply() {
			final Module context = Module.this;
			final Module module = context.<Module>findProposition(this.getModuleName()).canonicalize();
			
			if (module.getParameters().isEmpty() && module.getPropositions().size() == 1) {
				if (this.getConditionName() != null) {
					throw new IllegalArgumentException();
				}
				
				int i = 0;
				
				for (final Map.Entry<String, Integer> id : module.getPropositionIds().entrySet()) {
					context.addFact(this.getFactName() + "." + (++i), module.getPropositions().get(id.getValue()), this);
				}
				
				return this;
			}
			
			return this.addFactToContext(Module.apply(module, context.findProposition(this.getConditionName())));
		}
		
		@Override
		public final String toString() {
			return "Apply (" + this.getModuleName() + ") on (" + this.getConditionName() + ")";
		}
		
		private static final long serialVersionUID = 1974410943023589433L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-12-27)
	 */
	public final class ProofBySubstitute extends Proof {
		
		private final Expression<?> expression;
		
		private final Substitution substitution;
		
		public ProofBySubstitute(final String factName, Expression<?> expression,
				final Substitution substitution) {
			super(factName);
			this.expression = expression;
			this.substitution = substitution;
		}
		
		public final Expression<?> getExpression() {
			return this.expression;
		}
		
		public final Substitution getSubstitution() {
			return this.substitution;
		}
		
		@Override
		public final ProofBySubstitute apply() {
			return this.addFactToContext(equality(composite(this.getExpression(),
					this.getSubstitution()), this.getExpression().accept(this.getSubstitution().reset())));
		}
		
		@Override
		public final String toString() {
			return "Substitute in " + this.getExpression() + " using {" + join(",", this.getSubstitution().getBindings()) + "}" + (this.getSubstitution().getIndices().isEmpty() ? "" : " at " + this.getSubstitution().getIndices());
		}
		
		private static final long serialVersionUID = -2849009520329956261L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-12-27)
	 */
	public final class ProofByRewrite extends Proof {
		
		private final String propositionName;
		
		private final Collection<String> equalityNames;
		
		private int[] indices;
		
		public ProofByRewrite(final String factName, final String propositionName) {
			super(factName);
			this.propositionName = propositionName;
			this.equalityNames = new LinkedHashSet<>();
		}
		
		public final String getPropositionName() {
			return this.propositionName;
		}
		
		public final Collection<String> getEqualityNames() {
			return this.equalityNames;
		}
		
		public final int[] getIndices() {
			return this.indices;
		}
		
		public final ProofByRewrite using(final String equalityName) {
			if (this.getIndices() != null) {
				throw new IllegalStateException();
			}
			
			this.getEqualityNames().add(equalityName);
			
			return this;
		}
		
		public final ProofByRewrite at(final int... indices) {
			this.indices = indices;
			
			return this;
		}
		
		@Override
		public final ProofByRewrite apply() {
			final Substitution substitution = new Substitution().at(this.getIndices() == null ? ANY_INDEX : this.getIndices());
			
			for (final String equalityName : this.getEqualityNames()) {
				substitution.using(Module.this.findProposition(equalityName));
			}
			
			return this.addFactToContext(Module.this.findProposition(this.getPropositionName()).accept(substitution.reset()));
		}
		
		@Override
		public final String toString() {
			return "Rewrite (" + this.getPropositionName() + ") using (" + join(",", this.getEqualityNames()) + ")" + (this.getIndices().length == 0 ? "" : " at " + Arrays.toString(this.getIndices()));
		}
		
		private static final long serialVersionUID = 5020773952478671657L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-12-30)
	 */
	public final class ProofByBind extends Proof {
		
		private final String moduleName;
		
		private final Expression<?>[] values;
		
		public ProofByBind(final String factName, String moduleName,
				final Expression<?>[] values) {
			super(factName);
			this.moduleName = moduleName;
			this.values = values;
		}
		
		public final String getModuleName() {
			return this.moduleName;
		}
		
		public final Expression<?>[] getValues() {
			return this.values;
		}
		
		@Override
		public final Proof apply() {
			final Module module = Module.this.findProposition(this.getModuleName());
			final int n = this.getValues().length;
			
			for (int i = 0; i < n; ++i) {
				module.getParameters().get(i).equals(this.getValues()[i]);
			}
			
			try {
				return this.addFactToContext(module.accept(Variable.BIND));
			} finally {
				module.accept(Variable.RESET);
			}
		}
		
		@Override
		public final String toString() {
			return "Bind (" + this.getModuleName() + ") using (" + join(",", (Object[]) this.getValues()) + ")";
		}
		
		private static final long serialVersionUID = 2309957562673783419L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-12-27)
	 */
	public final class ProofByDeduce extends Proof {
		
		private final Module deduction;
		
		public ProofByDeduce(final String factName, final Module deduction) {
			super(factName);
			
			if (Module.this != deduction.getContext()
					|| deduction.getPropositionIds().size() != deduction.getPropositions().size()) {
				throw new IllegalArgumentException();
			}
			
			for (final String name : deduction.getPropositionIds().keySet()) {
				if (name == null) {
					throw new IllegalArgumentException();
				}
			}
			
			this.deduction = deduction.canonicalize();
		}
		
		public final Module getDeduction() {
			return this.deduction;
		}
		
		@Override
		public final ProofByDeduce apply() {
			final Module deduction = this.getDeduction();
			final Composite<Expression<?>> deducedFacts = deduction.getPropositions();
			final Expression<?> lastDeducedFact = deducedFacts.get(deducedFacts.size() - 1);
			final Expression<?> fact;
			
			fact = new Module();
			
			deduction.getParameters().forEach(((Module) fact)::parametrize);
			
			for (final Map.Entry<String, Integer> id : deduction.getPropositionIds().entrySet()) {
				if (deduction.getProof(id.getKey()) == null) {
					((Module) fact).addCondition(id.getKey(), deduction.getPropositions().get(id.getValue()));
				}
			}
			((Module) fact).getPropositions().getElements().add(lastDeducedFact);
			((Module) fact).getPropositionIds().put(deduction.getPropositionIds().get(deduction.getPropositionIds().size() - 1), 0);
			((Module) fact).getProofs().add(this);
			((Module) fact).canonicalize();
			
			return this.addFactToContext(fact.accept(Variable.BIND));
		}
		
		@Override
		public final String toString() {
			Proof proof = this;
			
			if (1 != this.getDeduction().getPropositions().size()) {
				return "Deduce in " + this.getDeduction().getPropositions().size() + " steps";
			}
			
			while (proof instanceof ProofByDeduce && ((ProofByDeduce) proof).getDeduction().getPropositions().size() == 1) {
				proof = ((ProofByDeduce) proof).getDeduction().getProof(((ProofByDeduce) proof).getDeduction().getPropositionIds().last());
			}
			
			return proof == null ? "Deduce immediately" : proof.toString();
		}
		
		private static final long serialVersionUID = 8959982180068107269L;
		
	}
	
	private static final long serialVersionUID = 9140955565054672814L;
	
	public static final String formatParameters(final Composite<Variable> parameters) {
		return parameters.isEmpty() ? "" : ("∀" + join(",", parameters.toArray()) + " ");
	}
	
	public static final String formatPropositions(final String separator, final Composite<Expression<?>> propositions) {
		return propositions.isEmpty() ? "()" : join(separator, propositions.toArray());
	}
	
	public static final Expression<?> apply(final Module module, final Expression<?> condition) {
		if (!module.getPropositions().get(0).accept(Variable.RESET).equals(condition)) {
			Tools.debugError(module.getPropositions().get(0), " VS ", condition);
			Tools.debugError(module.getPropositions().get(0).getClass(), " VS ", condition.getClass());
			throw new IllegalArgumentException();
		}
		
		if (module.getParameters().isEmpty() && module.getPropositions().size() == 2) {
			return module.getPropositions().get(1).accept(Variable.BIND);
		}
		
		{
			final Module fact = new Module();
			
			fact.getParameters().getElements().addAll(module.getParameters().getElements());
			fact.getPropositions().getElements().addAll(module.getPropositions().getElements().subList(1, module.getPropositions().size()));
			
			return fact.accept(Variable.BIND);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static final <E, F extends E> F removeLast(final List<E> list) {
		return (F) list.remove(lastIndex(list));
	}
	
}
