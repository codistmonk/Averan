package averan3.core;

import averan3.core.Module.Symbol;
import averan3.core.Module.Variable;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.aprog.tools.Pair;
import net.sourceforge.aprog.tools.Tools;

/**
 * @author codistmonk (creation 2014-12-1)
 */
public final class Substitution implements Expression {
	
	private final Module context;
	
	private final Expression expression;
	
	private final List<Pair<Expression, Expression>> bindings;
	
	public Substitution(final Module context, final Expression expression) {
		this(context, expression, new ArrayList<>());
	}
	
	public Substitution(final Module context, final Expression expression, final List<Pair<Expression, Expression>> bindings) {
		this.context = context;
		this.expression = expression;
		this.bindings = bindings;
	}
	
	public final Expression getExpression() {
		return this.expression;
	}
	
	public final List<Pair<Expression, Expression>> getBindings() {
		return this.bindings;
	}
	
	public final Substitution bind(final Expression pattern, final Expression replacement) {
		this.getBindings().add(new Pair<>(pattern, replacement));
		
		return this;
	}
	
	public final Expression apply() {
		this.getBindings().forEach(binding -> binding.getFirst().accept(ClearBindings.INSTANCE));
		
		return this.getExpression().accept(new Visitor<Expression>() {
			
			@Override
			public final Expression visit(final Module module) {
				for (final Pair<Expression, Expression> binding : getBindings()) {
					if (binding.getFirst().equals(module)) {
						return binding.getSecond();
					} else {
						binding.getFirst().accept(ClearBindings.INSTANCE);
					}
				}
				
				final Expression newCondition = module.getCondition().accept(this);
				final List<Expression> newFacts = new ArrayList<>(module.getFacts().size());
				
				if (module.getCondition() != newCondition | Module.listAccept(module.getFacts(), this, newFacts)) {
					return new Module(getContext(), newCondition, newFacts);
				}
				
				return null;
			}

			@Override
			public final Expression visit(final Substitution substitution) {
				for (final Pair<Expression, Expression> binding : getBindings()) {
					if (binding.getFirst().equals(substitution)) {
						return binding.getSecond();
					} else {
						binding.getFirst().accept(ClearBindings.INSTANCE);
					}
				}
				
				final Expression newExpression = substitution.getExpression().accept(this);
				final List<Pair<Expression, Expression>> newBindings = new ArrayList<>(getBindings().size());
				boolean rewritten = substitution.getExpression() != newExpression;
				
				for (final Pair<Expression, Expression> binding : getBindings()) {
					final Expression newPattern = binding.getFirst().accept(this);
					final Expression newReplacement = binding.getSecond().accept(this);
					rewritten |= binding.getFirst() != newPattern | binding.getSecond() != newReplacement;
					newBindings.add(new Pair<>(newPattern, newReplacement));
				}
				
				return rewritten ? new Substitution(getContext(), newExpression, newBindings) : substitution;
			}
			
			@Override
			public final Expression visit(final Symbol symbol) {
				for (final Pair<Expression, Expression> binding : getBindings()) {
					if (binding.getFirst().equals(symbol)) {
						return binding.getSecond();
					} else {
						binding.getFirst().accept(ClearBindings.INSTANCE);
					}
				}
				
				return symbol;
			}
			
			@Override
			public final Expression visit(final Variable variable) {
				for (final Pair<Expression, Expression> binding : getBindings()) {
					if (binding.getFirst().equals(variable)) {
						return binding.getSecond();
					} else {
						binding.getFirst().accept(ClearBindings.INSTANCE);
					}
				}
				
				return variable;
			}
			
			private static final long serialVersionUID = 2853731674514950228L;
			
		});
	}
	
	@Override
	public final <Value> Value accept(final Visitor<Value> visitor) {
		return visitor.visit(this);
	}
	
	@Override
	public final Module getContext() {
		return this.context;
	}
	
	@Override
	public final int hashCode() {
		return this.getExpression().hashCode() + this.getBindings().hashCode();
	}
	
	@Override
	public final boolean equals(final Object object) {
		if (object == null) {
			return false;
		}
		
		if (object == this) {
			return true;
		}
		
		final Substitution that = Tools.cast(this.getClass(), object);
		
		return that != null && this.getExpression().equals(that.getExpression()) && this.getBindings().equals(that.getBindings());
	}
	
	@Override
	public final String toString() {
		return this.getExpression().toString() + this.getBindings();
	}
	
	private static final long serialVersionUID = -6690623293481644744L;
	
	/**
	 * @author codistmonk (creation 2014-12-14)
	 */
	public static final class ClearBindings implements Visitor<Expression> {
		
		@Override
		public final Module visit(final Module module) {
			module.getCondition().accept(this);
			module.getFacts().forEach(fact -> fact.accept(this));
			
			return module;
		}
		
		@Override
		public final Substitution visit(final Substitution subsitution) {
			return subsitution;
		}

		@Override
		public Symbol visit(final Symbol symbol) {
			return symbol;
		}
		
		@Override
		public final Variable visit(final Variable variable) {
			return variable.clearBinding();
		}
		
		private static final long serialVersionUID = 2871303507152632488L;
		
		public static final ClearBindings INSTANCE = new ClearBindings();
		
	}
	
}
