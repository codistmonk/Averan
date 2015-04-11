package averan4.core;

import java.util.List;

/**
 * @author codistmonk (creation 2015-04-11)
 */
public final class ModusPonens implements Proof {
	
	private final String ruleName;
	
	private final String conditionName;
	
	public ModusPonens(final String ruleName, final String conditionName) {
		this.ruleName = ruleName;
		this.conditionName = conditionName;
	}
	
	public final String getRuleName() {
		return this.ruleName;
	}
	
	public final String getConditionName() {
		return this.conditionName;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public final List<Object> propositionFor(final Deduction context) {
		final List<Object> rule = context.getProposition(this.getRuleName());
		
		if (rule.size() != 3 || !Demo.IMPLIES.equals(rule.get(1))) {
			throw new IllegalArgumentException();
		}
		
		final List<Object> expectedCondition = (List<Object>) rule.get(0);
		final List<Object> condition = context.getProposition(this.getConditionName());
		
		Demo.checkArgument(expectedCondition.equals(condition));
		
		return (List<Object>) rule.get(2);
	}
	
	@Override
	public final String toString() {
		return "Apply " + this.getRuleName() + " on " + this.getConditionName();
	}
	
	private static final long serialVersionUID = 8564800788237315329L;
	
}