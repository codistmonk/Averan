package averan5.core;

import static net.sourceforge.aprog.tools.Tools.cast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author codistmonk (creation 2014-12-20)
 */
public final class Composite<E extends Expression<?>> implements Expression<E> {
	
	private final Composite<?> context;
	
	private final List<E> elements;
	
	private final Map<Class<? extends Interpretation<?>>, Interpretation<E>> interpretations;
	
	public Composite(final Composite<?> context) {
		this.context = context;
		this.elements = new ArrayList<>();
		this.interpretations = new HashMap<>();
	}
	
	public final Composite<?> getContext() {
		return this.context;
	}
	
	public List<E> getElements() {
		return this.elements;
	}
	
	public final Composite<?> getRoot() {
		return this.getContext() == null ? this : this.getContext().getRoot();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public final <I extends Interpretation<E>> I as(final Class<I> interpretationClass) {
		return (I) this.interpretations.computeIfAbsent(interpretationClass, cls -> Expression.super.as(interpretationClass));
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
	public final <V> V accept(final Expression.Visitor<V> visitor) {
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
	public static final class Module extends Interpretation.Default<Expression<?>> {
		
		public Module(final Composite<Expression<?>> composite) {
			super(composite);
			
			if (!isModule(composite)) {
				throw new IllegalArgumentException();
			}
		}
		
		public final Composite<Composite<?>> getFacts() {
			final FactList conclusion = this.getConclusion().as(FactList.class);
			
			if (conclusion.getComposite().getElementCount() == 1) {
				final Module module = conclusion.getComposite().getElement(1).as(Module.class);
				
				if (module != null) {
					return module.getFacts();
				}
			}
			
			return conclusion.getComposite();
		}
		
		public final String getPropositionName(final int index) {
			return this.getProposition(index).getElement(NAME).toString();
		}
		
		public final Expression<?> getProposition(final int index) {
			if (0 <= index) {
				throw new IllegalArgumentException();
			}
			
			final Composite<Composite<?>> facts = this.getFacts();
			int i = index + facts.getElementCount();
			
			if (0 <= i) {
				return facts.getElement(i);
			}
			
			Module module = facts.getContext().as(Module.class);
			++i;
			
			while (i < 0 && module != null) {
				module = module.getComposite().getContext().as(Module.class);
				++i;
			}
			
			if (i == 0) {
				return module.getCondition();
			}
			
			return null;
		}
		
		public final Composite<?> getCondition() {
			return (Composite<?>) this.getComposite().getElement(CONDITION);
		}
		
		@SuppressWarnings("unchecked")
		public final <E extends Expression<?>> E getConditionName() {
			return (E) this.getCondition().getElement(NAME);
		}
		
		@SuppressWarnings("unchecked")
		public final <E extends Expression<?>> E getConditionProposition() {
			return (E) this.getCondition().getElement(PROPOSITION);
		}
		
		@SuppressWarnings("unchecked")
		public final Composite<Composite<?>> getConclusion() {
			return (Composite<Composite<?>>) this.getComposite().getElement(CONCLUSION);
		}
		
		@SuppressWarnings("unchecked")
		public final <E extends Expression<?>> E applyTo(final Expression<?> expression) {
			if (this.getCondition().getElement(PROPOSITION).accept(Variable.Reset.INSTANCE).equals(expression)) {
				return (E) this.getConclusion().getElement(PROPOSITION).accept(Bind.INSTANCE);
			}
			
			return null;
		}
		
		private static final long serialVersionUID = -1926241237530848606L;
		
		public static final int CONDITION = 0;
		
		public static final int HELPER_1 = 1;
		
		public static final int CONCLUSION = 2;
		
		public static final int NAME = 0;
		
		public static final int PROPOSITION = 1;
		
		public static final int PROOF = 2;
		
		public static final int PROOF_TYPE = 0;
		
		public static final boolean isModule(final Expression<?> expression) {
			return isTriple(expression)
					&& isCondition(expression.getElement(CONDITION))
					&& IMPLIES.equals(expression.getElement(HELPER_1))
					&& FactList.isFactList(expression.getElement(CONCLUSION));
		}
		
		public static final boolean isCondition(final Expression<?> expression) {
			final Composite<?> composite = cast(Composite.class, expression);
			
			return composite != null && isPair(composite);
		}
		
	}
	
	/**
	 * @author codistmonk (creation 2014-12-20)
	 */
	public static final class FactList extends Interpretation.Default<Composite<?>> {
		
		public FactList(final Composite<?> composite) {
			super((Composite) composite);
			
			if (!isFactList(composite)) {
				throw new IllegalArgumentException();
			}
		}
		
		private static final long serialVersionUID = -2562022375060416809L;
		
		public static final boolean isFactList(final Expression<?> expression) {
			for (final Expression<?> element : expression) {
				if (!isTriple(element)) {
					return false;
				}
			}
			
			return true;
		}
		
	}
	
	/**
	 * @author codistmonk (creation 2014-12-20)
	 */
	public static final class Bind implements Expression.Visitor<Expression<?>> {
		
		@Override
		public final Symbol visit(final Symbol symbol) {
			return symbol;
		}
		
		@Override
		public final Expression<?> visit(final Variable variable) {
			final Expression<?> match  = variable.getMatch();
			
			return match != null ? match : variable;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public final Composite<?> visit(final Composite<?> composite) {
			final Composite<?> candidate = new Composite<>(composite.getContext());
			
			if (listAccept((Iterable<Expression<?>>) composite, this,
					(Collection<Expression<?>>) candidate.getElements())) {
				return candidate;
			}
			
			return composite;
		}
		
		private static final long serialVersionUID = -2879093293185572053L;
		
		public static final Bind INSTANCE = new Bind();
		
		public static final <T> boolean listAccept(final Iterable<Expression<?>> elements,
				final Visitor<T> visitor, final Collection<T> visitOutput) {
			final boolean[] result = { false };
			
			elements.forEach(e -> {
				final T object = e.accept(visitor);
				
				result[0] |= e != object;
				
				visitOutput.add(object);
			});
			
			return result[0];
		}
		
	}
	
	/**
	 * @author codistmonk (creation 2014-12-20)
	 */
	public static final class Substitution extends Interpretation.Default<Expression<?>> implements Expression.Visitor<Expression<?>> {
		
		private final Map<Expression<?>, Expression<?>> bindings;
		
		// TODO add indices
		
		public Substitution(final Composite<Expression<?>> composite) {
			super(composite);
			
			this.bindings = new HashMap<>();
			
			for (final Expression<?> element : composite) {
				if (isEquality(element)) {
					this.bindings.put(element.getElement(0), element.getElement(2));
				} else {
					throw new IllegalArgumentException();
				}
			}
		}
		
		@SuppressWarnings("unchecked")
		public final <E extends Expression<?>> E applyTo(final Expression<?> expression) {
			return (E) expression.accept(this);
		}
		
		@Override
		public final Expression<?> visit(final Symbol symbol) {
			return this.tryToReplace(symbol);
		}
		
		@Override
		public final Expression<?> visit(final Variable variable) {
			return this.tryToReplace(variable);
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public final Expression<?> visit(final Composite<?> composite) {
			Expression<?> candidate = this.tryToReplace(composite);
			
			if (candidate == composite) {
				candidate = new Composite<>(composite.getContext());
				
				if (!Bind.listAccept((Iterable<Expression<?>>) composite, this,
						(Collection<Expression<?>>) ((Composite<?>) candidate).getElements())) {
					return composite;
				}
			}
			
			return candidate;
		}
		
		private final Expression<?> tryToReplace(final Expression<?> expression) {
			for (final Map.Entry<Expression<?>, Expression<?>> binding : this.bindings.entrySet()) {
				if (binding.getKey().accept(Variable.Reset.INSTANCE).equals(expression)) {
					return binding.getValue().accept(Bind.INSTANCE);
				}
			}
			
			return expression;
		}
		
		private static final long serialVersionUID = -1572047035151529843L;
		
		public static final boolean isEquality(final Expression<?> expression) {
			return expression.getElementCount() == 3 && EQUALS.equals(expression.getElement(1));
		}
		
	}
	
	/**
	 * @author codistmonk (creation 2014-12-20)
	 */
	public static final class Equality extends Interpretation.Default<Expression<?>> {
		
		public Equality(final Composite<Expression<?>> composite) {
			super(composite);
			
			if (!Substitution.isEquality(composite)) {
				throw new IllegalArgumentException();
			}
		}
		
		@SuppressWarnings("unchecked")
		public final <E extends Expression<?>> E getLeft() {
			return (E) this.getComposite().getElement(LEFT);
		}
		
		@SuppressWarnings("unchecked")
		public final <E extends Expression<?>> E getRight() {
			return (E) this.getComposite().getElement(RIGHT);
		}
		
		public static final int LEFT = 0;
		
		public static final int RIGHT = 1;
		
		private static final long serialVersionUID = 6188931718222618805L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-12-20)
	 */
	public static final class Condition extends Interpretation.Default<Expression<?>> {
		
		public Condition(final Composite<Expression<?>> composite) {
			super(composite);
			
			if (!Module.isPair(composite)) {
				throw new IllegalArgumentException();
			}
		}
		
		@SuppressWarnings("unchecked")
		public final <E extends Expression<?>> E getName() {
			return (E) this.getComposite().getElement(Module.NAME);
		}
		
		@SuppressWarnings("unchecked")
		public final <E extends Expression<?>> E getProposition() {
			return (E) this.getComposite().getElement(Module.PROPOSITION);
		}
		
		private static final long serialVersionUID = -3056503743467136403L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-12-20)
	 */
	public static final class Fact extends Interpretation.Default<Expression<?>> {
		
		public Fact(final Composite<Expression<?>> composite) {
			super(composite);
			
			if (!Module.isTriple(composite)) {
				throw new IllegalArgumentException();
			}
		}
		
		@SuppressWarnings("unchecked")
		public final <E extends Expression<?>> E getName() {
			return (E) this.getComposite().getElement(Module.NAME);
		}
		
		@SuppressWarnings("unchecked")
		public final <E extends Expression<?>> E getProposition() {
			return (E) this.getComposite().getElement(Module.PROPOSITION);
		}
		
		@SuppressWarnings("unchecked")
		public final <E extends Expression<?>> E getProof() {
			return (E) this.getComposite().getElement(Module.PROOF);
		}
		
		private static final long serialVersionUID = -3210218161030047944L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-12-20)
	 */
	public static final class ProofByApply extends Interpretation.Default<Expression<?>> {
		
		public ProofByApply(final Composite<Expression<?>> composite) {
			super(composite);
			
			if (!(Module.isTriple(composite)
					&& APPLY.equals(composite.getElement(0)))) {
				throw new IllegalArgumentException();
			}
		}
		
		public static final Symbol APPLY = new Symbol("Apply");
		
		private static final long serialVersionUID = -6834557824434528656L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-12-20)
	 */
	public static final class ProofByRewrite extends Interpretation.Default<Expression<?>> {
		
		public ProofByRewrite(final Composite<Expression<?>> composite) {
			super(composite);
			
			if (!(Module.isTriple(composite)
					&& REWRITE.equals(composite.getElement(0)))) {
				throw new IllegalArgumentException();
			}
		}
		
		public static final Symbol REWRITE = new Symbol("Rewrite");
		
		private static final long serialVersionUID = -6834557824434528656L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-12-20)
	 */
	public static final class ProofBySubstitute extends Interpretation.Default<Expression<?>> {
		
		public ProofBySubstitute(final Composite<Expression<?>> composite) {
			super(composite);
			
			if (!(Module.isTriple(composite)
					&& SUBSTITUTE.equals(composite.getElement(0))
					&& isSubstitution(composite.getElement(2)))) {
				throw new IllegalArgumentException();
			}
		}
		
		public static final Symbol SUBSTITUTE = new Symbol("Substitute");
		
		private static final long serialVersionUID = -6834557824434528656L;
		
		public static final boolean isSubstitution(final Expression<?> expression) {
			final Composite<?> composite = cast(Composite.class, expression);
			
			return composite != null && composite.as(Substitution.class) != null;
		}
		
	}
	
}