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
		final Session session = new Session();
		final Variable x = new Variable("X");
		
		session.deduce("tautology", new Module().addCondition(null, x).addFact(null, x, null));
		{
			session.introduce();
			session.introduce();
			session.substitute(session.getCurrentFrame().newPropositionName(), new Symbol<>("X"));
		}
		// TODO
		
		SessionExporter.export(session, new ConsoleOutput());
	}
	
}
