package averan4.core;

import static averan4.core.Session.Stack.*;
import static averan4.core.Symbol.symbol;

import org.junit.Test;

import averan4.io.ConsoleOutput;
import averan4.io.SessionExporter;

/**
 * @author codistmonk (creation 2014-12-27)
 */
public final class SessionTest {
	
	@Test
	public final void test1() {
		pushSession(new Session());
		
		try {
			deduce("test");
			{
				final Variable varX = new Variable("X");
				
				deduce("tautologyA", new Module().addCondition(null, varX).addFact(null, varX, null));
				{
					final Expression<?> x = introduce();
					
					introduce();
					
					substitute(x);
					rewrite(name(-1), name(-1));
					rewrite(name(-3), name(-1));
				}
				
				deduce(null);
				{
					suppose(symbol("Y"));
					apply("tautologyA", name(-1));
					acceptModule();
				}
			}
		} finally {
			SessionExporter.export(popSession(), new ConsoleOutput());
		}
	}
	
}
