package jrewrite3;

import java.io.Serializable;
import java.util.List;

/**
 * @author codistmonk (creation 2014-08-01)
 */
public abstract interface Visitor<R> extends Serializable {
	
	public abstract R visitBeforeChildren(Composite composite);
	
	public default R visitAfterChildren(final Composite composite, final R beforeVisit,
			final List<R> childVisits) {
		return beforeVisit;
	}
	
	public abstract R visit(Module.Variable variable);
	
	public abstract R visitBeforeVariables(Module module);
	
	public default R visitAfterFacts(final Module module, final R beforeVisit,
			final List<R> variableVisits, final List<R> conditionVisits, final List<R> factVisits) {
		return beforeVisit;
	}
	
}
