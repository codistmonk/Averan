package averan3.core;

import static net.sourceforge.aprog.tools.Tools.cast;

import net.sourceforge.aprog.tools.Tools;

/**
 * @author codistmonk (creation 2015-01-04)
 */
final class Symbol<T> implements Expression<Symbol<T>> {
	
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
	public final <V> V accept(final Visitor<V> visitor) {
		return visitor.visit(this);
	}
	
	@Override
	public final boolean implies(final Expression<?> expression) {
		final Symbol<?> that = cast(this.getClass(), expression);
		
		return that != null && Tools.equals(this.getObject(), that.getObject());
	}
	
	@Override
	public final int hashCode() {
		return Tools.hashCode(this.getObject());
	}
	
	@Override
	public final boolean equals(final Object object) {
		if (this == object) {
			return true;
		}
		
		if (object instanceof Variable) {
			return object.equals(this);
		}
		
		final Symbol<?> that = cast(this.getClass(), object);
		
		return that != null && Tools.equals(this.getObject(), that.getObject());
	}
	
	@Override
	public final String toString() {
		return "" + this.getObject();
	}
	
	private static final long serialVersionUID = 4219694484232570312L;
	
}