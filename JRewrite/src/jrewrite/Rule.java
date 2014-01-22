package jrewrite;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author codistmonk (creation 2013-12-16)
 */
public final class Rule implements Serializable {
	
	private final Expression[] conditions;
	
	private final Expression[] prototypes;
	
	public Rule(final Expression[] conditions, final Expression[] prototypes) {
		this.conditions = conditions;
		this.prototypes = prototypes;
	}
	
	public final Expression[] getConditions() {
		return this.conditions;
	}
	
	public final Expression[] getPrototypes() {
		return this.prototypes;
	}
	
	public final boolean apply(final Collection<Expression> resultFacts, final Expression... conditionFacts) {
		final int n = this.getConditions().length;
		
		if (n != conditionFacts.length) {
			return false;
		}
		
		final Map<String, Expression> context = new HashMap<String, Expression>();
		
		for (int i = 0; i < n; ++i) {
			if (!this.getConditions()[i].matches(conditionFacts[i], context)) {
				return false;
			}
		}
		
		for (final Expression prototype : this.getPrototypes()) {
			resultFacts.add(prototype.refine(context));
		}
		
		return true;
	}
	
	/**
	 * {@value}.
	 */
	private static final long serialVersionUID = 8433595727266763956L;
	
}
