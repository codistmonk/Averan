package jrewrite2;

import static net.sourceforge.aprog.tools.Tools.set;
import static org.junit.Assert.*;
import net.sourceforge.aprog.tools.Tools;

import org.junit.Test;

/**
 * @author codistmonk (creation 2014-06-10)
 */
public final class ContextTest {
	
	@Test
	public final void test1() {
		final Context context = new Context();
		
		context.assume(null, template(v("n"), rule(nat("n"), nat(s("n")))));
		context.assume(null, nat("0"));
		context.assume(null, equality("1", s("0")));
		
		context.printTo(System.out);
		
		{
			final Context proof = context.prove(null, nat("1"));
			
			proof.printTo(System.out);
			
			proof.bind(null, 0, expression("0"));
			proof.apply(null, 4, 1);
			proof.rewriteRight(null, 5, 2, set(0));
			
			proof.printTo(System.out);
			
			assertTrue(proof.isGoalReached());
		}
		
		context.printTo(System.out);
		
		assertTrue(context.isGoalReached());
	}
	
	public static final Template template(final String[] variableNames, final Object expression) {
		int n = variableNames.length;
		Template result = new Template(variableNames[--n], expression(expression));
		
		while (0 < n) {
			result = new Template(variableNames[--n], result);
		}
		
		return result;
	}
	
	public static final String[] v(final String... variableNames) {
		return variableNames;
	}
	
	public static final Equality equality(final Object left, final Object right) {
		return new Equality(expression(left), expression(right));
	}
	
	public static final Rule rule(final Object condition, final Object expression) {
		return new Rule(expression(condition), expression(expression));
	}
	
	public static final Composite s(final Object object) {
		return apply1("S", object);
	}
	
	public static final Composite apply1(final Object function, final Object argument) {
		return composite(function, " ", argument);
	}
	
	public static final Composite nat(final Object object) {
		return composite(object, ":", "N");
	}
	
	public static final Composite composite(final Object... objects) {
		final int n = objects.length;
		final Expression[] expressions = new Expression[n];
		
		for (int i = 0; i < n; ++i) {
			expressions[i] = expression(objects[i]);
		}
		
		return new Composite(expressions);
	}
	
	public static final Expression expression(final Object object) {
		return object instanceof Expression ? (Expression) object : new Symbol(object.toString());
	}
	
}
