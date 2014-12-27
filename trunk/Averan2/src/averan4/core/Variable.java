package averan4.core;

import static net.sourceforge.aprog.tools.Tools.cast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author codistmonk (creation 2014-12-20)
 */
public final class Variable implements Expression<Variable> {
	
	private final String name;
	
	private Expression<?> match;
	
	public Variable(final String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
	@SuppressWarnings("unchecked")
	public final <E extends Expression<?>> E getMatch() {
		return (E) this.match;
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
	public final <V> V accept(final Expression.Visitor<V> visitor) {
		return visitor.visit(this);
	}
	
	@Override
	public final int hashCode() {
		return this.getName().hashCode();
	}
	
	@Override
	public final boolean equals(final Object object) {
		if (this == object) {
			return true;
		}
		
		final Expression<?> expression = cast(Expression.class, object);
		
		if (expression == null) {
			return false;
		}
		
		Expression<?> match = this.getMatch();
		
		if (match == null) {
			match = this.match = expression;
		}
		
		return match.equals(expression);
	}
	
	@Override
	public final String toString() {
		return this.getName() + "?";
	}
	
	private static final long serialVersionUID = 3015338717755848327L;
	
	public static final Reset RESET = Reset.INSTANCE;
	
	public static final Bind BIND = Bind.INSTANCE;
	
	/**
	 * @author codistmonk (creation 2014-12-20)
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
			return Visitor.visitElementsOf(composite, this);
		}
		
		@Override
		public final Module visit(final Module module) {
			return Visitor.visitElementsOf(module, this);
		}
		
		@Override
		public final Substitution visit(final Substitution substitution) {
			return Visitor.visitElementsOf(substitution, this);
		}
		
		@Override
		public final Equality visit(final Equality equality) {
			return Visitor.visitElementsOf(equality, this);
		}
		
		private static final long serialVersionUID = 6438401380761494994L;
		
		public static final Variable.Reset INSTANCE = new Reset();
		
	}
	
	/**
	 * @author codistmonk (creation 2014-12-20)
	 */
	public static final class Bind extends Expression.Visitor.ExpressionRewriter {
		
		@Override
		public final Symbol<?> visit(final Symbol<?> symbol) {
			return symbol;
		}
		
		@Override
		public final Expression<?> visit(final Variable variable) {
			final Expression<?> match  = variable.getMatch();
			
			return match != null ? match : variable;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public final Composite<?> visit(final Composite<Expression<?>> composite) {
			final Composite<?> candidate = new Composite<>();
			
			if (Composite.listAccept(composite, this,
					(Collection<Expression<?>>) candidate.getElements())) {
				return candidate;
			}
			
			return composite;
		}
		
		@Override
		public final Expression<?> visit(final Module module) {
			final Module newModule = this.push(new Module(this.peek()));
			
			try {
				if (Composite.listAccept(module.getConditions(), this, newModule.getConditions().getElements())
						| Composite.listAccept(module.getFacts(), this, newModule.getFacts().getElements())) {
					return newModule;
				}
				
				return module;
			} finally {
				this.pop();
			}
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public final Expression<?> visit(final Substitution substitution) {
			final Substitution candidate = new Substitution();
			
			if (Composite.listAccept((Iterable) substitution.getBindings(), this,
					(Collection) candidate.getBindings().getElements())
					| Composite.listAccept((Iterable) substitution.getIndices(), this,
							(Collection) candidate.getBindings().getElements())) {
				return candidate;
			}
			
			return substitution;
		}
		
		@Override
		public final Expression<?> visit(final Equality equality) {
			final Expression<?> newLeft = equality.getLeft().accept(this);
			final Expression<?> newRight = equality.getRight().accept(this);
			
			if (newLeft != equality.getLeft() || newRight != equality.getRight()) {
				return new Equality(newLeft, newRight);
			}
			
			return equality;
		}
		
		private static final long serialVersionUID = -2879093293185572053L;
		
		public static final Bind INSTANCE = new Bind();
		
	}
	
}