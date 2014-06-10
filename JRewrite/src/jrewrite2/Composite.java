package jrewrite2;

import static net.sourceforge.aprog.tools.Tools.cast;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.aprog.tools.Tools;

/**
 * @author codistmonk (creation 2014-06-10)
 */
public final class Composite implements Expression, Iterable<Expression> {
	
	private final List<Expression> children;
	
	public Composite(final Expression[] children) {
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
		return Tools.join("", this.children.toArray());
	}
	
	@Override
	public final Iterator<Expression> iterator() {
		return this.children.iterator();
	}
	
	@Override
	public final Object accept(final Visitor visitor) {
		Object result = visitor.visitBeforeChildren(this);
		
		if (result == null) {
			final int n = this.getChildCount();
			final Object[] childrenVisitationResults = new Object[n];
			
			for (int i = 0; i < n; ++i) {
				childrenVisitationResults[i] = this.getChild(i).accept(visitor);
			}
			
			result = visitor.visitAfterChildren(this, childrenVisitationResults);
		}
		
		return result;
	}
	
	/**
	 * {@value}.
	 */
	private static final long serialVersionUID = 7326597190592185422L;
	
}
