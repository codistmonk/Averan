package jrewrite;

import static jrewrite.JRewrite3Test.Sequence.s;
import static jrewrite.JRewrite3Test.Symbol.*;
import static net.sourceforge.aprog.tools.Tools.cast;
import static net.sourceforge.aprog.tools.Tools.debugPrint;
import static org.junit.Assert.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import jrewrite.JRewrite3Test.Expression;

import net.sourceforge.aprog.tools.Tools;

import org.junit.Test;

/**
 * @author codistmonk (creation 2013-10-26)
 */
public final class JRewrite3Test {
	
	@Test
	public final void test1() {
		debugPrint(new Date());
		
		final List<Expression> facts = new ArrayList<Expression>();
		final List<Expression> goals = new ArrayList<Expression>();
		final Symbol a = new Symbol("A");
		final Symbol b = new Symbol("B");
		
		facts.add(a);
		facts.add(new Sequence(a, IMPLIES, b));
		
		JRewrite2Test.ProofContext.printFactsAnsGoals(facts, goals, System.out);
		
		final ModusPonensRule rule = new ModusPonensRule();
		
		rule.apply(facts, 0, 1, goals, -1);
		
		JRewrite2Test.ProofContext.printFactsAnsGoals(facts, goals, System.out);
		
		assertEquals(b, facts.get(2));
	}
	
	@Test
	public final void test2() {
		debugPrint(new Date());
		
		final List<Expression> facts = new ArrayList<Expression>();
		final List<Expression> goals = new ArrayList<Expression>();
		final Symbol a = new Symbol("A");
		final Symbol b = new Symbol("B");
		
		facts.add(a);
		facts.add(new Sequence(a, IMPLIES, b));
		goals.add(b);
		
		JRewrite2Test.ProofContext.printFactsAnsGoals(facts, goals, System.out);
		
		final ModusPonensRule rule = new ModusPonensRule();
		
		rule.apply(facts, 0, -1, goals, 0);
		
		JRewrite2Test.ProofContext.printFactsAnsGoals(facts, goals, System.out);
		
		assertEquals(b, facts.get(2));
	}
	
	@Test
	public final void test3() {
		debugPrint(new Date());
		
		final List<Expression> facts = new ArrayList<Expression>();
		final List<Expression> goals = new ArrayList<Expression>();
		final Symbol a = new Symbol("A");
		final Symbol x = new Symbol("X");
		final Symbol equals = new Symbol("=");
		
		facts.add(a);
		facts.add(new LambdaExpression("X", new Sequence(x, equals, x)));
		goals.add(new Sequence(a, equals, a));
		
		JRewrite2Test.ProofContext.printFactsAnsGoals(facts, goals, System.out);
		
		new LambdaRule().apply(facts, 0, 1);
		
		JRewrite2Test.ProofContext.printFactsAnsGoals(facts, goals, System.out);
	}
	
	@Test
	public final void test4() {
		debugPrint(new Date());
		
		final List<Expression> facts = new ArrayList<Expression>();
		final List<Expression> goals = new ArrayList<Expression>();
		final Symbol a = new Symbol("a");
		final Symbol x = new Symbol("x");
		final Symbol r = new Symbol("R");
		final Symbol squared = new Symbol("^2");
		final Symbol isIn = new Symbol(":");
		
		facts.add(a);
		facts.add(s(a, isIn, r));
		facts.add(new LambdaExpression("x", s(s(x, isIn, r), IMPLIES, s(s(x, squared), isIn, r))));
		goals.add(s(s(a, squared), isIn, r));
		
		JRewrite2Test.ProofContext.printFactsAnsGoals(facts, goals, System.out);
		
		new LambdaRule().apply(facts, 0, 2);
		
		JRewrite2Test.ProofContext.printFactsAnsGoals(facts, goals, System.out);
		
		new ModusPonensRule().apply(facts, -1, -1, goals, 0);
		
		JRewrite2Test.ProofContext.printFactsAnsGoals(facts, goals, System.out);
		
		assertEquals(Collections.emptyList(), goals);
	}
	
	public static final List<Expression> find(final List<Expression> expressions, final Expression pattern) {
		final List<Expression> result = new ArrayList<Expression>();
		
		for (final Expression expression : expressions) {
			if (expression.matches(pattern)) {
				result.add(expression);
			}
		}
		
		return result;
	}
	
	/**
	 * @author codistmonk (creation 2013-10-26)
	 */
	public static final class LambdaRule implements Serializable {
		
		public final void apply(final List<Expression> facts, final int argumentIndex, final int lambdaExpressionIndex) {
			facts.add(((LambdaExpression) facts.get(lambdaExpressionIndex)).apply(facts.get(argumentIndex)));
		}
		
	}
	
	/**
	 * @author codistmonk (creation 2013-10-26)
	 */
	public static final class ModusPonensRule implements Serializable {
		
		public final void apply(final List<Expression> facts, final int antecedentIndex, final int conditionalIndex,
				final List<Expression> goals, final int conclusionIndex) {
			if (antecedentIndex < 0) {
				if (conditionalIndex < 0) {
					if (conclusionIndex < 0) {
						// [ _ ; _ -> _ ] |- _
						
						throw new RuntimeException("TODO");
					} else {
						// [ _ ; _ -> _ ] |- B
						
						final Expression conclusion = goals.get(conclusionIndex);
						final List<Expression> conclusions = find(facts, conclusion);
						
						if (!conclusions.isEmpty()) {
							goals.remove(conclusionIndex);
							
							return;
						}
						
						final List<Expression> conditionals = find(facts, new Sequence(ANY, IMPLIES, conclusion));
						
						for (final Expression conditional : conditionals) {
							final Expression antecedent = ((Sequence) conditional).getExpressions()[0];
							final List<Expression> antecedents = find(facts, antecedent);
							
							if (!antecedents.isEmpty()) {
								facts.add(conclusion);
								goals.remove(conclusionIndex);
							}
						}
					}
				} else {
					final Sequence conditional = (Sequence) facts.get(conditionalIndex);
					final Expression antecedent = conditional.getExpressions()[0];
					
					if (conclusionIndex < 0) {
						// [ _ ; A -> B ] |- _
						
						final List<Expression> antecedents = find(facts, antecedent);
						
						if (!antecedents.isEmpty()) {
							facts.add(conditional.getExpressions()[2]);
						}
					} else {
						// [ _ ; A -> B ] |- B
						
						final Expression conclusion = goals.get(conclusionIndex);
						
						if (!conclusion.matches(conditional.getExpressions()[2])) {
							return;
						}
						
						final List<Expression> antecedents = find(facts, antecedent);
						
						goals.remove(conclusionIndex);
						
						if (!antecedents.isEmpty()) {
							facts.add(conclusion);
						} else {
							goals.add(antecedent);
						}
					}
				}
			} else {
				final Expression antecedent = facts.get(antecedentIndex);
				
				if (conditionalIndex < 0) {
					if (conclusionIndex < 0) {
						// [ A ; _ -> _ ] |- _
						
						final List<Expression> conditionals = find(facts, new Sequence(antecedent, IMPLIES, ANY));
						
						for (final Expression conditional : conditionals) {
							facts.add(((Sequence) conditional).getExpressions()[2]);
						}
					} else {
						// [ A ; _ -> _ ] |- B
						
						final Expression conclusion = goals.get(conclusionIndex);
						final Sequence conditional = new Sequence(antecedent, IMPLIES, conclusion);
						final List<Expression> conditionals = find(facts, conditional);
						
						if (conditionals.isEmpty()) {
							goals.remove(conclusionIndex);
							goals.add(conditional);
						} else {
							facts.add(conclusion);
							goals.remove(conclusionIndex);
						}
					}
				} else {
					final Sequence conditional = (Sequence) facts.get(conditionalIndex);
					
					if (!antecedent.matches(conditional.getExpressions()[0])) {
						return;
					}
					
					if (conclusionIndex < 0) {
						// [ A ; A -> B ] |- _
						
						facts.add(conditional.getExpressions()[2]);
					} else {
						// [ A ; A -> B ] |- B
						
						final Expression conclusion = goals.get(conclusionIndex);
						
						if (!conclusion.matches(conditional.getExpressions()[2])) {
							return;
						}
						
						facts.add(conclusion);
						goals.remove(conclusionIndex);
					}
				}
			}
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = -654748664080601751L;
		
	}
	
	public static final String join(final Object[] list, final String separator) {
		final StringBuilder resultBuilder = new StringBuilder();
		final int n = list.length;
		
		if (0 < n) {
			resultBuilder.append(list[0]);
			
			for (int i = 1; i < n; ++i) {
				resultBuilder.append(separator).append(list[i]);
			}
		}
		
		return resultBuilder.toString();
	}
	
	/**
	 * @author codistmonk (creation 2013-10-26)
	 */
	public static abstract interface Expression extends Serializable {
		
		public abstract boolean matches(Expression expression);
		
		public abstract Expression subsitute(String symbol, Expression expression);
		
	}
	
	/**
	 * @author codistmonk (creation 2013-10-27)
	 */
	public static final class LambdaExpression implements Expression {
		
		private final Symbol symbol;
		
		private final Expression scope;
		
		public LambdaExpression(final String symbol, final Expression scope) {
			this.symbol = new Symbol(symbol);
			this.scope = scope;
		}
		
		public final Symbol getSymbol() {
			return this.symbol;
		}
		
		public final Expression getScope() {
			return this.scope;
		}
		
		public final Expression apply(final Expression value) {
			return this.getScope().subsitute(this.getSymbol().toString(), value);
		}
		
		@Override
		public final Expression subsitute(final String symbol, final Expression expression) {
			return new LambdaExpression(symbol, symbol.equals(this.getSymbol().toString()) ?
					this.getScope() : this.getScope().subsitute(symbol, expression));
		}
		
		@Override
		public final boolean matches(final Expression expression) {
			final LambdaExpression that = cast(this.getClass(), expression);
			
			return that != null && this.getScope().matches(that.apply(this.getSymbol()));
		}

		@Override
		public final int hashCode() {
			return this.getSymbol().hashCode() + this.getScope().hashCode();
		}
		
		@Override
		public final boolean equals(final Object object) {
			final LambdaExpression that = cast(this.getClass(), object);
			
			return that != null && this.getSymbol().equals(that.getSymbol()) && this.getScope() == that.getScope();
		}
		
		@Override
		public final String toString() {
			return this.getSymbol() + " |-> " + this.getScope();
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = -6376147234565655987L;
		
	}
	
	/**
	 * @author codistmonk (creation 2013-10-26)
	 */
	public static final class Sequence implements Expression {
		
		private final Expression[] expressions;
		
		public Sequence(final Expression... expressions) {
			this.expressions = expressions;
		}
		
		public final Expression[] getExpressions() {
			return this.expressions;
		}
		
		@Override
		public final Sequence subsitute(final String symbol, final Expression expression) {
			final int n = this.getExpressions().length;
			final Expression[] newExpressions = new Expression[n];
			
			for (int i = 0; i < n; ++i) {
				newExpressions[i] = this.getExpressions()[i].subsitute(symbol, expression);
			}
			
			return new Sequence(newExpressions);
		}
		
		@Override
		public final boolean matches(final Expression expression) {
			final Sequence sequence = cast(Sequence.class, expression);
			
			if (sequence != null) {
				final int n = this.getExpressions().length;
				
				if (n != sequence.getExpressions().length) {
					return false;
				}
				
				for (int i = 0; i < n; ++i) {
					if (!this.getExpressions()[i].matches(sequence.getExpressions()[i])) {
						return false;
					}
				}
				
				return true;
			}
			
			final Symbol symbol = cast(Symbol.class, expression);
			
			if (symbol != null && (symbol.equals(ANY) || this.getExpressions().length == 1)) {
				return this.getExpressions()[0].matches(symbol);
			}
			
			return false;
		}
		
		@Override
		public final int hashCode() {
			return Arrays.hashCode(this.getExpressions());
		}
		
		@Override
		public final boolean equals(final Object object) {
			final Sequence that = cast(this.getClass(), object);
			
			return that != null && Arrays.equals(this.getExpressions(), that.getExpressions());
		}
		
		@Override
		public final String toString() {
			return join(this.getExpressions(), " ");
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = 2479826737674803604L;
		
		public static final Sequence s(final Expression... expressions) {
			return new Sequence(expressions);
		}
		
	}
	
	/**
	 * @author codistmonk (creation 2013-10-26)
	 */
	public static final class Symbol implements Expression {
		
		private final String representation;
		
		public Symbol(final String representation) {
			this.representation = representation;
		}
		
		@Override
		public final Expression subsitute(final String symbol, final Expression expression) {
			return this.toString().equals(symbol) ? expression : this;
		}
		
		@Override
		public final boolean matches(final Expression expression) {
			return "_".equals(this.toString()) || "_".equals(expression.toString()) ||
					this.toString().equals(expression.toString());
		}
		
		@Override
		public final int hashCode() {
			return this.toString().hashCode();
		}
		
		@Override
		public final boolean equals(final Object object) {
			final Symbol that = cast(this.getClass(), object);
			
			return that != null && this.toString().equals(that.toString());
		}
		
		@Override
		public final String toString() {
			return this.representation;
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = -8766974220909149386L;
		
		public static final Symbol ANY = new Symbol("_");
		
		public static final Symbol IMPLIES = new Symbol("->");
		
	}
	
	
}
