package averan.core;

import java.io.Serializable;

/**
 * @author codistmonk (creation 2014-08-01)
 */
public abstract interface Visitor<R> extends Serializable {
	
	public abstract R visit(Composite composite);
	
	public abstract R visit(Module.Symbol symbol);
	
	public default R beginVisit(final Module module) {
		return null;
	}
	
	public default R endVisit(final Module module, final R moduleVisit) {
		return moduleVisit;
	}
	
}
