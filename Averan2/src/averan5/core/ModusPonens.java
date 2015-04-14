package averan5.core;

import static averan5.core.AveranTools.*;

import java.util.Arrays;
import java.util.List;

/**
 * @author codistmonk (creation 2015-04-11)
 */
public final class ModusPonens extends Proof.Abstract {
	
	private final String ruleName;
	
	private final String conditionName;
	
	public ModusPonens(final String provedPropositionName, final String ruleName, final String conditionName) {
		super(provedPropositionName, Arrays.asList("By applying", ruleName, "on", conditionName));
		this.ruleName = ruleName;
		this.conditionName = conditionName;
	}
	
	public final String getRuleName() {
		return this.ruleName;
	}
	
	public final String getConditionName() {
		return this.conditionName;
	}
	
	@Override
	public final Object getProvedPropositionFor(final Deduction context) {
		final List<Object> rule = checkRule(this.getRuleName(), context);
		final List<Object> expectedCondition = condition(rule);
		final List<Object> condition = checkProposition(this.getConditionName(), context);
		
		checkArgument(areEqual(expectedCondition, condition), "Expected condition: " + expectedCondition + " but was: " + condition);
		
		return rule.get(2);
	}
	
	private static final long serialVersionUID = 8564800788237315329L;
	
}