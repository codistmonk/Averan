package averan.common;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import multij.tools.IllegalInstantiationException;

/**
 * @author codistmonk (creation 2014-12-31)
 */
public final class Metadata {
	
	private Metadata() {
		throw new IllegalInstantiationException();
	}
	
	private static final Map<Object, Map<String, Object>> metadata = new IdentityHashMap<>();
	
	public static final Map<String, Object> of(final Object object) {
		return metadata.computeIfAbsent(object, e -> new LinkedHashMap<>());
	}
	
	@SuppressWarnings("unchecked")
	public static final <V> V put(final Object target, final String key, final Object value) {
		return (V) Metadata.of(target).put(key, value);
	}
	
	@SuppressWarnings("unchecked")
	public static final <V> V get(final Object target, final String key) {
		return (V) metadata.getOrDefault(target, Collections.emptyMap()).get(key);
	}
	
	@SuppressWarnings("unchecked")
	public static final <V> V remove(final Object target, final String key) {
		return (V) metadata.getOrDefault(target, Collections.emptyMap()).remove(key);
	}
	
	public static final <T> T copy(final Object source, final T target) {
		final Map<String, Object> sourceMetadata = metadata.get(source);
		
		if (sourceMetadata != null && !sourceMetadata.isEmpty()) {
			Metadata.of(target).putAll(sourceMetadata);
		}
		
		return target;
	}
	
}
