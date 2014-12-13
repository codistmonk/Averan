package averan2.core;

import static averan2.core.Module.FOR_ALL;
import static averan2.core.Module.IMPLIES;
import static java.util.Collections.nCopies;
import static net.sourceforge.aprog.tools.Tools.cast;
import static net.sourceforge.aprog.tools.Tools.join;
import static org.junit.Assert.*;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

/**
 * @author codistmonk (creation 2014-12-13)
 */
public final class SessionTest {
	
	@Test
	public final void test() {
		final Session session = new Session();
		
		assertEquals(1L, session.getStack().size());
		
		session.push("obvious", $(FOR_ALL, "X", "X", IMPLIES, "X"));
		{
			assertEquals(2L, session.getStack().size());
			
			session.introduce();
			session.introduce();
		}
		
		assertEquals(1L, session.getStack().size());
		
		new Session.Exporter().export(session);
	}
	
	public static final <E extends Expression> E $(final Object... objects) {
		if (objects.length == 1) {
			return expression(objects[0]);
		}
		
		final Composite<Expression> protoresult = new Composite<>();
		
		for (final Object object : objects) {
			protoresult.getElements().add(expression(object));
		}
		
		return specialize(protoresult);
	}
	
	public static final <E extends Expression> E expression(final Object object) {
		final Expression expression = cast(Expression.class, object);
		
		return expression != null ? (E) expression : (E) new Symbol(Module.ROOT, object.toString());
	}
	
	public static final Symbol DUMMY = new Symbol(Module.ROOT, "");
	
	public static final <E extends Expression> E specialize(final Composite<?> composite) {
		if (3 == composite.getElementCount()) {
			if (FOR_ALL.equals(composite.getElement(0))) {
				final Module result = new Module(Module.ROOT, DUMMY);
				
				setParameters(result, composite.getElement(1));
				setFact(result, composite.getElement(2));
				
				return (E) result;
			} else if (IMPLIES.equals(composite.getElement(1))) {
				final Module result = new Module(Module.ROOT, DUMMY);
				
				result.getConditions().getElements().add(new Condition(new Symbol(result, ""), composite.getElement(0)));
				result.getFacts().getElements().add(new Fact(new Symbol(result, ""), new Proof.Admit(result, composite.getElement(2))));
				
				return (E) result;
			}
		} else if (5 == composite.getElementCount()) {
			if (FOR_ALL.equals(composite.getElement(0)) && IMPLIES.equals(composite.getElement(3))) {
				final Module result = new Module(Module.ROOT, DUMMY);
				
				setParameters(result, composite.getElement(1));
				result.getConditions().getElements().add(new Condition(new Symbol(result, ""), composite.getElement(2)));
				result.getFacts().getElements().add(new Fact(new Symbol(result, ""), new Proof.Admit(result, composite.getElement(4))));
				
				return (E) result;
			}
		}
		
		return (E) composite;
	}

	public static void setFact(final Module result, final Expression fact) {
		final Module implication = cast(Module.class, fact);
		
		if (implication != null) {
			for (final Condition condition : implication.getConditions().getElements()) {
				result.getConditions().getElements().add(new Condition(new Symbol(result, ""), condition.getExpression()));
			}
			
			for (final Fact subfact : implication.getFacts().getElements()) {
				result.getFacts().getElements().add(new Fact(new Symbol(result, ""), new Proof.Admit(result, subfact)));
			}
		} else {
			result.getFacts().getElements().add(new Fact(new Symbol(result, ""), new Proof.Admit(result, fact)));
		}
	}
	
	private static final void setParameters(final Module module, final Expression parameters) {
		final Composite<?> composite = cast(Composite.class, parameters);
		
		if (composite != null) {
			for (final Expression parameter : composite) {
				module.getParameters().getElements().add(new Symbol(module, parameter.toString()));
			}
		} else {
			module.getParameters().getElements().add(new Symbol(module, parameters.toString()));
		}
	}
	
	/**
	 * @author codistmonk (creation 2014-12-13)
	 */
	public static final class Session implements Serializable {
		
		private final List<Context> stack;
		
		public Session() {
			this(new Module(Module.ROOT, new Symbol(Module.ROOT, "Module")));
		}
		
		public Session(final Module root) {
			this.stack = new LinkedList<>();
			this.stack.add(new Context(root, null));
		}
		
		public final Session introduce() {
			final Module module = this.getCurrentModule();
			final Module goal = this.getCurrentGoal();
			final Module newGoal;
			
			if (0 < goal.getParameters().getElementCount()) {
				module.parameter(goal.getParameters().getElement(0));
				
				newGoal = new Module(goal.getContext(), goal.getName(), new Composite<>(), goal.getConditions(), goal.getFacts());
				
				for (int i = 1; i < goal.getParameters().getElementCount(); ++i) {
					newGoal.parameter(goal.getParameters().getElement(i));
				}
				
				this.getCurrentContext().setGoal(newGoal);
			} else if (0 < goal.getConditions().getElementCount()) {
				module.getConditions().getElements().add(goal.getConditions().getElement(0));
				
				newGoal = new Module(goal.getContext(), goal.getName(), goal.getParameters(), new Composite<>(), goal.getFacts());
				
				for (int i = 1; i < goal.getConditions().getElementCount(); ++i) {
					newGoal.getConditions().getElements().add(goal.getConditions().getElement(i));
				}
				
				this.getCurrentContext().setGoal(newGoal);
			} else {
				newGoal = goal;
			}
			
			if (newGoal.getParameters().getElementCount() == 0
					&& newGoal.getConditions().getElementCount() == 0 && newGoal.getFacts().getElementCount() == 1) {
				this.getCurrentContext().setGoal(newGoal.getFacts().getElement(0).getExpression());
			}
			
			return this.tryToPop();
		}
		
		public final Session tryToPop() {
			final Expression goal = this.getCurrentGoal();
			
			if (goal != null) {
				final Module module = this.getCurrentModule();
				
				for (final Condition condition : module.getConditions().getElements()) {
					if (goal.equals(condition.getExpression())) {
						final Context context = this.getStack().remove(0);
						this.getCurrentModule().getFacts().getElements().add(
								new Fact(module.getName(), new Proof.Admit(this.getCurrentModule(), context.getInitialGoal())));
					}
				}
			}
			
			return this;
		}
		
		public final Session push(final String goalName, final Expression goal) {
			final Module currentModule = this.getCurrentModule();
			
			this.getStack().add(0, new Context(new Module(currentModule, new Symbol(currentModule, goalName)), goal));
			
			return this;
		}
		
		public final Context getCurrentContext() {
			return this.getStack().isEmpty() ? null : this.getStack().get(0);
		}
		
		public final Module getCurrentModule() {
			final Context context = this.getCurrentContext();
			
			return context == null ? null : context.getModule();
		}
		
		public final <E extends Expression> E getCurrentGoal() {
			final Context context = this.getCurrentContext();
			
			return context == null ? null : context.getGoal();
		}
		
		public final List<Context> getStack() {
			return this.stack;
		}
		
		private static final long serialVersionUID = -4301114061803192020L;
		
		/**
		 * @author codistmonk (creation 2014-12-13)
		 */
		public static final class Context implements Serializable {
			
			private final Module module;
			
			private final Expression initialGoal;
			
			private Expression goal;
			
			public Context(final Module module, final Expression goal) {
				this.module = module;
				this.initialGoal = goal;
				this.goal = goal;
			}
			
			public final Module getModule() {
				return this.module;
			}
			
			public final <E extends Expression> E getInitialGoal() {
				return (E) this.initialGoal;
			}
			
			public final <E extends Expression> E getGoal() {
				return (E) this.goal;
			}
			
			final Context setGoal(final Expression goal) {
				this.goal = goal;
				
				return this;
			}
			
			private static final long serialVersionUID = -3183421913917977496L;
			
		}
		
		/**
		 * @author codistmonk (creation 2014-12-13)
		 */
		public static final class Exporter implements Serializable {
			
			private final PrintStream out = System.out;
			
			public final void export(final Session session) {
				final int n = session.getStack().size();
				
				for (int i = n - 1; 0 <= i; --i) {
					this.export(session.getStack().get(i), n - 1 - i);
				}
			}
			
			public final int export(final Context context, final int level) {
				final Module module = context.getModule();
				String indent = join("", nCopies(level, "\t").toArray());
				
				this.out.println(indent + "((MODULE " + module.getName() + module.getParameters().getElements() + "))");
				this.out.println(indent + "((CONDITIONS))");
				
				for (final Expression condition : module.getConditions()) {
					this.out.println(indent + "\t" + condition);
				}
				
				this.out.println(indent + "((FACTS))");
				
				for (final Expression fact : module.getFacts()) {
					this.out.println(indent + "\t" + fact);
				}
				
				if (context.getGoal() != null) {
					this.out.println(indent + "((GOAL))");
					this.out.println(indent + "\t" + context.getGoal());
				}
				
				return level + 1;
			}
			
			private static final long serialVersionUID = 1289472532793115219L;
			
		}
		
	}
	
}
