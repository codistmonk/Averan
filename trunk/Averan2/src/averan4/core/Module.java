package averan4.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	
	/**
	 * @author codistmonk (creation 2014-12-26)
	 * @param <K>
	 * @param <V>
	 */
	public static final class IndexedMap<K, V> implements Map<K, V>, Container<K>, Serializable {
		
		private final Map<K, V> map;
		
		private final List<K> keys;
		
		public IndexedMap() {
			this(new HashMap<>());
		}
		
		public IndexedMap(final Map<K, V> map) {
			this.map = map;
			this.keys = new ArrayList<>();
			
			this.keys().addAll(map.keySet());
		}
		
		public final List<K> keys() {
			return this.keys;
		}
		
		public final Map<K, V> map() {
			return this.map;
		}
		
		@Override
		public final int size() {
			return this.map().size();
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public final <F extends K> F get(final int index) {
			return (F) this.keys().get(index);
		}
		
		@Override
		public final boolean isEmpty() {
			return this.map().isEmpty();
		}
		
		@Override
		public final boolean containsKey(final Object key) {
			return this.map().containsKey(key);
		}
		
		@Override
		public final boolean containsValue(final Object value) {
			return this.map().containsValue(value);
		}
		
		@Override
		public final V get(final Object key) {
			return this.map().get(key);
		}
		
		@Override
		public final V put(final K key, final V value) {
			if (!this.containsKey(key)) {
				this.keys().add(key);
			}
			
			return this.map().put(key, value);
		}
		
		@Override
		public final V remove(final Object key) {
			return this.map().remove(key);
		}
		
		@Override
		public final void putAll(final Map<? extends K, ? extends V> m) {
			final Collection<K> newKeys = new LinkedHashSet<>(m.keySet());
			
			newKeys.removeAll(this.keySet());
			
			this.keys().addAll(newKeys);
			this.map().putAll(m);
		}
		
		@Override
		public final void clear() {
			this.keys().clear();
			this.map().clear();
		}
		
		@Override
		public final Set<K> keySet() {
			return this.map().keySet();
		}
		
		@Override
		public final Collection<V> values() {
			return this.map().values();
		}
		
		@Override
		public final Set<Entry<K, V>> entrySet() {
			return this.map.entrySet();
		}
		
		private static final long serialVersionUID = 85013155965917473L;
		
	}
	
}
