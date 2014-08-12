package averan.core;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import averan.core.Module.Symbol;

/**
 * @author codistmonk (creation 2014-08-09)
 */
public final class Pattern implements Serializable {
	
	private final Map<String, Expression> bindings; 
	
	private final Expression template;
	
	public Pattern(final Expression template) {
		this.bindings = new HashMap<>();
		this.template = template.accept(new SetupAny());
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
		return this.template.equals(object);
	}
	
	@SuppressWarnings("unchecked")
	public final <E extends Expression> E get(final String anyName) {
		return (E) this.getBindings().get(anyName);
	}
	
	/**
	 * @author codistmonk (creation 2014-08-09)
	 */
	final class SetupAny implements Visitor<Expression> {
		
		@Override
		public final Expression endVisit(final Composite composite, final Expression compositeVisit,
				final Supplier<List<Expression>> childVisits) {
			childVisits.get();
			
			return composite;
		}
		
		@Override
		public final Expression endVisit(final Module module, final Expression moduleVisit,
				final Supplier<List<Expression>> parameterVisits,
				final Supplier<List<Expression>> conditionVisits,
				final Supplier<List<Expression>> factVisits) {
			parameterVisits.get();
			conditionVisits.get();
			factVisits.get();
			
			return module;
		}
		
		@Override
		public final Expression visit(final Symbol symbol) {
			return symbol;
		}
		
		public final Map<String, Expression> getBindings() {
			return Pattern.this.getBindings();
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = -5894410726685899719L;
		
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
			this.bindings = ((SetupAny) visitor).getBindings();
			
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
	
}
