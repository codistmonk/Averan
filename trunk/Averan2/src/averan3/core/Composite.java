package averan3.core;

import static net.sourceforge.aprog.tools.Tools.cast;
import static net.sourceforge.aprog.tools.Tools.join;
import averan.common.Container;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author codistmonk (creation 2015-01-04)
 */
final class Composite<E extends Expression<?>> implements Expression<E> {
	
	private final Composite<?> parent;
	
//	private final Composite<Variable> parameters;
	
	private final List<E> elements;
	
	public Composite(final Composite<?> parent) {
		this.parent = parent;
//		this.parameters = new Composite<>(this);
		this.elements = new ArrayList<>();
	}
	
	public final Composite<?> getParent() {
		return this.parent;
	}
	
//	public final Composite<Variable> getParameters() {
//		return this.parameters;
//	}
	
	public final Composite<Expression<?>> getParameters() {
		if (this.size() == 2) {
			final Composite<Expression<?>> candidate = cast(Composite.class, this.get(0));
			
			if (candidate != null && (candidate.isEmpty()
					|| 2 <= candidate.size() && FORALL.equals(candidate.get(0)))) {
				return candidate;
			}
		}
		
		return null;
	}
	
	public final <F extends Expression<?>> F getContents() {
		return (F) (this.getParameters() != null ? this.get(1) : this);
	}
	
	public final Composite<E> setupAsBlock() {
		if (!this.isEmpty()) {
			throw new IllegalStateException();
		}
		
		this.add((E) new Composite<>(this)); // parameters
		this.add((E) new Composite<>(this)); // parametrizedContent
		
		return this;
	}
	
	@SuppressWarnings("unchecked")
	public final Composite<E> add(final E element) {
		final Composite<?> composite = cast(Composite.class, element);
		
		if (composite == null || composite.getParent() == this) {
			this.elements.add((E) element);
		} else {
			final Composite<Expression<?>> newComposite = new Composite<>(this);
			
//			composite.getParameters().forEach(newComposite.getParameters()::add);
			composite.forEach(newComposite::add);
			
			this.elements.add((E) newComposite);
		}
		
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
	
	@Override
	public final <V> V accept(final Visitor<V> visitor) {
		return visitor.visit(this);
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
//		return formatParameters(this.getParameters()) + this.elements.toString();
		return this.elements.toString();
	}
	
	private static final long serialVersionUID = -7248608001486237448L;
	
	public static final Symbol<String> FORALL = new Symbol<>("∀");
	
	public static final Symbol<String> IMPLIES = new Symbol<>("->");
	
	public static final Symbol<String> EQUALS = new Symbol<>("=");
	
	public static final String formatParameters(final Container<Variable> parameters) {
		return parameters.isEmpty() ? "" : ("∀" + join(",", parameters.toArray()) + " ");
	}
	
}
