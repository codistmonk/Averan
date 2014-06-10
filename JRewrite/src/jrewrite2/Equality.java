package jrewrite2;

import static jrewrite2.Symbol.enclose;
import static net.sourceforge.aprog.tools.Tools.cast;

/**
 * @author codistmonk (creation 2014-06-10)
 */
public final class Equality implements Expression {
	
	private final Composite composite;
	
	public Equality(final Expression left, final Expression right) {
		this(new Composite(left, right));
	}
	
	public Equality(final Composite composite) {
		if (composite.getChildCount() != 2) {
			throw new IllegalArgumentException();
		}
		
		this.composite = composite;
	}
	
	public final Composite getComposite() {
		return this.composite;
	}
	
	public final Expression getLeft() {
		return this.getComposite().getChild(0);
	}
	
	public final Expression getRight() {
		return this.getComposite().getChild(1);
	}
	
	@Override
	public final int hashCode() {
		return this.getComposite().hashCode();
	}
	
	@Override
	public final boolean equals(final Object object) {
		final Equality that = cast(this.getClass(), object);
		
		return that != null && this.getComposite().equals(that.getComposite());
	}
	
	@Override
	public final String toString() {
		return enclose(this.getLeft()) + " = " + enclose(this.getRight());
	}
	
	@Override
	public final Object accept(final Visitor visitor) {
		Object result = visitor.visitBeforeChildren(this);
		
		if (result == null) {
			result = visitor.visitAfterChildren(this
					, this.getComposite().computeChildrenVisitationResults(visitor));
		}
		
		return result;
	}
	
	/**
	 * {@value}.
	 */
	private static final long serialVersionUID = -6030046899340956435L;
	
}
