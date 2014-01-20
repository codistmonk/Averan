package jrewrite;

import static net.sourceforge.aprog.tools.Tools.array;
import static net.sourceforge.aprog.tools.Tools.cast;
import static org.junit.Assert.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

/**
 * @author codistmonk (creation 2013-12-16)
 */
public final class JRewrite4Test {
	
	@Test
	public final void test1() {
		assertTrue(variable("x").matches(operation(constant("1"), "+", constant("2")), new HashMap<String, Expression>()));
		assertTrue(operation(constant("1"), "+", constant("2")).matches(operation(constant("1"), "+", constant("2")), new HashMap<String, Expression>()));
		assertFalse(operation(constant("1"), "+", constant("1")).matches(operation(constant("1"), "+", constant("2")), new HashMap<String, Expression>()));
		assertFalse(operation(variable("x"), "+", variable("x")).matches(operation(constant("1"), "+", constant("2")), new HashMap<String, Expression>()));
		assertTrue(operation(variable("x"), "+", variable("x")).matches(operation(constant("1"), "+", constant("1")), new HashMap<String, Expression>()));
	}
	
	@Test
	public final void test2() {
		final List<Expression> facts = new ArrayList<Expression>();
		final Rule commutativity = rule(
				operation(variable("x"), "=", variable("y")),
				operation(variable("y"), "=", variable("x")));
		
		facts.add(operation(constant("1"), "=", constant("2")));
		commutativity.apply(facts, facts.get(0));
		
		assertEquals("2=1", facts.get(1).toString());
	}
	
	@Test
	public final void test3() {
		final List<Expression> facts = new ArrayList<Expression>();
		
		facts.add(operation(constant("2"), "=", operation(constant("1"), "+", constant("1"))));
		facts.add(operation(constant("2"), "=", constant("2")));
		facts.add(operation(constant("2"), "=", constant("2")));
		
		rewrite(facts, 0, 1, 0);
		rewrite(facts, 0, 2, 1);
		
		assertEquals("1+1=2", facts.get(1).toString());
		assertEquals("2=1+1", facts.get(2).toString());
	}
	
	public static final Variable variable(final String symbol) {
		return new Variable(symbol);
	}
	
	public static final Constant constant(final String symbol) {
		return new Constant(symbol);
	}
	
	public static final Composite operation(final Expression left, final String operator, final Expression right) {
		return new Composite(left, constant(operator), right);
	}
	
	public static final Rule rule(final Expression condition, final Expression prototype) {
		return new Rule(array(condition), array(prototype));
	}
	
	public static final void rewrite(final List<Expression> facts, final int equalityIndex, final int targetExpressionIndex, final int subExpressionIndex) {
		final List<Expression> equality = ((Composite) facts.get(equalityIndex)).getSubexpressions();
		
		if (equality.size() != 3 || !"=".equals(equality.get(1).toString())) {
			throw new IllegalArgumentException(equality.toString());
		}
		
		facts.set(targetExpressionIndex, facts.get(targetExpressionIndex).rewrite(
				equality.get(0), new AtomicInteger(subExpressionIndex), equality.get(2)));
	}
	
	/**
	 * @author codistmonk (creation 2013-12-16)
	 */
	public static abstract interface Expression extends Serializable {
		
		public abstract boolean matches(Expression expression, Map<String, Expression> context);
		
		public abstract Expression refine(Map<String, Expression> context);
		
		public abstract Expression rewrite(Expression pattern, AtomicInteger index, Expression replacement);
		
	}
	
	/**
	 * @author codistmonk (creation 2013-12-16)
	 */
	public static final class Variable implements Expression {
		
		private final String symbol;
		
		public Variable(final String symbol) {
			this.symbol = symbol;
		}
		
		@Override
		public final int hashCode() {
			return this.symbol.hashCode();
		}
		
		@Override
		public final boolean equals(final Object object) {
			final Variable that = cast(this.getClass(), object);
			
			return that != null && this.symbol.equals(that.symbol);
		}
		
		@Override
		public final boolean matches(final Expression expression,
				final Map<String, Expression> context) {
			final Expression match = context.get(this.symbol);
			
			if (match != null) {
				return match.matches(expression, context);
			}
			
			context.put(this.symbol, expression);
			
			return true;
		}
		
		@Override
		public final Expression refine(final Map<String, Expression> context) {
			final Expression match = context.get(this.symbol);
			
			return match != null ? match : this;
		}
		
		@Override
		public final Expression rewrite(final Expression pattern, final AtomicInteger index,
				final Expression replacement) {
			return this.equals(pattern) && index.decrementAndGet() < 0 ? replacement : this;
		}
		
		@Override
		public final String toString() {
			return this.symbol;
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = 7582719153138235143L;
		
	}
	
	/**
	 * @author codistmonk (creation 2013-12-16)
	 */
	public static final class Constant implements Expression {
		
		private final String symbol;
		
		public Constant(final String symbol) {
			this.symbol = symbol;
		}
		
		@Override
		public final int hashCode() {
			return this.symbol.hashCode();
		}
		
		@Override
		public final boolean equals(final Object object) {
			final Constant that = cast(this.getClass(), object);
			
			return that != null && this.symbol.equals(that.symbol);
		}
		
		@Override
		public final boolean matches(final Expression expression,
				final Map<String, Expression> context) {
			if (expression instanceof Variable) {
				return expression.matches(this, context);
			}
			
			return this.toString().equals(expression.toString());
		}
		
		@Override
		public final Constant refine(final Map<String, Expression> context) {
			return this;
		}
		
		@Override
		public final Expression rewrite(final Expression pattern, final AtomicInteger index,
				final Expression replacement) {
			return this.equals(pattern) && index.decrementAndGet() < 0 ? replacement : this;
		}
		
		@Override
		public final String toString() {
			return this.symbol;
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = 4327968938495811368L;
		
	}
	
	/**
	 * @author codistmonk (creation 2013-12-16)
	 */
	public static final class Composite implements Expression {
		
		private final List<Expression> subexpressions;
		
		public Composite(final Expression... subexpressions) {
			this.subexpressions = new ArrayList<Expression>();
			
			for (final Expression subexpression : subexpressions) {
				this.getSubexpressions().add(subexpression);
			}
		}
		
		public Composite(final Iterable<Expression> subexpressions) {
			this.subexpressions = new ArrayList<Expression>();
			
			for (final Expression subexpression : subexpressions) {
				this.getSubexpressions().add(subexpression);
			}
		}
		
		public final List<Expression> getSubexpressions() {
			return this.subexpressions;
		}
		
		@Override
		public final int hashCode() {
			return this.getSubexpressions().hashCode();
		}
		
		@Override
		public final boolean equals(final Object object) {
			final Composite that = cast(this.getClass(), object);
			
			return that != null && this.getSubexpressions().equals(that.getSubexpressions());
		}

		@Override
		public final boolean matches(final Expression expression, final Map<String, Expression> context) {
			if (expression instanceof Variable) {
				return expression.matches(this, context);
			}
			
			final Composite that = cast(this.getClass(), expression);
			
			if (that == null) {
				return false;
			}
			
			final int n = this.getSubexpressions().size();
			
			if (n != that.getSubexpressions().size()) {
				return false;
			}
			
			for (int i = 0; i < n; ++i) {
				if (!this.getSubexpressions().get(i).matches(that.getSubexpressions().get(i), context)) {
					return false;
				}
			}
			
			return true;
		}
		
		@Override
		public final Composite refine(final Map<String, Expression> context) {
			final Composite result = new Composite();
			
			for (final Expression subexpression : this.getSubexpressions()) {
				result.getSubexpressions().add(subexpression.refine(context));
			}
			
			return result;
		}
		
		@Override
		public final Expression rewrite(final Expression pattern, final AtomicInteger index,
				final Expression replacement) {
			if (this.equals(pattern) && index.decrementAndGet() < 0) {
				return replacement;
			}
			
			final int n = this.getSubexpressions().size();
			
			for (int i = 0; i < n; ++i) {
				final Expression subExpression = this.getSubexpressions().get(i).rewrite(pattern, index, replacement);
				
				if (index.get() < 0) {
					final Composite result = new Composite(this.getSubexpressions());
					
					result.getSubexpressions().set(i, subExpression);
					
					return result;
				}
			}
			
			return this;
		}
		
		@Override
		public final String toString() {
			final StringBuilder resultBuilder = new StringBuilder();
			
			for (final Expression subexpression : this.getSubexpressions()) {
				resultBuilder.append(subexpression);
			}
			
			return resultBuilder.toString();
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = -4963212000398975579L;
		
	}
	
	/**
	 * @author codistmonk (creation 2013-12-16)
	 */
	public static final class Rule implements Serializable {
		
		private final Expression[] conditions;
		
		private final Expression[] prototypes;
		
		public Rule(final Expression[] conditions, final Expression[] prototypes) {
			this.conditions = conditions;
			this.prototypes = prototypes;
		}
		
		public final Expression[] getConditions() {
			return this.conditions;
		}
		
		public final Expression[] getPrototypes() {
			return this.prototypes;
		}
		
		public final boolean apply(final Collection<Expression> resultFacts, final Expression... conditionFacts) {
			final int n = this.getConditions().length;
			
			if (n != conditionFacts.length) {
				return false;
			}
			
			final Map<String, Expression> context = new HashMap<String, Expression>();
			
			for (int i = 0; i < n; ++i) {
				if (!this.getConditions()[i].matches(conditionFacts[i], context)) {
					return false;
				}
			}
			
			for (final Expression prototype : this.getPrototypes()) {
				resultFacts.add(prototype.refine(context));
			}
			
			return true;
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = 8433595727266763956L;
		
	}
	
}
