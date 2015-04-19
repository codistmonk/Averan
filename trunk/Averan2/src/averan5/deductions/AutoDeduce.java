package averan5.deductions;

import static averan5.core.AveranTools.*;
import static averan5.deductions.AutoDeduce.Unify.unify;
import static net.sourceforge.aprog.tools.Tools.*;

import averan5.core.Deduction;
import averan5.core.Goal;
import averan5.deductions.StandardTest.ExpressionCombiner;
import averan5.deductions.StandardTest.ExpressionRewriter;
import averan5.deductions.StandardTest.ExpressionVisitor;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import net.sourceforge.aprog.tools.IllegalInstantiationException;
import net.sourceforge.aprog.tools.Pair;
import net.sourceforge.aprog.tools.Tools;

/**
 * @author codistmonk (creation 2015-04-18)
 */
public final class AutoDeduce {
	
	private AutoDeduce() {
		throw new IllegalInstantiationException();
	}
	
	public static final boolean autoDeduce(final Object goal) {
		return autoDeduce(goal, 3) != null;
	}
	
	public static final String autoDeduce(final Object goal, final int depth) {
		if (depth <= 0) {
			return null;
		}
		
		final Goal g = Goal.deduce(goal);
		
		g.intros();
		
		final Pair<String, Object> justification = justify(g.getProposition());
		
		if (justification == null) {
			pop();
			
			debugPrint(goal);
			
			return null;
		}
		
		{
			String justificationName = justification.getFirst();
			Object justificationProposition = justification.getSecond();
			
			while (!isTerminus(justificationProposition)) {
				if (isBlock(justificationProposition)) {
					final Unifier variable = (Unifier) variable(justificationProposition);
					final Object value = variable.getObject();
					
					bind(justificationName, value != null ? value : variable);
				} else {
					final String conditionJustificationName = autoDeduce(condition(justificationProposition), depth - 1);
					
					if (conditionJustificationName == null) {
						pop();
						
						return null;
					}
					
					apply(justificationName, conditionJustificationName);
				}
				
				justificationName = name(-1);
				justificationProposition = proposition(-1);
			}
			
			if (deduction().getParameters().isEmpty() && deduction().getPropositions().isEmpty()) {
				pop();
				
				return justificationName;
			}
			
			recall(justificationName);
		}
		
		g.conclude();
		
		final String result = name(-1);
		
		//XXX UGLY...
		deduction().getPropositions().put(result, lock(proposition(result)));
		
		return result;
	}
	
	public static final Object lock(final Object expression) {
		return new ExpressionRewriter() {
			
			@Override
			public final Object visit(final Object expression) {
				final Unifier unifier = cast(Unifier.class, expression);
				final Object candidate = unifier == null ? null : unifier.getObject();
				
				if (candidate != null) {
					return candidate;
				}
				
				return ExpressionRewriter.super.visit(expression);
			}
			
			private static final long serialVersionUID = -1945085756067374461L;
			
		}.apply(expression);
	}
	
	public static final void recall(final String propositionName) {
		if (!propositionName.equals(name(-1)) || !deduction().getProofs().containsKey(propositionName)) {
			subdeduction();
			{
				bind("recall", proposition(propositionName));
				apply(name(-1), propositionName);
			}
			conclude();
		}
	}
	
	public static final boolean isTerminus(final Object expression) {
		return !isBlock(expression) && !isRule(expression);
	}
	
	public static final Pair<String, Object> justify(final Object goal) {
		final Map<Unifier, Pair<Unifier, Unifier>> snapshot = snapshot(goal);
		Deduction deduction = deduction();
		
		while (deduction != null) {
			final List<String> propositionNames = deduction.getPropositionNames();
			
			for (final ListIterator<String> i = propositionNames.listIterator(propositionNames.size()); i.hasPrevious();) {
				final String propositionName = i.previous();
				final Object unifiable = unifiable(deduction.getProposition(propositionName));
				
				if (unify(goal, terminus(unifiable)) != null) {
					return new Pair<>(propositionName, unifiable);
				}
				
				restore(snapshot);
			}
			
			deduction = deduction.getParent();
		}
		
		return null;
	}
	
	public static final Object terminus(final Object expression) {
		if (isBlock(expression)) {
			return terminus(scope(expression));
		}
		
		if (isRule(expression)) {
			return terminus(conclusion(expression));
		}
		
		return expression;
	}
	
	public static final Object unifiable(final Object expression) {
		return new ExpressionRewriter() {
			
			private final Map<Object, Object> unifiers = new HashMap<>();
			
			@Override
			public final Object visit(final Object expression) {
				return this.unifiers.getOrDefault(expression, expression);
			}
			
			@Override
			public final Object visit(final List<?> expression) {
				if (isBlock(expression)) {
					final Object variable = variable(expression);
					
					if (!(variable instanceof Unifier)) {
						final boolean remove = !this.unifiers.containsKey(variable);
						final Object old = this.unifiers.put(variable, new Unifier());
						
						try {
							return ExpressionRewriter.super.visit(expression);
						} finally {
							if (remove) {
								this.unifiers.remove(variable);
							} else {
								this.unifiers.put(variable, old);
							}
						}
					}
				}
				
				return ExpressionRewriter.super.visit(expression);
			}
			
			private static final long serialVersionUID = -7683840568399205564L;
			
		}.apply(expression);
	}
	
	public static final void restore(final Map<Unifier, Pair<Unifier, Unifier>> snapshot) {
		snapshot.forEach(Unifier::restore);
	}
	
	public static final Map<Unifier, Pair<Unifier, Unifier>> snapshot(final Object expression) {
		return new ExpressionVisitor<Map<Unifier, Pair<Unifier, Unifier>>>() {
			
			private final Map<Unifier, Pair<Unifier, Unifier>> result = new HashMap<>();
			
			@Override
			public final Map<Unifier, Pair<Unifier, Unifier>> visit(final Object expression) {
				if (expression instanceof Unifier) {
					((Unifier) expression).snapshotTo(this.result);
				}
				
				return this.result;
			}
			
			private static final long serialVersionUID = -9159689594221863543L;
			
		}.apply(expression);
	}
	
	/**
	 * @author codistmonk (creation 2015-04-19)
	 */
	public static final class Unify implements ExpressionCombiner {
		
		@Override
		public final Object visit(final Object expression1, final Object expression2) {
			if (expression1 instanceof Unifier) {
				return ((Unifier) expression1).unifies(expression2) ? expression1 : null;
			}
			
			if (expression2 instanceof Unifier) {
				return ((Unifier) expression2).unifies(expression1) ? expression2 : null;
			}
			
			return Tools.equals(expression1, expression2) ? expression1 : null;
		}
		
		private static final long serialVersionUID = 3182367276867731182L;
		
		public static final Unify INSTANCE = new Unify();
		
		public static final Object unify(final Object object1, final Object object2) {
			return INSTANCE.apply(object1, object2);
		}
		
	}
	
	/**
	 * @author codistmonk (creation 2015-04-19)
	 */
	public static final class Unifier implements Serializable {
		
		private Collection<Unifier> unifiers;
		
		private Object[] object;
		
		private Unifier(final Collection<Unifier> unifiers, final Object[] object) {
			this.unifiers = unifiers;
			this.object = object;
		}
		
		public Unifier() {
			this(new HashSet<>(), new Object[1]);
			
			this.unifiers.add(this);
		}
		
		public final Object getObject() {
			return this.object[0];
		}
		
		public final void snapshotTo(final Map<Unifier, Pair<Unifier, Unifier>> snapshot) {
			for (final Unifier unifier : this.unifiers) {
				snapshot.computeIfAbsent(unifier, Unifier::snapshot);
			}
		}
		
		public final Pair<Unifier, Unifier> snapshot() {
			final Unifier structure = new Unifier(this.unifiers, this.object);
			final Unifier contents = new Unifier();
			contents.unifiers.clear();
			contents.unifiers.addAll(this.unifiers);
			contents.object[0] = this.getObject();
			
			return new Pair<>(structure, contents);
		}
		
		public final Unifier restore(final Pair<Unifier, Unifier> snapshot) {
			final Unifier structure = snapshot.getFirst();
			final Unifier contents = snapshot.getSecond();
			this.unifiers = structure.unifiers;
			this.object = structure.object;
			this.unifiers.clear();
			this.unifiers.addAll(contents.unifiers);
			this.object[0] = contents.getObject();
			
			return this;
		}
		
		public final boolean unifies(final Object object) {
			if (this == object) {
				return true;
			}
			
			if (object == null) {
				return false;
			}
			
			final Object thisObject = this.getObject();
			final Unifier that = cast(this.getClass(), object);
			
			if (that != null) {
				final Object thatObject = that.getObject();
				final boolean merge = thisObject != null && thatObject != null;
				
				if (merge && !thisObject.equals(thatObject)) {
					return false;
				}
				
				final Unifier absorber, absorbed;
				
				if (merge || thatObject == null) {
					absorber = this;
					absorbed = that;
				} else {
					absorber = that;
					absorbed = this;
				}
				
				for (final Unifier unifier : absorbed.unifiers) {
					absorber.unifiers.add(unifier);
					unifier.unifiers = absorber.unifiers;
					unifier.object = absorber.object;
				}
				
				return true;
			}
			
			{
				if (thisObject == null) {
					this.object[0] = object;
					
					return true;
				}
				
				return Tools.equals(thisObject, object);
			}
		}
		
		@Override
		public final String toString() {
			return this.unifiers.stream().map(u -> ids.computeIfAbsent(u, k -> id.incrementAndGet())).collect(toTreeSet())
					+ "{" + (this.getObject() != null ? this.getObject() : "") + "}";
		}
		
		private static final long serialVersionUID = 4343191681740782407L;
		
		private static final  AtomicInteger id = new AtomicInteger();
		
		private static final Map<Unifier, Integer> ids = new WeakHashMap<>();
		
	}
	
}
