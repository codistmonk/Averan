package averan5.core;

import static net.sourceforge.aprog.tools.Tools.cast;

import java.util.Collection;

/**
 * @author codistmonk (creation 2014-12-20)
 */
public final class Variable implements Expression<Variable> {
	
	private final String name;
	
	private Expression<?> match;
	
	public Variable(final String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
	@SuppressWarnings("unchecked")
	public final <E extends Expression<?>> E getMatch() {
		return (E) this.match;
	}
	
	public final Variable reset() {
		this.match = null;
		
		return this;
	}
	
	@Override
	public final int getElementCount() {
		return 1;
	}
	
	@Override
	public final Variable getElement(final int index) {
		return index == 0 ? this : null;
	}
	
	@Override
	public final <V> V accept(final Expression.Visitor<V> visitor) {
		return visitor.visit(this);
	}
	
	@Override
	public final int hashCode() {
		return this.getName().hashCode();
	}
	
	@Override
	public final boolean equals(final Object object) {
		if (this == object) {
			return true;
		}
		
		final Expression<?> expression = cast(Expression.class, object);
		
		if (expression == null) {
			return false;
		}
		
		Expression<?> match = this.getMatch();
		
		if (match == null) {
			match = this.match = expression;
		}
		
		return match.equals(expression);
	}
	
	@Override
	public final String toString() {
		return this.getName() + "?";
	}
	
	private static final long serialVersionUID = 3015338717755848327L;
	
	public static final Reset RESET = Reset.INSTANCE;
	
	public static final Bind BIND = Bind.INSTANCE;
	
	/**
	 * @author codistmonk (creation 2014-12-20)
	 */
	public static final class Reset implements Expression.Visitor<Expression<?>> {
		
		@Override
		public final Expression<?> visit(final Symbol symbol) {
			return symbol;
		}
		
		@Override
		public final Expression<?> visit(final Variable variable) {
			return variable.reset();
		}
		
		@Override
		public final Expression<?> visit(final Composite<?> composite) {
			for (final Expression<?> element : composite) {
				element.accept(this);
			}
			
			return composite;
		}
		
		private static final long serialVersionUID = 6438401380761494994L;
		
		public static final Variable.Reset INSTANCE = new Reset();
		
	}
	
	/**
	 * @author codistmonk (creation 2014-12-20)
	 */
	public static final class Bind implements Expression.Visitor<Expression<?>> {
		
		@Override
		public final Symbol visit(final Symbol symbol) {
			return symbol;
		}
		
		@Override
		public final Expression<?> visit(final Variable variable) {
			final Expression<?> match  = variable.getMatch();
			
			return match != null ? match : variable;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public final Composite<?> visit(final Composite<?> composite) {
			final Composite<?> candidate = new Composite<>(composite.getContext());
			
			if (Composite.listAccept((Iterable<Expression<?>>) composite, this,
					(Collection<Expression<?>>) candidate.getElements())) {
				return candidate.setElementContexts();
			}
			
			return composite;
		}
		
		private static final long serialVersionUID = -2879093293185572053L;
		
		public static final Bind INSTANCE = new Bind();
		
	}
	
}