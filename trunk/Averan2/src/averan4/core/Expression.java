package averan4.core;

import java.io.Serializable;

/**
 * @author codistmonk (creation 2014-12-20)
 */
public abstract interface Expression<E extends Expression<?>> extends Container<E> {
	
	public abstract <V> V accept(Visitor<V> visitor);
	
	public static final Symbol EQUALS = new Symbol("=");
	
	public static final Symbol IMPLIES = new Symbol("->");
	
	/**
	 * @author codistmonk (creation 2014-12-20)
	 */
	public static abstract interface Visitor<V> extends Serializable {
		
		public default V visit(final Symbol<?> symbol) {
			return this.visit((Expression<?>) symbol);
		}
		
		public default V visit(final Variable variable) {
			return this.visit((Expression<?>) variable);
		}
		
		public default V visit(final Composite<?> composite) {
			return this.visit((Expression<?>) composite);
		}
		
		public default V visit(final Module module) {
			return this.visit((Expression<?>) module);
		}
		
		public default V visit(final Expression<?> expression) {
			return null;
		}
		
	}
	
}
