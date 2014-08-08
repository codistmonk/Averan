package jrewrite3.demo;

import static jrewrite3.core.ExpressionTools.*;
import static jrewrite3.modules.Standard.IDENTITY;
import jrewrite3.core.Session;
import jrewrite3.modules.Standard;
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
		final Session session = new Session().load(Standard.MODULE);
		
		session.claim("symmetry_of_identity",
				$(forAll("x", "y"), $($("x", "=", "y"), "->", $("y", "=", "x"))));
		
		{
			session.introduce("x");
			session.introduce("y");
			session.introduce("eqxy");
			
			session.claim("eqyx", session.getCurrentGoal());
			
			{
				session.bind("idx", IDENTITY, session.getParameter("x"));
				session.rewrite("eqyx", "idx", "eqxy", 0);
			}
		}
		
		session.new Exporter(new Session.Printer(System.out), true).printSession();
	}
	
}
