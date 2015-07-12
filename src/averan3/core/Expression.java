package averan3.core;

import static multij.tools.Tools.cast;

import averan.common.Container;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import multij.tools.Pair;

/**
 * @author codistmonk (creation 2015-01-04)
 */
public abstract interface Expression<E extends Expression<?>> extends Container<E> {
	
	public abstract <T> T accept(Visitor<T> visitor);
	
	public abstract boolean implies(Expression<?> expression);
	
	/**
	 * @author codistmonk (creation 2015-01-04)
	 */
	public static abstract interface Visitor<V> extends Serializable {
		
		public abstract V visit(Symbol<?> symbol);
		
		public abstract V visit(Variable variable);
		
		public abstract V visit(Composite<Expression<?>> composite);
		
	}
	
	/**
	 * @author codistmonk (creation 2015-01-04)
	 */
	public static final class Substitution implements Visitor<Expression<?>> {
		
		private final List<Pair<Expression<?>, Expression<?>>> bindings = new ArrayList<>();
		
		private final Collection<Integer> indices = new TreeSet<>();
		
		private int index = -1;
		
		public final Substitution reset() {
			this.index = -1;
			
			return this;
		}
		
		public final List<Pair<Expression<?>, Expression<?>>> getBindings() {
			return this.bindings;
		}
		
		public final Collection<Integer> getIndices() {
			return this.indices;
		}
		
		public final Substitution bind(final Expression<?> pattern, final Expression<?> replacement) {
			this.getBindings().add(new Pair<>(pattern, replacement));
			
			return this;
		}
		
		public final Substitution at(final Iterable<? extends Number> indices) {
			indices.forEach(i -> this.getIndices().add(i.intValue()));
			
			return this;
		}
		
		public final Substitution at(final int... indices) {
			for (final int index : indices) {
				this.getIndices().add(index);
			}
			
			return this;
		}
		
		@Override
		public final Expression<?> visit(final Symbol<?> symbol) {
			return this.tryToReplace(symbol);
		}
		
		@Override
		public final Expression<?> visit(final Variable variable) {
			return this.tryToReplace(variable);
		}
		
		@Override
		public final Expression<?> visit(final Composite<Expression<?>> composite) {
			{
				final Expression<?> candidate = this.tryToReplace(composite);
				
				if (candidate != composite) {
					return candidate;
				}
			}
			
			{
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
		}
		
		@Override
		public final String toString() {
			return this.getBindings().toString() + this.getIndices().toString();
		}
		
		private final Expression<?> tryToReplace(final Expression<?> expression) {
			for (final Pair<Expression<?>, Expression<?>> binding : this.getBindings()) {
				if (binding.getFirst().accept(new PatternEquals(expression))
						&& (this.getIndices().isEmpty() || this.getIndices().contains(++this.index))) {
					return binding.getSecond().accept(Variable.BIND);
				}
			}
			
			return expression;
		}
		
		private static final long serialVersionUID = -8741979436763611725L;
		
		/**
		 * @author codistmonk (creation 2015-01-05)
		 */
		public static final class PatternEquals implements Visitor<Boolean> {
			
			private Expression<?> target;
			
			public PatternEquals(final Expression<?> target) {
				this.target = target;
			}
			
			@Override
			public final Boolean visit(final Symbol<?> symbol) {
				return symbol.implies(this.target);
			}
			
			@Override
			public final Boolean visit(final Variable variable) {
				return variable == this.target;
			}
			
			@Override
			public final Boolean visit(final Composite<Expression<?>> composite) {
				final int n = composite.size();
				final Composite<?> targetAsComposite = cast(Composite.class, this.target);
				
				if (targetAsComposite == null || n != targetAsComposite.size()) {
					return false;
				}
				
				for (int i = 0; i < n; ++i) {
					this.target = targetAsComposite.get(i);
					
					if (!composite.get(i).accept(this)) {
						return false;
					}
				}
				
				return true;
			}
			
			private static final long serialVersionUID = -5840795213809431978L;
			
		}
		
	}
	
	/**
	 * @author codistmonk (creation 2015-01-04)
	 */
	public static final class CollectVariables implements Visitor<CollectVariables> {
		
		private final Map<Variable, Variable> variables = new IdentityHashMap<>();
		
		public final Map<Variable, Variable> getVariables() {
			return this.variables;
		}
		
		@Override
		public final CollectVariables visit(final Symbol<?> symbol) {
			return this;
		}
		
		@Override
		public final CollectVariables visit(final Variable variable) {
			this.getVariables().put(variable, variable);
			
			return this;
		}
		
		@Override
		public final CollectVariables visit(final Composite<Expression<?>> composite) {
			composite.forEach(element -> element.accept(this));
			
			return this;
		}
		
		private static final long serialVersionUID = 792062106535998586L;
		
	}
	
}
