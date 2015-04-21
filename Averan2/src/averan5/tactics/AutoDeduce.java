package averan5.tactics;

import static averan5.deductions.Standard.recall;
import static averan5.expressions.Expressions.*;
import static averan5.proofs.Stack.*;
import static net.sourceforge.aprog.tools.Tools.*;
import averan5.expressions.Unifier;
import averan5.proofs.Deduction;

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
		return autoDeduce(goal, null, new HashMap<>(), 3) != null;
	}
	
	public static final String autoDeduce(final Object goal, final String previousJustificationName, final Map<Unifier, Pair<Unifier, Unifier>> snapshot, final int depth) {
		if (depth <= 0) {
			return null;
		}
		
		final Goal g = Goal.deduce(goal);
		
		g.intros();
		
		Pair<String, Object> justification = justify(g.getProposition(), previousJustificationName, snapshot);
		final String checkpoint = name(-1);
		String candidate = justification == null ? null : autoBindApply(justification.getFirst(), justification.getSecond(), depth);
		
		while (justification != null && candidate == null) {
			restore(snapshot);
			removeAfter(checkpoint, deduction());
			justification = justify(g.getProposition(), justification.getFirst(), snapshot);
			candidate = justification == null ? null : autoBindApply(justification.getFirst(), justification.getSecond(), depth);
		}
		
		debugPrint(g.getProposition(), justification, previousJustificationName);
		
		if (candidate == null || deduction().getParameters().isEmpty() && deduction().getPropositions().isEmpty()) {
			pop();
			
			return candidate;
		}
		
		recall(candidate);
		
		g.conclude();
		
		return name(-1);
	}
	
	public static final void removeAfter(final String checkpoint, final Deduction deduction) {
		final List<String> propositionNames = deduction.getPropositionNames();
		
		for (final ListIterator<String> i = propositionNames.listIterator(propositionNames.size()); i.hasPrevious();) {
			final String name = i.previous();
			
			if (name.equals(checkpoint)) {
				break;
			}
			
			deduction.getPropositions().remove(name);
			deduction.getProofs().remove(name);
			i.remove();
		}
	}
	
	public static final String autoBindApply(final String propositionName, final Object unifiableProposition, final int depth) {
		if (isUltimate(unifiableProposition)) {
			return propositionName;
		}
		
		if (isBlock(unifiableProposition)) {
			final Object variable = variable(unifiableProposition);
			final Object value = variable instanceof Unifier ? ((Unifier) variable).getObject() : null;
			
			bind(propositionName, value != null ? value : variable);
			
			return autoBindApply(name(-1), proposition(-1), depth);
		}
		
		{
			final Object condition = condition(unifiableProposition);
			final Map<Unifier, Pair<Unifier, Unifier>> snapshot = new HashMap<>();
			final String checkpoint = name(-1);
			String conditionJustificationName = autoDeduce(condition, null, snapshot, depth - 1);
			String result = null;
			
			while (conditionJustificationName != null && result == null) {
				apply(propositionName, conditionJustificationName);
				result = autoBindApply(name(-1), proposition(-1), depth);
				
				if (result == null) {
					restore(snapshot);
					removeAfter(checkpoint, deduction());
					conditionJustificationName = autoDeduce(condition, conditionJustificationName, snapshot, depth - 1);
				}
			}
			
//			if (conditionJustificationName == null) {
//				return null;
//			}
//			
//			apply(propositionName, conditionJustificationName);
//			
//			result = autoBindApply(name(-1), proposition(-1), depth);
//			
//			if (result == null) {
//				restore(snapshot);
//				// TODO retry autoDeduce(condition)
//			}
//			
			return result;
		}
	}
	
	public static final Pair<String, Object> justify(final Object goal, final String checkpoint, final Map<Unifier, Pair<Unifier, Unifier>> snapshot) {
		Deduction deduction = deduction();
		boolean checkpointReached = checkpoint == null;
		
		while (deduction != null) {
			final List<String> propositionNames = deduction.getPropositionNames();
			
			for (final ListIterator<String> i = propositionNames.listIterator(propositionNames.size()); i.hasPrevious();) {
				final String propositionName = i.previous();
				
				if (checkpointReached) {
					final Object unifiable = unifiable(deduction.getProposition(propositionName));
					
					snapshot.putAll(snapshot(goal));
					snapshot.putAll(snapshot(unifiable));
					
					if (unify(goal, ultimate(unifiable)) != null) {
						return new Pair<>(propositionName, unifiable);
					}
					
					restore(snapshot);
					snapshot.clear(); // XXX necessary?
				} else {
					checkpointReached = propositionName.equals(checkpoint);
				}
			}
			
			deduction = deduction.getParent();
		}
		
		return null;
	}
	
}
