package averan3.core;

import static averan3.core.Composite.*;
import static averan3.core.Session.*;
import static net.sourceforge.aprog.tools.Tools.append;
import static net.sourceforge.aprog.tools.Tools.cast;
import static net.sourceforge.aprog.tools.Tools.getThisMethodName;

import averan3.core.Proof.Deduction;
import averan3.deductions.Standard;
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
				suppose(rule("a", "b"));
				suppose($("a"));
				apply(name(-2), name(-1));
				conclude();
			}
			
			deduce(rule(rule("a", "b"), "a", "b"));
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
			include(Standard.DEDUCTION);
			
			deduce(rule(rule("a", "b"), rule("b", "c"), rule("a", "c")));
			{
				intros();
				check(autoDeduce());
				conclude();
			}
			
			deduce();
			{
				suppose(rule("a", "b"));
				suppose(rule("b", "c"));
				deduce(rule("a", "c"));
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
	
	@Test
	public final void test3() {
		final String deductionName = this.getClass().getName() + "." + getThisMethodName();
		
		build(deductionName, () -> {
			include(Standard.DEDUCTION);
			
			deduce(rule(rule("a", "b"), rule("b", "c"), rule("a", "c")));
			{
				check(autoDeduce());
				conclude();
			}
		}, new ConsoleOutput());
	}
	
	@Test
	public final void test4() {
		final String deductionName = this.getClass().getName() + "." + getThisMethodName();
		
		build(deductionName, () -> {
			include(Standard.DEDUCTION);
			
			deduce(rule(rule("a", "b"), rule("b", "c"), rule("c", "d"), rule("a", "d")));
			{
				check(autoDeduce());
				conclude();
			}
		}, new ConsoleOutput());
	}
	
	@Test
	public final void test5() {
		final String deductionName = this.getClass().getName() + "." + getThisMethodName();
		
		build(deductionName, () -> {
			include(Standard.DEDUCTION);
			
			{
				final Variable $x = new Variable("x");
				
				suppose($(forall($x), rule($($x, " is real"), $($($x, "+", $x), " is real"))));
			}
			
			check(autoDeduce($(rule($("0", " is real"), $($("0", "+", "0"), " is real")))));
			
			{
				final Variable $x = new Variable("x");
				final Variable $y = new Variable("y");
				final Variable $m = new Variable("m");
				final Variable $n = new Variable("n");
				final Variable $o = new Variable("o");
				
				suppose($(forall($x, $y, $m, $n, $o), rule($($x, " is matrix ", $m, "×", $n),
						$($y, " is matrix ", $n, "×", $o),
						$($($x, $y), " is matrix ", $m, "×", $o))));
			}
			
			{
				autoDeduce(rule($("a", " is matrix ", "i", "×", "j"),
						$("b", " is matrix ", "j", "×", "k"),
						$($("a", "b"), " is matrix ", "i", "×", "k")));
			}
			
			{
				autoDeduce(rule($("a", " is matrix ", "i", "×", "j"),
						$("b", " is matrix ", "j", "×", "k"),
						$($("a", "b"), " is matrix ", new Variable("m?"), "×", new Variable("n?"))));
			}
		}, new ConsoleOutput());
	}
	
	public static final AtomicInteger autoDeduceDepth = new AtomicInteger(5); 
	
	public static final Composite<Expression<?>> rule(final Object condition0, final Object conclusion0, final Object... moreConclusions) {
		final Composite<Expression<?>> result = $$(condition0, IMPLIES, conclusion0);
		Composite<Expression<?>> end = result;
		
		for (final Object conclusion : moreConclusions) {
			final Composite<Expression<?>> newEnd = $$(end.removeLast(), IMPLIES, conclusion); 
			end.add(newEnd);
			end = newEnd;
		}
		
		return result;
	}
	
	public static final void check(final boolean ok) {
		check(ok, "");
	}
	
	public static final void check(final boolean ok, final String message) {
		if (!ok) {
			throw new RuntimeException(message);
		}
	}
	
	public static final boolean autoDeduce() {
		return autoDeduce(null, goal());
	}
	
	public static final boolean autoDeduce(final String propositionName) {
		return autoDeduce(propositionName, goal());
	}
	
	public static final boolean autoDeduce(final Expression<?> goal) {
		return autoDeduce(null, goal);
	}
	
	public static final boolean autoDeduce(final String propositionName, final Expression<?> goal) {
		return autoDeduce(propositionName, goal, autoDeduceDepth.get());
	}
	
	public static final boolean autoDeduce(final int depth) {
		return autoDeduce(null, goal(), depth);
	}
	
	public static final boolean autoDeduce(final String propositionName, final int depth) {
		return autoDeduce(propositionName, goal(), depth);
	}
	
	public static final boolean autoDeduce(final Expression<?> goal, final int depth) {
		return autoDeduce(null, goal, depth);
	}
	
	public static final boolean autoDeduce(final String propositionName, final Expression<?> goal, final int depth) {
		if (depth <= 0) {
			return false;
		}
		
		deduce(propositionName, goal);
		{
			intros();
			
			use_justifications:
			{
				Tools.debugPrint(goal());
				for (final Justification justification : justify(goal())) {
					Tools.debugPrint(justification);
					String justificationName = justification.getPropositionName();
					
					deduce();
					subdeduction:
					{
						for (final Justification.Step step : justification.getSteps()) {
							Tools.debugPrint(step);
							
							if (step instanceof Justification.Recall) {
								apply("recall", justificationName);
							}
							
							if (step instanceof Justification.Apply) {
								if (autoDeduce(((Justification.Apply) step).getCondition(), depth - 1)) {
									apply(justificationName, name(-1));
									justificationName = name(-1);
								} else {
									cancel();
									break subdeduction;
								}
							}
							
							if (step instanceof Justification.Bind) {
								bind(justificationName, ((Justification.Bind) step).getValues().toArray(new Expression[0]));
							}
						}
						
						conclude();
						
						break use_justifications;
					}
					
				}
				
				cancel();
				
				return false;
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
				final Justification.Step[] steps = findSteps(proposition, goal);
				
				if (0 < steps.length) {
					result.add(new Justification(proof.getPropositionName(), steps, proposition.accept(Variable.BIND)));
				}
			}
			
			deduction = deduction.getParent();
		}
		
		return result;
	}
	
	public static final Justification.Step[] findSteps(final Expression<?> proposition, final Expression<?> goal, final Justification.Step... steps) {
		if (proposition.accept(Variable.RESET).equals(goal.accept(Variable.RESET))) {
			return append(steps, new Justification.Recall());
		}
		
		proposition.accept(Variable.RESET);
		
		@SuppressWarnings("unchecked")
		final Composite<Expression<?>> composite = cast(Composite.class, proposition);
		
		if (composite == null) {
			return Justification.NOTHING;
		}
		
		final Composite<Expression<?>> parameters = composite.getParameters();
		
		if (parameters != null && parameters.isList()) {
			return findSteps(composite.getContents(), goal, append(steps,
					new Justification.Bind(parameters)));
		}
		
		if (composite.getCondition() != null) {
			return findSteps(composite.getConclusion(), goal, append(steps,
					new Justification.Apply(composite.getCondition())));
		}
		
		proposition.accept(Variable.RESET);
		
		return Justification.NOTHING;
	}
	
	public static final List<Expression<?>> extractValues(final Composite<Expression<?>> parameters) {
		final List<Expression<?>> result = new ArrayList<>();
		final int n = parameters.getListSize();
		
		for (int i = 1; i < n; ++i) {
			final Variable parameter = (Variable) parameters.getListElement(i);
			result.add(parameter.getMatch() != null ? parameter.getMatch() : null);
		}
		
		return result;
	}
	
	/**
	 * @author codistmonk (creation 2015-01-07)
	 */
	public static final class Justification implements Serializable {
		
		private final String propositionName;
		
		private final Step[] steps;
		
		private final Expression<?> boundProposition;
		
		public Justification(final String propositionName, final Step[] steps,
				final Expression<?> boundProposition) {
			this.propositionName = propositionName;
			this.steps = steps;
			this.boundProposition = boundProposition;
		}
		
		public final String getPropositionName() {
			return this.propositionName;
		}
		
		public final Step[] getSteps() {
			return this.steps;
		}
		
		public final Expression<?> getBoundProposition() {
			return this.boundProposition;
		}
		
		@Override
		public final String toString() {
			return "(" + this.getPropositionName() + ": " + proposition(this.getPropositionName()) + " " + Arrays.toString(this.getSteps()) + " " + this.getBoundProposition() + ")";
		}
		
		private static final long serialVersionUID = -3897122482315195936L;
		
		public static final Step[] NOTHING = {};
		
		/**
		 * @author codistmonk (creation 2015-01-07)
		 */
		public static abstract interface Step extends Serializable {
			// NOP
		}
		
		/**
		 * @author codistmonk (creation 2015-01-07)
		 */
		public static final class Recall implements Step {
			
			private static final long serialVersionUID = -391482341455285510L;
			
			@Override
			public final String toString() {
				return "Recall";
			}
			
		}
		
		/**
		 * @author codistmonk (creation 2015-01-07)
		 */
		public static final class Apply implements Step {
			
			private final Expression<?> condition;
			
			private Expression<?> boundCondition;
			
			public Apply(final Expression<?> condition) {
				this.condition = condition;
			}
			
			public final Expression<?> getCondition() {
				if (this.boundCondition == null) {
					this.boundCondition = this.condition.accept(Variable.BIND);
				}
				
				return this.boundCondition;
			}
			
			@Override
			public final String toString() {
				return "Apply on " + this.getCondition();
			}
			
			private static final long serialVersionUID = -882415070219823810L;
			
		}
		
		/**
		 * @author codistmonk (creation 2015-01-07)
		 */
		public static final class Bind implements Step {
			
			private final Composite<Expression<?>> parameters;
			
			private List<Expression<?>> values;
			
			public Bind(final Composite<Expression<?>> values) {
				this.parameters = values;
			}
			
			public final List<Expression<?>> getValues() {
				if (this.values == null) {
					this.values = extractValues(this.parameters);
				}
				
				return this.values;
			}
			
			@Override
			public final String toString() {
				return "Bind " + this.getValues();
			}
			
			private static final long serialVersionUID = -2178774924606129974L;
			
		}
		
	}
	
}
