package jrewrite3.demo;

import static jrewrite3.Module.IDENTITY;
import static jrewrite3.Module.equality;
import jrewrite3.Module;
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
			final Module proposition = new Module(session.getCurrentContext().getModule());
			final Symbol x = proposition.parameter("x");
			final Symbol y = proposition.parameter("y");
			
			proposition.new Suppose(equality(x, y)).execute();
			proposition.new Admit(equality(y, x)).execute();
			
			session.prove(proposition);
			session.introduce();
			session.introduce();
			session.introduce("x=y");
			session.prove(session.getCurrentContext().getCurrentGoal());
			session.bind(IDENTITY, x);
			session.rewrite("#0", "x=y", 0);
		}
		
		session.printTo(System.out, true);
	}
	
}
