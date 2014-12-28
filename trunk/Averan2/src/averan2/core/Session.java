package averan2.core;

import static averan2.core.Equality.equality;
import static averan2.core.Expression.CollectParameters.collectParameters;
import static net.sourceforge.aprog.tools.Tools.cast;
import static net.sourceforge.aprog.tools.Tools.ignore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.sourceforge.aprog.tools.IllegalInstantiationException;
import net.sourceforge.aprog.tools.Pair;

/**
 * @author codistmonk (creation 2014-12-20)
 */
public final class Session implements Serializable {
	
	private final Module root;
	
	private final List<Frame> frames;
	
	public Session() {
		this.root = new Module();
		this.frames = new ArrayList<>();
	}
	
	public final Module getRoot() {
		return this.root;
	}
	
	public final List<Frame> getFrames() {
		return this.frames;
	}
	
	public final Session deduce(final String factName, final Expression<?> goal) {
		this.getFrames().add(this.new Frame(this.propositionName(factName), goal));
		
		return this;
	}
	
	public final Session cancelFrame() {
		this.getFrames().remove(this.getFrames().size() - 1);
		
		return this;
	}
	
	public final void cancelSession() {
		throw new RuntimeException();
	}
	
	public final Session conclude() {
		if (this.getFrames().size() <= 1 || this.getCurrentFrame().getGoal() != null) {
			throw new IllegalStateException();
		}
		
		final Frame frame = this.getFrames().remove(this.getFrames().size() - 1);
		
		this.getCurrentModule().new ProofByDeduce(frame.getName(), frame.getModule()).apply();
		
		return this.reduce();
	}
	
	@SuppressWarnings("unchecked")
	public final <E extends Expression<?>> E introduce() {
		final Frame frame = this.getCurrentFrame();
		final Expression<?> goal = frame.getGoal();
		final List<Variable> parameters = goal.accept(collectParameters());
		
		if (!parameters.isEmpty()) {
			final Variable parameter = parameters.get(0);
			final Symbol<String> introducedForParameter = new Symbol<>(parameter.getName());
			
			parameter.reset().equals(introducedForParameter);
			
			frame.getIntroducedBindings().add(equality(introducedForParameter, parameter));
			frame.setGoal(goal.accept(Variable.BIND));
			
			return (E) introducedForParameter;
		}
		
		{
			final Module module = (Module) goal;
			final Expression<?> condition = module.getConditions().get(0);
			
			this.getCurrentModule().addCondition(frame.newPropositionName(), condition);
			frame.setGoal(Module.apply(module, condition));
			
			return this.reduce().getCurrentFrame() == frame ? (E) condition : null;
		}
	}
	
	public final Session suppose(final String conditionName, final Expression<?> condition) {
		this.getCurrentModule().addCondition(this.propositionName(conditionName), condition);
		
		return this.reduce();
	}
	
	public final Session apply(final String factName, final String moduleName, final String conditionName) {
		this.getCurrentModule().new ProofByApply(this.propositionName(factName), moduleName, conditionName).apply();
		
		return this.reduce();
	}
	
	public final Session substitute(final String factName, final Expression<?> expression, final Equality... equalities) {
		final Substitution substitution = new Substitution();
		
		for (final Equality equality : equalities) {
			substitution.bind(equality);
		}
		
		this.getCurrentModule().new ProofBySubstitute(this.propositionName(factName), expression, substitution).apply();
		
		return this.reduce();
	}
	
	public final Session rewrite(final String factName, final String propositionName, final String equalityName,
			final int... indices) {
		this.getCurrentModule().new ProofByRewrite(this.propositionName(factName), propositionName).using(equalityName).at(indices).apply();
		
		return this.reduce();
	}
	
	public final Module getCurrentModule() {
		final Frame currentFrame = this.getCurrentFrame();
		
		return currentFrame == null ? null : currentFrame.getModule();
	}
	
	public final Frame getCurrentFrame() {
		return this.getFrames().isEmpty() ? null : this.getFrames().get(this.getFrames().size() - 1);
	}
	
	final Module getContextForNewModule() {
		final Module currentModule = this.getCurrentModule();
		
		return currentModule != null ? currentModule : this.getRoot();
	}
	
	private final Session reduce() {
		final Frame frame = this.getCurrentFrame();
		final int factCount = frame.getModule().getFacts().size();
		
		if (0 < factCount && frame.getModule().getFacts().get(factCount - 1).equals(frame.getGoal())) {
			this.getFrames().remove(this.getFrames().size() - 1);
			
			final Substitution substitution = new Substitution(true);
			
			for (final Equality binding : frame.getIntroducedBindings()) {
				binding.getRight().accept(Variable.RESET);
				substitution.bind(binding);
			}
			
			this.getCurrentModule().new ProofByDeduce(frame.getName(), (Module) frame.getModule().accept(substitution.reset())).apply();
			
			return this.reduce();
		}
		
		return this;
	}
	
	private final String propositionName(final String propositionName) {
		return propositionName != null ? propositionName : this.getCurrentFrame().newPropositionName();
	}
	
	/**
	 * @author codistmonk (creation 2014-12-20)
	 */
	public final class Frame implements Serializable {
		
		private final String name;
		
		private final Module module;
		
		private final List<Equality> introducedBindings;
		
		private Expression<?> goal;
		
		public Frame(final String name, final Expression<?> goal) {
			this.name = name;
			this.module = new Module(getContextForNewModule());
			this.introducedBindings = new ArrayList<>();
			this.goal = goal;
		}
		
		public final List<Equality> getIntroducedBindings() {
			return this.introducedBindings;
		}
		
		public final String newPropositionName() {
			return this.getName() + "." + (this.getModule().getConditions().size() + this.getModule().getFacts().size() + 1);
		}
		
		public final String getName() {
			return this.name;
		}
		
		public final Module getModule() {
			return this.module;
		}
		
		@SuppressWarnings("unchecked")
		public final <E extends Expression<?>> E getGoal() {
			return (E) this.goal;
		}
		
		final Frame setGoal(final Expression<?> goal) {
			final Module goalAsModule = cast(Module.class, goal);
			
			if (goalAsModule != null
					&& goalAsModule.canonicalize().getConditions().size() <= 0
					&& goalAsModule.getFacts().size() == 1) {
				this.goal = goalAsModule.getFacts().get(0);
			} else {
				this.goal = goal;
			}
			
			return this;
		}
		
		private static final long serialVersionUID = -5943416769824876039L;
		
	}
	
	private static final long serialVersionUID = 181621455530572267L;
	
	/**
	 * @author codistmonk (creation 2014-12-28)
	 */
	public static final class Stack {
		
		private Stack() {
			throw new IllegalInstantiationException();
		}
		
		private static final List<Session> stack = new ArrayList<>();
		
		public static final Session pushSession(final Session session) {
			stack.add(session);
			
			return session;
		}
		
		public static final Session session() {
			return stack.get(stack.size() - 1);
		}
		
		public static final Session popSession() {
			return stack.remove(stack.size() - 1);
		}
		
		public static final Session include(final Module module) {
			final Session session = session();
			
			for (final Map.Entry<String, Integer> id : module.getConditionIds().entrySet()) {
				session.getRoot().addCondition(id.getKey(), module.getConditions().get(id.getValue()));
			}
			
			for (final Map.Entry<String, Integer> id : module.getFactIds().entrySet()) {
				session.getRoot().addFact(id.getKey(), module.getFacts().get(id.getValue()), module.getProof(id.getKey()));
			}
			
			return session;
		}
		
		public static final <E extends Expression<?>> E introduce() {
			return session().introduce();
		}
		
		public static final Session intros() {
			try {
				while (true) {
					introduce();
				}
			} catch (final Exception exception) {
				ignore(exception);
			}
			
			return session();
		}
		
		public static final Session deduce() {
			return deduce(null, null);
		}
		
		public static final Session deduce(final String factName) {
			return deduce(factName, null);
		}
		
		public static final Session deduce(final Expression<?> goal) {
			return deduce(null, goal);
		}
		
		public static final Session deduce(final String factName, final Expression<?> goal) {
			return session().deduce(factName, goal);
		}
		
		public static final <E extends Expression<?>> E goal() {
			return frame().getGoal();
		}
		
		public static final Frame frame() {
			return session().getCurrentFrame();
		}
		
		public static final Module module() {
			return session().getCurrentModule();
		}
		
		public static final String newName() {
			return frame().newPropositionName();
		}
		
		public static final String name(final int index) {
			return module().getPropositionName(index);
		}
		
		public static final Session conclude() {
			return session().conclude();
		}
		
		public static final Session suppose(final Expression<?> condition) {
			return suppose(null, condition);
		}
		
		public static final Session suppose(final String conditionName, final Expression<?> condition) {
			return session().suppose(conditionName, condition);
		}
		
		public static final Session apply(final String moduleName, final String conditionName) {
			return apply(null, moduleName, conditionName);
		}
		
		public static final Session apply(final String factName, final String moduleName, final String conditionName) {
			return session().apply(factName, moduleName, conditionName);
		}
		
		public static final Session substitute(final Expression<?> expression, final Equality... equalities) {
			return substitute(null, expression, equalities);
		}
		
		public static final Session substitute(final String factName,
				final Expression<?> expression, final Equality... equalities) {
			return session().substitute(factName, expression, equalities);
		}
		
		public static final Session rewrite(final String propositionName,
				final String equalityName, final int... indices) {
			return rewrite(null, propositionName, equalityName, indices);
		}
		
		public static final Session rewrite(final String factName, final String propositionName,
				final String equalityName, final int... indices) {
			return session().rewrite(factName, propositionName, equalityName, indices);
		}
		
		public static final List<Pair<String, Expression<?>>> matchesFor(final Expression<?> pattern) {
			return null; // TODO
		}
		
		public static final List<Pair<String, Expression<?>>> justificationsFor(final Expression<?> goal) {
			final List<Pair<String, Expression<?>>> result = new ArrayList<>();
			Module module = module();
			
			while (module != null) {
				for (int i = module.getFacts().size() - 1; 0 <= i; --i) {
					final Expression<?> proposition = module.getFacts().get(i);
					
					if (canDeduce(proposition, goal)) {
						result.add(new Pair<>(module.getFactIds().get(i), proposition.accept(Variable.BIND)));
					}
				}
				
				for (int i = module.getConditions().size() - 1; 0 <= i; --i) {
					final Expression<?> proposition = module.getConditions().get(i);
					
					if (canDeduce(proposition, goal)) {
						result.add(new Pair<>(module.getConditionIds().get(i), proposition.accept(Variable.BIND)));
					}
				}
				
				module = module.getContext();
			}
			
			return result;
		}
		
		public static final boolean canDeduce(final Expression<?> expression, final Expression<?> suffix) {
			if (expression.accept(Variable.RESET).equals(suffix)) {
				return true;
			}
			
			final Module module = cast(Module.class, expression);
			
			if (module == null) {
				return false;
			}
			
			for (final Expression<?> fact : module.canonicalize().getFacts()) {
				if (fact.accept(Variable.RESET).equals(suffix)) {
					return true;
				}
			}
			
			final Module suffixAsModule = cast(Module.class, suffix);
			
			// TODO
			
			return false;
		}
		
	}
	
}
