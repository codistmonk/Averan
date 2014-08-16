package averan.modules;

import static averan.core.ExpressionTools.*;
import static org.junit.Assert.*;

import org.junit.Test;

import averan.core.Expression;
import averan.core.Session;
import averan.modules.Standard;

/**
 * @author codistmonk (creation 2014-08-04)
 */
public final class StandardTest {
	
	@Test
	public final void test1() {
		final Session session = new Session();
		
		session.load(Standard.MODULE);
		
		final Expression expected = $($("1", "=", "2"), "->", $("2", "=", "1"));
		
		session.claim("actual", expected);
		
		assertNull(session.getCurrentContext().getModule().getPropositionOrNull("actual"));
		
		{
			session.introduce("1=2");
			
			session.claim($("2", "=", "1"));
			
			{
				session.bind("1=2->2=1", Standard.SYMMETRY_OF_EQUALITY, (Expression) $("1"), $("2"));
				session.apply("1=2->2=1", "1=2");
			}
		}
		
		assertEquals(expected, session.getCurrentContext().getModule().getPropositionOrNull("actual"));
	}
	
}
