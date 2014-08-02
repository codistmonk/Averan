package jrewrite3;

import static org.junit.Assert.*;
import jrewrite3.Module.Admit;
import jrewrite3.Module.Symbol;

import org.junit.Test;

/**
 * @author codistmonk (creation 2014-08-02)
 */
public final class ModuleTest {
	
	@Test
	public final void test1() {
		final Module module1 = new Module(null);
		
		assertEquals(module1, module1);
		
		final Module module2 = new Module(null);
		
		assertEquals(module1, module2);
		
		{
			final Symbol x = module1.parameter("x");
			final Symbol y = module1.parameter("y");
			module1.execute(new Admit(Module.equality(x, y)));
		}
		
		assertNotEquals(module1, module2);
		
		{
			final Symbol y = module2.parameter("y");
			final Symbol x = module2.parameter("x");
			module2.execute(new Admit(Module.equality(y, x)));
		}
		
		assertEquals(module1, module2);
	}
	
}
