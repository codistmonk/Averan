package averan4.core;

import static net.sourceforge.aprog.tools.Tools.cast;
import static net.sourceforge.aprog.tools.Tools.join;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
		return this.proofs.get(this.getFactIds().get(factName));
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
	
	final Module addCondition(final String name, final Expression<?> proposition) {
		return this.addProposition(name, proposition, this.getConditions(), this.getConditionIds());
	}
	
	final Module addFact(final String name, final Expression<?> proposition, final Proof proof) {
		return this.addProposition(name, proposition, this.getFacts(), this.getFactIds()).addProof(proof);
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
		
		abstract Proof apply();
		
		private static final long serialVersionUID = 5282247624876197313L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-12-26)
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
		final ProofByApply apply() {
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
			
			{
				final Expression<?> condition = context.findProposition(this.getConditionName());
				
				if (!module.getConditions().get(0).accept(Variable.RESET).equals(condition)) {
					throw new IllegalArgumentException();
				}
				
				if (module.getConditions().size() == 1 && module.getFacts().size() == 1) {
					context.addFact(this.getFactName(), module.getFacts().get(0).accept(Variable.BIND), this);
				} else {
					final Module fact = new Module(null);
					
					fact.getConditions().getElements().addAll(module.getConditions().getElements().subList(1, module.getConditions().size() - 1));
					fact.getFacts().getElements().addAll(module.getFacts().getElements());
					
					context.addFact(this.getFactName(), fact.accept(Variable.BIND), this);
				}
				
				return this;
			}
		}
		
		private static final long serialVersionUID = 1974410943023589433L;
		
	}
	
	private static final long serialVersionUID = 9140955565054672814L;
	
	public static final String formatPropositions(final String separator, final Composite<Expression<?>> propositions) {
		return propositions.size() == 0 ? "()" : join(separator, propositions.getElements().toArray());
	}
	
}
