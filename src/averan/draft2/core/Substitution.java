package averan.draft2.core;

import static averan.draft2.core.Composite.listAccept;
import static averan.draft2.core.Equality.equality;
import static averan.draft2.core.Symbol.symbol;
import static multij.tools.Tools.cast;
import static multij.tools.Tools.join;
import averan.common.Metadata;

import java.util.Collection;
import java.util.List;

/**
 * @author codistmonk (creation 2014-12-20)
 */
public final class Substitution implements Expression.Visitor<Expression<?>>, Expression<Expression<?>> {
	
	private final Composite<Equality> bindings;
	
	private final Composite<Symbol<Integer>> indices;
	
	private final Symbol<MutableInteger> currentIndex;
	
	private final boolean copyProofData;
	
	public Substitution() {
		this(false);
	}
	
	public Substitution(final boolean copyProofData) {
		this.bindings = new Composite<>();
		this.indices = new Composite<>();
		this.currentIndex = symbol(new MutableInteger());
		this.copyProofData = copyProofData;
	}
	
	public final Substitution using(final Equality equality) {
		this.getBindings().getElements().add(equality);
		
		return this;
	}
	
	public final Substitution at(final int... indices) {
		for (final int index : indices) {
			this.getIndices().getElements().add(symbol(new Integer(index)));
		}
		
		return this;
	}
	
	public final Composite<Equality> getBindings() {
		return this.bindings;
	}
	
	public final Composite<Symbol<Integer>> getIndices() {
		return this.indices;
	}
	
	public final Substitution reset() {
		this.currentIndex.getObject().set(-1);
		
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
	
	@SuppressWarnings("unchecked")
	@Override
	public final Expression<?> visit(final Composite<Expression<?>> composite) {
		Expression<?> candidate = this.tryToReplace(composite);
		
		if (candidate == composite) {
			candidate = new Composite<>();
			
			if (!listAccept(composite, this,
					((Composite<Expression<?>>) candidate).getElements())) {
				return composite;
			}
		}
		
		return Metadata.copy(composite, candidate);
	}
	
	@Override
	public final Expression<?> visit(final Module module) {
		Expression<?> candidate = this.tryToReplace(module);
		
		if (candidate == module) {
			candidate = new Module(this.copyProofData ? module.getContext() : null);
			final List<Expression<?>> newFacts = ((Module) candidate).getPropositions().getElements();
			
			if (!listAccept(module.getPropositions(), this, newFacts)) {
				return module;
			}
			
			module.getParameters().forEach(((Module) candidate)::parametrize);
			
			if (this.copyProofData) {
				((Module) candidate).getPropositionIds().putAll(module.getPropositionIds());
				((Module) candidate).getProofs().addAll(module.getProofs());
			}
		}
		
		return Metadata.copy(module, candidate);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public final Expression<?> visit(final Substitution substitution) {
		Expression<?> candidate = this.tryToReplace(substitution);
		
		if (candidate == substitution) {
			candidate = new Substitution();
			
			if (!listAccept((Iterable) substitution.getBindings(), this,
					(Collection) ((Substitution) candidate).getBindings().getElements())
					& !listAccept((Iterable) substitution.getIndices(), this,
							(Collection) ((Substitution) candidate).getIndices().getElements())) {
				return substitution;
			}
		}
		
		return Metadata.copy(substitution, candidate);
	}
	
	@Override
	public final Expression<?> visit(final Equality equality) {
		final Expression<?> candidate = this.tryToReplace(equality);
		
		if (candidate == equality) {
			final Expression<?> newLeft = equality.getLeft().accept(this);
			final Expression<?> newRight = equality.getRight().accept(this);
			
			if (newLeft != equality.getLeft() || newRight != equality.getRight()) {
				return equality(newLeft, newRight);
			}
		}
		
		return Metadata.copy(equality, candidate);
	}
	
	@Override
	public final int size() {
		return 2;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public final <E extends Expression<?>> E get(final int index) {
		switch (index) {
		case 0:
			return (E) this.getBindings();
		case 1:
			return (E) this.getIndices();
		}
		
		return null;
	}
	
	@Override
	public final <V> V accept(final Visitor<V> visitor) {
		return visitor.visit(this);
	}
	
	@Override
	public final boolean implies(final Expression<?> expression) {
		if (this == expression) {
			return true;
		}
		
		final Substitution that = cast(this.getClass(), expression);
		
		return that != null && this.getBindings().implies(that.getBindings())
				&& this.getIndices().implies(that.getIndices());
	}
	
	@Override
	public final int hashCode() {
		return this.getBindings().hashCode() + this.getIndices().hashCode();
	}
	
	@Override
	public final boolean equals(final Object object) {
		if (this == object) {
			return true;
		}
		
		final Variable variable = cast(Variable.class, object);
		
		if (variable != null) {
			return variable.equals(this);
		}
		
		final Substitution that = cast(this.getClass(), object);
		
		return that != null && this.getBindings().equals(that.getBindings())
				&& this.getIndices().equals(that.getIndices());
	}

	@Override
	public final String toString() {
		return "{" + join(",", this.getBindings()) + "}[" + join(",", this.getIndices()) + "]";
	}
	
	@SuppressWarnings("unchecked")
	private final Expression<?> tryToReplace(final Expression<?> expression) {
		for (final Equality binding : this.bindings) {
			if (binding.getLeft().accept(Variable.RESET).equals(expression)
					&& (this.indices.size() == 0 || 0 <= indexOf((Symbol) this.nextIndex(), this.indices))) {
				return binding.getRight().accept(Variable.BIND);
			}
		}
		
		return expression;
	}
	
	private final Symbol<MutableInteger> nextIndex() {
		this.currentIndex.getObject().increment();
		
		return this.currentIndex;
	}
	
	private static final long serialVersionUID = -1572047035151529843L;
	
	public static final int[] ANY_INDEX = {};
	
	public static final <E extends Expression<?>> int indexOf(final E needle, final Composite<E> haystack) {
		final int n = haystack.size();
		
		for (int i = 0; i < n; ++i) {
			if (needle.equals(haystack.get(i))) {
				return i;
			}
		}
		
		return -1;
	}
	
	/**
	 * @author codistmonk (creation 2014-12-27)
	 */
	public static final class MutableInteger extends Number {
		
		private int value;
		
		public final MutableInteger set(final int value) {
			this.value = value;
			
			return this;
		}
		
		public final MutableInteger increment() {
			return this.increment(1);
		}
		
		public final MutableInteger increment(final int amount) {
			this.value += amount;
			
			return this;
		}
		
		@Override
		public final int intValue() {
			return this.value;
		}
		
		@Override
		public final long longValue() {
			return this.intValue();
		}
		
		@Override
		public final float floatValue() {
			return this.intValue();
		}
		
		@Override
		public final double doubleValue() {
			return this.intValue();
		}
		
		@Override
		public final int hashCode() {
			return this.intValue();
		}
		
		@Override
		public final boolean equals(final Object object) {
			final Number that = cast(Number.class, object);
			
			return that != null && this.intValue() == that.intValue();
		}
		
		@Override
		public final String toString() {
			return Integer.toString(this.intValue());
		}
		
		private static final long serialVersionUID = -4135566800174496145L;
		
	}
	
}
