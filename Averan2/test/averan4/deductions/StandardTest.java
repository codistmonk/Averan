package averan4.deductions;

import static averan4.core.AveranTools.*;
import static averan4.deductions.Standard.*;
import static net.sourceforge.aprog.tools.Tools.*;

import org.junit.Before;
import org.junit.Test;

import averan4.core.Goal;
import averan4.io.Simple;

/**
 * @author codistmonk (creation 2015-04-13)
 */
public final class StandardTest {
	
	@Before
	public final void beforeEachTest() {
		push(getCallerMethodName());
	}
	
	@Test
	public final void testRewriteLeft() {
		try {
			supposeRewriteLeft();
			
			suppose($equality("a", "b"));
			
			final Goal goal = Goal.deduce($equality("b", "b"));
			
			rewriteLeft(name(-1), name(-1));
			
			goal.conclude();
		} catch (final Exception exception) {
			Simple.print(deduction(), 1);
			
			throw new AssertionError("Test failed", exception);
		}
	}
	
	@Test
	public final void testDeduceIdentity() {
		try {
			supposeRewriteLeft();
			deduceIdentity();
			
			final Goal goal = Goal.deduce($equality("a", "a"));
			
			bind("identity", $("a"));
			
			goal.conclude();
		} catch (final Exception exception) {
			Simple.print(deduction(), 1);
			
			throw new AssertionError("Test failed", exception);
		}
	}
	
	@Test
	public final void testDeduceRecall() {
		try {
			supposeRewriteLeft();
			deduceIdentity();
			deduceRecall();
			
			suppose($("a"));
			
			final Goal goal = Goal.deduce($rule("a", "a"));
			
			bind("recall", $("a"));
			
			goal.conclude();
		} catch (final Exception exception) {
			Simple.print(deduction(), 1);
			
			throw new AssertionError("Test failed", exception);
		}
	}
	
}
