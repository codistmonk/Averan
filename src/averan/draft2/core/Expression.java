package averan.draft2.core;

import averan.common.Container;

import java.io.Serializable;

/**
 * @author codistmonk (creation 2014-12-20)
 */
public abstract interface Expression<E extends Expression<?>> extends Container<E> {
	
	public abstract boolean implies(Expression<?> expression);
	
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
	
}
