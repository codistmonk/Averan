package averan4.core;

import static net.sourceforge.aprog.tools.Tools.cast;
import static net.sourceforge.aprog.tools.Tools.join;

import java.io.Serializable;
import java.util.ArrayList;
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
				this.addFact(id.getKey(), fact.getConditions().get(id.getValue()), fact.getProof(id.getKey()));
			}
		}
		
		return this;
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
		
		return that != null && this.canonicalize().getConditions().equals(that.getConditions())
				&& this.getFacts().equals(that.getFacts());
	}
	
	@Override
	public final String toString() {
		return join("->", this.getConditions().getElements().toArray())
				+ "->" + join("/\\", this.getFacts().getElements().toArray());
	}
	
	final Module addCondition(final String name, final Expression<?> proposition) {
		return this.addProposition(name, proposition, this.getConditions(), this.getConditionIds());
	}
	
	final Module addFact(final String name, final Expression<?> proposition, final Proof proof) {
		return this.addProposition(name, proposition, this.getFacts(), this.getFactIds()).addProof(proof);
	}
	
	private final Module addProposition(final String name, final Expression<?> proposition,
			final Composite<Expression<?>> propositions, final IndexedMap<String, Integer> propositionIds) {
		propositionIds.put(name, propositions.size());
		propositions.getElements().add(proposition);
		
		return this;
	}
	
	private final Module addProof(final Proof proof) {
		this.proofs.add(proof);
		
		return this;
	}
	
	private static final long serialVersionUID = 9140955565054672814L;
	
	/**
	 * @author codistmonk (creation 2014-12-26)
	 */
	public static abstract interface Proof extends Serializable {
		
	}
	
}
