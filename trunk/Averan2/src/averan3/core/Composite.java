package averan3.core;

import static net.sourceforge.aprog.tools.Tools.cast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.aprog.tools.Tools;

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
	
	@SuppressWarnings("unchecked")
	public final Composite<E> setElementContexts() {
		final int n = this.size();
		
		for (int i = 0; i < n; ++i) {
			final Composite<?> composite = cast(Composite.class, this.get(i));
			
			if (composite != null && composite.getContext() != this) {
				this.getElements().set(i, (E) composite.copyUnder(this));
			}
		}
		
		return this;
	}
	
	@SuppressWarnings("unchecked")
	public final Composite<E> copyUnder(final Composite<?> context) {
		final Composite<E> result = new Composite<>(context);
		
		for (final E element : this) {
			result.getElements().add((E) result.attach(element));
		}
		
		return result;
	}
	
	public final Expression<?> attach(final Expression<?> expression) {
		final Composite<?> composite = cast(Composite.class, expression);
		
		if (composite != null && composite.getContext() != this) {
			return composite.copyUnder(this);
		}
		
		return expression;
	}
	
	public final Composite<?> getContext() {
		return this.context;
	}
	
	public final List<E> getElements() {
		return this.elements;
	}
	
	public final Composite<?> getRoot() {
		return this.getContext() == null ? this : this.getContext().getRoot();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public final <I extends Interpretation<E>> I as(final Class<I> interpretationClass) {
		try {
			if (Proof.class.equals(interpretationClass)) {
				return (I) this.interpretations.computeIfAbsent(
						interpretationClass, cls -> (I) Proof.interpret((Composite<Expression<?>>) this));
			}
			
			return (I) this.interpretations.computeIfAbsent(
					interpretationClass, cls -> Expression.super.as(interpretationClass));
		} catch (final Exception exception) {
			return null;
		}
	}
	
	@Override
	public final int size() {
		return this.getElements().size();
	}
	
	@Override
	public final E get(final int index) {
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
	
	/**
	 * @author codistmonk (creation 2014-12-20)
	 */
	public static final class Module extends Interpretation.Default<Expression<?>> {
		
		private String frameName;
		
		private int frameSize;
		
		public Module(final Composite<Expression<?>> composite) {
			super(composite);
			
			if (!isModule(composite)) {
				Tools.debugPrint(composite);
				throw new IllegalArgumentException();
			}
		}
		
		public final String getFrameName() {
			return this.frameName;
		}
		
		public final Module setFrameName(final String frameName) {
			this.frameName = frameName;
			
			return this;
		}
		
		public final int getFrameSize() {
			return this.frameSize;
		}
		
		public final String newPropositionName() {
			final Module parent;
			
			if (this.getFrameName() != null || (parent = this.getParent()) == null) {
				return Integer.toString(++this.frameSize);
			}
			
			return parent.newPropositionName();
		}
		
		public final Module getParent() {
			Composite<?> superContext = this.getComposite().getContext();
			
			while (superContext != null) {
				final Module result = superContext.as(Module.class);
				
				if (result != null) {
					return result;
				}
				
				superContext = superContext.getContext();
			}
			
			return null;
		}
		
		public final Composite<Composite<?>> getFacts() {
			final FactList conclusion = this.getConclusion().as(FactList.class);
			Tools.debugPrint(this.getConclusion());
			Tools.debugPrint(conclusion);
			if (conclusion.getComposite().size() == 1) {
				final Module module = conclusion.getComposite().get(1).as(Module.class);
				
				if (module != null) {
					return module.getFacts();
				}
			}
			
			return conclusion.getComposite();
		}
		
		public final String getPropositionName(final int index) {
			return this.getProposition(index).get(NAME).toString();
		}
		
		public final Expression<?> getProposition(final int index) {
			if (0 <= index) {
				throw new IllegalArgumentException();
			}
			
			final Composite<Composite<?>> facts = this.getFacts();
			int i = index + facts.size();
			
			if (0 <= i) {
				return facts.get(i);
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
			return isSingle(this.getComposite()) ? null : (Composite<?>) this.getComposite().get(0);
		}
		
		@SuppressWarnings("unchecked")
		public final <E extends Expression<?>> E getConditionName() {
			return (E) this.getCondition().get(NAME);
		}
		
		@SuppressWarnings("unchecked")
		public final <E extends Expression<?>> E getConditionProposition() {
			return (E) this.getCondition().get(PROPOSITION);
		}
		
		@SuppressWarnings("unchecked")
		public final Composite<Composite<?>> getConclusion() {
			return (Composite<Composite<?>>) this.getComposite().get(this.getComposite().size() - 1);
		}
		
		public final Module setConclusion(final Composite<Composite<?>> conclusion) {
			final Composite<Expression<?>> thisComposite = this.getComposite();
			
			thisComposite.getElements().set(this.getComposite().size() - 1,
					conclusion.getContext() == thisComposite ? conclusion : conclusion.copyUnder(thisComposite));
			
			return this;
		}
		
		@SuppressWarnings("unchecked")
		public final <E extends Expression<?>> E applyTo(final Expression<?> expression) {
			if (this.getCondition().get(PROPOSITION).accept(Variable.RESET).equals(expression)) {
				return (E) this.getConclusion().get(PROPOSITION).accept(Variable.BIND);
			}
			
			return null;
		}
		
		private static final long serialVersionUID = -1926241237530848606L;
		
		public static final int NAME = 0;
		
		public static final int PROPOSITION = 1;
		
		public static final int PROOF = 2;
		
		public static final int PROOF_TYPE = 0;
		
		public static final boolean isModule(final Expression<?> expression) {
			Tools.debugPrint(expression);
			return isSingle(expression) && FactList.isFactList(expression.get(0))
					|| isTriple(expression)
					&& Condition.isCondition(expression.get(0))
					&& IMPLIES.equals(expression.get(1))
					&& FactList.isFactList(expression.get(2));
		}
		
	}
	
	/**
	 * @author codistmonk (creation 2014-12-20)
	 */
	public static final class FactList extends Interpretation.Default<Composite<?>> {
		
		public FactList(final Composite<Composite<?>> composite) {
			super(composite);
			
			if (!isFactList(composite)) {
				throw new IllegalArgumentException();
			}
		}
		
		private static final long serialVersionUID = -2562022375060416809L;
		
		public static final boolean isFactList(final Expression<?> expression) {
			Tools.debugPrint(expression);
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
	public static final class Substitution extends Interpretation.Default<Expression<?>> implements Expression.Visitor<Expression<?>> {
		
		private final Map<Expression<?>, Expression<?>> bindings;
		
		private final int[] indices;
		
		private int currentIndex;
		
		public Substitution(final Composite<Expression<?>> composite) {
			super(composite);
			
			this.bindings = new HashMap<>();
			
			final int n = composite.size() - 1;
			
			for (int i = 0; i < n; ++i) {
				final Expression<?> element = composite.get(i);
				
				if (Equality.isEquality(element)) {
					this.bindings.put(element.get(0), element.get(2));
				} else {
					throw new IllegalArgumentException();
				}
			}
			
			{
				final Composite<?> indices = (Composite<?>) composite.get(n);
				final int indexCount = indices.size();
				
				this.indices = new int[indexCount];
				
				for (int i = 0; i < indexCount; ++i) {
					this.indices[i] = Integer.parseInt(indices.get(i).toString());
				}
			}
			
		}
		
		@SuppressWarnings("unchecked")
		public final <E extends Expression<?>> E applyTo(final Expression<?> expression) {
			this.currentIndex = -1;
			
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
				
				if (!listAccept((Iterable<Expression<?>>) composite, this,
						(Collection<Expression<?>>) ((Composite<?>) candidate).getElements())) {
					return composite;
				}
				
				((Composite<?>) candidate).setElementContexts();
			}
			
			return candidate;
		}
		
		private final Expression<?> tryToReplace(final Expression<?> expression) {
			for (final Map.Entry<Expression<?>, Expression<?>> binding : this.bindings.entrySet()) {
				if (binding.getKey().accept(Variable.RESET).equals(expression)
						&& (this.indices.length == 0 || 0 <= Arrays.binarySearch(this.indices, ++this.currentIndex))) {
					return binding.getValue().accept(Variable.BIND);
				}
			}
			
			return expression;
		}
		
		private static final long serialVersionUID = -1572047035151529843L;
		
		public static final boolean isSubstitution(final Expression<?> expression) {
			for (final Expression<?> element : expression) {
				if (!Equality.isEquality(element)) {
					return false;
				}
			}
			
			return true;
		}
		
	}
	
	/**
	 * @author codistmonk (creation 2014-12-20)
	 */
	public static final class Equality extends Interpretation.Default<Expression<?>> {
		
		public Equality(final Composite<Expression<?>> composite) {
			super(composite);
			
			if (!isEquality(composite)) {
				throw new IllegalArgumentException();
			}
		}
		
		@SuppressWarnings("unchecked")
		public final <E extends Expression<?>> E getLeft() {
			return (E) this.getComposite().get(LEFT);
		}
		
		@SuppressWarnings("unchecked")
		public final <E extends Expression<?>> E getRight() {
			return (E) this.getComposite().get(RIGHT);
		}
		
		public static final int LEFT = 0;
		
		public static final int HELPER_1 = 1;
		
		public static final int RIGHT = 2;
		
		private static final long serialVersionUID = 6188931718222618805L;
		
		public static final boolean isEquality(final Expression<?> expression) {
			return expression.size() == 3 && EQUALS.equals(expression.get(HELPER_1));
		}
		
	}
	
	/**
	 * @author codistmonk (creation 2014-12-20)
	 */
	public static final class Condition extends Interpretation.Default<Expression<?>> {
		
		public Condition(final Composite<Expression<?>> composite) {
			super(composite);
			
			if (!isCondition(composite)) {
				throw new IllegalArgumentException();
			}
		}
		
		@SuppressWarnings("unchecked")
		public final <E extends Expression<?>> E getName() {
			return (E) this.getComposite().get(Module.NAME);
		}
		
		@SuppressWarnings("unchecked")
		public final <E extends Expression<?>> E getProposition() {
			return (E) this.getComposite().get(Module.PROPOSITION);
		}
		
		private static final long serialVersionUID = -3056503743467136403L;
		
		public static final boolean isCondition(final Expression<?> expression) {
			return isPair(expression);
		}
		
	}
	
	/**
	 * @author codistmonk (creation 2014-12-20)
	 */
	public static final class Fact extends Interpretation.Default<Expression<?>> {
		
		public Fact(final Composite<Expression<?>> composite) {
			super(composite);
			
			if (!isFact(composite)) {
				throw new IllegalArgumentException();
			}
		}
		
		@SuppressWarnings("unchecked")
		public final <E extends Expression<?>> E getName() {
			return (E) this.getComposite().get(Module.NAME);
		}
		
		@SuppressWarnings("unchecked")
		public final <E extends Expression<?>> E getProposition() {
			return (E) this.getComposite().get(Module.PROPOSITION);
		}
		
		public final Composite<?> getProof() {
			return (Composite<?>) this.getComposite().get(Module.PROOF);
		}
		
		private static final long serialVersionUID = -3210218161030047944L;
		
		public static final boolean isFact(final Expression<?> expression) {
			return isTriple(expression);
		}
		
	}
	
	/**
	 * @author codistmonk (creation 2014-12-20)
	 */
	public static abstract interface Proof extends Interpretation<Expression<?>> {
		
		@SuppressWarnings("unchecked")
		public static <P extends Proof> P interpret(final Composite<Expression<?>> composite) {
			final Symbol tag = (Symbol) composite.get(0);
			
			if (NoProof.TAG.equals(tag)) {
				return (P) new NoProof(composite);
			}
			
			if (ProofByApply.TAG.equals(tag)) {
				return (P) new ProofByApply(composite);
			}
			
			if (ProofByRewrite.TAG.equals(tag)) {
				return (P) new ProofByRewrite(composite);
			}
			
			if (ProofBySubstitute.TAG.equals(tag)) {
				return (P) new ProofBySubstitute(composite);
			}
			
			return null;
		}
		
	}
	
	/**
	 * @author codistmonk (creation 2014-12-24)
	 */
	public static final class NoProof extends Interpretation.Default<Expression<?>> implements Proof {
		
		public NoProof(final Composite<Expression<?>> composite) {
			super(composite);
			
			if (!(composite.size() == 1 && Symbol.EMPTY.equals(composite.get(0)))) {
				throw new IllegalArgumentException();
			}
		}
		
		private static final long serialVersionUID = 1978589716308033753L;
		
		public static final Symbol TAG = Symbol.EMPTY;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-12-20)
	 */
	public static final class ProofByApply extends Interpretation.Default<Expression<?>> implements Proof {
		
		public ProofByApply(final Composite<Expression<?>> composite) {
			super(composite);
			
			if (!(isTriple(composite)
					&& TAG.equals(composite.get(0)))) {
				throw new IllegalArgumentException();
			}
		}
		
		public static final Symbol TAG = new Symbol("Apply");
		
		private static final long serialVersionUID = -6834557824434528656L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-12-20)
	 */
	public static final class ProofByRewrite extends Interpretation.Default<Expression<?>> implements Proof {
		
		public ProofByRewrite(final Composite<Expression<?>> composite) {
			super(composite);
			
			if (!(isTriple(composite)
					&& TAG.equals(composite.get(0)))) {
				throw new IllegalArgumentException();
			}
		}
		
		public static final Symbol TAG = new Symbol("Rewrite");
		
		private static final long serialVersionUID = -6834557824434528656L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-12-20)
	 */
	public static final class ProofBySubstitute extends Interpretation.Default<Expression<?>> implements Proof {
		
		public ProofBySubstitute(final Composite<Expression<?>> composite) {
			super(composite);
			
			if (!(isTriple(composite)
					&& TAG.equals(composite.get(0))
					&& isSubstitution(composite.get(2)))) {
				throw new IllegalArgumentException();
			}
		}
		
		private static final long serialVersionUID = -6834557824434528656L;
		
		public static final Symbol TAG = new Symbol("Substitute");
		
		public static final boolean isSubstitution(final Expression<?> expression) {
			final Composite<?> composite = cast(Composite.class, expression);
			
			return composite != null && composite.as(Substitution.class) != null;
		}
		
	}
	
}