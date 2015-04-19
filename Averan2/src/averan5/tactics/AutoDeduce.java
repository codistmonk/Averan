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
		
		{
			String justificationName = justification.getFirst();
			Object justificationProposition = justification.getSecond();
			
			while (!isUltimate(justificationProposition)) {
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
		
		return name(-1);
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
