package averan4.core;

import static org.junit.Assert.*;

import org.junit.Test;

import averan4.io.ConsoleOutput;
import averan4.io.SessionExporter;

/**
 * @author codistmonk (creation 2014-12-27)
 */
public final class SessionTest {
	
	@Test
	public final void test1() {
		final Session session = new Session().deduce("test", null);
		final Variable varX = new Variable("X");
		
		session.deduce("tautologyA", new Module().addCondition(null, varX).addFact(null, varX, null));
		{
			final Expression<?> x = session.introduce();
			session.introduce();
			
			session.substitute(null, x);
			session.rewrite(null, session.getCurrentModule().getPropositionName(-1), session.getCurrentModule().getPropositionName(-1));
			session.rewrite(null, session.getCurrentModule().getPropositionName(-3), session.getCurrentModule().getPropositionName(-1));
		}
		
		session.deduce(null, null);
		{
			session.suppose(null, new Symbol<>("Y"));
			session.apply(null, "tautologyA", session.getCurrentModule().getPropositionName(-1));
			session.acceptModule();
		}
		
		SessionExporter.export(session, new ConsoleOutput());
	}
	
}
