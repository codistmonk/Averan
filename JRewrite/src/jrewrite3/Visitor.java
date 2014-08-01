package jrewrite3;

import java.io.Serializable;
import java.util.List;

/**
 * @author codistmonk (creation 2014-08-01)
 */
public abstract interface Visitor extends Serializable {
	
	public abstract Object visitBeforeChildren(Composite composite);
	
	public default Object visitAfterChildren(final Composite composite, final Object beforeVisit,
			final List<Object> childVisits) {
		return beforeVisit;
	}
	
	public abstract Object visit(Module.Variable variable);
	
	public abstract Object visitBeforeVariables(Module module);
	
	public default Object visitAfterFacts(final Module module, final Object beforeVisit,
			final List<Object> variableVisits, final List<Object> conditionVisits, final List<Object> factVisits) {
		return beforeVisit;
	}
	
}
