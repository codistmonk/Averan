package jrewrite2;

import static net.sourceforge.aprog.tools.Tools.cast;

/**
 * @author codistmonk (creation 2014-06-10)
 */
public final class Symbol implements Expression {
	
	private final String string;
	
	public Symbol(final String string) {
		this.string = string;
	}
	
	@Override
	public final int hashCode() {
		return this.toString().hashCode();
	}
	
	@Override
	public final boolean equals(final Object object) {
		final Symbol that = cast(this.getClass(), object);
		
		return that != null && this.toString().equals(that.toString());
	}
	
	@Override
	public final String toString() {
		return this.string;
	}
	
	@Override
	public final Object accept(final Visitor visitor) {
		return visitor.visit(this);
	}
	
	/**
	 * {@value}.
	 */
	private static final long serialVersionUID = 1502089546938825632L;
	
	public static final String enclose(final Expression expression) {
		return expression instanceof Symbol ? expression.toString() : "(" + expression + ")";
	}
	
}
