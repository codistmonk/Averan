package averan2.core;

import java.io.Serializable;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import net.sourceforge.aprog.tools.IllegalInstantiationException;

/**
 * @author codistmonk (creation 2014-12-20)
 */
public abstract interface Expression<E extends Expression<?>> extends Container<E> {
	
	public abstract boolean implies(Expression<?> expression);
	
	public abstract <V> V accept(Visitor<V> visitor);
	
	/**
	 * @author codistmonk (creation 2014-12-20)
	 */
	public static abstract interface Visitor<V> extends Serializable {
		
		public abstract V visit(Symbol<?> symbol);
		
		public abstract V visit(Variable variable);
		
		public abstract V visit(Composite<Expression<?>> composite);
		
		public abstract V visit(Module module);
		
		public abstract V visit(Substitution substitution);
		
		public abstract V visit(Equality equality);
		
		public static <E extends Expression<?>> E visitElementsOf(final E expression, final Visitor<?> visitor) {
			for (final Expression<?> element : expression) {
				element.accept(visitor);
			}
			
			return expression;
		}
		
	}
	
	/**
	 * @author codistmonk (creation 2014-12-31)
	 */
	public static final class Metadata {
		
		private Metadata() {
			throw new IllegalInstantiationException();
		}
		
		private static final Map<Expression<?>, Map<String, Object>> metadata = new IdentityHashMap<>();
		
		public static final Map<String, Object> of(final Expression<?> expression) {
			return metadata.computeIfAbsent(expression, e -> new LinkedHashMap<>());
		}
		
		@SuppressWarnings("unchecked")
		public static final <V> V put(final Expression<?> target, final String key, final Object value) {
			return (V) Metadata.of(target).put(key, value);
		}
		
		@SuppressWarnings("unchecked")
		public static final <V> V get(final Expression<?> target, final String key) {
			return (V) metadata.getOrDefault(target, Collections.emptyMap()).get(key);
		}
		
		@SuppressWarnings("unchecked")
		public static final <V> V remove(final Expression<?> target, final String key) {
			return (V) metadata.getOrDefault(target, Collections.emptyMap()).remove(key);
		}
		
		public static final <E extends Expression<?>> E copy(final Expression<?> source, final E target) {
			final Map<String, Object> sourceMetadata = metadata.get(source);
			
			if (sourceMetadata != null && !sourceMetadata.isEmpty()) {
				Metadata.of(target).putAll(sourceMetadata);
			}
			
			return target;
		}
		
	}
	
}
