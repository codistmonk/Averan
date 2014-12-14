package averan3.core;

import java.io.Serializable;

/**
 * @author codistmonk (creation 2014-12-14)
 */
public abstract interface Expression extends Serializable {
	
	public abstract <Value> Value accept(Visitor<Value> visitor);
	
	public abstract Module getContext();
	
	/**
	 * @author codistmonk (creation 2014-12-14)
	 */
	public static abstract interface Visitor<Value> extends Serializable {
		
		public abstract Value visit(Module module);
		
		public abstract Value visit(Module.Symbol symbol);
		
		public abstract Value visit(Module.Variable variable);
		
	}
	
}
