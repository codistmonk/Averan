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
	
	@Test
	public final void test2() {
		final Session session = new Session();
		
		addStandardFactsTo(session);
		
		session.assume("definition_of_S", template(v("n"), rule(nat("n"), nat(s("n")))));
		session.assume("definition_of_0", nat("0"));
		session.assume("recurrence_principle", template(v("P"), rule(
				apply1("P", "0"),
				rule(
						template(v("n"), rule(apply1("P", "n"), apply1("P", s("n")))),
						template(v("n"), rule(nat("n"), apply1("P", "n")))))));
		session.assume("definition_of_1", equality("1", s("0")));
		session.assume("definition_of_2", equality("2", s("1")));
		session.assume("definition_of_3", equality("3", s("2")));
		session.assume("definition_of_4", equality("4", s("3")));
		session.assume("definition_of_5", equality("5", s("4")));
		session.assume("definition_of_6", equality("6", s("5")));
		session.assume("definition_of_7", equality("7", s("6")));
		session.assume("definition_of_8", equality("8", s("7")));
		session.assume("definition_of_9", equality("9", s("8")));
		
		session.assume("definition_of_+_0", template(v("m"), equality(plus("m", "0"), "m")));
		session.assume("definition_of_+_n", template(v("m", "n"), equality(plus("m", s("n")), s(plus("m", "n")))));
		
		// TODO prove
		session.assume("right_addition_to_equality", template(v("a", "b", "c"), rule(equality("a", "b"), equality(plus("a", "c"), plus("b", "c")))));
		
		session.assume("definition_of_*_0", template(v("m"), equality(times("m", "0"), "0")));
		session.assume("definition_of_*_n", template(v("m", "n"), equality(times("m", s("n")), plus(times("m", "n"), "m"))));
		
		// TODO prove
		session.assume("left_multiplication_by_0", template(v("n"), equality(times("0", "n"), "0")));
		// TODO prove
		session.assume("right_multiplication_by_0", template(v("n"), equality(times("n", "0"), "0")));
		// TODO prove
		session.assume("left_multiplication_by_1", template(v("n"), equality(times("1", "n"), "n")));
		// TODO prove
		session.assume("right_multiplication_by_1", template(v("n"), equality(times("n", "1"), "n")));
		
		session.assume("definition_of_/", template(v("a", "b", "c"), rule(equality("a", times("b", "c")), equality(div("a","c"), "b"))));
		
		// TODO prove
		session.assume("left_distributivity_of_*_over_+", template(v("a", "b", "c")
				, equality(times("a", plus("b", "c")), plus(times("a", "b"), times("a", "c")))));
		
		// TODO prove
		session.assume("left_addition_of_fraction", template(v("a", "b", "c")
				, equality(plus(div("a", "b"), "c"), div(plus("a", times("b", "c")), "c"))));
		
		// TODO prove
		session.assume("fraction_of_0", template(v("n")
				, equality(div("0", s("n")), "0")));
		
		session.assume("definition_of_sum_0", template(v("E")
				, equality(apply2("sum", "E", "0"), apply1("E", "0"))));
		session.assume("definition_of_sum_n", template(v("E", "n")
				, equality(apply2("sum", "E", s("n")), plus(apply2("sum", "E", "n"), apply1("E", s("n"))))));
		
		session.assume("definition_of_Id", template(v("x"), equality(apply1("Id", "x"), "x")));
		
		session.printTo(System.out);
		
		session.prove("arithmetic_series", template(v("n"), equality(apply2("sum", "Id", "n"), div(times("n", plus("n", "1")), "2"))));
		session.assume("definition_of_P", template(v("n"), equality(apply1("P", "n"), equality(apply2("sum", "Id", "n"), div(times("n", plus("n", "1")), "2")))));
		session.bind("definition_of_P_0(a)", "definition_of_P", expression("0"));
		session.bind("sum_Id_0(a)", "definition_of_sum_0", expression("Id"));
		session.bind("Id_0", "definition_of_Id", expression("0"));
		session.rewriteLeft("sum_Id_0(b)", "sum_Id_0(a)", "Id_0");
		session.rewriteLeft("definition_of_P_0(b)", "definition_of_P_0(a)", "sum_Id_0(b)");
		session.bind("left_multiplication_by_0", plus("0", "1"));
		session.rewriteLeft("definition_of_P_0(c)", "definition_of_P_0(b)", -1);
		session.bind("fraction_of_0", expression("1"));
		session.rewriteRight(-1, "definition_of_2");
		session.rewriteLeft("definition_of_P_0(d)", "definition_of_P_0(c)", -1);
		session.bind(Session.IDENTITY, expression("0"));
		session.rewriteRight("trueness_of_P_0", -1, "definition_of_P_0(d)");
		
		session.printTo(System.out);
		
		session.prove("trueness_of_P_S_n", template(v("n"), rule(apply1("P", "n"), apply1("P", s("n")))));
		session.introduce("declaration_of_n");
		session.introduce("trueness_of_P_n");
		session.bind("definition_of_P_n(a)", "definition_of_P", expression("n"));
		session.bind("definition_of_P_S_n(a)", "definition_of_P", s("n"));
		
		session.printTo(System.out);
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
	
	public static final Composite apply2(final Object function, final Object argument1, final Object argument2) {
		return composite(function, " ", argument1, " ", argument2);
	}
	
	public static final Composite nat(final Object object) {
		return composite(object, " : ", "N");
	}
	
	public static final Composite plus(final Object object1, final Object object2) {
		return composite(object1, " + ", object2);
	}
	
	public static final Composite times(final Object object1, final Object object2) {
		return composite(object1, " * ", object2);
	}
	
	public static final Composite div(final Object object1, final Object object2) {
		return composite(object1, " / ", object2);
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
