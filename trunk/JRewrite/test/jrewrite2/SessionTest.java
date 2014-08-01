package jrewrite2;

import static net.sourceforge.aprog.tools.Tools.append;
import static net.sourceforge.aprog.tools.Tools.array;
import static net.sourceforge.aprog.tools.Tools.set;
import static org.junit.Assert.*;

import java.io.Serializable;

import net.sourceforge.aprog.events.EventManager;
import net.sourceforge.aprog.events.EventManager.Event.Listener;
import net.sourceforge.aprog.tools.Tools;

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
				apply("P", "0"),
				rule(
						template(v("n"), rule(apply("P", "n"), apply("P", s("n")))),
						template(v("n"), rule(nat("n"), apply("P", "n")))))));
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
		session.assume("S_n_is_n+1", template(v("n"), equality(s("n"), plus("n", "1"))));
		// TODO prove
		session.assume("m_+_S_n_is_S_m_+_n", template(v("m", "n"), equality(plus("m", s("n")), plus(s("m"), "n"))));
		
		// TODO prove
		session.assume("right_addition_to_equality", template(v("a", "b", "c"), rule(equality("a", "b"), equality(plus("a", "c"), plus("b", "c")))));
		
		session.assume("definition_of_*_0", template(v("m"), equality(times("m", "0"), "0")));
		session.assume("definition_of_*_n", template(v("m", "n"), equality(times("m", s("n")), plus(times("m", "n"), "m"))));
		
		// TODO prove
		session.assume("commutativity_of_multiplication", template(v("a", "b"), equality(times("a", "b"), times("b", "a"))));
		// TODO prove
		session.assume("left_multiplication_by_0", template(v("n"), equality(times("0", "n"), "0")));
		// TODO prove
		session.assume("right_multiplication_by_0", template(v("n"), equality(times("n", "0"), "0")));
		// TODO prove
		session.assume("left_multiplication_by_1", template(v("n"), equality(times("1", "n"), "n")));
		// TODO prove
		session.assume("right_multiplication_by_1", template(v("n"), equality(times("n", "1"), "n")));
		
		session.assume("definition_of_/", template(v("a", "b", "c"), rule(equality("a", times("b", "c")), equality(fraction("a","c"), "b"))));
		
		// TODO prove
		session.assume("left_distributivity_of_*_over_+", template(v("a", "b", "c")
				, equality(times("a", plus("b", "c")), plus(times("a", "b"), times("a", "c")))));
		
		// TODO prove
		session.assume("left_addition_of_fraction", template(v("a", "b", "c")
				, equality(plus(fraction("a", "b"), "c"), fraction(plus("a", times("b", "c")), "c"))));
		
		// TODO prove
		session.assume("fraction_of_0", template(v("n")
				, equality(fraction("0", s("n")), "0")));
		// TODO prove
		session.assume("addition_of_fractions_with_same_denominator", template(v("a", "b", "c")
				, equality(plus(fraction("a", "c"), fraction("b", "c")), fraction(plus("a", "b"), "c"))));
		// TODO prove
		session.assume("right_multiplication_by_denominator", template(v("a", "b")
				, equality("a", fraction(times("a", s("b")), s("b")))));
		
		session.assume("definition_of_sum_0", template(v("E")
				, equality(apply("sum", "E", "0"), apply("E", "0"))));
		session.assume("definition_of_sum_n", template(v("E", "n")
				, equality(apply("sum", "E", s("n")), plus(apply("sum", "E", "n"), apply("E", s("n"))))));
		
		session.assume("definition_of_Id", template(v("x"), equality(apply("Id", "x"), "x")));
		
		session.printTo(System.out);
		
		session.prove("arithmetic_series", template(v("n"), rule(nat("n"), equality(apply("sum", "Id", "n"), fraction(times("n", plus("n", "1")), "2")))));
		session.assume("definition_of_P", template(v("n"), equality(apply("P", "n"), equality(apply("sum", "Id", "n"), fraction(times("n", plus("n", "1")), "2")))));
		session.bind("definition_of_P_0(a)", "definition_of_P", expression("0"));
		session.bind("sum_Id_0(a)", "definition_of_sum_0", expression("Id"));
		session.bind("Id_0", "definition_of_Id", expression("0"));
		session.rewriteLeft("sum_Id_0(b)", "sum_Id_0(a)", "Id_0");
		session.rewriteLeft("definition_of_P_0(b)", "definition_of_P_0(a)", "sum_Id_0(b)");
		session.bind("left_multiplication_by_0", plus("0", "1"));
		session.rewriteLeft("definition_of_P_0(c)", "definition_of_P_0(b)", -1);
		session.bind("fraction_of_0", expression("1"));
		session.printTo(System.out);
		session.rewriteRight(-1, "definition_of_2");
		session.rewriteLeft("definition_of_P_0(d)", "definition_of_P_0(c)", -1);
		session.bind(Session.IDENTITY, expression("0"));
		session.rewriteRight("trueness_of_P_0", -1, "definition_of_P_0(d)");
		
		session.printTo(System.out);
		
		session.prove("trueness_of_P_S_n", template(v("n"), rule(apply("P", "n"), apply("P", s("n")))));
		session.introduce("declaration_of_n");
		session.introduce("trueness_of_P_n");
		session.bind("definition_of_P_n(a)", "definition_of_P", expression("n"));
		session.bind("definition_of_P_S_n", "definition_of_P", s("n"));
		session.rewriteLeft("equation(a)", "trueness_of_P_n", "definition_of_P_n(a)");
		session.bind("right_addition_to_equality", ((Equality) session.getProposition("equation(a)")).getLeft());
		session.bind(-1, ((Equality) session.getProposition("equation(a)")).getRight());
		session.bind(-1, s("n"));
		session.apply("equation(b)", -1, "equation(a)");
		session.bind("definition_of_Id", s("n"));
		session.rewriteRight("equation(c)", "equation(b)", -1, set(0));
		session.bind("definition_of_sum_n", expression("Id"));
		session.bind(-1, expression("n"));
		session.rewriteRight("equation(d)", "equation(c)", -1);
		session.bind("S_n_is_n+1", expression("n"));
		session.rewriteRight("equation(e)", "equation(d)", -1);
		session.bind("right_multiplication_by_denominator", s("n"));
		session.bind(-1, expression("1"));
		session.rewriteRight(-1, "definition_of_2");
		session.rewriteLeft("equation(f)", "equation(e)", -1, set(2));
		session.bind("addition_of_fractions_with_same_denominator", times("n", s("n")));
		session.bind(-1, times(s("n"), "2"));
		session.bind(-1, expression("2"));
		session.rewriteLeft("equation(g)", "equation(f)", -1);
		session.bind("commutativity_of_multiplication", expression("n"));
		session.bind(-1, s("n"));
		session.rewriteLeft("equation(h)", "equation(g)", -1);
		session.bind("left_distributivity_of_*_over_+", s("n"));
		session.bind(-1, expression("n"));
		session.bind(-1, expression("2"));
		session.rewriteRight("equation(i)", "equation(h)", -1);
		session.bind("m_+_S_n_is_S_m_+_n", expression("n"));
		session.bind(-1, expression("1"));
		session.rewriteRight(-1, "definition_of_2");
		session.rewriteLeft("equation(i)", -1);
		session.rewriteRight(-1, "definition_of_P_S_n");
		
		session.printTo(System.out);
		
		session.bind("recurrence_principle", expression("P"));
		session.apply(-1, "trueness_of_P_0");
		session.apply(-1, "trueness_of_P_S_n");
		session.bind("definition_of_P", ((Template) session.getProposition(-1)).new Variable());
		session.rewriteLeft(-2, -1);
		
		session.printTo(System.out);
		
		assertEquals(0L, session.getDepth());
		assertTrue(session.isGoalReached());
	}
	
	@Test
	public final void test3() {
		final Session session = new Session();
		
		addStandardFactsTo(session);
		
		session.assume("definition_of_n", nat("n"));
		session.assume("definition_of_Observation", equality("Observation", composite("R", "^", "n")));
		session.assume("definition_of_Sample", equality("Sample", powerset("Observation")));
		session.assume("definition_of_mean", forall("E", equality(apply("mean", "E"), fraction(
				apply("sum", card("E"), lambda("i", sub("E", "i")))
				, card("E")
		))));
		session.assume("definition_of_C_i", forallNat("i", membership(sub("C", "i"), "Sample")));
		session.assume("definition_of_m", forallNat("i", equality(sub("m", "i"), apply("mean", sub("C", "i")))));
		session.assume("definition_of_covariance", forall("m", equality(apply("cov", "m"), times("m", transpose("m")))));
		session.assume("definition_of_between_class_covariance", equality("S_B", apply("cov", minus(sub("m", 2), sub("m", 1)))));
		session.assume("definition_of_within_class_covariance", forallNat("i", equality(apply("Sw", "i")
				, apply("sum", card(sub("C", "i")), template(v("j"), apply("cov", minus(sub("C", "i", "j"), sub("m", "i"))))))));
		session.assume("definition_of_total_within_class_covariance", equality("S_W", plus(apply("Sw", 1), apply("Sw", 2))));
		session.assume("definition_of_objective", forall("w", equality(apply("J", "w"), fraction(qForm("S_B", "w"), qForm("S_W", "w")))));
		
		session.printTo(System.out);
	}
	
	public static final Composite qForm(final Object innerMatrix, final Object outerMatrix) {
		return times(transpose(outerMatrix), times(innerMatrix, outerMatrix));
	}
	
	public static final Composite transpose(final Object matrix) {
		return composite(matrix, "ᵀ");
	}
	
	public static final Composite powerset(final Object set) {
		return apply("℘", set);
	}
	
	public static final Template forallNat(final String variableName, final Object proposition) {
		return forall(variableName, rule(nat(variableName), proposition));
	}
	
	public static final Template forall(final String variableName, final Object proposition) {
		return template(v(variableName), proposition);
	}
	
	public static final Template lambda(final String variableName, final Object proposition) {
		return forall(variableName, proposition);
	}
	
	public static final Composite card(final Object object) {
		return composite("|", object, "|");
	}
	
	public static final Composite sub(final Object container, final Object... keys) {
		return composite(container, "_", infix(",", keys));
	}
	
	public static final Composite infix(final Object operator, final Object... arguments) {
		final int n = arguments.length;
		final Object[] objects = new Object[n * 2 - 1];
		
		for (int i = 0; i < n; ++i) {
			objects[i * 2] = arguments[i];
		}
		
		for (int i = 1; i < n; ++i) {
			objects[i * 2 - 1] = operator;
		}
		
		return composite(objects);
	}
	
	public static final void addStandardFactsTo(final Session session) {
		session.assume(Session.IDENTITY, template(v("x"), equality("x", "x")));
		
		{
			session.prove(Session.SYMMETRY_OF_EQUALITY
					, template(v("x", "y"), rule(equality("x", "y"), equality("y", "x"))));
			
			session.introduce("declaration_of_x");
			session.introduce();
			session.introduce();
			session.rewriteLeft("declaration_of_x", -1, set(0));
		}
		
		session.printTo(System.out, true);
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
		return apply("S", object);
	}
	
	public static final Composite apply(final Object function, final Object... arguments) {
		return infix(" ", append(array(function), arguments));
	}
	
	public static final Composite nat(final Object object) {
		return membership(object, "ℕ");
	}
	
	public static final Composite membership(final Object element, final Object set) {
		return infix(" : ", element, set);
	}
	
	public static final Composite plus(final Object object1, final Object object2) {
		return infix(" + ", object1, object2);
	}
	
	public static final Composite minus(final Object object1, final Object object2) {
		return infix(" - ", object1, object2);
	}
	
	public static final Composite times(final Object object1, final Object object2) {
		return infix(" * ", object1, object2);
	}
	
	public static final Composite fraction(final Object object1, final Object object2) {
		return infix(" / ", object1, object2);
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