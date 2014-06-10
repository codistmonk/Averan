package jrewrite2;

import static jrewrite2.Symbol.enclose;
import static net.sourceforge.aprog.tools.Tools.cast;

/**
 * @author codistmonk (creation 2014-06-10)
 */
public final class Rule implements Expression {
	
	private final Composite composite;
	
	public Rule(final Expression condition, final Expression expression) {
		this(new Composite(condition, expression));
	}
	
	public Rule(final Composite composite) {
		if (composite.getChildCount() != 2) {
			throw new IllegalArgumentException();
		}
		
		this.composite = composite;
	}
	
	public final Composite getComposite() {
		return this.composite;
	}
	
	public final Expression getCondition() {
		return this.composite.getChild(0);
	}
	
	public final Expression getExpression() {
		return this.composite.getChild(1);
	}
	
	@Override
	public final int hashCode() {
		return this.getComposite().hashCode();
	}
	
	@Override
	public final boolean equals(final Object object) {
		final Rule that = cast(this.getClass(), object);
		
		return that != null && this.getComposite().equals(that.getComposite());
	}
	
	@Override
	public final String toString() {
		return enclose(this.getCondition()) + " -> " + enclose(this.getExpression());
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
	private static final long serialVersionUID = -2460814622416134342L;
	
}
