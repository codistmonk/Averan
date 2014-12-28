package averan2.core;

import static averan2.core.Composite.composite;
import static averan2.core.Equality.equality;
import static averan2.core.Substitution.ANY_INDEX;
import static net.sourceforge.aprog.tools.Tools.cast;
import static net.sourceforge.aprog.tools.Tools.join;

import java.io.Serializable;
import java.util.ArrayList;
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
	
	private final Composite<Expression<?>> conditions;
	
	private final IndexedMap<String, Integer> conditionIds;
	
	private final Composite<Expression<?>> facts;
	
	private final IndexedMap<String, Integer> factIds;
	
	private final List<Proof> proofs;
	
	public Module() {
		this(null);
	}
	
	public Module(final Module context) {
		this.context = context;
		this.conditions = new Composite<>();
		this.conditionIds = new IndexedMap<>();
		this.facts = new Composite<>();
		this.factIds = new IndexedMap<>();
		this.proofs = new ArrayList<>();
	}
	
	public final Module canonicalize() {
		while (this.getFacts().size() == 1 && this.getFacts().get(0) instanceof Module) {
			final Module fact = (Module) this.getFacts().getElements().remove(0);
			
			for (final Map.Entry<String, Integer> id : fact.getConditionIds().entrySet()) {
				this.addCondition(id.getKey(), fact.getConditions().get(id.getValue()));
			}
			
			for (final Map.Entry<String, Integer> id : fact.getFactIds().entrySet()) {
				this.addFact(id.getKey(), fact.getFacts().get(id.getValue()), fact.getProof(id.getKey()));
			}
		}
		
		return this;
	}
	
	public final <E extends Expression<?>> E findProposition(final String name) {
		if (name == null) {
			return null;
		}
		
		Integer index = this.getFactIds().get(name);
		
		if (index != null) {
			return this.getFacts().get(index);
		}
		
		index = this.getConditionIds().get(name);
		
		if (index != null) {
			return this.getConditions().get(index);
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
	public final Composite<Expression<?>> get(final int index) {
		switch (index) {
		case 0:
			return this.getConditions();
		case 1:
			return this.getFacts();
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
		
		for (int j = this.getFactIds().size() - 1; 0 <= j; --j, ++i) {
			if (i == -1) {
				return this.getFactIds().get(j);
			}
		}
		
		for (int j = this.getConditionIds().size() - 1; 0 <= j; --j, ++i) {
			if (i == -1) {
				return this.getConditionIds().get(j);
			}
		}
		
		return this.getContext() != null ? this.getContext().getPropositionName(i) : null;
	}
	
	public final <E extends Expression<?>> E getProposition(final int index) {
		if (0 <= index) {
			throw new IllegalArgumentException("Expected negative value but got: " + index);
		}
		
		int i = index;
		
		for (int j = this.getFacts().size() - 1; 0 <= j; --j, ++i) {
			if (i == -1) {
				return this.getFacts().get(j);
			}
		}
		
		for (int j = this.getConditions().size() - 1; 0 <= j; --j, ++i) {
			if (i == -1) {
				return this.getConditions().get(j);
			}
		}
		
		return this.getContext() != null ? this.getContext().getProposition(i) : null;
	}
	
	public final Composite<Expression<?>> getConditions() {
		return this.conditions;
	}
	
	public final IndexedMap<String, Integer> getConditionIds() {
		return this.conditionIds;
	}
	
	public final Composite<Expression<?>> getFacts() {
		return this.facts;
	}
	
	public final IndexedMap<String, Integer> getFactIds() {
		return this.factIds;
	}
	
	public final Proof getProof(final String factName) {
		return this.getProofs().get(this.getFactIds().get(factName));
	}
	
	@Override
	public final int hashCode() {
		this.canonicalize();
		
		return this.getConditions().hashCode() + this.getFacts().hashCode();
	}
	
	@Override
	public final boolean equals(final Object object) {
		final Module that = cast(this.getClass(), object);
		
		return that != null && this.canonicalize().getConditions().equals(that.canonicalize().getConditions())
				&& this.getFacts().equals(that.getFacts());
	}
	
	@Override
	public final String toString() {
		return formatPropositions("->", this.getConditions()) + "->" + formatPropositions("/\\", this.getFacts());
	}
	
	public final Module suppose(final Expression<?> condition) {
		return this.addCondition(null, condition);
	}
	
	public final Module conclude(final Expression<?> fact) {
		return this.addFact(null, fact, null);
	}
	
	final Module addCondition(final String name, final Expression<?> proposition) {
		return this.addProposition(name, proposition, this.getConditions(), this.getConditionIds());
	}
	
	final Module addFact(final String name, final Expression<?> proposition, final Proof proof) {
		return this.addProposition(name, proposition, this.getFacts(), this.getFactIds()).addProof(proof);
	}
	
	final List<Proof> getProofs() {
		return this.proofs;
	}
	
	private final Module addProposition(final String name, final Expression<?> proposition,
			final Composite<Expression<?>> propositions, final IndexedMap<String, Integer> propositionIds) {
		if (this.findProposition(name) != null) {
			throw new IllegalArgumentException("Duplicate proposition name: " + name);
		}
		
		propositionIds.put(name, propositions.size());
		propositions.getElements().add(proposition);
		
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
			
			if (module.getConditions().size() == 0) {
				if (this.getConditionName() != null) {
					throw new IllegalArgumentException();
				}
				
				int i = 0;
				
				for (final Map.Entry<String, Integer> id : module.getFactIds().entrySet()) {
					context.addFact(this.getFactName() + "." + (++i), module.getFacts().get(id.getValue()), this);
				}
				
				return this;
			}
			
			return this.addFactToContext(Module.apply(module, context.findProposition(this.getConditionName())));
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
		
		private static final long serialVersionUID = 5020773952478671657L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-12-27)
	 */
	public final class ProofByDeduce extends Proof {
		
		private final Module deduction;
		
		public ProofByDeduce(final String factName, final Module deduction) {
			super(factName);
			
			if (Module.this != deduction.getContext()
					|| deduction.getConditionIds().size() != deduction.getConditions().size()
					|| deduction.getFactIds().size() != deduction.getFacts().size()) {
				throw new IllegalArgumentException();
			}
			
			for (final String name : deduction.getConditionIds().keySet()) {
				if (name == null) {
					throw new IllegalArgumentException();
				}
			}
			
			for (final String name : deduction.getFactIds().keySet()) {
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
			final Composite<Expression<?>> deducedFacts = this.getDeduction().getFacts();
			final Expression<?> lastDeducedFact = deducedFacts.get(deducedFacts.size() - 1);
			final Expression<?> fact;
			
			if (this.getDeduction().getConditions().size() == 0) {
				fact = lastDeducedFact;
			} else {
				fact = new Module();
				
				((Module) fact).getConditions().getElements().addAll(this.getDeduction().getConditions().getElements());
				((Module) fact).getFacts().getElements().add(lastDeducedFact);
				((Module) fact).canonicalize();
			}
			
			return this.addFactToContext(fact);
		}
		
		private static final long serialVersionUID = 8959982180068107269L;
		
	}
	
	private static final long serialVersionUID = 9140955565054672814L;
	
	public static final String formatPropositions(final String separator, final Composite<Expression<?>> propositions) {
		return propositions.size() == 0 ? "()" : join(separator, propositions.getElements().toArray());
	}
	
	public static final Expression<?> apply(final Module module, final Expression<?> condition) {
		if (!module.getConditions().get(0).accept(Variable.RESET).equals(condition)) {
			Tools.debugError(module.getConditions().get(0), condition);
			throw new IllegalArgumentException();
		}
		
		if (module.getConditions().size() == 1 && module.getFacts().size() == 1) {
			return module.getFacts().get(0).accept(Variable.BIND);
		}
		
		{
			final Module fact = new Module();
			
			fact.getConditions().getElements().addAll(module.getConditions().getElements().subList(1, module.getConditions().size()));
			fact.getFacts().getElements().addAll(module.getFacts().getElements());
			
			return fact.accept(Variable.BIND);
		}
	}
	
}
