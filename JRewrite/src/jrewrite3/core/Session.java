package jrewrite3.core;

import static net.sourceforge.aprog.tools.Tools.cast;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jrewrite3.core.Module.Claim;
import jrewrite3.core.Module.Command;
import jrewrite3.core.Module.Symbol;

/**
 * @author codistmonk (creation 2014-08-02)
 */
public final class Session implements Serializable {
	
	private final List<Module> modules;
	
	private final List<ProofContext> stack;
	
	public Session() {
		this(new Module(Module.ROOT));
	}
	
	public Session(final Module mainModule) {
		this("main", mainModule);
	}
	
	public Session(final String name, final Module mainModule) {
		this.modules = new ArrayList<>();
		this.stack = new ArrayList<ProofContext>();
		this.stack.add(0, new ProofContext(name, mainModule, null));
	}
	
	public final Session load(final Module module) {
		this.modules.add(module);
		
		return this;
	}
	
	public final ProofContext getCurrentContext() {
		return this.stack.get(0);
	}
	
	public final Expression getCurrentGoal() {
		return this.getCurrentContext().getCurrentGoal();
	}
	
	public final Symbol getParameter(final String name) {
		return this.getCurrentContext().getModule().findParameter(name);
	}
	
	public final Symbol getParameter(final int index) {
		final List<Symbol> parameters = this.getCurrentContext().getModule().getParameters();
		final int n = parameters.size();
		
		return parameters.get((index + n) % n);
	}
	
	public final Session suppose(final Expression condition) {
		return this.suppose(null, condition);
	}
	
	public final Session suppose(final String conditionName, final Expression condition) {
		this.getCurrentContext().getModule().new Suppose(conditionName, condition).execute();
		
		return this.pop();
	}
	
	public final Session rewrite(final String sourceName, final String equalityName, final Integer... indices) {
		return this.rewrite(null, sourceName, equalityName, indices);
	}
	
	public final Session rewrite(final String factName, final String sourceName, final String equalityName, final Integer... indices) {
		final Module module = this.getCurrentContext().getModule();
		
		module.new Rewrite(factName, module, sourceName, module, equalityName).atIndices(indices).execute();
		
		return this.pop();
	}
	
	public final Session apply(final String moduleName, final String conditionName) {
		return this.apply(null, moduleName, conditionName);
	}
	
	public final Session apply(final String factName, final String moduleName, final String conditionName) {
		final Module module = this.getCurrentContext().getModule();
		
		module.new Apply(factName, module, moduleName).apply(module, conditionName).execute();
		
		return this.pop();
	}
	
	public final Session bind(final String moduleName, final Expression... expressions) {
		return this.bind(null, moduleName, expressions);
	}
	
	public final Session bind(final String factName, final String moduleName, final Expression... expressions) {
		Module module = this.getCurrentContext().getModule();
		
		if (module.getPropositionOrNull(moduleName) == null) {
			module = null;
			
			for (final Module otherModule : this.modules) {
				if (otherModule.getPropositionOrNull(moduleName) != null) {
					module = otherModule;
					break;
				}
			}
		}
		
		this.getCurrentContext().getModule().new Bind(
				factName, module, moduleName).bind(expressions).execute();
		
		return this.pop();
	}
	
	public final Session introduce() {
		return this.introduce(null);
	}
	
	public final Session introduce(final String parameterOrConditionName) {
		this.getCurrentContext().introduce(parameterOrConditionName);
		
		return this.pop();
	}
	
	public final Session claim(final Expression proposition) {
		return this.claim(null, proposition);
	}
	
	public final Session claim(final String factName, final Expression proposition) {
		final ProofContext proofContext = new ProofContext(factName,
				new Module(this.getCurrentContext().getModule()), proposition);
		
		this.stack.add(0, proofContext);
		
		return this.pop();
	}
	
	public final void printTo(final PrintStream output, final boolean printProofs) {
		final int n = this.stack.size();
		String indent = "";
		
		for (int i = n - 1; 0 <= i; --i, indent += ATOMIC_INDENT) {
			final ProofContext context = this.stack.get(i);
			final Module module = context.getModule();
			
			output.println(indent + "((MODULE " + context.getName() + "))");
			
			printModule(module, output, printProofs, indent);
			
			if (context.getCurrentGoal() != null) {
				output.println(indent + "((GOAL))");
				output.println(indent + ATOMIC_INDENT + context.getCurrentGoal());
			}
		}
	}
	
	private final Session pop() {
		while (1 < this.stack.size() && this.getCurrentContext().isGoalReached()) {
			final ProofContext previous = this.stack.remove(0);
			final Module proof = previous.getModule();
			final Expression fact = previous.getInitialGoal() instanceof Module ?
					proof : previous.getInitialGoal();
			
			this.getCurrentContext().getModule().new Claim(previous.getName(), fact, proof).execute();
		}
		
		return this;
	}
	
	/**
	 * {@value}.
	 */
	private static final long serialVersionUID = -4545117345944633693L;
	
	public static final String ATOMIC_INDENT = "\t";
	
	public static final void printModule(final Module module, final PrintStream output,
			final boolean printProofs, final String indent) {
		if (!module.getParameters().isEmpty()) {
			output.println(indent + "∀" + module.getParameters());
		}
		
		final List<Expression> conditions = module.getConditions();
		
		if (!conditions.isEmpty()) {
			output.println(indent + "((CONDITIONS))");
			
			for (final Map.Entry<String, Integer> entry : module.getConditionIndices().entrySet()) {
				output.println(indent + "(" + entry.getKey() + ")");
				output.println(indent + ATOMIC_INDENT + conditions.get(entry.getValue()));
			}
		}
		
		{
			final List<Expression> facts = module.getFacts();
			
			if (facts.isEmpty()) {
				output.println(indent + "()");
			} else {
				final List<Command> proofs = module.getProofs();
				
				output.println(indent + "((FACTS))");
				
				for (final Map.Entry<String, Integer> entry : module.getFactIndices().entrySet()) {
					output.println(indent + "(" + entry.getKey() + ")");
					output.println(indent + ATOMIC_INDENT + facts.get(entry.getValue()));
					
					if (printProofs) {
						output.println(indent + ATOMIC_INDENT + "((PROOF))");
						
						final Command command = proofs.get(entry.getValue());
						final Claim claim = cast(Claim.class, command);
						
						if (claim == null) {
							output.println(indent + ATOMIC_INDENT + ATOMIC_INDENT + command);
						} else {
							printModule(claim.getProofContext(), output, printProofs,
									indent + ATOMIC_INDENT + ATOMIC_INDENT);
						}
					}
				}
			}
		}
	}
	
	/**
	 * @author codistmonk (creation 2014-08-02)
	 */
	public static final class ProofContext implements Serializable {
		
		private final String name;
		
		private final Module module;
		
		private final Expression initialGoal;
		
		private Expression currentGoal;
		
		private boolean goalReached;
		
		private int uncheckedConditionIndex;
		
		private int uncheckedFactIndex;
		
		public ProofContext(final String name, final Module module, final Expression goal) {
			this.name = name;
			this.module = module;
			this.initialGoal = goal;
			this.currentGoal = goal;
		}
		
		public final String getName() {
			return this.name;
		}
		
		public final Module getModule() {
			return this.module;
		}
		
		public final Expression getCurrentGoal() {
			return this.currentGoal;
		}
		
		public final boolean isGoalReached() {
			if (!this.goalReached) {
				final List<Expression> conditions = this.getModule().getConditions();
				final int conditionCount = conditions.size();
				
				while (this.uncheckedConditionIndex < conditionCount) {
					if (!this.goalReached
							&& this.getCurrentGoal().equals(conditions.get(this.uncheckedConditionIndex))) {
						this.goalReached = true;
					}
					
					++this.uncheckedConditionIndex;
				}
			}
			
			if (!this.goalReached) {
				final List<Expression> facts = this.getModule().getFacts();
				final int factCount = facts.size();
				
				while (this.uncheckedFactIndex < factCount) {
					if (!this.goalReached
							&& this.getCurrentGoal().equals(facts.get(this.uncheckedFactIndex))) {
						this.goalReached = true;
					}
					
					++this.uncheckedFactIndex;
				}
			}
			
			return this.goalReached;
		}
		
		public final Expression getInitialGoal() {
			return this.initialGoal;
		}
		
		public final void introduce(final String parameterOrConditionName) {
			final Module goal = (Module) this.getCurrentGoal();
			final List<Symbol> parameters = goal.getParameters();
			final List<Expression> conditions = goal.getConditions();
			
			if (!parameters.isEmpty()) {
				final List<Symbol> newGoalParameters = new ArrayList<>(parameters.subList(1, parameters.size()));
				final Symbol parameter = parameters.get(0);
				final Symbol introducedParameter = this.getModule().parameter(parameter.toString());
				
				this.setCurrentGoal(new Module(
						goal.getParent(),
						newGoalParameters,
						new ArrayList<>(conditions),
						new ArrayList<>(goal.getFacts())).accept(new Rewriter().rewrite(parameter, introducedParameter)));
			} else if (!conditions.isEmpty()) {
				final List<Expression> newConditions = new ArrayList<>(conditions.subList(1, conditions.size()));
				
				this.getModule().new Suppose(parameterOrConditionName, conditions.get(0)).execute();
				
				this.setCurrentGoal(new Module(
						goal.getParent(),
						new ArrayList<>(parameters),
						newConditions,
						new ArrayList<>(goal.getFacts())));
			} else {
				throw new IllegalStateException();
			}
		}
		
		private final void setCurrentGoal(final Expression currentGoal) {
			this.currentGoal = simplify(currentGoal);
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = -6558665995824497727L;
		
		public static final Expression simplify(final Expression expression) {
			final Module module = cast(Module.class, expression);
			
			if (module == null || !module.isFree() || module.getFacts().size() != 1) {
				return expression;
			}
			
			return module.getFacts().get(0);
		}
		
	}
	
}
