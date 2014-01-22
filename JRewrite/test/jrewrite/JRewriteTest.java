package jrewrite;

import static jrewrite.JRewriteTools.constant;
import static jrewrite.JRewriteTools.operation;
import static jrewrite.JRewriteTools.rewrite;
import static jrewrite.JRewriteTools.rule;
import static jrewrite.JRewriteTools.variable;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;

/**
 * @author codistmonk (creation 2013-12-16)
 */
public final class JRewriteTest {
	
	@Test
	public final void test1() {
		assertTrue(variable("x").matches(operation(constant("1"), "+", constant("2")), new HashMap<String, Expression>()));
		assertTrue(operation(constant("1"), "+", constant("2")).matches(operation(constant("1"), "+", constant("2")), new HashMap<String, Expression>()));
		assertFalse(operation(constant("1"), "+", constant("1")).matches(operation(constant("1"), "+", constant("2")), new HashMap<String, Expression>()));
		assertFalse(operation(variable("x"), "+", variable("x")).matches(operation(constant("1"), "+", constant("2")), new HashMap<String, Expression>()));
		assertTrue(operation(variable("x"), "+", variable("x")).matches(operation(constant("1"), "+", constant("1")), new HashMap<String, Expression>()));
	}
	
	@Test
	public final void test2() {
		final List<Expression> facts = new ArrayList<Expression>();
		final Rule commutativity = rule(
				operation(variable("x"), "=", variable("y")),
				operation(variable("y"), "=", variable("x")));
		
		facts.add(operation(constant("1"), "=", constant("2")));
		commutativity.apply(facts, facts.get(0));
		
		assertEquals("2=1", facts.get(1).toString());
	}
	
	@Test
	public final void test3() {
		final List<Expression> facts = new ArrayList<Expression>();
		
		facts.add(operation(constant("2"), "=", operation(constant("1"), "+", constant("1"))));
		facts.add(operation(constant("2"), "=", constant("2")));
		facts.add(operation(constant("2"), "=", constant("2")));
		
		rewrite(facts, 0, 1, 0);
		rewrite(facts, 0, 2, 1);
		
		assertEquals("1+1=2", facts.get(1).toString());
		assertEquals("2=1+1", facts.get(2).toString());
	}
	
}
