package averan4.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
		this.conditions = new Composite<Expression<?>>();
		this.conditionIds = new IndexedMap<>();
		this.facts = new Composite<Expression<?>>();
		this.factIds = new IndexedMap<>();
		this.proofs = new ArrayList<>();
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
	
	private static final long serialVersionUID = 9140955565054672814L;
	
	/**
	 * @author codistmonk (creation 2014-12-26)
	 */
	public static abstract interface Proof extends Serializable {
		
	}
	
}
