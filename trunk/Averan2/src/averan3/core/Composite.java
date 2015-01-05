package averan3.core;

import static net.sourceforge.aprog.tools.Tools.cast;

import java.util.ArrayList;
import java.util.List;

/**
 * @author codistmonk (creation 2015-01-04)
 */
final class Composite<E extends Expression<?>> implements Expression<E> {
	
	private final List<E> elements;
	
	public Composite() {
		this.elements = new ArrayList<>();
	}
	
	@SuppressWarnings("unchecked")
	public final <F extends E> F getKey() {
		return this.size() == 3 && EQUALS.equals(this.get(1)) ? (F) this.get(0) : null;
	}
	
	@SuppressWarnings("unchecked")
	public final <F extends E> F getValue() {
		return this.getKey() != null ? (F) this.get(2) : null;
	}
	
	@SuppressWarnings("unchecked")
	public final <F extends E> F getCondition() {
		return this.size() == 3 && IMPLIES.equals(this.get(1)) ? (F) this.get(0) : null;
	}
	
	@SuppressWarnings("unchecked")
	public final <F extends E> F getConclusion() {
		return this.getCondition() != null ? (F) this.get(2) : null;
	}
	
	public final Composite<Expression<?>> getParameters() {
		if (this.size() == 2) {
			@SuppressWarnings("unchecked")
			final Composite<Expression<?>> candidate = cast(Composite.class, this.get(0));
			
			if (candidate != null && (2 <= candidate.size() && FORALL.equals(candidate.get(0)))) {
				return candidate;
			}
		}
		
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public final <F extends Expression<?>> F getContents() {
		return (F) (this.getParameters() != null ? this.get(1) : this);
	}
	
	public final Composite<E> add(final E element) {
		this.elements.add(element);
		
		return this;
	}
	
	public final E removeLast() {
		return this.elements.remove(this.size() - 1);
	}
	
	@Override
	public final int size() {
		return this.elements.size();
	}
	
	@Override
	public final E get(final int index) {
		return 0 <= index && index < this.size() ? this.elements.get(index) : null;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public final <V> V accept(final Visitor<V> visitor) {
		return visitor.visit((Composite<Expression<?>>) this);
	}
	
	@Override
	public final boolean implies(final Expression<?> expression) {
		final Composite<?> that = cast(this.getClass(), expression);
		
		if (that == null) {
			return false;
		}
		
		final int thisSize = this.size();
		final int thatSize = that.size();
		
		full_implication:
		if (thisSize == thatSize) {
			for (int i = 0; i < thisSize; ++i) {
				if (!this.get(i).implies(that.get(i))) {
					break full_implication;
				}
			}
		}
		
		if (this.getParameters() != null) {
			return this.getContents().implies(expression);
		}
		
		return false;
	}
	
	@Override
	public final int hashCode() {
		return this.elements.hashCode();
	}
	
	@Override
	public final boolean equals(final Object object) {
		if (this == object) {
			return true;
		}
		
		if (object instanceof Variable) {
			return object.equals(this);
		}
		
		final Composite<?> that = cast(this.getClass(), object);
		
		return that != null && this.elements.equals(that.elements);
	}
	
	@Override
	public final String toString() {
		return this.elements.toString();
	}
	
	private static final long serialVersionUID = -7248608001486237448L;
	
	public static final Symbol<String> FORALL = new Symbol<>("∀");
	
	public static final Symbol<String> IMPLIES = new Symbol<>("→");
	
	public static final Symbol<String> EQUALS = new Symbol<>("=");
	
}
