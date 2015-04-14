package averan5.deductions;

import static averan5.core.AveranTools.*;
import static averan5.deductions.Standard.*;
import static net.sourceforge.aprog.tools.Tools.*;

import averan5.core.Deduction;
import averan5.core.Goal;
import averan5.core.Proof;

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
	
	@Test
	public final void testJustify3() {
		build(() -> {
			suppose($rule("a", "b"));
			suppose($("a"));
			
			final Goal goal = Goal.deduce($("b"));
			
			conclude(justify(goal.getProposition()).get(0));
			
			goal.conclude();
		});
	}
	
	public static final List<Proof> justify(final Object goal) {
		final List<Proof> result = new ArrayList<>();
		Deduction deduction = deduction();
		
		while (deduction != null) {
			final List<String> propositionNames = deduction.getPropositionNames();
			
			for (int i = propositionNames.size() - 1; 0 <= i; --i) {
				final String propositionName = propositionNames.get(i);
				final Object proposition = deduction.getProposition(propositionName);
				
				if (areEqual(goal, proposition)) {
					result.add(new Recall(propositionName));
				}
				
				if (isRule(proposition) && areEqual(goal, conclusion(proposition))) {
					final List<Proof> conditionJustifications = justify(condition(proposition));
					
					if (!conditionJustifications.isEmpty()) {
						final Proof justification = conditionJustifications.get(0);
						final Recall recall = cast(Recall.class, justification);
						
						subdeduction();
						
						if (recall == null) {
							conclude(justification);
							apply(propositionName, name(-1));
						} else {
							apply(propositionName, recall.getPropositionName());
						}
						
						return set(result, pop());
					}
				}
				
				// \/X P
				// \/Y Q
			}
			
			deduction = deduction.getParent();
		}
		
		return result;
	}
	
	/**
	 * @author codistmonk (creation 2015-04-14)
	 */
	public static final class Recall implements Proof {
		
		private final String propositionName;
		
		public Recall(final String propositionName) {
			this.propositionName = propositionName;
		}
		
		public final String getPropositionName() {
			return this.propositionName;
		}
		
		@Override
		public final Deduction concludeIn(final Deduction context) {
			push(context);
			
			try {
				bind("recall", context.getProposition(this.getPropositionName()));
				apply(name(-1), this.getPropositionName());
			} finally {
				pop(context);
			}
			
			return context;
		}
		
		private static final long serialVersionUID = 3450261358246212849L;
		
	}
	
	public static final void autoDeduce(final String propositionName, final List<Object> goal) {
		// TODO
	}
	
	public static final Deduction build(final Runnable deductionBuilder) {
		return Standard.build(getCallerMethodName(), deductionBuilder);
	}
	
}
