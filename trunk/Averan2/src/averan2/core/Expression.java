package averan2.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author codistmonk (creation 2014-12-20)
 */
public abstract interface Expression<E extends Expression<?>> extends Container<E> {
	
	public abstract <V> V accept(Visitor<V> visitor);
	
	/**
	 * @author codistmonk (creation 2014-12-20)
	 */
	public static abstract interface Visitor<V> extends Serializable {
		
		public abstract V visit(Symbol<?> symbol);
		
		public abstract V visit(Variable variable);
		
		public abstract V visit(Composite<Expression<?>> composite);
		
		public abstract V visit(Module module);
		
		public abstract V visit(Substitution substitution);
		
		public abstract V visit(Equality equality);
		
		public static <E extends Expression<?>> E visitElementsOf(final E expression, final Visitor<?> visitor) {
			for (final Expression<?> element : expression) {
				element.accept(visitor);
			}
			
			return expression;
		}
		
	}
	
	/**
	 * @author codistmonk (creation 2014-12-28)
	 */
	public static final class CollectParameters implements Visitor<List<Variable>> {
		
		private final Map<Variable, Variable> done = new IdentityHashMap<>();
		
		private final List<Variable> result = new ArrayList<>();
		
		@Override
		public final List<Variable> visit(final Symbol<?> symbol) {
			return this.result;
		}
		
		@Override
		public final List<Variable> visit(final Variable variable) {
			if (this.done.putIfAbsent(variable, variable) == null) {
				this.result.add(variable);
			}
			
			return this.result;
		}
		
		@Override
		public final List<Variable> visit(final Composite<Expression<?>> composite) {
			Visitor.visitElementsOf(composite, this);
			
			return this.result;
		}
		
		@Override
		public final List<Variable> visit(final Module module) {
			if (0 < module.getConditions().size()) {
				return module.getConditions().get(0).accept(this);
			}
			
			return module.getFacts().accept(this);
		}
		
		@Override
		public final List<Variable> visit(final Substitution substitution) {
			Visitor.visitElementsOf(substitution, this);
			
			return this.result;
		}
		
		@Override
		public final List<Variable> visit(final Equality equality) {
			Visitor.visitElementsOf(equality, this);
			
			return this.result;
		}
		
		private static final long serialVersionUID = -936926873552336509L;
		
		public static final CollectParameters collectParameters() {
			return new CollectParameters();
		}
		
	}
	
}
