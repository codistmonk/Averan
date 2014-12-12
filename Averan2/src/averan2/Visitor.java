package averan2;

import java.io.Serializable;

/**
 * @author codistmonk (creation 2014-12-11)
 *
 * @param <T>
 */
public abstract interface Visitor<T> extends Serializable {
	
	public abstract T visit(Symbol symbol);
	
	public abstract T visit(Module module);
	
	public abstract T visit(Composite composite);
	
	public abstract T visit(Proposition proposition);
	
	public abstract T visit(Proof proof);
	
}
