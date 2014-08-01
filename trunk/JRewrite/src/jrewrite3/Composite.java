package jrewrite3;

import static net.sourceforge.aprog.tools.Tools.cast;

import java.util.ArrayList;
import java.util.List;

/**
 * @author codistmonk (creation 2014-08-01)
 */
final class Composite implements Expression {
	
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
		final R beforeVisit = visitor.visitBeforeChildren(this);
		final List<R> childVisits = Expression.listAccept(this.getChildren(), visitor);
		
		return visitor.visitAfterChildren(this, beforeVisit, childVisits);
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
		return this.getChildren().toString();
	}
	
	/**
	 * {@value}.
	 */
	private static final long serialVersionUID = -280050093929737583L;
	
}