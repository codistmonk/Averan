package averan3.core;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author codistmonk (creation 2015-01-06)
 */
public final class SessionTest {
	
	@Test
	public final void test1() {
		Session.begin();
		
		try {
			
		} finally {
			Session.export(Session.end(), new ConsoleOutput());
		}
	}
	
}
