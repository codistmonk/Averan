package jrewrite2;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author codistmonk (creation 2014-06-10)
 */
public final class ContextTest {
	
	@Test
	public final void test1() {
		final Context context = new Context();
		
		context.printTo(System.out);
	}
	
}
