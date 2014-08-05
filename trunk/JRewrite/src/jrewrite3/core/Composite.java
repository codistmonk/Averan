package jrewrite3.core;

import static net.sourceforge.aprog.tools.Tools.cast;
import static net.sourceforge.aprog.tools.Tools.join;

import java.util.ArrayList;
import java.util.List;

import jrewrite3.core.Module.Symbol;

/**
 * @author codistmonk (creation 2014-08-01)
 */
public final class Composite implements Expression {
	
	private final List<Expression> children;
	
	public Composite() {
		this(8);
	}
	
	public Composite(final int initialCapacity) {
		this(new ArrayList<>(initialCapacity));
	}
	
	public Composite(final List<Expression> children) {
		this.children = children;
	}
	
	public final List<Expression> getChildren() {
		return this.children;
	}
	
	@Override
	public final <R> R accept(final Visitor<R> visitor) {
		return visitor.endVisit(this, visitor.beginVisit(this),
				Expression.listAcceptor(this.getChildren(), visitor));
	}
	
	@Override
	public final int hashCode() {
		return this.getChildren().hashCode();
	}
	
	@Override
	public final boolean equals(final Object object) {
		final Composite that = cast(this.getClass(), object);
		
		return that != null && this.getChildren().equals(that.getChildren());
	}
	
	@Override
	public final String toString() {
		final StringBuilder resultBuilder = new StringBuilder();
		
		for (final Expression child : this.getChildren()) {
			if (child instanceof Symbol) {
				resultBuilder.append(child);
			} else {
				resultBuilder.append('(').append(child).append(')');
			}
		}
		
		return resultBuilder.toString();
	}
	
	/**
	 * {@value}.
	 */
	private static final long serialVersionUID = -280050093929737583L;
	
}