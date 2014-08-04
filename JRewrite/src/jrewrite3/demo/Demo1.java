package jrewrite3.demo;

import static jrewrite3.ExpressionTools.*;
import static jrewrite3.Module.IDENTITY;

import jrewrite3.Module.Symbol;
import jrewrite3.Session;

import net.sourceforge.aprog.tools.IllegalInstantiationException;

/**
 * @author codistmonk (creation 2014-08-03)
 */
public final class Demo1 {
	
	private Demo1() {
		throw new IllegalInstantiationException();
	}
	
	/**
	 * @param commandLineArguments
	 * <br>Unused
	 */
	public static final void main(final String[] commandLineArguments) {
		final Session session = new Session();
		
		{
			session.prove($(forAll("x", "y"), $($("x", "=", "y"), "->", $("y", "=", "x"))));
			
			session.introduce();
			final Symbol x = session.getCurrentContext().getModule().getParameters().get(0);
			session.introduce();
			session.introduce("x=y");
			session.prove(session.getCurrentContext().getCurrentGoal());
			session.bind(IDENTITY, x);
			session.rewrite("#0", "x=y", 0);
		}
		
		session.printTo(System.out, true);
	}
	
}
