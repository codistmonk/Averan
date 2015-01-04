package averan3.core;

import averan.common.Container;
import averan.common.IdentityKey;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import net.sourceforge.aprog.tools.Pair;

/**
 * @author codistmonk (creation 2015-01-04)
 */
public abstract interface Expression<E extends Expression<?>> extends Container<E> {
	
	public abstract <T> T accept(Visitor<T> visitor);
	
	/**
	 * @author codistmonk (creation 2015-01-04)
	 */
	public static abstract interface Visitor<V> extends Serializable {
		
		public abstract V visit(Symbol<?> symbol);
		
		public abstract V visit(Variable variable);
		
		public abstract V visit(Composite<?> composite);
		
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
		public final Expression<?> visit(final Composite<?> composite) {
			final Expression<?> candidate = this.tryToReplace(composite);
			
			if (candidate != composite) {
				return candidate;
			}
			
			final Composite<Expression<?>> newComposite = new Composite<>(composite.getParent());
			boolean returnNewComposite = false;
			
			for (final Expression<?> element : composite) {
				final Expression<?> newElement = element.accept(this);
				
				newComposite.add(newElement);
				
				if (!returnNewComposite && newElement != element) {
					returnNewComposite = true;
				}
			}
			
			final Collection<Variable> variables = newComposite.accept(new CollectVariables()).getVariables().keySet();
			
			for (final Variable parameter : composite.getParameters()) {
				if (!variables.contains(parameter)) {
					newComposite.getParameters().add(parameter);
				} else if (!returnNewComposite) {
					returnNewComposite = true;
				}
			}
			
			return returnNewComposite ? newComposite : composite;
		}
		
		@Override
		public final String toString() {
			return this.getBindings().toString() + this.getIndices().toString();
		}
		
		private final Expression<?> tryToReplace(final Expression<?> expression) {
			for (final Pair<Expression<?>, Expression<?>> binding : this.getBindings()) {
				if (binding.getFirst().accept(Variable.RESET).equals(expression)
						&& (this.getIndices().isEmpty() || this.getIndices().contains(++this.index))) {
					return binding.getSecond().accept(Variable.BIND);
				}
			}
			
			return expression;
		}
		
		private static final long serialVersionUID = -8741979436763611725L;
		
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
		public final CollectVariables visit(final Composite<?> composite) {
			composite.forEach(element -> element.accept(this));
			
			return this;
		}
		
		private static final long serialVersionUID = 792062106535998586L;
		
	}
	
}
