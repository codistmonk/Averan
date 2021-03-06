package averan.draft3.core;

import static multij.tools.Tools.cast;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * @author codistmonk (creation 2015-01-04)
 */
public final class Variable implements Expression<Variable> {
	
	private final String name;
	
	private Expression<?> match;
	
	private boolean locked;
	
	public Variable(final String name) {
		this.name = name;
	}
	
	public final boolean isLocked() {
		return this.locked;
	}
	
	public final Variable lock() {
		if (this.isLocked()) {
//			throw new IllegalStateException();
		}
		
		this.locked = true;
		
		return this;
	}
	
	public final Variable unlock() {
		if (!this.isLocked()) {
			throw new IllegalStateException();
		}
		
		this.locked = false;
		
		return this;
	}
	
	public final String getName() {
		return this.name;
	}
	
	public final Expression<?> getMatch() {
		if (this.isLocked()) {
			return this;
		}
		
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
		final Variable that = cast(this.getClass(), object);
		
		if (false) {
			final Expression<?> thisMatch = this.getMatch();
			
			if (that != null) {
				final Expression<?> thatMatch = that.getMatch();
				
				if (thisMatch == null && thatMatch == null) {
					this.match = that.match = that;
					
					return true;
				}
				
				if (thisMatch == null) {
					this.match = thatMatch;
					
					return true;
				}
				
				if (thatMatch == null) {
					that.match = thisMatch;
					
					return true;
				}
				
				if (thisMatch instanceof Variable || thatMatch instanceof Variable) {
					return thisMatch == thatMatch;
				}
				
				return thisMatch.equals(thatMatch);
			}
			
			if (thisMatch == null) {
				this.match = (Expression<?>) object;
				
				return true;
			}
			
			return !(thisMatch instanceof Variable) && thisMatch.equals(object);
		}
		
		if (this.isLocked()) {
			return this == object || (that != null && !that.isLocked() && that.equals(this));
		}
		
		if (this.match == null) {
			this.match = (Expression<?>) object;
			
			return true;
		}
		
		return this == object || (this.getMatch() != this && this.getMatch().equals(object));
	}
	
	@Override
	public final String toString() {
		return "$" + getNumberedName(this) + "<" + (this.locked ? "!" : this.getMatch() == null ? "" : "...") + ">";
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
	
	public static final Lock LOCK = new Lock();
	
	public static final Unlock UNLOCK = new Unlock();
	
	public static final boolean DEBUG = false;
	
	private static final Map<String, Map<Variable, Integer>> names = new HashMap<>();
	
	public static final String getNumberedName(final Variable variable) {
		if (!DEBUG) {
			return variable.getName();
		}
		
		final Map<Variable, Integer> variables = names.computeIfAbsent(variable.getName(), name -> new IdentityHashMap<>());
		final Integer index = variables.computeIfAbsent(variable, v -> variables.size() + 1);
		
		return variable.getName() + (index.equals(1) ? "" : "#" + index);
	}
	
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
		public final Composite<?> visit(final Composite<Expression<?>> composite) {
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
		public final Expression<?> visit(final Composite<Expression<?>> composite) {
			final Composite<Expression<?>> newComposite = new Composite<>();
			boolean returnNewComposite = false;
			
			for (final Expression<?> element : composite) {
				final Expression<?> newElement = element.accept(this);
				
				newComposite.add(newElement);
				
				if (!returnNewComposite && element != newElement) {
					returnNewComposite = true;
				}
			}
			
			return returnNewComposite ? newComposite : composite;
		}
		
		private static final long serialVersionUID = 8086793860438225779L;
		
	}
	
	/**
	 * @author codistmonk (creation 2015-01-24)
	 */
	public static final class Lock implements Visitor<Expression<?>> {
		
		@Override
		public Symbol<?> visit(final Symbol<?> symbol) {
			return symbol;
		}
		
		@Override
		public final Variable visit(final Variable variable) {
			return variable.lock();
		}
		
		@Override
		public final Composite<Expression<?>> visit(final Composite<Expression<?>> composite) {
			composite.forEach(element -> element.accept(this));
			
			return composite;
		}
		
		private static final long serialVersionUID = -6140058828588056521L;
		
	}
	
	/**
	 * @author codistmonk (creation 2015-01-24)
	 */
	public static final class LockFreeVariables implements Visitor<Expression<?>> {
		
		private Map<Variable, Variable> declared = new IdentityHashMap<>();
		
		@Override
		public Symbol<?> visit(final Symbol<?> symbol) {
			return symbol;
		}
		
		@Override
		public final Variable visit(final Variable variable) {
			if (!this.declared.containsKey(variable)) {
				variable.lock();
			}
			
			return variable;
		}
		
		@Override
		public final Composite<Expression<?>> visit(final Composite<Expression<?>> composite) {
			final Composite<Expression<?>> parameters = composite.getParameters();
			
			if (parameters != null) {
				final int n = parameters.getListSize();
				
				for (int i = 1; i < n; ++i) {
					final Variable parameter = (Variable) parameters.getListElement(i);
					this.declared.put(parameter, parameter);
				}
				
				composite.getContents().forEach(element -> element.accept(this));
				
				for (int i = 1; i < n; ++i) {
					this.declared.remove(parameters.getListElement(i));
				}
			} else {
				composite.forEach(element -> element.accept(this));
			}
			
			return composite;
		}
		
		private static final long serialVersionUID = -6140058828588056521L;
		
	}
	
	/**
	 * @author codistmonk (creation 2015-01-08)
	 */
	public static final class Unlock implements Visitor<Expression<?>> {
		
		@Override
		public Symbol<?> visit(final Symbol<?> symbol) {
			return symbol;
		}

		@Override
		public final Variable visit(final Variable variable) {
			return variable.unlock();
		}
		
		@Override
		public final Composite<Expression<?>> visit(final Composite<Expression<?>> composite) {
			composite.forEach(element -> element.accept(this));
			
			return composite;
		}
		
		private static final long serialVersionUID = -6533689663664108766L;
		
	}
	
}