package averan2.core;

import static multij.tools.Tools.cast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author codistmonk (creation 2014-12-20)
 */
public final class Composite<E extends Expression<?>> implements Expression<E> {
	
	private final List<E> elements;
	
	public Composite() {
		this.elements = new ArrayList<>();
	}
	
	@Override
	public final int size() {
		return this.getElements().size();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public final <F extends E> F get(final int index) {
		return (F) this.getElements().get(index);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public final <V> V accept(final Expression.Visitor<V> visitor) {
		return visitor.visit((Composite<Expression<?>>) this);
	}
	
	@Override
	public final Stream<E> stream() {
		return this.getElements().stream();
	}
	
	@Override
	public final boolean implies(final Expression<?> expression) {
		if (this == expression) {
			return true;
		}
		
		final Composite<?> that = cast(this.getClass(), expression);
		
		if (that == null) {
			return false;
		}
		
		final int thisSize = this.size();
		final int thatSize = that.size();
		
		if (thisSize != thatSize) {
			return false;
		}
		
		for (int i = 0; i < thisSize; ++i) {
			if (!this.get(i).implies(that.get(i))) {
				return false;
			}
		}
		
		return true;
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
		
		final Variable variable = cast(Variable.class, object);
		
		if (variable != null) {
			return variable.equals(this);
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
	
	public static final Composite<Expression<?>> composite(final Expression<?>... expressions) {
		final Composite<Expression<?>> result = new Composite<>();
		
		for (final Expression<?> element : expressions) {
			result.getElements().add(element);
		}
		
		return result;
	}
	
}
