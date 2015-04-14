package averan5.deductions;

import static averan4.core.AveranTools.*;
import static averan4.deductions.Standard.*;
import static net.sourceforge.aprog.tools.Tools.*;

import averan4.core.Deduction;
import averan4.core.Goal;
import averan4.core.Proof;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * @author codistmonk (creation 2015-04-13)
 */
public final class StandardTest {
	
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
	
	@Test
	public final void testJustify1() {
		build(() -> {
			supposeRewrite();
			deduceIdentity();
			deduceRecall();
			
			suppose($("a"));
			
			final Goal goal = Goal.deduce($("a"));
			
			conclude(justify(goal.getProposition()).get(0));
			
			goal.conclude();
		});
	}
	
	@Test
	public final void testJustify2() {
		build(() -> {
			supposeRewrite();
			deduceIdentity();
			deduceRecall();
			
			suppose($forall("a", "a"));
			
			final Goal goal = Goal.deduce($forall("b", "b"));
			
			conclude(justify(goal.getProposition()).get(0));
			
			goal.conclude();
		});
	}
	
	public static final List<Proof> justify(final List<Object> goal) {
		final List<Proof> result = new ArrayList<>();
		Deduction deduction = deduction();
		
		while (deduction != null) {
			final List<String> propositionNames = deduction.getPropositionNames();
			
			for (int i = propositionNames.size() - 1; 0 <= i; --i) {
				final String propositionName = propositionNames.get(i);
				final List<Object> proposition = deduction.getProposition(propositionName);
				
				if (areEqual(goal, proposition)) {
					subdeduction();
					
					bind("recall", goal);
					apply(name(-1), propositionName);
					
					return set(result, pop());
				}
			}
			
			deduction = deduction.getParent();
		}
		
		return result;
	}
	
	public static final void autoDeduce(final String propositionName, final List<Object> goal) {
		// TODO
	}
	
	public static final Deduction build(final Runnable deductionBuilder) {
		return Standard.build(getCallerMethodName(), deductionBuilder);
	}
	
}
