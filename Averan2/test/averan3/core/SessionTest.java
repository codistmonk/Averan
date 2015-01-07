package averan3.core;

import static averan3.core.Composite.*;
import static averan3.core.Session.*;
import static net.sourceforge.aprog.tools.Tools.cast;
import static net.sourceforge.aprog.tools.Tools.getThisMethodName;





import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import averan3.core.Proof.Deduction;
import averan3.io.ConsoleOutput;
import net.sourceforge.aprog.tools.Pair;



import net.sourceforge.aprog.tools.Tools;

import org.junit.Test;

/**
 * @author codistmonk (creation 2015-01-06)
 */
public final class SessionTest {
	
	@Test
	public final void test1() {
		final String deductionName = this.getClass().getName() + "." + getThisMethodName();
		
		build(deductionName, () -> {
			deduce();
			{
				suppose($("a", IMPLIES, "b"));
				suppose($("a"));
				apply(name(-2), name(-1));
				conclude();
			}
			
			deduce($($("a", IMPLIES, "b"), IMPLIES, $("a", IMPLIES, "b")));
			{
				intros();
				apply(name(-2), name(-1));
				conclude();
			}
		}, new ConsoleOutput());
	}
	
	@Test
	public final void test2() {
		final String deductionName = this.getClass().getName() + "." + getThisMethodName();
		
		build(deductionName, () -> {
			deduce($($("a", IMPLIES, "b"), IMPLIES, $($("b", IMPLIES, "c"), IMPLIES, $("a", IMPLIES, "c"))));
			{
				intros();
				check(autoDeduce());
				conclude();
			}
			
			deduce();
			{
				suppose($("a", IMPLIES, "b"));
				suppose($("b", IMPLIES, "c"));
				deduce($("a", IMPLIES, "c"));
				{
					intros();
					apply(name(-3), name(-1));
					apply(name(-3), name(-1));
					conclude();
				}
				conclude();
			}
		}, new ConsoleOutput());
	}
	
	public static final AtomicInteger autoDeduceDepth = new AtomicInteger(4); 
	
	public static final boolean autoDeduce() {
		return autoDeduce(null, goal(), autoDeduceDepth.get());
	}
	
	public static final void check(final boolean ok) {
		check(ok, "");
	}
	
	public static final void check(final boolean ok, final String message) {
		if (!ok) {
			throw new RuntimeException(message);
		}
	}
	
	public static final boolean autoDeduce(final String propositionName, final Expression<?> goal, final int depth) {
		deduce(propositionName, goal);
		{
			intros();
			
			Tools.debugPrint(justify(goal));
			
			conclude();
		}
		return false; // TODO
	}
	
	public static final List<Pair<String, Expression<?>>> justify(final Expression<?> goal) {
		List<Pair<String, Expression<?>>> result = new ArrayList<>();
		
		Deduction deduction = deduction();
		
		while (deduction != null) {
			final List<Proof> proofs = deduction.getProofs();
			
			for (int i = proofs.size() - 1; 0 <= i; --i) {
				final Proof proof = proofs.get(i);
				final Expression<?> proposition = proof.getProposition();
				
				if (justifies(proposition, goal)) {
					result.add(new Pair<>(proof.getPropositionName(), proposition.accept(Variable.BIND)));
				}
			}
			
			deduction = deduction.getParent();
		}
		
		return result;
	}
	
	public static final boolean justifies(final Expression<?> proposition, final Expression<?> goal) {
		if (proposition.accept(Variable.RESET).equals(goal.accept(Variable.RESET))) {
			return true;
		}
		
		proposition.accept(Variable.RESET);
		
		final Composite<Expression<?>> composite = cast(Composite.class, proposition);
		
		if (composite == null) {
			return false;
		}
		
		final Composite<Expression<?>> parameters = composite.getParameters();
		
		if (parameters != null && parameters.isList()) {
			return justifies(composite.getContents(), goal);
		}
		
		if (composite.getCondition() != null) {
			return justifies(composite.getConclusion(), goal);
		}
		
		return false;
	}
	
}
