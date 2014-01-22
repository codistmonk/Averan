package jrewrite;

import static net.sourceforge.aprog.tools.Tools.cast;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author codistmonk (creation 2013-12-16)
 */
public final class Constant implements Expression {
	
	private final String symbol;
	
	public Constant(final String symbol) {
		this.symbol = symbol;
	}
	
	@Override
	public final int hashCode() {
		return this.symbol.hashCode();
	}
	
	@Override
	public final boolean equals(final Object object) {
		final Constant that = cast(this.getClass(), object);
		
		return that != null && this.symbol.equals(that.symbol);
	}
	
	@Override
	public final boolean matches(final Expression expression,
			final Map<String, Expression> context) {
		if (expression instanceof Variable) {
			return expression.matches(this, context);
		}
		
		return this.toString().equals(expression.toString());
	}
	
	@Override
	public final Constant refine(final Map<String, Expression> context) {
		return this;
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
	private static final long serialVersionUID = 4327968938495811368L;
	
}
