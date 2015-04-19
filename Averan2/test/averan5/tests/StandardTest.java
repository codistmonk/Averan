package averan5.tests;

import static averan5.deductions.Standard.*;
import static averan5.expressions.Expressions.*;
import static averan5.proofs.AveranTools.*;
import static net.sourceforge.aprog.tools.Tools.*;

import averan5.deductions.Standard;
import averan5.proofs.Deduction;
import averan5.tactics.Goal;

import org.junit.Test;

/**
 * @author codistmonk (creation 2015-04-13)
 */
public final class StandardTest {
	
//	@Test
//	public final void areEqualTest() {
//		{
//			assertTrue(areEqual("a", "a"));
//			assertFalse(areEqual("a", "b"));
//			assertTrue(areEqual($forall("a", "a"), $forall("b", "b")));
//			assertTrue(areEqual($equality($forall("a", "a"), $forall("a", "a")), $equality($forall("b", "b"), $forall("b", "b"))));
//			assertTrue(areEqual($forall("a", "a"), $forall("b", "a")));
//		}
//		
//		{
//			assertFalse(areEqual($new("a"), $new("a")));
//			assertFalse(areEqual($new("a"), $new("b")));
//			
//			{
//				final Object a = $new("a");
//				final Object b = $new("b");
//				
//				assertTrue(areEqual($forall(a, a), $forall(b, b)));
//			}
//		}
//	}
	
	@Test
	public final void testRewrite() {
		build(() -> {
			supposeRewrite();
			
			suppose($equality("a", "b"));
			
			final Goal goal = Goal.deduce($equality("b", "b"));
			
			rewrite(name(-1), name(-1));
			
			goal.conclude();
		});
	}
	
	@Test
	public final void testRewriteRight() {
		build(() -> {
			supposeRewrite();
			deduceIdentity();
			deduceCommutativityOfEquality();
			
			suppose($equality("a", "b"));
			
			final Goal goal = Goal.deduce($equality("a", "a"));
			
			rewriteRight(name(-1), name(-1));
			
			goal.conclude();
		});
	}
	
	@Test
	public final void testDeduceIdentity() {
		build(() -> {
			supposeRewrite();
			deduceIdentity();
			
			final Goal goal = Goal.deduce($equality("a", "a"));
			
			bind("identity", $("a"));
			
			goal.conclude();
		});
	}
	
	@Test
	public final void testDeduceRecall() {
		build(() -> {
			supposeRewrite();
			deduceIdentity();
			deduceRecall();
			
			suppose($("a"));
			
			final Goal goal = Goal.deduce($rule("a", "a"));
			
			bind("recall", $("a"));
			
			goal.conclude();
		});
	}
	
	public static final Deduction build(final Runnable deductionBuilder) {
		return build(getCallerMethodName(), deductionBuilder, 2);
	}
	
	public static final Deduction build(final String deductionName, final Runnable deductionBuilder, final int debugDepth) {
		return Standard.build(deductionName, deductionBuilder, debugDepth);
	}
	
}
