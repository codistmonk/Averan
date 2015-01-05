package averan3.core;

import static averan3.core.Composite.FORALL;
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
	public final boolean implies(final Expression<?> expression) {
		return this.equals(expression);
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
		} else if (object != this.getFilter()) {
			return false;
		} else {
			this.match = null;
		}
		
		if (this.match == null) {
			this.match = (Expression<?>) object;
			
			return true;
		}
		
		return this.getMatch().equals(object);
	}
	
	@Override
	public final String toString() {
//		return "$" + this.getName() + "(" + formatFilter(this.getFilter()) + ")<" + (this.getMatch() == null ? "" : this.getMatch()) + ">";
		return "$" + this.getName() + "(" + formatFilter(this.getFilter()) + ")<"
				+ (this.getMatch() == null ? "" : "...") + ">";
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
		public final Composite<?> visit(final Composite<?> composite) {
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
		public final Expression<?> visit(final Composite<?> composite) {
			final Composite<Expression<?>> parameters = composite.getParameters();
			Composite<Expression<?>> candidate = null;
			boolean returnCandidate = false;
			
			if (parameters != null) {
				final Composite<Expression<?>> newParameters = new Composite<>().add(FORALL);
				
				final int n = parameters.size();
				
				for (int i = 1; i < n; ++i) {
					final Variable parameter = (Variable) parameters.get(i);
					
					if (parameter.getMatch() == null) {
						newParameters.add(parameter);
					} else if (!returnCandidate) {
						returnCandidate = true;
					}
				}
				
				if (1 < newParameters.size()) {
					candidate = new Composite<>().add(newParameters);
				}
			}
			
			if (parameters == null) {
				candidate = new Composite<>();
				
				for (final Expression<?> element : composite) {
					final Expression<?> newElement = element.accept(this);
					
					candidate.add(newElement);
					
					if (!returnCandidate && newElement != element) {
						returnCandidate = true;
					}
				}
				
				return returnCandidate ? candidate : composite;
			} else if (candidate == null) {
				return composite.getContents().accept(this);
			} else {
				final Expression<?> newContents = composite.getContents().accept(this);
				
				if (!returnCandidate && newContents != composite.getContents()) {
					returnCandidate = true;
				}
				
				return returnCandidate ? candidate.add(newContents) : composite;
			}
		}
		
		private static final long serialVersionUID = 8086793860438225779L;
		
	}
	
}