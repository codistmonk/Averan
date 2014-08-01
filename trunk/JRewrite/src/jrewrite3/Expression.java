package jrewrite3;

import java.io.Serializable;
import java.util.List;

/**
 * @author codistmonk (creation 2014-08-01)
 */
public abstract interface Expression extends Serializable {
	
	public abstract Object accept(Visitor visitor);
	
}

/**
 * @author codistmonk (creation 2014-08-01)
 */
final class Composite implements Expression {
	
	private final List<Expression> children;
	
}

/**
 * @author codistmonk (creation 2014-08-01)
 */
final class Module implements Expression {
	
	private final Module parent;
	
	private final List<Variable> variables;
	
	/**
	 * @author codistmonk (creation 2014-08-01)
	 */
	public final class Variable implements Expression {
		
	}
	
	public static final Module ROOT = new Module();
	
}
