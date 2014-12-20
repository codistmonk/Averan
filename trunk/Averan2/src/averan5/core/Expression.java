package averan5.core;

import java.io.Serializable;

/**
 * @author codistmonk (creation 2014-12-20)
 */
public abstract interface Expression<E extends Expression<?>> extends Container<E> {
	
	public abstract <V> V accept(Visitor<V> visitor);
	
	/**
	 * @author codistmonk (creation 2014-12-20)
	 */
	public static abstract interface Visitor<V> extends Serializable {
		
		public abstract V visit(Symbol symbol);
		
		public abstract V visit(Variable variable);
		
		public abstract V visit(Composite<?> composite);
		
	}
	
	public static final Symbol EQUALS = new Symbol("=");
	
	public static final Symbol IMPLIES = new Symbol("->");
	
}
