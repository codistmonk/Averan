package averan.core;

import static net.sourceforge.aprog.tools.Tools.cast;

import averan.core.Module.Symbol;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author codistmonk (creation 2014-08-09)
 */
public final class Pattern implements Serializable {
	
	private final Map<Any.Key, Expression> bindings; 
	
	private final Expression template;
	
	public Pattern(final Expression template) {
		this.bindings = new HashMap<>();
		this.template = template.accept(new SetBindings(this.bindings));
	}
	
	public final Map<Any.Key, Expression> getBindings() {
		return this.bindings;
	}
	
	public final Expression getTemplate() {
		return this.template;
	}
	
	@Override
	public final int hashCode() {
		return 0;
	}
	
	@Override
	public final boolean equals(final Object object) {
		this.getBindings().clear();
		
		return this.getTemplate().equals(object);
	}
	
	@SuppressWarnings("unchecked")
	public final <E extends Expression> E get(final String anyName) {
		return (E) this.getBindings().get(new Any.Key(anyName));
	}
	
	@SuppressWarnings("unchecked")
	public final <E extends Expression> E get(final String anyName, final int anyNumber) {
		return (E) this.getBindings().get(new Any.Key(anyName, anyNumber));
	}
	
	/**
	 * {@value}.
	 */
	private static final long serialVersionUID = -8302462150472406630L;
	
	public static final Any any(final String name) {
		return new Any(name);
	}
	
	public static final Any any(final String name, final int number) {
		return new Any(name, number);
	}
	
	/**
	 * @author codistmonk (creation 2014-08-09)
	 */
	public static final class Any implements Expression {
		
		private Map<Key, Expression> bindings;
		
		private final Key key;
		
		public Any(final String name) {
			this(name, 0);
		}
		
		public Any(final String name, final int number) {
			this.key = new Key(name, number);
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public final <R> R accept(final Visitor<R> visitor) {
			this.bindings = ((SetBindings) visitor).getBindings();
			
			return (R) this;
		}
		
		@Override
		public final boolean equals(final Object object) {
			final Expression alreadyBound = this.bindings.get(this.key);
			
			if (alreadyBound == null) {
				this.bindings.put(this.key, (Expression) object);
				
				return true;
			}
			
			return alreadyBound.equals(object);
		}
		
		@Override
		public final String toString() {
			return this.key.toString();
		}
		
		final void setBindings(final Map<Any.Key, Expression> bindings) {
			this.bindings = bindings;
		}
		
		@Override
		public final int hashCode() {
			return 0;
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = -6185178560899095806L;
		
		/**
		 * @author codistmonk (creation 2014-08-16)
		 */
		public static final class Key implements Serializable {
			
			private final String name;
			
			private final int number;
			
			public Key(final String name) {
				this(name, 0);
			}
			
			public Key(final String name, final int number) {
				this.name = name;
				this.number = number;
			}
			
			public final String getName() {
				return this.name;
			}
			
			public final int getNumber() {
				return this.number;
			}
			
			@Override
			public final int hashCode() {
				return this.getName().hashCode() + this.getNumber();
			}
			
			@Override
			public final boolean equals(final Object object) {
				final Key that = cast(this.getClass(), object);
				
				return that != null && this.getName().equals(that.getName()) && this.getNumber() == that.getNumber();
			}
			
			@Override
			public final String toString() {
				return this.getName() + (this.getNumber() == 0 ? "" : this.getNumber());
			}
			
			/**
			 * {@value}.
			 */
			private static final long serialVersionUID = -2165477797773036188L;
			
		}
		
	}
	
	/**
	 * @author codistmonk (creation 2014-08-09)
	 */
	private static final class SetBindings implements Visitor<Expression> {
		
		private final Map<Any.Key, Expression> bindings;
		
		SetBindings(final Map<Any.Key, Expression> bindings) {
			this.bindings = bindings;
		}
		
		@Override
		public final Expression visit(final Composite composite) {
			composite.childrenAcceptor(this).get();
			
			return composite;
		}
		
		@Override
		public final Expression visit(final Module module) {
			module.parametersAcceptor(this).get();
			module.conditionsAcceptor(this).get();
			module.factsAcceptor(this).get();
			
			return module;
		}
		
		@Override
		public final Expression visit(final Symbol symbol) {
			return symbol;
		}
		
		public final Map<Any.Key, Expression> getBindings() {
			return this.bindings;
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = -5894410726685899719L;
		
	}
	
}
