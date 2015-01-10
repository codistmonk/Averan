package averan3.deductions;

import static averan3.core.Composite.*;
import static averan3.core.Session.*;
import static net.sourceforge.aprog.tools.Tools.append;
import static net.sourceforge.aprog.tools.Tools.cast;
import static net.sourceforge.aprog.tools.Tools.join;
import averan3.core.Composite;
import averan3.core.Expression;
import averan3.core.Proof;
import averan3.core.Proof.Deduction.Instance;
import averan3.core.Proof.Deduction.Supposition;
import averan3.core.Variable;
import averan3.core.Proof.Deduction;
import averan3.io.HTMLOutput;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import net.sourceforge.aprog.tools.IllegalInstantiationException;
import net.sourceforge.aprog.tools.Tools;

/**
 * @author codistmonk (creation 2015-01-07)
 */
public final class Standard {
	
	private Standard() {
		throw new IllegalInstantiationException();
	}
	
	public static final AtomicInteger autoDeduceDepth = new AtomicInteger(4); 
	
	public static final Deduction DEDUCTION = build(Standard.class.getName(), new Runnable() {
		
		@Override
		public final void run() {
			setupIdentitySymmetryRecall();
			
			{
				final Variable $E = new Variable("E");
				final Variable $F = new Variable("F");
				final Variable $X = new Variable("X");
				final Variable $Y = new Variable("Y");
				final Variable $T = new Variable("..");
				
				suppose("bind1",
						$(forall($E, $X, $T, $Y, $F),
								rule($($$(FORALL, $($X, $T)), $E),
										equality($($E, list(equality($X, $Y)), list()), $F),
										$($$(FORALL, $T), $F))));
			}
			
			{
				final Variable $E = new Variable("E");
				final Variable $F = new Variable("F");
				final Variable $X = new Variable("X");
				final Variable $Y = new Variable("Y");
				final Variable $I = new Variable("I");
				final Variable $T = new Variable("..");
				
				suppose("rewrite1",
						$(forall($E, $X, $Y, $T, $I, $F),
								rule($E,
										equality($X, $Y),
										equality($($E, $$(equality($X, $Y), $T), $I), $F),
										$F)));
			}
			
			{
				final Variable $X = variable("X");
				final Variable $Y = variable("Y");
				
				deduce("left_elimination_of_equality",
						$(forall($X, $Y), rule($X, equality($X, $Y), $Y)));
				{
					intros();
					rewrite(name(-2), name(-1));
					conclude();
				}
			}
			
			{
				final Variable $X = variable("X");
				final Variable $Y = variable("Y");
				
				deduce("right_elimination_of_equality",
						$(forall($X, $Y), rule($Y, equality($X, $Y), $X)));
				{
					intros();
					rewriteRight(name(-2), name(-1));
					conclude();
				}
			}
			
			{
				final Variable $X = variable("X");
				final Variable $Y = variable("Y");
				
				suppose("left_introduction_of_conjunction",
						$(forall($X, $Y), rule($X, conjunction($X, $Y))));
			}
			
			{
				final Variable $X = variable("X");
				final Variable $Y = variable("Y");
				
				suppose("right_introduction_of_conjunction",
						$(forall($X, $Y), rule($Y, conjunction($X, $Y))));
			}
			
			{
				final Variable $X = variable("X");
				final Variable $Y = variable("Y");
				
				suppose("left_elimination_of_conjunction",
						$(forall($X, $Y), rule(conjunction($X, $Y), $X)));
			}
			
			{
				final Variable $X = variable("X");
				final Variable $Y = variable("Y");
				
				suppose("right_elimination_of_conjunction",
						$(forall($X, $Y), rule(conjunction($X, $Y), $Y)));
			}
			
			{
				final Variable $X = variable("X");
				final Variable $Y = variable("Y");
				
				check(autoDeduce("commutativity_of_conjunction",
						$(forall($X, $Y), rule(conjunction($X, $Y), conjunction($Y, $X))), 3));
			}
			
			{
				final Variable $X = variable("X");
				final Variable $Y = variable("Y");
				
				suppose("left_introduction_of_disjunction",
						$(forall($X, $Y), rule($X, disjunction($X, $Y))));
			}
			
			{
				final Variable $X = variable("X");
				final Variable $Y = variable("Y");
				
				suppose("right_introduction_of_disjunction",
						$(forall($X, $Y), rule($Y, disjunction($X, $Y))));
			}
			
			{
				final Variable $X = variable("X");
				final Variable $Y = variable("Y");
				final Variable $Z = variable("Z");
				
				suppose("elimination_of_disjunction",
						$(forall($X, $Y, $Z), rule(rule($X, $Z), rule($Y, $Z), rule(disjunction($X, $Y), $Z))));
			}
			
			{
				final Variable $X = variable("X");
				final Variable $Y = variable("Y");
				
				check(autoDeduce("commutativity_of_disjunction",
						$(forall($X, $Y), rule(disjunction($X, $Y), disjunction($Y, $X))), 3));
			}
		}
		
	}, new HTMLOutput());
	
	public static final void setupIdentitySymmetryRecall() {
		deduce("identity");
		{
			final Variable x = introduce("x");
			
			substitute($$(x, $(), $()));
			rewrite(name(-1), name(-1));
			conclude();
		}
		
		deduce("symmetry_of_equality");
		{
			final Variable x = introduce("x");
			final Variable y = introduce("y");
			
			suppose($(x, EQUALS, y));
			bind("identity", x);
			rewrite(name(-1), name(-2), 0);
			conclude();
		}
		
		deduce("recall");
		{
			final Variable p = introduce("P");
			
			suppose(p);
			bind("identity", p);
			rewrite(name(-2), name(-1));
			conclude();
		}
	}
	
	public static final Composite<?> conjunction(final Object... expressions) {
		return binaryOperation("⋀", expressions);
	}
	
	public static final Composite<?> disjunction(final Object... expressions) {
		return binaryOperation("⋁", expressions);
	}
	
	public static final Composite<?> membership(final Object element, final Object set) {
		return binaryOperation("∈", element, set);
	}
	
	public static final void rewriteRight(final String targetName, final String equalityName, final int... indices) {
		rewriteRight(null, targetName, equalityName, indices);
	}
	
	public static final void rewriteRight(final String propositionName, final String targetName, final String equalityName, final int... indices) {
		deduce(propositionName);
		{
			apply("symmetry_of_equality", equalityName);
			rewrite(targetName, name(-1), indices);
			conclude("By right-rewriting (" + equalityName + ") in (" + targetName + ") at indices " + Arrays.toString(indices));
		}
	}
	
	public static final void bind1(final String targetName, final Expression<?> value) {
		bind1(null, targetName, value);
	}
	
	public static final void bind1(final String propositionName, final String targetName, final Expression<?> value) {
		deduce(propositionName);
		{
			final Composite<?> target = proposition(targetName);
			final Variable parameter = (Variable) target.getParameters().getListElement(1);
			
			apply("bind1", targetName);
			substitute($$(target.get(1), $$().append($(parameter , EQUALS, value)), $()));
			apply(name(-2), name(-1));
			conclude("By binding " + parameter.getName() + " with " + value + " in (" + targetName + ")");
		}
	}
	
	public static final void rewrite1(final String targetName, final String equalityName, final int... indices) {
		rewrite1(null, targetName, equalityName, indices);
	}
	
	@SuppressWarnings("unchecked")
	public static final void rewrite1(final String propositionName, final String targetName, final String equalityName, final int... indices) {
		deduce(propositionName);
		{
			apply("rewrite1", targetName);
			apply(name(-1), equalityName);
			
			final Composite<Expression<?>> block = proposition(name(-1));
			
			block.getParameters().getListElement(1).equals(list());
			block.getParameters().getListElement(2).equals(indices(indices));
			
			substitute((Composite<Expression<?>>) block.getContents().get(0).get(0).accept(Variable.BIND));
			apply(name(-2), name(-1));
			conclude("By rewriting (" + targetName + ") using (" + equalityName + ") at indices " + Arrays.toString(indices));
		}
	}
	
	public static final Composite<Expression<?>> indices(final int... indices) {
		final Composite<Expression<?>> result = list();
		
		for (final int index : indices) {
			result.append($(index));
		}
		
		return result;
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
	
	public static final int callDepth() {
		String methodId = Tools.debug(Tools.DEBUG_STACK_OFFSET + 1);
		methodId = methodId.substring(0, methodId.indexOf('('));
		int result = 0;
		
		for (final StackTraceElement element : Thread.currentThread().getStackTrace()) {
			if (element.toString().startsWith(methodId)) {
				++result;
			}
		}
		
		return result;
	}
	
	public static final boolean autoDeduce(final String propositionName, final Expression<?> goal, final int depth) {
		if (depth <= 0) {
			return false;
		}
		
		final String indent = join("", Collections.nCopies(callDepth() - 1, "    "));
		int toConclude = 0;
		
		deduce(propositionName, goal);
		++toConclude;
		{
			intros();
			
			while (goal() instanceof Composite<?> && ((Composite<?>) goal()).getParameters() != null) {
				deduce((Expression<?>) goal());
				++toConclude;
				intros();
			}
			
			use_justifications:
			{
				log(indent, "???");
				log(indent, "TRYING TO PROVE ", goal());
				
				final List<Justification> justifications = justify(goal());
				
				for (final Justification justification : justifications) {
					log(indent, "???");
					log(indent, "TRYING TO USE", justification);
					
					String justificationName = justification.getPropositionName();
					
					deduce();
					subdeduction:
					{
						for (final Justification.Step step : justification.getSteps()) {
							step.bind();
							
							log(indent, "STEP", step);
							
							if (step instanceof Justification.Recall &&
									(!justificationName.equals(name(-1)) || proof(-1) instanceof Supposition)) {
								apply("recall", justificationName);
								log(indent, "GENERATED", name(-1), proof(-1).getProposition());
							}
							
							if (step instanceof Justification.Apply) {
								if (autoDeduce(null, ((Justification.Apply) step).getCondition(), depth - 1)) {
									log(indent, "GENERATED", name(-1), proof(-1).getProposition());
									try {
										// FIXME failure should be predicted before calling apply
										apply(justificationName, name(-1));
									} catch (final Exception exception) {
										Tools.debugError(exception);
										cancel();
										break subdeduction;
									}
									log(indent, "GENERATED", name(-1), proof(-1).getProposition());
									justificationName = name(-1);
								} else {
									cancel();
									break subdeduction;
								}
							}
							
							if (step instanceof Justification.Bind) {
								bind(justificationName, ((Justification.Bind) step).getValues().toArray(new Expression[0]));
								justificationName = name(-1);
								log(indent, "GENERATED", name(-1), proof(-1).getProposition());
							}
						}
						
						if (!deduction().canConclude()) {
							log(indent, "XXX");
							log(indent, "FAILED TO USE", justification);
							
							cancel();
							
							break use_justifications;
						}
						
						log(indent, "USED", justification);
						
						//simplification:
						{
							final Deduction deduction = deduction();
							
							if (deduction.getRootParameters() == null && deduction.getProofs().size() == 1) {
								cancel();
								deduction.getProofs().get(0).copyFor(deduction(), deduction.getPropositionName()).conclude();
							} else {
								conclude();
							}
						}
						
						if (deduction().canConclude()) {
							break use_justifications;
						}
					}
					
					log(indent, "XXX");
					log(indent, "FAILED TO USE", justification);
				}
			}
			
			if (!deduction().canConclude()) {
				log(indent, "XXX");
				log(indent, "FAILED TO PROVE", goal());
				
				for (int i = 0; i < toConclude; ++i) {
					cancel();
				}
				
				return false;
			}
			
			log(indent, "PROVED", goal());
			
			//simplification:
			{
				final Deduction deduction = deduction();
				
				if (deduction.getRootParameters() == null && deduction.getProofs().size() == 1) {
					cancel();
					deduction.getProofs().get(0).copyFor(deduction(), deduction.getPropositionName()).conclude();
					--toConclude;
				}
			}
			
			for (int i = 0; i < toConclude; ++i) {
				conclude();
			}
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
				try {
					final Expression<?> proposition = proof.getProposition().accept(new Instance());
					final Justification.Step[] steps = findSteps(proposition, goal);
					
					if (0 < steps.length) {
						result.add(new Justification(proof.getPropositionName(), steps, proposition.accept(Variable.BIND)));
					}
				} catch (final RuntimeException exception) {
					Tools.debugError(proof.getProposition());
					throw exception;
				}
			}
			
			deduction = deduction.getParent();
		}
		
		return result;
	}
	
	public static final Justification.Step[] findSteps(final Expression<?> proposition, final Expression<?> goal, final Justification.Step... steps) {
		if (proposition.accept(Variable.RESET).equals(goal.accept(Variable.RESET))) {
			for (final Justification.Step step : steps) {
				step.bind();
			}
			
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
			
			public abstract Step bind();
			
		}
		
		/**
		 * @author codistmonk (creation 2015-01-07)
		 */
		public static final class Recall implements Step {
			
			@Override
			public final Recall bind() {
				return this;
			}
			
			@Override
			public final String toString() {
				return "Recall";
			}
			
			private static final long serialVersionUID = -391482341455285510L;
			
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
				return this.boundCondition;
			}
			
			@Override
			public final Apply bind() {
				if (this.boundCondition == null) {
					this.boundCondition = this.condition;
				}
				
				this.boundCondition = this.boundCondition.accept(Variable.BIND);
				
				return this;
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
			
			public Bind(final Composite<Expression<?>> parameters) {
				this.parameters = parameters;
			}
			
			public final List<Expression<?>> getValues() {
				return this.values;
			}
			
			@Override
			public final Bind bind() {
				if (this.values == null) {
					this.values = extractValues(this.parameters);
				} else {
					final int n = this.parameters.getListSize();
					
					for (int i = 1; i < n; ++i) {
						final Variable parameter = (Variable) this.parameters.getListElement(i);
						
						if (this.values.get(i - 1) == null && parameter.getMatch() != null) {
							this.values.set(i - 1, parameter.getMatch());
						}
					}
				}
				
				
				return this;
			}
			
			@Override
			public final String toString() {
				return "Bind " + this.parameters + " with " + this.getValues();
			}
			
			private static final long serialVersionUID = -2178774924606129974L;
			
		}
		
	}
	
}
