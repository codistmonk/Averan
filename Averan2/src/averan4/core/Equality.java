package averan4.core;

import static net.sourceforge.aprog.tools.Tools.cast;

/**
 * @author codistmonk (creation 2014-12-26)
 */
public final class Equality implements Expression<Expression<?>> {
	
	private final Expression<?> left;
	
	private final Expression<?> right;
	
	public Equality(final Expression<?> left, final Expression<?> right) {
		this.left = left;
		this.right = right;
	}
	
	@SuppressWarnings("unchecked")
	public final <E extends Expression<?>> E getLeft() {
		return (E) this.left;
	}
	
	@SuppressWarnings("unchecked")
	public final <E extends Expression<?>> E getRight() {
		return (E) this.right;
	}
	
	@Override
	public final int size() {
		return 2;
	}
	
	@Override
	public final <E extends Expression<?>> E get(final int index) {
		switch (index) {
		case 0:
			return this.getLeft();
		case 1:
			return this.getRight();
		}
		
		return null;
	}
	
	@Override
	public final <V> V accept(final Visitor<V> visitor) {
		return visitor.visit(this);
	}
	
	@Override
	public final int hashCode() {
		return this.getLeft().hashCode() + this.getRight().hashCode();
	}
	
	@Override
	public final boolean equals(final Object object) {
		final Equality that = cast(this.getClass(), object);
		
		return that != null && this.getLeft().equals(that.getLeft()) && this.getRight().equals(that.getRight());
	}
	
	@Override
	public final String toString() {
		return this.getLeft() + "=" + this.getRight();
	}
	
	private static final long serialVersionUID = 6733893065994517695L;
	
}
