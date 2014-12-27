package averan4.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import averan4.core.Variable.Bind;

/**
 * @author codistmonk (creation 2014-12-20)
 */
public abstract interface Expression<E extends Expression<?>> extends Container<E> {
	
	public abstract <V> V accept(Visitor<V> visitor);
	
	public static final Symbol<String> EQUALS = new Symbol<>("=");
	
	public static final Symbol<String> IMPLIES = new Symbol<>("->");
	
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
		
		/**
		 * @author codistmonk (creation 2014-12-27)
		 */
		public static abstract class ExpressionRewriter implements Visitor<Expression<?>> {
			
			private List<Module> stack = new ArrayList<>();
			
			protected ExpressionRewriter reset() {
				this.stack.clear();
				this.stack.add(null);
				
				return this;
			}
			
			protected final Module push(final Module module) {
				this.stack.add(module);
				
				return module;
			}
			
			protected final Module peek() {
				return this.stack.get(this.stack.size() - 1);
			}
			
			protected final Module pop() {
				return this.stack.remove(this.stack.size() - 1);
			}
			
			private static final long serialVersionUID = 4722234631965653828L;
			
		}
		
	}
	
}
