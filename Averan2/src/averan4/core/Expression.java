package averan4.core;

import java.io.Serializable;

/**
 * @author codistmonk (creation 2014-12-20)
 */
public abstract interface Expression<E extends Expression<?>> extends Container<E> {
	
	public abstract <V> V accept(Visitor<V> visitor);
	
	public default <I extends Interpretation<E>> I as(final Class<I> interpretationClass) {
		try {
			return interpretationClass.getConstructor(this.getClass()).newInstance(this);
		} catch (final Exception exception) {
			return null;
		}
	}
	
	public static final Symbol EQUALS = new Symbol("=");
	
	public static final Symbol IMPLIES = new Symbol("->");
	
	/**
	 * @author codistmonk (creation 2014-12-20)
	 */
	public static abstract interface Visitor<V> extends Serializable {
		
		public abstract V visit(Symbol symbol);
		
		public abstract V visit(Variable variable);
		
		public abstract V visit(Composite<?> composite);
		
	}
	
	/**
	 * @author codistmonk (creation 2014-12-20)
	 */
	public static abstract interface Interpretation<E extends Expression<?>> extends Serializable {
		
		public abstract Composite<E> getComposite();
		
		/**
		 * @author codistmonk (creation 2014)
		 *
		 * @param <E>
		 */
		public static abstract class Default<E extends Expression<?>> implements Interpretation<E> {
			
			private final Composite<E> composite;
			
			protected Default(final Composite<E> composite) {
				this.composite = composite;
			}
			
			@Override
			public final Composite<E> getComposite() {
				return this.composite;
			}
			
			private static final long serialVersionUID = 3493628090781849471L;
			
			public static final boolean isPair(final Expression<?> expression) {
				return expression.size() == 2;
			}
			
			public static final boolean isTriple(final Expression<?> expression) {
				return expression.size() == 3;
			}
			
		}
		
	}
	
}
