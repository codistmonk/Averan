package jrewrite2;

import static jrewrite2.Symbol.enclose;
import static net.sourceforge.aprog.tools.Tools.cast;

/**
 * @author codistmonk (creation 2014-06-10)
 */
public final class Template implements Expression {
	
	private final String variableName;
	
	private final Expression proposition;
	
	public Template(final String variableName,
			final Expression proposition) {
		this.variableName = variableName;
		this.proposition = proposition;
	}
	
	public final String getVariableName() {
		return this.variableName;
	}
	
	public final Expression getProposition() {
		return this.proposition;
	}
	
	@Override
	public final int hashCode() {
		return this.getVariableName().hashCode() + this.getProposition().hashCode();
	}
	
	@Override
	public final boolean equals(final Object object) {
		final Template that = cast(this.getClass(), object);
		
		return that != null && this.getVariableName().equals(that.getVariableName())
				&& this.getProposition().equals(that.getProposition());
	}
	
	@Override
	public final String toString() {
		return "?" + this.getVariableName() + " " + enclose(this.getProposition());
	}
	
	@Override
	public final Object accept(final Visitor visitor) {
		Object result = visitor.visitBeforeChildren(this);
		
		if (result == null) {
			final Object[] childrenVisitationResults = {
					this.getProposition().accept(visitor)
			};
			result = visitor.visitAfterChildren(this, childrenVisitationResults);
		}
		
		return result;
	}
	
	/**
	 * {@value}.
	 */
	private static final long serialVersionUID = -7812276973493623763L;
	
}
