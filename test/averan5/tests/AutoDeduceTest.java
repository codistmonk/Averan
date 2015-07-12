package averan5.tests;

import static averan5.deductions.Standard.*;
import static averan5.expressions.Expressions.*;
import static averan5.proofs.Stack.*;
import static averan5.tactics.AutoDeduce.autoDeduce;
import static averan5.tests.StandardTest.build;
import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author codistmonk (creation 2015-04-18)
 */
public final class AutoDeduceTest {
	
	@Test
	public final void testAutoDeduce1() {
		build(new Runnable() {
			
			@Override
			public final void run() {
				supposeRewrite();
				deduceIdentity();
				deduceRecall();
				
				suppose($("a"));
				
				assertTrue(autoDeduce($("a")));
			}
			
		});
	}
	
	@Test
	public final void testAutoDeduce2() {
		build(() -> {
			supposeRewrite();
			deduceIdentity();
			deduceRecall();
			
			suppose($forall("a", "a"));
			
			assertTrue(autoDeduce($forall("b", "b")));
		});
	}
	
	@Test
	public final void testAutoDeduce3() {
		build(() -> {
			suppose($rule("a", "b"));
			suppose($("a"));
			
			assertTrue(autoDeduce($("b")));
		});
	}
	
	@Test
	public final void testAutoDeduce4() {
		build(() -> {
			suppose($rule("a", "b"));
			suppose($rule("b", "c"));
			suppose($("a"));
			
			assertTrue(autoDeduce($("c")));
		});
	}
	
	@Test
	public final void testAutoDeduce5() {
		build(() -> {
			suppose($rule("a", "b", "c"));
			suppose($("a"));
			suppose($("b"));
			
			assertTrue(autoDeduce($("c")));
		});
	}
	
	@Test
	public final void testAutoDeduce6() {
		build(() -> {
			supposeRewrite();
			deduceIdentity();
			deduceRecall();
			
			assertFalse(autoDeduce($("b")));
		});
	}
	
	@Test
	public final void testAutoDeduce7() {
		build(() -> {
			suppose($forall("a", "a"));
			
			assertTrue(autoDeduce($("b")));
		});
	}
	
	@Test
	public final void testAutoDeduce8() {
		build(() -> {
			suppose($forall("b", $rule("a", "b")));
			suppose($("a"));
			
			assertTrue(autoDeduce($("c")));
		});
	}
	
	@Test
	public final void testAutoDeduce9() {
		build(() -> {
			suppose($forall("b", $rule($equality("a", "b"), "c")));
			suppose($equality("a", "d"));
			
			assertTrue(autoDeduce($("c")));
		});
	}
	
	@Test
	public final void testAutoDeduce10() {
		build(() -> {
			suppose($rule($forall("b", $equality("a", "b")), "c"));
			suppose($forall("b", $equality("a", "b")));
			
			assertTrue(autoDeduce($("c")));
		});
	}
	
	@Test
	public final void testAutoDeduce11() {
		build(() -> {
			final Object a = "a";
			final Object b = "b";
			final Object c = "c";
			
			suppose($forall(a, $forall(b, $forall(c,
					$rule($equality(a, b), $equality(b, c), $equality(a, c))))));
			suppose($equality("d", "e"));
			suppose($equality("e", "f"));
			
			assertTrue(autoDeduce($equality("d", "f")));
		});
	}
	
}
