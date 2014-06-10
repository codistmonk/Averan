package jrewrite2;

import java.io.Serializable;

/**
 * @author codistmonk (creation 2014-06-10)
 */
public abstract interface Expression extends Serializable {
	
	public abstract Object accept(Visitor visitor);
	
}
