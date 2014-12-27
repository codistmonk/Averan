package averan4.core;

import averan4.core.Expression.Visitor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.aprog.tools.Tools;

/**
 * @author codistmonk (creation 2014-12-20)
 */
public final class Session implements Serializable {
	
	private final List<Frame> frames;
	
	public Session() {
		this.frames = new ArrayList<>();
	}
	
	public final List<Frame> getFrames() {
		return this.frames;
	}
	
	public final Session deduce(final String factName, final Expression<?> goal) {
		this.getFrames().add(this.new Frame(factName, goal));
		
		return this;
	}
	
	public final Session cancelFrame() {
		this.getFrames().remove(this.getFrames().size() - 1);
		
		return this;
	}
	
	public final void cancelSession() {
		throw new RuntimeException();
	}
	
	public final Session accept(final String... factNames) {
		final Frame frame = this.getCurrentFrame();
		
		if (frame.getGoal() == null) {
			// TODO
		} else {
			// TODO
		}
		
		return this;
	}
	
	public final Session introduce() {
		final Frame frame = this.getCurrentFrame();
		final Module goal = frame.getGoal();
		final Expression<?> condition = goal.getConditions().get(0);
		final List<Variable> variables = getVariables(condition);
		
		if (!variables.isEmpty()) {
			final Variable variable = variables.get(0);
			final Symbol<String> introducedForVariable = new Symbol<>(variable.getName());
			
			variable.reset().equals(introducedForVariable);
			
			frame.introducedForVariables.add(introducedForVariable);
			frame.goal = goal.accept(Variable.BIND);
		} else {
			this.getCurrentModule().addCondition(frame.newPropositionName(), condition);
			frame.goal = Module.apply(goal, condition);
		}
		
		return this.accept();
	}
	
	public final Session suppose(final String conditionName, final Expression<?> conditionProposition) {
		// TODO
		return this.accept();
	}
	
	public final Session apply(final String factName, final String moduleName, final String conditionName) {
		// TODO
		return this.accept();
	}
	
	public final Session substitute(final String factName, final Expression<?> expression, final Equality... equalities) {
		final Substitution substitution = new Substitution();
		
		for (final Equality equality : equalities) {
			substitution.bind(equality);
		}
		
		this.getCurrentModule().new ProofBySubstitute(factName, expression, substitution).apply();
		
		return this.accept();
	}
	
	public final Session rewrite(final String factName, final String propositionName, final String equalityName,
			final int... indices) {
		// TODO
		return this.accept();
	}
	
	public final Module getCurrentModule() {
		final Frame currentFrame = this.getCurrentFrame();
		
		return currentFrame == null ? null : currentFrame.getModule();
	}
	
	public final Frame getCurrentFrame() {
		return this.getFrames().isEmpty() ? null : this.getFrames().get(this.getFrames().size() - 1);
	}
	
	/**
	 * @author codistmonk (creation 2014-12-20)
	 */
	public final class Frame implements Serializable {
		
		private final String name;
		
		private final Module module;
		
		private final List<Symbol<String>> introducedForVariables;
		
		private Expression<?> goal;
		
		public Frame(final String name, final Expression<?> goal) {
			this.name = name;
			this.module = new Module(getCurrentModule());
			this.introducedForVariables = new ArrayList<>();
			this.goal = goal;
		}
		
		public final List<Symbol<String>> getIntroducedForVariables() {
			return this.introducedForVariables;
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
		
		public final <E extends Expression<?>> E getGoal() {
			return (E) this.goal;
		}
		
		private static final long serialVersionUID = -5943416769824876039L;
		
	}
	
	private static final long serialVersionUID = 181621455530572267L;
	
	public static final List<Variable> getVariables(final Expression<?> expression) {
		return expression.accept(new Visitor<List<Variable>>() {
			
			private final Map<Variable, Variable> done = new IdentityHashMap<>();
			
			private final List<Variable> result = new ArrayList<>();
			
			@Override
			public final List<Variable> visit(final Symbol<?> symbol) {
				return this.result;
			}
			
			@Override
			public final List<Variable> visit(final Variable variable) {
				if (this.done.putIfAbsent(variable, variable) == null) {
					this.result.add(variable);
				}
				
				return this.result;
			}
			
			@Override
			public final List<Variable> visit(final Composite<Expression<?>> composite) {
				Expression.Visitor.visitElementsOf(composite, this);
				
				return this.result;
			}
			
			@Override
			public final List<Variable> visit(final Module module) {
				Expression.Visitor.visitElementsOf(module, this);
				
				return this.result;
			}
			
			@Override
			public final List<Variable> visit(final Substitution substitution) {
				Expression.Visitor.visitElementsOf(substitution, this);
				
				return this.result;
			}
			
			@Override
			public final List<Variable> visit(final Equality equality) {
				Expression.Visitor.visitElementsOf(equality, this);
				
				return this.result;
			}
			
			private static final long serialVersionUID = 7741098542636452396L;
			
		});
	}
	
}
