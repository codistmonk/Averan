package averan3.core;

import static net.sourceforge.aprog.tools.Tools.cast;

/**
 * @author codistmonk (creation 2014-12-20)
 */
public final class Symbol implements Expression<Symbol> {
	
	private final Object object;
	
	public Symbol(final Object object) {
		this.object = object;
	}
	
	public final Object getObject() {
		return this.object;
	}
	
	@Override
	public final int size() {
		return 1;
	}
	
	@Override
	public final Symbol get(final int index) {
		return index == 0 ? this : null;
	}
	
	@Override
	public final <V> V accept(final Expression.Visitor<V> visitor) {
		return visitor.visit(this);
	}
	
	@Override
	public final int hashCode() {
		return this.toString().hashCode();
	}
	
	@Override
	public final boolean equals(final Object object) {
		if (this == object) {
			return true;
		}
		
		final Symbol that = cast(this.getClass(), object);
		
		return that != null && this.getObject().equals(that.getObject());
	}
	
	@Override
	public final String toString() {
		return this.getObject().toString();
	}
	
	private static final long serialVersionUID = -2105054482894427294L;
	
	public static final Symbol EMPTY = new Symbol("");
	
}
