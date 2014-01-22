package jrewrite;

import static net.sourceforge.aprog.tools.Tools.array;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import net.sourceforge.aprog.tools.IllegalInstantiationException;

/**
 * @author codistmonk (creation 2014-01-22)
 */
public final class JRewriteTools {
	
	private JRewriteTools() {
		throw new IllegalInstantiationException();
	}
	
	public static final Variable variable(final String symbol) {
		return new Variable(symbol);
	}
	
	public static final Constant constant(final String symbol) {
		return new Constant(symbol);
	}
	
	public static final Composite operation(final Expression left, final String operator, final Expression right) {
		return new Composite(left, constant(operator), right);
	}
	
	public static final Rule rule(final Expression condition, final Expression prototype) {
		return new Rule(array(condition), array(prototype));
	}
	
	public static final void rewrite(final List<Expression> facts, final int equalityIndex, final int targetExpressionIndex, final int subExpressionIndex) {
		final List<Expression> equality = ((Composite) facts.get(equalityIndex)).getSubexpressions();
		
		if (equality.size() != 3 || !"=".equals(equality.get(1).toString())) {
			throw new IllegalArgumentException(equality.toString());
		}
		
		facts.set(targetExpressionIndex, facts.get(targetExpressionIndex).rewrite(
				equality.get(0), new AtomicInteger(subExpressionIndex), equality.get(2)));
	}
	
}
