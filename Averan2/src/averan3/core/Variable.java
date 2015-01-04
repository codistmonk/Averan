package averan3.core;

import static net.sourceforge.aprog.tools.Tools.cast;

/**
 * @author codistmonk (creation 2015-01-04)
 */
final class Variable implements Expression<Variable> {
	
	private final String name;
	
	private final Object filter;
	
	private Expression<?> match;
	
	public Variable(final String name) {
		this(name, Expression.class);
	}
	
	public Variable(final String name, final Object filter) {
		this.name = name;
		this.filter = filter;
	}
	
	public final String getName() {
		return this.name;
	}
	
	public final Object getFilter() {
		return this.filter;
	}
	
	public final Expression<?> getMatch() {
		return this.match;
	}
	
	public final Variable reset() {
		this.match = null;
		
		return this;
	}
	
	@Override
	public final int size() {
		return 1;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public final Variable get(final int index) {
		return index == 0 ? this : null;
	}
	
	@Override
	public final <V> V accept(final Visitor<V> visitor) {
		return visitor.visit(this);
	}
	
	@Override
	public final int hashCode() {
		return super.hashCode();
	}
	
	@Override
	public final boolean equals(final Object object) {
		if (this == object) {
			return true;
		}
		
		final Class<?> type = cast(Class.class, this.getFilter());
		
		if (type != null) {
			if (!type.isInstance(object)) {
				return false;
			}
		} else if (object != type) {
			return false;
		}
		
		if (this.match == null) {
			this.match = (Expression<?>) object;
			
			return true;
		}
		
		return this.getMatch().equals(object);
	}
	
	@Override
	public final String toString() {
		return "$(" + formatFilter(this.getFilter()) + ")<" + (this.getMatch() == null ? "" : this.getMatch()) + ">";
	}
	
	public static final String formatFilter(final Object filter) {
		final Class<?> type = cast(Class.class, filter);
		
		if (type != null) {
			return Character.toString(type.getSimpleName().charAt(0));
		}
		
		return "" + filter;
	}
	
	private static final long serialVersionUID = -7248608001486237448L;
	
	public static final Reset RESET = new Reset();
	
	public static final Bind BIND = new Bind();
	
	/**
	 * @author codistmonk (creation 2015-01-04)
	 */
	public static final class Reset implements Expression.Visitor<Expression<?>> {
		
		@Override
		public final Symbol<?> visit(final Symbol<?> symbol) {
			return symbol;
		}
		
		@Override
		public final Variable visit(final Variable variable) {
			return variable.reset();
		}
		
		@Override
		public final Expression<?> visit(final Composite<?> composite) {
			composite.forEach(element -> element.accept(this));
			
			return composite;
		}
		
		private static final long serialVersionUID = 8086793860438225779L;
		
	}
	
	/**
	 * @author codistmonk (creation 2015-01-04)
	 */
	public static final class Bind implements Expression.Visitor<Expression<?>> {
		
		@Override
		public final Symbol<?> visit(final Symbol<?> symbol) {
			return symbol;
		}
		
		@Override
		public final Expression<?> visit(final Variable variable) {
			return variable.getMatch() != null ? variable.getMatch() : variable;
		}
		
		@Override
		public final Composite<?> visit(final Composite<?> composite) {
			final Composite<Expression<?>> newComposite = new Composite<>(composite.getParent());
			boolean returnNewComposite = false;
			
			for (final Variable parameter : composite.getParameters()) {
				if (parameter.getMatch() == null) {
					newComposite.getParameters().add(parameter);
				} else if (!returnNewComposite) {
					returnNewComposite = true;
				}
			}
			
			for (final Expression<?> element : composite) {
				final Expression<?> newElement = element.accept(this);
				
				newComposite.add(newElement);
				
				if (!returnNewComposite && newElement != element) {
					returnNewComposite = true;
				}
			}
			
			return returnNewComposite ? newComposite : composite;
		}
		
		private static final long serialVersionUID = 8086793860438225779L;
		
	}
	
}