package averan5.deductions;

import static averan5.core.AveranTools.*;
import static averan5.deductions.AutoDeduce.autoDeduce;
import static averan5.deductions.AutoDeduce.Unify.unify;
import static averan5.deductions.Standard.*;
import static averan5.deductions.StandardTest.build;
import static org.junit.Assert.*;

import averan5.deductions.AutoDeduce.Unifier;

import org.junit.Test;

/**
 * @author codistmonk (creation 2015-04-18)
 */
public final class AutoDeduceTest {
	
	@Test
	public final void testUnify1() {
		assertNotNull(unify($equality("a", "d"), $equality("a", new Unifier())));
		assertNotNull(unify($equality("a", new Unifier()), $equality("a", "d")));
	}
	
	@Test
	public final void testJustify1() {
		build(() -> {
			supposeRewrite();
			deduceIdentity();
			deduceRecall();
			
			suppose($("a"));
			
			assertTrue(autoDeduce($("a")));
		});
	}
	
	@Test
	public final void testJustify2() {
		build(() -> {
			supposeRewrite();
			deduceIdentity();
			deduceRecall();
			
			suppose($forall("a", "a"));
			
			assertTrue(autoDeduce($forall("b", "b")));
		});
	}
	
	@Test
	public final void testJustify3() {
		build(() -> {
			suppose($rule("a", "b"));
			suppose($("a"));
			
			assertTrue(autoDeduce($("b")));
		});
	}
	
	@Test
	public final void testJustify4() {
		build(() -> {
			suppose($rule("a", "b"));
			suppose($rule("b", "c"));
			suppose($("a"));
			
			assertTrue(autoDeduce($("c")));
		});
	}
	
	@Test
	public final void testJustify5() {
		build(() -> {
			suppose($rule("a", "b", "c"));
			suppose($("a"));
			suppose($("b"));
			
			assertTrue(autoDeduce($("c")));
		});
	}
	
	@Test
	public final void testJustify6() {
		build(() -> {
			supposeRewrite();
			deduceIdentity();
			deduceRecall();
			
			assertFalse(autoDeduce($("b")));
		});
	}
	
	@Test
	public final void testJustify7() {
		build(() -> {
			suppose($forall("a", "a"));
			
			assertTrue(autoDeduce($("b")));
		});
	}
	
	@Test
	public final void testJustify8() {
		build(() -> {
			suppose($forall("b", $rule("a", "b")));
			suppose($("a"));
			
			assertTrue(autoDeduce($("c")));
		});
	}
	
	@Test
	public final void testJustify9() {
		build(() -> {
			suppose($forall("b", $rule($equality("a", "b"), "c")));
			suppose($equality("a", "d"));
			
			assertTrue(autoDeduce($("c")));
		});
	}
	
}
