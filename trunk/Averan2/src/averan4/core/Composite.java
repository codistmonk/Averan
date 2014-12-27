package averan4.core;

import static net.sourceforge.aprog.tools.Tools.cast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author codistmonk (creation 2014-12-20)
 */
public final class Composite<E extends Expression<?>> implements Expression<E> {
	
	private final List<E> elements;
	
	public Composite() {
		this.elements = new ArrayList<E>();
	}
	
	@Override
	public final int size() {
		return this.getElements().size();
	}
	
	@Override
	public final E get(final int index) {
		return this.getElements().get(index);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public final <V> V accept(final Expression.Visitor<V> visitor) {
		return visitor.visit((Composite<Expression<?>>) this);
	}
	
	@Override
	public final int hashCode() {
		return this.getElements().hashCode();
	}
	
	@Override
	public final boolean equals(final Object object) {
		if (this == object) {
			return true;
		}
		
		final Composite<?> that = cast(this.getClass(), object);
		
		return that != null && this.getElements().equals(that.getElements());
	}
	
	@Override
	public final String toString() {
		return this.getElements().toString();
	}
	
	final List<E> getElements() {
		return this.elements;
	}
	
	private static final long serialVersionUID = -3768801167161895932L;
	
	public static final <T> boolean listAccept(final Iterable<Expression<?>> elements,
			final Visitor<T> visitor, final Collection<T> visitOutput) {
		final boolean[] result = { false };
		
		elements.forEach(e -> {
			final T object = e.accept(visitor);
			
			result[0] |= e != object;
			
			visitOutput.add(object);
		});
		
		return result[0];
	}
	
}