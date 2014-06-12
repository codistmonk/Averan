package jrewrite2;

import static jrewrite2.Symbol.enclose;
import static net.sourceforge.aprog.tools.Tools.array;
import static net.sourceforge.aprog.tools.Tools.cast;

/**
 * @author codistmonk (creation 2014-06-10)
 */
public final class Template implements Expression {
	
	private final String variableName;
	
	private final Expression proposition;
	
	public Template(final String variableName, final Expression proposition) {
		this.variableName = variableName;
		this.proposition = (Expression) proposition.accept(new Context.Rewriter(new Symbol(variableName), this.new Variable()));
	}
	
	public Template(final String variableName, final Expression proposition, final Template.Variable oldVariable) {
		this.variableName = variableName;
		this.proposition = (Expression) proposition.accept(new Context.Rewriter(oldVariable, this.new Variable()));
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
			result = visitor.visitAfterChildren(this, array(this.getProposition().accept(visitor)));
		}
		
		return result;
	}
	
	/**
	 * @author codistmonk (creation 2014-06-12)
	 */
	public final class Variable implements Expression {
		
		public final Template getTemplate() {
			return Template.this;
		}
		
		@Override
		public final int hashCode() {
			return this.getTemplate().getVariableName().hashCode();
		}
		
		@Override
		public final boolean equals(final Object object) {
			final Variable that = cast(this.getClass(), object);
			
			return that != null && this.getTemplate().toString().equals(that.getTemplate().toString());
		}
		
		@Override
		public final String toString() {
			return this.getTemplate().getVariableName();
		}
		
		@Override
		public final Object accept(final Visitor visitor) {
			return visitor.visit(this);
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = -6993567931969693569L;
		
	}
	
	/**
	 * {@value}.
	 */
	private static final long serialVersionUID = -7812276973493623763L;
	
}
