package averan.core;

import java.io.Serializable;

/**
 * @author codistmonk (creation 2014-08-01)
 */
public abstract interface Visitor<R> extends Serializable {
	
	public default R visit(final Pattern.Any any) {
		return null;
	}
	
	public abstract R visit(Composite composite);
	
	public abstract R visit(Module.Symbol symbol);
	
	public abstract R visit(Module module);
	
}
