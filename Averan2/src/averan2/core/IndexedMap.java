package averan2.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author codistmonk (creation 2014-12-26)
 * @param <K>
 * @param <V>
 */
public final class IndexedMap<K, V> implements Map<K, V>, Container<K> {
	
	private final Map<K, V> map;
	
	private final List<K> keys;
	
	public IndexedMap() {
		this(new LinkedHashMap<>());
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
		return 0 <= index && index < this.size() ? (F) this.keys().get(index) : null;
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
	
	@Override
	public final String toString() {
		return this.map().toString();
	}
	
	private static final long serialVersionUID = 85013155965917473L;
	
}
