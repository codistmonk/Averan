package jrewrite2;

import static net.sourceforge.aprog.tools.Tools.cast;

/**
 * @author codistmonk (creation 2014-06-10)
 */
public final class Equality implements Expression {
	
	private final Expression left;
	
	private final Expression right;
	
	public Equality(final Expression left, final Expression right) {
		this.left = left;
		this.right = right;
	}
	
	public final Expression getLeft() {
		return this.left;
	}
	
	public final Expression getRight() {
		return this.right;
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
		return "(" + this.getLeft() + ") = (" + this.getRight() + ")";
	}
	
	@Override
	public final Object accept(final Visitor visitor) {
		Object result = visitor.visitBeforeChildren(this);
		
		if (result == null) {
			final Object[] childrenVisitationResults = {
					this.getLeft().accept(visitor),
					this.getRight().accept(visitor)
			};
			result = visitor.visitAfterChildren(this, childrenVisitationResults);
		}
		
		return result;
	}
	
	/**
	 * {@value}.
	 */
	private static final long serialVersionUID = -6030046899340956435L;
	
}
