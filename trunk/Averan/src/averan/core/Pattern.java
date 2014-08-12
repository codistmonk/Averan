package averan.core;

import averan.core.Module.Symbol;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author codistmonk (creation 2014-08-09)
 */
public final class Pattern implements Serializable {
	
	private final Map<String, Expression> bindings; 
	
	private final Expression template;
	
	public Pattern(final Expression template) {
		this.bindings = new HashMap<>();
		this.template = template.accept(new SetBindings(this.bindings));
	}
	
	public final Map<String, Expression> getBindings() {
		return this.bindings;
	}
	
	@Override
	public final int hashCode() {
		return 0;
	}
	
	@Override
	public final boolean equals(final Object object) {
		this.getBindings().clear();
		
		return this.template.equals(object);
	}
	
	@SuppressWarnings("unchecked")
	public final <E extends Expression> E get(final String anyName) {
		return (E) this.getBindings().get(anyName);
	}
	
	/**
	 * {@value}.
	 */
	private static final long serialVersionUID = -8302462150472406630L;
	
	public static final Any any(final String name) {
		return new Any(name);
	}
	
	/**
	 * @author codistmonk (creation 2014-08-09)
	 */
	public static final class Any implements Expression {
		
		private Map<String, Expression> bindings;
		
		private final String name;
		
		public Any(final String name) {
			this.name = name;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public final <R> R accept(final Visitor<R> visitor) {
			this.bindings = ((SetBindings) visitor).getBindings();
			
			return (R) this;
		}
		
		@Override
		public final boolean equals(final Object object) {
			final Expression alreadyBound = this.bindings.get(this.toString());
			
			if (alreadyBound == null) {
				this.bindings.put(this.toString(), (Expression) object);
				
				return true;
			}
			
			return alreadyBound.equals(object);
		}
		
		@Override
		public final String toString() {
			return this.name;
		}
		
		final void setBindings(final Map<String, Expression> bindings) {
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
		
	}
	
	/**
	 * @author codistmonk (creation 2014-08-09)
	 */
	private static final class SetBindings implements Visitor<Expression> {
		
		private final Map<String, Expression> bindings;
		
		SetBindings(final Map<String, Expression> bindings) {
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
		
		public final Map<String, Expression> getBindings() {
			return this.bindings;
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = -5894410726685899719L;
		
	}
	
}
