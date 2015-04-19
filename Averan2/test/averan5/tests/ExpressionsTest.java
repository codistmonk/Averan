package averan5.tests;

import static averan5.expressions.Expressions.*;
import static org.junit.Assert.*;

import org.junit.Test;

import averan5.expressions.Unifier;

/**
 * @author codistmonk (creation 2015-04-19)
 */
public final class ExpressionsTest {
	
	@Test
	public final void testUnify1() {
		assertNotNull(unify("a", "a"));
		assertNull(unify("a", "b"));
		assertNotNull(unify($forall("a", "a"), $forall("b", "b")));
		assertNotNull(unify($equality($forall("a", "a"), $forall("a", "a")), $equality($forall("b", "b"), $forall("b", "b"))));
		assertNotNull(unify($forall("a", "a"), $forall("b", "a")));
	}
	
	@Test
	public final void testUnify2() {
		assertNull(unify($new("a"), $new("a")));
		assertNull(unify($new("a"), $new("b")));
		
		{
			final Object a = $new("a");
			final Object b = $new("b");
			
			assertNotNull(unify($forall(a, a), $forall(b, b)));
		}
	}
	
	@Test
	public final void testUnify3() {
		assertNotNull(unify($equality("a", "d"), $equality("a", new Unifier())));
		assertNotNull(unify($equality("a", new Unifier()), $equality("a", "d")));
	}
	
}
