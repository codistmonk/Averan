package averan.draft3.deductions;

import static averan.draft3.core.Session.apply;
import static averan.draft3.core.Session.bind;
import static averan.draft3.core.Session.cancel;
import static averan.draft3.core.Session.conclude;
import static averan.draft3.core.Session.deduce;
import static averan.draft3.core.Session.deduction;
import static averan.draft3.core.Session.goal;
import static averan.draft3.core.Session.intros;
import static averan.draft3.core.Session.log;
import static averan.draft3.core.Session.name;
import static averan.draft3.core.Session.proof;
import static averan.draft3.core.Session.proposition;
import static averan.draft3.core.Session.recursionDepth;
import static multij.tools.Tools.append;
import static multij.tools.Tools.cast;
import static multij.tools.Tools.ignore;
import static multij.tools.Tools.join;
import averan.draft3.core.Composite;
import averan.draft3.core.Expression;
import averan.draft3.core.Proof;
import averan.draft3.core.Session;
import averan.draft3.core.Variable;
import averan.draft3.core.Proof.Deduction;
import averan.draft3.core.Proof.FreeVariablePreventsConclusionException;
import averan.draft3.core.Proof.Deduction.Instance;
import averan.draft3.core.Proof.Deduction.Supposition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import multij.tools.IllegalInstantiationException;
import multij.tools.Tools;

/**
 * @author codistmonk (creation 2015-01-11)
 */
public final class AutoDeduce {
	
	private AutoDeduce() {
		throw new IllegalInstantiationException();
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
		return autoDeduce(propositionName, goal, Standard.autoDeduceDepth.get());
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
		
		final String indent = join("", Collections.nCopies(recursionDepth() - 1, "    "));
		int toConclude = 0;
		
		deduce(propositionName, goal);
		++toConclude;
		{
			intros();
			
			while (goal() instanceof Composite<?> && ((Composite<?>) goal()).getParameters() != null) {
				Session.breakpoint(0);
				
				deduce((Expression<?>) goal());
				++toConclude;
				intros();
			}
			
			use_justifications:
			{
				log(indent, "???");
				log(indent, "TRYING TO PROVE ", goal());
				
				final List<AutoDeduce.Justification> justifications = justify(goal());
				
				for (final AutoDeduce.Justification justification : justifications) {
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
								try {
									bind(justificationName, ((Justification.Bind) step).getValues().toArray(new Expression[0]));
									justificationName = name(-1);
									log(indent, "GENERATED", name(-1), proof(-1).getProposition());
								} catch (final FreeVariablePreventsConclusionException exception) {
									cancel();
									break subdeduction;
								}
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
	
	public static final List<AutoDeduce.Justification> justify(final Expression<?> goal) {
		List<AutoDeduce.Justification> result = new ArrayList<>();
		
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
		
		if (parameters != null) {
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
		
		private final Justification.Step[] steps;
		
		private final Expression<?> boundProposition;
		
		public Justification(final String propositionName, final Justification.Step[] steps,
				final Expression<?> boundProposition) {
			this.propositionName = propositionName;
			this.steps = steps;
			this.boundProposition = boundProposition;
		}
		
		public final String getPropositionName() {
			return this.propositionName;
		}
		
		public final Justification.Step[] getSteps() {
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
		
		public static final Justification.Step[] NOTHING = {};
		
		/**
		 * @author codistmonk (creation 2015-01-07)
		 */
		public static abstract interface Step extends Serializable {
			
			public abstract Justification.Step bind();
			
		}
		
		/**
		 * @author codistmonk (creation 2015-01-07)
		 */
		public static final class Recall implements Justification.Step {
			
			@Override
			public final Justification.Recall bind() {
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
		public static final class Apply implements Justification.Step {
			
			private final Expression<?> condition;
			
			private Expression<?> boundCondition;
			
			public Apply(final Expression<?> condition) {
				this.condition = condition;
			}
			
			public final Expression<?> getCondition() {
				return this.boundCondition;
			}
			
			@Override
			public final Justification.Apply bind() {
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
		public static final class Bind implements Justification.Step {
			
			private final Composite<Expression<?>> parameters;
			
			private List<Expression<?>> values;
			
			public Bind(final Composite<Expression<?>> parameters) {
				this.parameters = parameters;
			}
			
			public final List<Expression<?>> getValues() {
				return this.values;
			}
			
			@Override
			public final Justification.Bind bind() {
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