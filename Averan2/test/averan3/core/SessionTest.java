package averan3.core;

import static org.junit.Assert.*;

import org.junit.Test;

import averan3.core.Session.ConsoleExporter;

/**
 * @author codistmonk (creation 2014-12-14)
 */
public final class SessionTest {
	
	@Test
	public final void test1() {
		final Session session = new Session();
		
		session.push(ModuleTest.newTautology());
		
		new ConsoleExporter().export(session);
		
		fail("TODO");
	}

}
