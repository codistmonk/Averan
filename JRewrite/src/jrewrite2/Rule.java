package jrewrite2;

import static net.sourceforge.aprog.tools.Tools.cast;

/**
 * @author codistmonk (creation 2014-06-10)
 */
public final class Rule implements Expression {
	
	private final Expression condition;
	
	private final Expression expression;
	
	public Rule(final Expression condition, final Expression expression) {
		this.condition = condition;
		this.expression = expression;
	}
	
	public final Expression getCondition() {
		return this.condition;
	}
	
	public final Expression getExpression() {
		return this.expression;
	}
	
	@Override
	public final int hashCode() {
		return this.getCondition().hashCode() + this.getExpression().hashCode();
	}
	
	@Override
	public final boolean equals(final Object object) {
		final Rule that = cast(this.getClass(), object);
		
		return that != null && this.getCondition().equals(that.getCondition())
				&& this.getExpression().equals(that.getExpression());
	}
	
	@Override
	public final String toString() {
		return "(" + this.getCondition() + ") -> (" + this.getExpression() + ")";
	}
	
	@Override
	public final Object accept(final Visitor visitor) {
		Object result = visitor.visitBeforeChildren(this);
		
		if (result == null) {
			final Object[] childrenVisitationResults = {
					this.getCondition().accept(visitor),
					this.getExpression().accept(visitor)
			};
			result = visitor.visitAfterChildren(this, childrenVisitationResults);
		}
		
		return result;
	}
	
	/**
	 * {@value}.
	 */
	private static final long serialVersionUID = -2460814622416134342L;
	
}
