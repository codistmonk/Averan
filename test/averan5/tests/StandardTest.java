package averan5.tests;

import static averan5.deductions.Standard.*;
import static averan5.expressions.Expressions.*;
import static averan5.proofs.Stack.*;
import static multij.tools.Tools.*;

import averan5.deductions.Standard;
import averan5.proofs.Deduction;
import averan5.tactics.Goal;

import org.junit.Test;

/**
 * @author codistmonk (creation 2015-04-13)
 */
public final class StandardTest {
	
	@Test
	public final void testRewrite() {
		build(new Runnable() {
			
			@Override
			public final void run() {
				supposeRewrite();
				
				suppose($equality("a", "b"));
				
				final Goal goal = Goal.deduce($equality("b", "b"));
				
				rewrite(name(-1), name(-1));
				
				goal.conclude();
			}
			
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
		return build(getCallerMethodName(), deductionBuilder, 3);
	}
	
	public static final Deduction build(final String deductionName, final Runnable deductionBuilder, final int debugDepth) {
		return Standard.build(deductionName, deductionBuilder, debugDepth);
	}
	
}
