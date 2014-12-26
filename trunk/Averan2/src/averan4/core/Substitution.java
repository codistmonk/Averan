package averan4.core;

import static averan4.core.Composite.listAccept;
import static net.sourceforge.aprog.tools.Tools.cast;
import static net.sourceforge.aprog.tools.Tools.join;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author codistmonk (creation 2014-12-20)
 */
public final class Substitution implements Expression<Expression<?>>, Expression.Visitor<Expression<?>> {
	
	private final Composite<Equality> bindings;
	
	private final Composite<Symbol<AtomicInteger>> indices;
	
	private final Symbol<AtomicInteger> currentIndex;
	
	public Substitution() {
		this.bindings = new Composite<>();
		this.indices = new Composite<>();
		this.currentIndex = new Symbol<>(new AtomicInteger());
	}
	
	public final Composite<Equality> getBindings() {
		return this.bindings;
	}
	
	public final Composite<Symbol<AtomicInteger>> getIndices() {
		return this.indices;
	}
	
	public final Substitution reset() {
		this.currentIndex.getObject().set(0);
		
		return this;
	}
	
	@Override
	public final Expression<?> visit(final Symbol<?> symbol) {
		return this.tryToReplace(symbol);
	}
	
	@Override
	public final Expression<?> visit(final Variable variable) {
		return this.tryToReplace(variable);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public final Expression<?> visit(final Composite<?> composite) {
		Expression<?> candidate = this.tryToReplace(composite);
		
		if (candidate == composite) {
			candidate = new Composite<>();
			
			if (!listAccept((Composite<Expression<?>>) composite, this,
					(Collection<Expression<?>>) ((Composite<?>) candidate).getElements())) {
				return composite;
			}
		}
		
		return candidate;
	}
	
	@Override
	public final int size() {
		return 2;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public final <E extends Expression<?>> E get(final int index) {
		switch (index) {
		case 0:
			return (E) this.getBindings();
		case 1:
			return (E) this.getIndices();
		}
		
		return null;
	}
	
	@Override
	public final <V> V accept(final Visitor<V> visitor) {
		return visitor.visit(this);
	}
	
	@Override
	public final int hashCode() {
		return this.getBindings().hashCode() + this.getIndices().hashCode();
	}
	
	@Override
	public final boolean equals(final Object object) {
		final Substitution that = cast(this.getClass(), object);
		
		return that != null && this.getBindings().equals(that.getBindings())
				&& this.getIndices().equals(that.getIndices());
	}
	
	@Override
	public final String toString() {
		return "{" + join(",", this.getBindings().getElements().toArray())
				+ "}[" + join(",", this.getIndices().getElements().toArray()) + "]";
	}
	
	private final Expression<?> tryToReplace(final Expression<?> expression) {
		for (final Equality binding : this.bindings) {
			if (binding.getLeft().accept(Variable.RESET).equals(expression)
					&& (this.indices.size() == 0 || 0 <= indexOf(this.nextIndex(), this.indices))) {
				return binding.getRight().accept(Variable.BIND.reset());
			}
		}
		
		return expression;
	}
	
	private final Symbol<AtomicInteger> nextIndex() {
		this.currentIndex.getObject().incrementAndGet();
		
		return this.currentIndex;
	}
	
	private static final long serialVersionUID = -1572047035151529843L;
	
	public static final <E extends Expression<?>> int indexOf(final E needle, final Composite<E> haystack) {
		final int n = haystack.size();
		
		for (int i = 0; i < n; ++i) {
			if (needle.equals(haystack.get(i))) {
				return i;
			}
		}
		
		return -1;
	}
	
}
