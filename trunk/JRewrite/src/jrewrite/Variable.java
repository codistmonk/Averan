package jrewrite;

import static net.sourceforge.aprog.tools.Tools.cast;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author codistmonk (creation 2013-12-16)
 */
public final class Variable implements Expression {
	
	private final String symbol;
	
	public Variable(final String symbol) {
		this.symbol = symbol;
	}
	
	@Override
	public final int hashCode() {
		return this.symbol.hashCode();
	}
	
	@Override
	public final boolean equals(final Object object) {
		final Variable that = cast(this.getClass(), object);
		
		return that != null && this.symbol.equals(that.symbol);
	}
	
	@Override
	public final boolean matches(final Expression expression,
			final Map<String, Expression> context) {
		final Expression match = context.get(this.symbol);
		
		if (match != null) {
			return match.matches(expression, context);
		}
		
		context.put(this.symbol, expression);
		
		return true;
	}
	
	@Override
	public final Expression refine(final Map<String, Expression> context) {
		final Expression match = context.get(this.symbol);
		
		return match != null ? match : this;
	}
	
	@Override
	public final Expression rewrite(final Expression pattern, final AtomicInteger index,
			final Expression replacement) {
		return this.equals(pattern) && index.decrementAndGet() < 0 ? replacement : this;
	}
	
	@Override
	public final String toString() {
		return this.symbol;
	}
	
	/**
	 * {@value}.
	 */
	private static final long serialVersionUID = 7582719153138235143L;
	
}
