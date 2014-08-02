package jrewrite3;

import static jrewrite3.Module.EQUAL;
import static jrewrite3.ModuleTest.$;
import static org.junit.Assert.*;

import jrewrite3.Module.Symbol;

import org.junit.Test;

/**
 * @author codistmonk (creation 2014)
 *
 */
public final class RewriterTest {
	
	@Test
	public final void test1() {
		final Symbol variable = $("x");
		final Symbol value = $("42");
		final Rewriter rewriter = new Rewriter().rewrite(variable, value);
		
		assertEquals((Object) $("x"), (Object) $("x"));
		assertEquals(value, variable.accept(rewriter));
	}
	
	@Test
	public final void test2() {
		final Expression definitionOf2 = $("2", EQUAL, $("1", "+", "1"));
		final Rewriter rewriter = new Rewriter().rewrite($("1", "+", "1"), $("2"));
		
		assertEquals($("2", EQUAL, "2"), definitionOf2.accept(rewriter));
	}
	
}
