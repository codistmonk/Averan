package jrewrite3.core;

import java.io.Serializable;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author codistmonk (creation 2014-08-01)
 */
public abstract interface Visitor<R> extends Serializable {
	
	public default R beginVisit(final Composite composite) {
		return null;
	}
	
	public default R endVisit(final Composite composite, final R compositeVisit,
			final Supplier<List<R>> childVisits) {
		return compositeVisit;
	}
	
	public abstract R visit(Module.Symbol symbol);
	
	public default R beginVisit(final Module module) {
		return null;
	}
	
	public default R endVisit(final Module module, final R moduleVisit,
			final Supplier<List<R>> parameterVisits,
			final Supplier<List<R>> conditionVisits,
			final Supplier<List<R>> factVisits) {
		return moduleVisit;
	}
	
}
