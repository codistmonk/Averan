package averan5.deductions;

import static averan5.core.AveranTools.*;
import static averan5.deductions.Unify.unify;
import static net.sourceforge.aprog.tools.Tools.*;

import averan5.core.Deduction;
import averan5.core.Goal;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import net.sourceforge.aprog.tools.IllegalInstantiationException;
import net.sourceforge.aprog.tools.Pair;

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
	
}
