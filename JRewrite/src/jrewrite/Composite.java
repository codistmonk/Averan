package jrewrite;

import static net.sourceforge.aprog.tools.Tools.cast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author codistmonk (creation 2013-12-16)
 */
public final class Composite implements Expression {
	
	private final List<Expression> subexpressions;
	
	public Composite(final Expression... subexpressions) {
		this.subexpressions = new ArrayList<Expression>();
		
		for (final Expression subexpression : subexpressions) {
			this.getSubexpressions().add(subexpression);
		}
	}
	
	public Composite(final Iterable<Expression> subexpressions) {
		this.subexpressions = new ArrayList<Expression>();
		
		for (final Expression subexpression : subexpressions) {
			this.getSubexpressions().add(subexpression);
		}
	}
	
	public final List<Expression> getSubexpressions() {
		return this.subexpressions;
	}
	
	@Override
	public final int hashCode() {
		return this.getSubexpressions().hashCode();
	}
	
	@Override
	public final boolean equals(final Object object) {
		final Composite that = cast(this.getClass(), object);
		
		return that != null && this.getSubexpressions().equals(that.getSubexpressions());
	}

	@Override
	public final boolean matches(final Expression expression, final Map<String, Expression> context) {
		if (expression instanceof Variable) {
			return expression.matches(this, context);
		}
		
		final Composite that = cast(this.getClass(), expression);
		
		if (that == null) {
			return false;
		}
		
		final int n = this.getSubexpressions().size();
		
		if (n != that.getSubexpressions().size()) {
			return false;
		}
		
		for (int i = 0; i < n; ++i) {
			if (!this.getSubexpressions().get(i).matches(that.getSubexpressions().get(i), context)) {
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public final Composite refine(final Map<String, Expression> context) {
		final Composite result = new Composite();
		
		for (final Expression subexpression : this.getSubexpressions()) {
			result.getSubexpressions().add(subexpression.refine(context));
		}
		
		return result;
	}
	
	@Override
	public final Expression rewrite(final Expression pattern, final AtomicInteger index,
			final Expression replacement) {
		if (this.equals(pattern) && index.decrementAndGet() < 0) {
			return replacement;
		}
		
		final int n = this.getSubexpressions().size();
		
		for (int i = 0; i < n; ++i) {
			final Expression subExpression = this.getSubexpressions().get(i).rewrite(pattern, index, replacement);
			
			if (index.get() < 0) {
				final Composite result = new Composite(this.getSubexpressions());
				
				result.getSubexpressions().set(i, subExpression);
				
				return result;
			}
		}
		
		return this;
	}
	
	@Override
	public final String toString() {
		final StringBuilder resultBuilder = new StringBuilder();
		
		for (final Expression subexpression : this.getSubexpressions()) {
			resultBuilder.append(subexpression);
		}
		
		return resultBuilder.toString();
	}
	
	/**
	 * {@value}.
	 */
	private static final long serialVersionUID = -4963212000398975579L;
	
}
