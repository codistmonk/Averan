package jrewrite2;

import static net.sourceforge.aprog.tools.Tools.set;
import static org.junit.Assert.*;

import java.io.Serializable;

import net.sourceforge.aprog.events.EventManager;
import net.sourceforge.aprog.events.EventManager.Event.Listener;

import org.junit.Test;

/**
 * @author codistmonk (creation 2014-06-10)
 */
public final class SessionTest {
	
	@Test
	public final void test1() {
		final Session session = new Session();
		final EventCounter eventCounter = new EventCounter();
		
		EventManager.getInstance().addListener(session.getRootContext(), Context.Event.class, eventCounter);
		
		addStandardFactsTo(session);
		
		session.assume("definition_of_S", template(v("n"), rule(nat("n"), nat(s("n")))));
		session.assume("definition_of_0", nat("0"));
		session.assume("definition_of_1", equality("1", s("0")));
		
		session.printTo(System.out);
		
		{
			session.prove(nat("1"));
			
			session.bind("definition_of_S", expression("0"));
			session.apply(-1, "definition_of_0");
			session.rewriteRight(-1, "definition_of_1", set(0));
			
			session.printTo(System.out);
			
			assertTrue(session.isGoalReached());
		}
		
		assertEquals(2L, eventCounter.getSubcontextCreations());
		assertEquals(6L, eventCounter.getFactAdditions());
		assertEquals(0L, eventCounter.getFactRemovals());
		
		session.undo();
		
		assertEquals(2L, eventCounter.getSubcontextCreations());
		assertEquals(6L, eventCounter.getFactAdditions());
		assertEquals(1L, eventCounter.getFactRemovals());
		
		session.prove(session.getGoal());
		session.undo();
		
		session.printTo(System.out);
		
		assertEquals(5L, session.getFactCount());
		
		assertTrue(session.isGoalReached());
	}
	
	public static final void addStandardFactsTo(final Session session) {
		session.assume(Session.IDENTITY, template(v("x"), equality("x", "x")));
		
		{
			session.prove(Session.SYMMETRY_OF_EQUALITY, template(v("x", "y"), rule(equality("x", "y"), equality("y", "x"))));
			session.introduce("declaration_of_x");
			session.introduce();
			session.introduce();
			session.rewriteLeft("declaration_of_x", -1, set(0));
		}
	}
	
	public static final Template template(final String[] variableNames, final Object expression) {
		int n = variableNames.length;
		Template result = new Template(variableNames[--n], expression(expression));
		
		while (0 < n) {
			result = new Template(variableNames[--n], result);
		}
		
		return result;
	}
	
	public static final String[] v(final String... variableNames) {
		return variableNames;
	}
	
	public static final Equality equality(final Object left, final Object right) {
		return new Equality(expression(left), expression(right));
	}
	
	public static final Rule rule(final Object condition, final Object expression) {
		return new Rule(expression(condition), expression(expression));
	}
	
	public static final Composite s(final Object object) {
		return apply1("S", object);
	}
	
	public static final Composite apply1(final Object function, final Object argument) {
		return composite(function, " ", argument);
	}
	
	public static final Composite nat(final Object object) {
		return composite(object, " : ", "N");
	}
	
	public static final Composite composite(final Object... objects) {
		final int n = objects.length;
		final Expression[] expressions = new Expression[n];
		
		for (int i = 0; i < n; ++i) {
			expressions[i] = expression(objects[i]);
		}
		
		return new Composite(expressions);
	}
	
	public static final Expression expression(final Object object) {
		return object instanceof Expression ? (Expression) object : new Symbol(object.toString());
	}
	
	/**
	 * @author codistmonk (creation 2014-06-11)
	 */
	public static final class EventCounter implements Serializable {
		
		private int subcontextCreations;
		
		private int factAdditions;
		
		private int factRemovals;
		
		public final int getSubcontextCreations() {
			return this.subcontextCreations;
		}
		
		public final int getFactAdditions() {
			return this.factAdditions;
		}
		
		public final int getFactRemovals() {
			return this.factRemovals;
		}
		
		@Listener
		public final void subcontextAdded(final Context.SubcontextCreatedEvent event) {
			++this.subcontextCreations;
		}
		
		@Listener
		public final void factAdded(final Context.FactAddedEvent event) {
			++this.factAdditions;
		}
		
		@Listener
		public final void factAdded(final Context.FactRemovedEvent event) {
			++this.factRemovals;
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = 7886550936698464154L;
		
	}
	
}
