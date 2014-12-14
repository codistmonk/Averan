package averan3.core;

import static net.sourceforge.aprog.tools.Tools.cast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.aprog.tools.Tools;

/**
 * @author codistmonk (creation 2014-12-14)
 */
public final class Module implements Expression {
	
	private final Module context;
	
	private Expression condition;
	
	private final List<Expression> facts;
	
	private final Map<String, Expression> bindings;
	
	public Module() {
		this(null);
	}
	
	public Module(final Module context) {
		this(context, null, new ArrayList<>());
	}
	
	public Module(final Module context, final Expression condition, final List<Expression> facts) {
		this.context = context;
		this.condition = condition;
		this.facts = facts;
		this.bindings = new HashMap<>();
	}
	
	@Override
	public final Module getContext() {
		return this.context;
	}
	
	public final Expression getCondition() {
		return this.condition;
	}
	
	public final Module setCondition(final Expression condition) {
		if (this.getCondition() != null) {
			throw new IllegalStateException();
		}
		
		this.condition = condition;
		
		return this;
	}
	
	public final List<Expression> getFacts() {
		return this.facts;
	}
	
	public final Map<String, Expression> getBindings() {
		return this.bindings;
	}
	
	public final Module clearBindings() {
		return ClearBindings.INSTANCE.visit(this);
	}
	
	public final Expression apply(final Expression expression) {
		this.clearBindings();
		
		if (Tools.equals(this.getCondition(), expression)) {
			if (1 == this.getFacts().size()) {
				return this.getFacts().get(0).accept(BindVariables.INSTANCE);
			}
			
			if (this.getCondition() == null) {
				return this;
			}
			
			final Module result = new Module(this.getContext(), null, new ArrayList<>(this.getFacts().size()));
			
			listAccept(this.getFacts(), BindVariables.INSTANCE, result.getFacts());
			
			return result;
		}
		
		return null;
	}
	
	@Override
	public final <Value> Value accept(final Visitor<Value> visitor) {
		return visitor.visit(this);
	}
	
	@Override
	public final int hashCode() {
		return Tools.hashCode(this.getCondition()) + this.getFacts().hashCode();
	}
	
	@Override
	public final boolean equals(final Object object) {
		if (object == null) {
			return false;
		}
		
		if (object == this) {
			return true;
		}
		
		final Module that = cast(this.getClass(), object);
		
		return that != null && Tools.equals(this.getCondition(), that.getCondition()) && this.getFacts().equals(that.getFacts());
	}
	
	@Override
	public final String toString() {
		if (this.getCondition() == null) {
			return this.getFacts().toString();
		}
		
		return this.getCondition() + "->" + this.getFacts();
	}
	
	/**
	 * @author codistmonk (creation 2014-12-14)
	 */
	public final class Symbol implements Expression {
		
		private final String string;
		
		public Symbol(final String string) {
			this.string = string;
		}
		
		@Override
		public final Module getContext() {
			return Module.this;
		}
		
		@Override
		public final <Value> Value accept(final Visitor<Value> visitor) {
			return visitor.visit(this);
		}
		
		@Override
		public final int hashCode() {
			return this.getContext().hashCode() + this.toString().hashCode();
		}
		
		@Override
		public final boolean equals(final Object object) {
			if (object == null) {
				return false;
			}
			
			if (object == this) {
				return true;
			}
			
			final Symbol that = cast(this.getClass(), object);
			
			return that != null && this.getContext() == that.getContext() && this.toString().equals(that.toString());
		}
		
		@Override
		public final String toString() {
			return this.string;
		}
		
		private static final long serialVersionUID = 764418126172564525L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-12-14)
	 */
	public final class Variable implements Expression {
		
		private final String name;
		
		public Variable(final String name) {
			this.name = name;
		}
		
		@Override
		public final Module getContext() {
			return Module.this;
		}
		
		public final Expression getMatch() {
			return this.getContext().getBindings().get(this.name);
		}
		
		public final Variable clearBinding() {
			this.getContext().getBindings().remove(this.name);
			
			return this;
		}
		
		@Override
		public final <Value> Value accept(final Visitor<Value> visitor) {
			return visitor.visit(this);
		}
		
		@Override
		public final int hashCode() {
			return this.getContext().hashCode() + this.name.hashCode();
		}
		
		@Override
		public final boolean equals(final Object object) {
			if (object == null) {
				return false;
			}
			
			if (object == this) {
				return true;
			}
			
			final Expression that = cast(Expression.class, object);
			
			if (that == null) {
				return false;
			}
			
			return this.getContext().getBindings().computeIfAbsent(this.name, k -> that).equals(that);
		}
		
		@Override
		public final String toString() {
			final Expression match = this.getMatch();
			
			return match == null ? "?" + this.name : this.name + "<" + match + ">";
		}
		
		private static final long serialVersionUID = -6462837915699316626L;
		
	}
	
	private static final long serialVersionUID = 1746923129591201730L;
	
	public static final boolean listAccept(final List<Expression> expressions, final Visitor<Expression> visitor, final List<Expression> values) {
		boolean result = false;
		
		for (final Expression expression : expressions) {
			final Expression value = expression.accept(visitor);
			
			values.add(value);
			
			result |= expression != value;
		}
		
		return result;
	}
	
	/**
	 * @author codistmonk (creation 2014-12-14)
	 */
	public static final class BindVariables implements Visitor<Expression> {
		
		@Override
		public final Expression visit(final Module module) {
			final Expression newCondition = module.getCondition().accept(this);
			final List<Expression> newFacts = new ArrayList<>(module.getFacts().size());
			
			if (module.getCondition() != newCondition | listAccept(module.getFacts(), this, newFacts)) {
				new Module(module.getContext(), newCondition, newFacts);
			}
			
			return module;
		}
		
		@Override
		public final Expression visit(final Substitution subsitution) {
			return subsitution;
		}
		
		@Override
		public final Expression visit(final Symbol symbol) {
			return symbol;
		}
		
		@Override
		public final Expression visit(final Variable variable) {
			final Expression match = variable.getMatch();
			
			return match != null ? match : variable;
		}
		
		private static final long serialVersionUID = -2013390428527433004L;
		
		public static final Module.BindVariables INSTANCE = new BindVariables();
		
	}
	
	/**
	 * @author codistmonk (creation 2014-12-14)
	 */
	public static final class ClearBindings implements Visitor<Expression> {
		
		@Override
		public final Module visit(final Module module) {
			module.getBindings().clear();
			
			if (module.getCondition() != null) {
				module.getCondition().accept(this);
			}
			
			module.getFacts().forEach(fact -> fact.accept(this));
			
			return module;
		}
		
		@Override
		public final Substitution visit(final Substitution substitution) {
			substitution.getExpression().accept(this);
			substitution.getBindings().forEach(binding -> {
				binding.getFirst().accept(this);
				binding.getSecond().accept(this);
			});
			
			return substitution;
		}
		
		@Override
		public final Symbol visit(final Symbol symbol) {
			return symbol;
		}
		
		@Override
		public final Variable visit(final Variable variable) {
			return variable;
		}
		
		private static final long serialVersionUID = 3357838530966427076L;
		
		public static final Module.ClearBindings INSTANCE = new ClearBindings();
		
	}
	
}