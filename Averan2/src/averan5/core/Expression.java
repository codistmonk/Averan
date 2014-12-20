package averan5.core;

import static net.sourceforge.aprog.tools.Tools.cast;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.aprog.tools.Tools;

/**
 * @author codistmonk (creation 2014-12-20)
 */
public interface Expression<E extends Expression<?>> extends Container<E> {
	
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
	
	/**
	 * @author codistmonk (creation 2014-12-20)
	 */
	public static final class Symbol implements Expression<Symbol> {
		
		private final Object object;
		
		public Symbol(final Object object) {
			this.object = object;
		}
		
		public final Object getObject() {
			return this.object;
		}
		
		@Override
		public final int getElementCount() {
			return 1;
		}
		
		@Override
		public final Symbol getElement(final int index) {
			return index == 0 ? this : null;
		}
		
		@Override
		public final <V> V accept(final Visitor<V> visitor) {
			return visitor.visit(this);
		}
		
		@Override
		public final int hashCode() {
			return this.toString().hashCode();
		}
		
		@Override
		public final boolean equals(final Object object) {
			if (this == object) {
				return true;
			}
			
			final Symbol that = cast(this.getClass(), object);
			
			return that != null && this.getObject().equals(that.getObject());
		}
		
		@Override
		public final String toString() {
			return this.getObject().toString();
		}
		
		private static final long serialVersionUID = -2105054482894427294L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-12-20)
	 */
	public static final class Variable implements Expression<Variable> {
		
		private final String name;
		
		private Expression<?> match;
		
		public Variable(final String name) {
			this.name = name;
		}
		
		public String getName() {
			return this.name;
		}
		
		public final <E extends Expression<?>> E getMatch() {
			return (E) this.match;
		}
		
		public final Variable reset() {
			this.match = null;
			
			return this;
		}
		
		@Override
		public final int getElementCount() {
			return 1;
		}
		
		@Override
		public final Variable getElement(final int index) {
			return index == 0 ? this : null;
		}
		
		@Override
		public final <V> V accept(final Visitor<V> visitor) {
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
		
		/**
		 * @author codistmonk (creation 2014-12-20)
		 */
		public static final class Reset implements Visitor<Expression<?>> {
			
			@Override
			public final Expression<?> visit(final Symbol symbol) {
				return symbol;
			}
			
			@Override
			public final Expression<?> visit(final Variable variable) {
				return variable.reset();
			}
			
			@Override
			public final Expression<?> visit(final Composite<?> composite) {
				for (final Expression<?> element : composite) {
					element.accept(this);
				}
				
				return composite;
			}
			
			private static final long serialVersionUID = 6438401380761494994L;
			
			public static final Reset INSTANCE = new Reset();
			
		}
		
	}
	
	/**
	 * @author codistmonk (creation 2014-12-20)
	 */
	public static final class Composite<E extends Expression<?>> implements Expression<E> {
		
		private final List<E> elements;
		
		private final Map<Class<? extends Interpretation<?>>, Interpretation<E>> interpretations;
		
		public Composite() {
			this.elements = new ArrayList<>();
			this.interpretations = new HashMap<>();
		}
		
		public List<E> getElements() {
			return this.elements;
		}
		
		@SuppressWarnings("unchecked")
		public final <I extends Interpretation<E>> I as(final Class<? extends Interpretation<E>> interpretationClass) {
				return (I) this.interpretations.computeIfAbsent(interpretationClass, cls -> {
					try {
						return (Interpretation<E>) cls.getConstructor(this.getClass()).newInstance(this);
					} catch (final Exception exception) {
						return null;
					}
				});
		}
		
		@Override
		public final int getElementCount() {
			return this.getElements().size();
		}
		
		@Override
		public final E getElement(final int index) {
			return this.getElements().get(index);
		}
		
		@Override
		public final <V> V accept(final Visitor<V> visitor) {
			return visitor.visit(this);
		}
		
		@Override
		public final int hashCode() {
			return this.getElements().hashCode();
		}
		
		@Override
		public final boolean equals(final Object object) {
			if (this == object) {
				return true;
			}
			
			final Composite<?> that = cast(this.getClass(), object);
			
			return that != null && this.getElements().equals(that.getElements());
		}
		
		@Override
		public final String toString() {
			return this.getElements().toString();
		}
		
		private static final long serialVersionUID = -3768801167161895932L;
		
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
				
			}
			
		}
		
		/**
		 * @author codistmonk (creation 2014-12-20)
		 */
		public static final class Module extends Interpretation.Default<Expression<?>> {
			
			public Module(final Composite<Expression<?>> composite) {
				super(composite);
				
				if (composite.getElementCount() != 3 || !IMPLIES.equals(composite.getElement(1))) {
					throw new IllegalArgumentException();
				}
			}
			
			@SuppressWarnings("unchecked")
			public final <E extends Expression<?>> E getCondition() {
				return (E) this.getComposite().getElement(0);
			}
			
			@SuppressWarnings("unchecked")
			public final <E extends Expression<?>> E getConclusion() {
				return (E) this.getComposite().getElement(2);
			}
			
			public final <E extends Expression<?>> E applyTo(final Expression<?> expression) {
				return null; // TODO
			}
			
			private static final long serialVersionUID = -1926241237530848606L;
			
		}
		
		/**
		 * @author codistmonk (creation 2014-12-20)
		 */
		public static final class Substitution extends Interpretation.Default<Expression<?>> {
			
			private final Map<Expression<?>, Expression<?>> bindings;
			
			public Substitution(final Composite<Expression<?>> composite) {
				super(composite);
				
				this.bindings = new HashMap<>();
				
				if (isEquality(composite)) {
					this.bindings.put(composite.getElement(0), composite.getElement(2));
				} else {
					for (final Expression<?> element : composite) {
						if (isEquality(element)) {
							this.bindings.put(element.getElement(0), element.getElement(2));
						} else {
							throw new IllegalArgumentException();
						}
					}
				}
			}
			
			public final Substitution reset() {
				this.bindings.keySet().forEach(pattern -> pattern.accept(Variable.Reset.INSTANCE));
				
				return this;
			}
			
			public final <E extends Expression<?>> E applyTo(final Expression<?> expression) {
				return null; // TODO
			}
			
			private static final long serialVersionUID = -1572047035151529843L;
			
			public static final boolean isEquality(final Expression<?> expression) {
				return expression.getElementCount() == 3 && EQUALS.equals(expression.getElement(1));
			}
			
		}
		
	}
	
}
