package averan5.tactics;

import static averan5.deductions.Standard.recall;
import static averan5.expressions.Expressions.*;
import static averan5.proofs.Stack.*;
import static net.sourceforge.aprog.tools.Tools.*;

import averan5.expressions.Unifier;
import averan5.proofs.Deduction;

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
		
		final String candidate = autoApply(justification, depth);
		
		if (candidate == null) {
			return null;
		}
		
		if (!candidate.isEmpty()) {
			return candidate;
		}
		
		g.conclude();
		
		return name(-1);
	}
	
	private static final String autoApply(final Pair<String, Object> justification, final int depth) {
		final String candidate = autoApply(justification.getFirst(), justification.getSecond(), depth);
		
		if (candidate == null || deduction().getParameters().isEmpty() && deduction().getPropositions().isEmpty()) {
			pop();
			
			return candidate;
		}
		
		recall(candidate);
		
		return "";
	}
	
	private static final String autoApply(final String justificationName, final Object justificationProposition, final int depth) {
		if (isUltimate(justificationProposition)) {
			return justificationName;
		}
		
		debugPrint(justificationName, justificationProposition);
		
		if (isBlock(justificationProposition)) {
			final Object variable = variable(justificationProposition);
			final Object value = variable instanceof Unifier ? ((Unifier) variable).getObject() : null;
			
			bind(justificationName, value != null ? value : variable);
		} else {
			final String conditionJustificationName = autoDeduce(condition(justificationProposition), depth - 1);
			
			if (conditionJustificationName == null) {
				return null;
			}
			
			apply(justificationName, conditionJustificationName);
		}
		
		return autoApply(name(-1), proposition(-1), depth);
	}
	
	public static final Pair<String, Object> justify(final Object goal) {
		final Map<Unifier, Pair<Unifier, Unifier>> snapshot = snapshot(goal);
		Deduction deduction = deduction();
		
		while (deduction != null) {
			final List<String> propositionNames = deduction.getPropositionNames();
			
			for (final ListIterator<String> i = propositionNames.listIterator(propositionNames.size()); i.hasPrevious();) {
				final String propositionName = i.previous();
				final Object unifiable = unifiable(deduction.getProposition(propositionName));
				
				if (unify(goal, ultimate(unifiable)) != null) {
					return new Pair<>(propositionName, unifiable);
				}
				
				restore(snapshot);
			}
			
			deduction = deduction.getParent();
		}
		
		return null;
	}
	
}
