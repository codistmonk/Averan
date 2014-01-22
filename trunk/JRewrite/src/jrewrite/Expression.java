package jrewrite;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author codistmonk (creation 2013-12-16)
 */
public abstract interface Expression extends Serializable {
	
	public abstract boolean matches(Expression expression, Map<String, Expression> context);
	
	public abstract Expression refine(Map<String, Expression> context);
	
	public abstract Expression rewrite(Expression pattern, AtomicInteger index, Expression replacement);
	
}
