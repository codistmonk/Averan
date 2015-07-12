package averan.draft1.core;

import static averan.draft1.core.ExpressionTools.$;
import static averan.draft1.core.Module.EQUAL;
import static org.junit.Assert.*;

import org.junit.Test;

import averan.draft1.core.Expression;
import averan.draft1.core.Rewriter;
import averan.draft1.core.Module.Symbol;

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
