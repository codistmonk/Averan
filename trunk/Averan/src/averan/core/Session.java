package averan.core;

import static net.sourceforge.aprog.tools.Tools.cast;

import averan.core.Module.Symbol;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author codistmonk (creation 2014-08-02)
 */
public final class Session implements Serializable {
	
	private final List<ProofContext> stack;
	
	public Session() {
		this(new Module(Module.ROOT));
	}
	
	public Session(final Module mainModule) {
		this(mainModule.getName(), mainModule);
	}
	
	public Session(final String name, final Module mainModule) {
		this.stack = new ArrayList<ProofContext>();
		this.stack.add(0, new ProofContext(name, mainModule, null));
	}
	
	public final Session trust(final Module module) {
		for (final Symbol symbol : module.getParameters()) {
			this.getCurrentContext().parameter(symbol.toString());
		}
		
		for (final Map.Entry<String, Integer> entry : module.getConditionIndices().entrySet()) {
			this.suppose(entry.getKey(), module.getConditions().get(entry.getValue()));
		}
		
		for (final Map.Entry<String, Integer> entry : module.getFactIndices().entrySet()) {
			module.getStatements().get(entry.getValue()).copyFor(this.getCurrentModule()).execute();
		}
		
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
		return (E) this.getCurrentModule().getProposition(name);
	}
	
	public final Expression getCurrentGoal() {
		return this.getCurrentContext().getCurrentGoal();
	}
	
	public final Symbol getParameter(final String name) {
		return this.getCurrentModule().findParameter(name);
	}
	
	public final Symbol getParameter(final int index) {
		final List<Symbol> parameters = this.getCurrentModule().getParameters();
		final int n = parameters.size();
		
		return parameters.get((index + n) % n);
	}
	
	public final Session suppose(final Expression condition) {
		return this.suppose(this.newPropositionName(), condition);
	}
	
	public final Session suppose(final String conditionName, final Expression condition) {
		this.checkPropositionNameAvailable(conditionName);
		
		this.getCurrentModule().new Suppose(conditionName, condition).execute();
		
		return this.tryToPop();
	}
	
	public final Session admit(final Expression fact) {
		return this.admit(this.newPropositionName(), fact);
	}
	
	public final Session admit(final String factName, final Expression fact) {
		this.checkPropositionNameAvailable(factName);
		
		this.getCurrentModule().new Admit(factName, fact).execute();
		
		return this.tryToPop();
	}
	
	public final Session rewrite(final String sourceName, final String equalityName, final Integer... indices) {
		return this.rewrite(this.newPropositionName(), sourceName, equalityName, indices);
	}
	
	public final Session rewrite(final String factName, final String sourceName, final String equalityName, final Integer... indices) {
		this.checkPropositionNameAvailable(factName);
		
		final Module module = this.getCurrentModule();
		
		module.new Rewrite(factName, module, sourceName, module, equalityName).atIndices(indices).execute();
		
		return this.tryToPop();
	}
	
	public final Session apply(final String moduleName, final String conditionName) {
		return this.apply(this.newPropositionName(), moduleName, conditionName);
	}
	
	public final Session apply(final String factName, final String moduleName, final String conditionName) {
		this.checkPropositionNameAvailable(factName);
		
		final Module context = this.getCurrentModule();
		
		context.new Apply(factName, context, moduleName, context, conditionName).execute();
		
		return this.tryToPop();
	}
	
	@SuppressWarnings("unchecked")
	public final <E extends Expression> E getCondition(final int index) {
		final List<Expression> conditions = this.getCurrentModule().getConditions();
		final int n = conditions.size();
		
		return (E) conditions.get((n + index) % n);
	}
	
	public final String getConditionName(final int index) {
		Module context = this.getCurrentModule();
		Map<String, Integer> conditionIndices = context.getConditionIndices();
		int relativeIndex = index;
		int n = conditionIndices.size();
		
		while (n + relativeIndex < 0) {
			context = context.getParent();
			
			if (context == null) {
				throw new IllegalArgumentException();
			}
			
			relativeIndex += n;
			conditionIndices = context.getConditionIndices();
			n = conditionIndices.size();
		}
		
		final int i = (n + relativeIndex) % n;
		
		return conditionIndices.entrySet().stream().reduce(
				"", (old, entry) -> entry.getValue().equals(i) ? entry.getKey() : old, (u, t) -> t);
	}
	
	@SuppressWarnings("unchecked")
	public final <E extends Expression> E getFact(final int index) {
		final List<Expression> facts = this.getCurrentModule().getFacts();
		final int n = facts.size();
		
		return (E) facts.get((n + index) % n);
	}
	
	public final String getFactName(final int index) {
		Module context = this.getCurrentModule();
		Map<String, Integer> factIndices = context.getFactIndices();
		int relativeIndex = index;
		int n = factIndices.size();
		
		while (n + relativeIndex < 0) {
			context = context.getParent();
			
			if (context == null) {
				throw new IllegalArgumentException();
			}
			
			relativeIndex += n;
			factIndices = context.getFactIndices();
			n = factIndices.size();
		}
		
		final int i = (n + relativeIndex) % n;
		
		return factIndices.entrySet().stream().reduce(
				"", (old, entry) -> entry.getValue().equals(i) ? entry.getKey() : old, (u, t) -> t);
	}
	
	public final Module getCurrentModule() {
		return this.getCurrentContext().getModule();
	}
	
	public final Session bind(final String moduleName, final Expression... expressions) {
		return this.bind(this.newPropositionName(), moduleName, expressions);
	}
	
	public final Session bind(final String factName, final String moduleName, final Expression... expressions) {
		this.checkPropositionNameAvailable(factName);
		
		Module module = this.getCurrentModule();
		
//		if (module.getPropositionOrNull(moduleName) == null) {
//			module = null;
//			
//			for (final Module otherModule : this.trustedModules) {
//				if (otherModule.getPropositionOrNull(moduleName) != null) {
//					module = otherModule;
//					break;
//				}
//			}
//		}
		
		this.getCurrentModule().new Bind(
				factName, module, moduleName).bind(expressions).execute();
		
		return this.tryToPop();
	}
	
	public final Session introduce() {
		return this.introduce(null);
	}
	
	public final Session introduce(final String parameterOrConditionName) {
		this.getCurrentContext().introduce(parameterOrConditionName);
		
		return this.tryToPop();
	}
	
	@SuppressWarnings("unchecked")
	public final <E extends Expression> E introduceAndGet() {
		final List<Symbol> parameters = this.getCurrentModule().getParameters();
		final List<Expression> conditions = this.getCurrentModule().getConditions();
		final int oldParameterCount = parameters.size();
		final int oldConditionCount = conditions.size();
		
		this.introduce();
		
		if (oldParameterCount < parameters.size()) {
			return (E) this.getParameter(-1);
		}
		
		if (oldConditionCount < conditions.size()) {
			return this.getCondition(-1);
		}
		
		throw new IllegalStateException();
	}
	
	public final Session claim(final Expression proposition) {
		return this.claim(this.newPropositionName(), proposition);
	}
	
	public final Session claim(final String factName, final Expression proposition) {
		if (proposition == null) {
			throw new NullPointerException();
		}
		
		this.checkPropositionNameAvailable(factName);
		
		final ProofContext proofContext = new ProofContext(factName,
				new Module(this.getCurrentModule(), factName), proposition);
		
		this.getStack().add(0, proofContext);
		
		return this.tryToPop();
	}
	
	public final Session substitute(final Composite substitution) {
		return this.substitute(this.newPropositionName(), substitution);
	}
	
	public final Session substitute(final String factName, final Composite substitution) {
		this.checkPropositionNameAvailable(factName);
		
		this.getCurrentModule().new Substitute(factName, substitution).execute();
		
		return this.tryToPop();
	}
	
	public final Session checkPropositionNameAvailable(final String propositionName) {
		if (this.getCurrentModule().getPropositionOrNull(propositionName) != null) {
			throw new IllegalArgumentException("Name already in use: " + propositionName);
		}
		
		return this;
	}
	
	public final String newPropositionName() {
		return this.getCurrentModule().newPropositionName();
	}
	
	public final Session abort() {
		if (1 < this.getStack().size()) {
			this.getStack().remove(0);
		}
		
		return this;
	}
	
	public final Session tryToPop() {
		while (1 < this.getStack().size() && this.getCurrentContext().isGoalReached()) {
			final ProofContext previous = this.getStack().remove(0);
			final Module proof = previous.getModule();
			final Expression fact = previous.getInitialGoal();
			
			this.getCurrentModule().new Claim(previous.getName(), fact, proof).execute();
		}
		
		return this;
	}
	
	/**
	 * {@value}.
	 */
	private static final long serialVersionUID = -4545117345944633693L;
	
	/**
	 * @author codistmonk (creation 2014-08-02)
	 */
	public static final class ProofContext implements Serializable {
		
		private final String name;
		
		private final Module module;
		
		private final Expression initialGoal;
		
		private Expression currentGoal;
		
		private boolean goalReached;
		
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
		
		@SuppressWarnings("unchecked")
		public final <E extends Expression> E getCurrentGoal() {
			return (E) this.currentGoal;
		}
		
		public final boolean isGoalReached() {
			if (!this.goalReached) {
				this.goalReached = this.getModule().implies(this.getInitialGoal());
			}
			
			return this.goalReached;
		}
		
		public final Expression getInitialGoal() {
			return this.initialGoal;
		}
		
		public final Symbol parameter(final String name) {
			final Module module = this.getModule();
			Symbol result = module.getParameter(name);
			
			if (result == null) {
				result = module.new Parametrize(name).executeAndGet();
			}
			
			return result;
		}
		
		public final void introduce(final String parameterOrConditionName) {
			final Module goal = (Module) this.getCurrentGoal();
			final List<Module> trustedModules = goal.getTrustedModules();
			final List<Symbol> parameters = goal.getParameters();
			final List<Expression> conditions = goal.getConditions();
			
			if (!parameters.isEmpty()) {
				final List<Symbol> newGoalParameters = new ArrayList<>(parameters.subList(1, parameters.size()));
				final Symbol parameter = parameters.get(0);
				final Symbol introducedParameter = this.parameter(parameter.toString());
				
				this.setCurrentGoal(new Module(
						goal.getParent(),
						this.getName(),
						trustedModules,
						newGoalParameters,
						new ArrayList<>(conditions),
						new ArrayList<>(goal.getFacts())).accept(new Rewriter().rewrite(parameter, introducedParameter)));
			} else if (!conditions.isEmpty()) {
				final List<Expression> newConditions = new ArrayList<>(conditions.subList(1, conditions.size()));
				
				this.getModule().new Suppose(parameterOrConditionName, conditions.get(0)).execute();
				
				this.setCurrentGoal(new Module(
						goal.getParent(),
						this.getName(),
						trustedModules,
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
