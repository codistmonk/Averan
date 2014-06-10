package jrewrite2;

import static jrewrite2.Symbol.enclose;
import static net.sourceforge.aprog.tools.Tools.cast;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * @author codistmonk (creation 2014-06-10)
 */
public final class Composite implements Expression, Iterable<Expression> {
	
	private final List<Expression> children;
	
	public Composite(final Expression... children) {
		this.children = Arrays.asList(children);
	}
	
	public final Expression getChild(final int index) {
		return this.children.get(index);
	}
	
	public final int getChildCount() {
		return this.children.size();
	}
	
	@Override
	public final int hashCode() {
		return this.children.hashCode();
	}
	
	@Override
	public final boolean equals(final Object object) {
		final Composite that = cast(this.getClass(), object);
		
		return that != null && this.children.equals(that.children);
	}
	
	@Override
	public final String toString() {
		final StringBuilder resultBuilder = new StringBuilder();
		
		for (final Expression child : this) {
			resultBuilder.append(enclose(child));
		}
		
		return resultBuilder.toString();
	}
	
	@Override
	public final Iterator<Expression> iterator() {
		return this.children.iterator();
	}
	
	@Override
	public final Object accept(final Visitor visitor) {
		Object result = visitor.visitBeforeChildren(this);
		
		if (result == null) {
			result = visitor.visitAfterChildren(this, this.computeChildrenVisitationResults(visitor));
		}
		
		return result;
	}
	
	public final Object[] computeChildrenVisitationResults(final Visitor visitor) {
		final int n = this.getChildCount();
		final Object[] result = new Object[n];
		
		for (int i = 0; i < n; ++i) {
			result[i] = this.getChild(i).accept(visitor);
		}
		
		return result;
	}
	
	/**
	 * {@value}.
	 */
	private static final long serialVersionUID = 7326597190592185422L;
	
}
