package averan3.core;

import static averan3.core.Composite.*;
import static averan3.core.Session.*;
import static net.sourceforge.aprog.tools.Tools.append;
import static net.sourceforge.aprog.tools.Tools.array;
import static net.sourceforge.aprog.tools.Tools.cast;
import static net.sourceforge.aprog.tools.Tools.getThisMethodName;
import averan3.core.Proof.Deduction;
import averan3.io.ConsoleOutput;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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
		if (depth <= 0) {
			return false;
		}
		
		deduce(propositionName, goal);
		{
			intros();
			
			final List<Justification> justifications = justify(goal);
			
			Tools.debugPrint(justifications);
			
			for (final Justification justification : justifications) {
				
			}
			
			conclude();
		}
		
		return true;
	}
	
	public static final List<Justification> justify(final Expression<?> goal) {
		List<Justification> result = new ArrayList<>();
		
		Deduction deduction = deduction();
		
		while (deduction != null) {
			final List<Proof> proofs = deduction.getProofs();
			
			for (int i = proofs.size() - 1; 0 <= i; --i) {
				final Proof proof = proofs.get(i);
				final Expression<?> proposition = proof.getProposition();
				final Object[] steps = findSteps(proposition, goal);
				
				if (0 < steps.length) {
					result.add(new Justification(proof.getPropositionName(), steps, proposition.accept(Variable.BIND)));
				}
			}
			
			deduction = deduction.getParent();
		}
		
		return result;
	}
	
	public static final Object[] findSteps(final Expression<?> proposition, final Expression<?> goal, final Object... steps) {
		if (proposition.accept(Variable.RESET).equals(goal.accept(Variable.RESET))) {
			return append(steps, EQUALS);
		}
		
		proposition.accept(Variable.RESET);
		
		@SuppressWarnings("unchecked")
		final Composite<Expression<?>> composite = cast(Composite.class, proposition);
		
		if (composite == null) {
			return array();
		}
		
		final Composite<Expression<?>> parameters = composite.getParameters();
		
		if (parameters != null && parameters.isList()) {
			return findSteps(composite.getContents(), goal, append(steps, FORALL));
		}
		
		if (composite.getCondition() != null) {
			return findSteps(composite.getConclusion(), goal, append(steps, IMPLIES));
		}
		
		return array();
	}
	
	/**
	 * @author codistmonk (creation 2015-01-07)
	 */
	public static final class Justification implements Serializable {
		
		private final String propositionName;
		
		private final Object[] steps;
		
		private final Expression<?> boundProposition;
		
		public Justification(final String propositionName, final Object[] steps,
				final Expression<?> boundProposition) {
			this.propositionName = propositionName;
			this.steps = steps;
			this.boundProposition = boundProposition;
		}
		
		public final String getPropositionName() {
			return this.propositionName;
		}
		
		public final Object[] getSteps() {
			return this.steps;
		}
		
		public final Expression<?> getBoundProposition() {
			return this.boundProposition;
		}
		
		@Override
		public final String toString() {
			return "(" + this.getPropositionName() + " " + Arrays.toString(this.getSteps()) + " " + this.getBoundProposition() + ")";
		}
		
		private static final long serialVersionUID = -3897122482315195936L;
		
	}
	
}
