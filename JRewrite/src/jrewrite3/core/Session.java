package jrewrite3.core;

import static java.lang.Math.max;
import static java.util.Collections.nCopies;
import static net.sourceforge.aprog.tools.Tools.cast;
import static net.sourceforge.aprog.tools.Tools.join;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.sourceforge.aprog.tools.Tools;
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
	
	public final List<ProofContext> getStack() {
		return this.stack;
	}
	
	public final ProofContext getCurrentContext() {
		return this.getStack().get(0);
	}
	
	@SuppressWarnings("unchecked")
	public final <E extends Expression> E getProposition(final String name) {
		return (E) this.getCurrentContext().getModule().getProposition(name);
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
		
		module.new Apply(factName, module, moduleName, module, conditionName).execute();
		
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
		
		this.getStack().add(0, proofContext);
		
		return this.pop();
	}
	
	private final Session pop() {
		while (1 < this.getStack().size() && this.getCurrentContext().isGoalReached()) {
			final ProofContext previous = this.getStack().remove(0);
			final Module proof = previous.getModule();
//			final Expression fact = previous.getInitialGoal() instanceof Module ?
//					proof : previous.getInitialGoal();
			final Expression fact = previous.getInitialGoal();
			
			this.getCurrentContext().getModule().new Claim(previous.getName(), fact, proof).execute();
		}
		
		return this;
	}
	
	/**
	 * {@value}.
	 */
	private static final long serialVersionUID = -4545117345944633693L;
	
	public static final String ATOMIC_INDENT = "\t";
	
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
	
	/**
	 * @author codistmonk (creation 2014-08-08)
	 */
	public final class Exporter implements Serializable {
		
		private final ExporterOutput output;
		
		private final boolean printProofs;
		
		public Exporter(final boolean printProofs) {
			this(new Printer(), printProofs);
		}
		
		public Exporter(final ExporterOutput output, final boolean printProofs) {
			this.output = output;
			this.printProofs = printProofs;
		}
		
		public final void exportSession() {
			final Session session = Session.this;
			final int n = session.getStack().size();
			
			this.output.beginSession();
			
			for (int i = n - 1; 0 <= i; --i) {
				this.exportContext(session.getStack().get(i));
			}
			
			this.output.endSession();
		}
		
		private final void exportContext(final ProofContext context) {
			final Module module = context.getModule();
			
			this.output.subcontext(context.getName());
			
			this.exportModule(module);
			
			final Expression currentGoal = context.getCurrentGoal();
			
			if (currentGoal != null) {
				this.output.processCurrentGoal(currentGoal);
			}
		}
		
		private final void exportModule(final Module module) {
			if (!module.getParameters().isEmpty()) {
				this.output.processModuleParameters(module);
			}
			
			final List<Expression> conditions = module.getConditions();
			
			if (!conditions.isEmpty()) {
				this.output.beginModuleConditions(module);
				
				for (final Map.Entry<String, Integer> entry : module.getConditionIndices().entrySet()) {
					this.output.processModuleCondition(entry.getKey(), conditions.get(entry.getValue()));
				}
				
				this.output.endModuleConditions(module);
			}
			
			{
				this.output.beginModuleFacts(module);
				
				final List<Expression> facts = module.getFacts();
				final List<Command> proofs = module.getProofs();
				
				for (final Map.Entry<String, Integer> entry : module.getFactIndices().entrySet()) {
					this.output.processModuleFact(entry.getKey(), facts.get(entry.getValue()));
					
					if (this.printProofs) {
						this.output.beginModuleFactProof();
						
						final Command command = proofs.get(entry.getValue());
						final Claim claim = cast(Claim.class, command);
						
						if (claim == null) {
							this.output.processModuleFactProof(command);
						} else {
							this.exportModule(claim.getProofContext());
						}
						
						this.output.endModuleFactProof();
					}
				}
				
				this.output.endModuleFacts(module);
			}
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = 2272468614566549833L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-08-08)
	 */
	public static final class Printer implements ExporterOutput {
		
		private final PrintStream output;
		
		private int indentLevel;
		
		private String indent;
		
		public Printer() {
			this(System.out);
		}
		
		public Printer(final PrintStream output) {
			this.output = output;
			this.indentLevel = -1;
			this.indent = "";
		}
		
		@Override
		public final void beginSession() {
			// NOP
		}
		
		@Override
		public final void subcontext(final String name) {
			this.indent = join("", nCopies(++this.indentLevel, ATOMIC_INDENT).toArray());
			
			this.output.println(this.indent + "((MODULE " + name + "))");
		}
		
		@Override
		public final void processModuleParameters(final Module module) {
			this.output.println(this.indent + "∀" + module.getParameters());
		}
		
		@Override
		public final void beginModuleConditions(final Module module) {
			this.output.println(this.indent + "((CONDITIONS))");
		}
		
		@Override
		public final void processModuleCondition(final String conditionName, final Expression condition) {
			this.output.println(this.indent + "(" + conditionName + ")");
			this.output.println(this.indent + ATOMIC_INDENT + condition);
		}
		
		@Override
		public final void endModuleConditions(final Module module) {
			// NOP
		}
		
		@Override
		public final void beginModuleFacts(final Module module) {
			if (module.getFacts().isEmpty()) {
				this.output.println(this.indent + "()");
			} else {
				this.output.println(this.indent + "((FACTS))");
			}
		}
		
		@Override
		public final void processModuleFact(final String factName, final Expression fact) {
			this.output.println(this.indent + "(" + factName + ")");
			this.output.println(this.indent + ATOMIC_INDENT + fact);
		}
		
		@Override
		public final void beginModuleFactProof() {
			this.output.println(this.indent + ATOMIC_INDENT + "((PROOF))");
			this.indent = join("", nCopies(++this.indentLevel, ATOMIC_INDENT).toArray());
		}
		
		@Override
		public final void processModuleFactProof(final Command command) {
			this.output.println(this.indent + ATOMIC_INDENT + command);
		}
		
		@Override
		public final void endModuleFactProof() {
			this.indent = join("", nCopies(max(0, --this.indentLevel), ATOMIC_INDENT).toArray());
		}
		
		@Override
		public final void endModuleFacts(final Module module) {
			// NOP
		}
		
		@Override
		public final void processCurrentGoal(final Expression currentGoal) {
			this.output.println(this.indent + "((GOAL))");
			this.output.println(this.indent + ATOMIC_INDENT + currentGoal);
		}
		
		@Override
		public final void endSession() {
			this.output.flush();
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = 5124521844835011803L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-08-08)
	 */
	public static interface ExporterOutput extends Serializable {
		
		public abstract void beginSession();
		
		public abstract void subcontext(String name);
		
		public abstract void processModuleParameters(Module module);
		
		public abstract void beginModuleConditions(Module module);
		
		public abstract void processModuleCondition(String conditionName, Expression condition);
		
		public abstract void endModuleConditions(Module module);
		
		public abstract void beginModuleFacts(Module module);
		
		public abstract void processModuleFact(String factName, Expression fact);
		
		public abstract void beginModuleFactProof();
		
		public abstract void processModuleFactProof(Command command);
		
		public abstract void endModuleFactProof();
		
		public abstract void endModuleFacts(Module module);
		
		public abstract void processCurrentGoal(Expression currentGoal);
		
		public abstract void endSession();
		
	}
	
}
