package averan4.deductions;

import static averan4.core.AveranTools.*;
import static averan4.deductions.Standard.*;
import static net.sourceforge.aprog.tools.Tools.*;

import org.junit.Test;

import averan4.core.Goal;
import averan4.io.Simple;

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
	
	public static final void build(final Runnable deductionBuilder) {
		push(getCallerMethodName());
		
		try {
			deductionBuilder.run();
		} catch (final Exception exception) {
			Simple.print(deduction(), 1);
			
			throw unchecked(exception);
		}
	}
	
}
