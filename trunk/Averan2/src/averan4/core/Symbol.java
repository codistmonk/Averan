package averan4.core;

import static net.sourceforge.aprog.tools.Tools.cast;

/**
 * @author codistmonk (creation 2014-12-20)
 */
public final class Symbol<T> implements Expression<Symbol<T>> {
	
	private final T object;
	
	public Symbol(final T object) {
		this.object = object;
	}
	
	public final T getObject() {
		return this.object;
	}
	
	@Override
	public final int size() {
		return 1;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public final Symbol<T> get(final int index) {
		return index == 0 ? this : null;
	}
	
	@Override
	public final <V> V accept(final Expression.Visitor<V> visitor) {
		return visitor.visit(this);
	}
	
	@Override
	public final int hashCode() {
		return this.toString().hashCode();
	}
	
	@Override
	public final boolean equals(final Object object) {
		if (this == object) {
			return true;
		}
		
		final Symbol<?> that = cast(this.getClass(), object);
		
		return that != null && this.getObject().equals(that.getObject());
	}
	
	@Override
	public final String toString() {
		return this.getObject().toString();
	}
	
	private static final long serialVersionUID = -2105054482894427294L;
	
	public static final Symbol<String> EMPTY = symbol("");
	
	public static final <T> Symbol<T> symbol(final T object) {
		return new Symbol<>(object);
	}
	
}
