package jrewrite;

import static net.sourceforge.aprog.tools.Tools.cast;
import static net.sourceforge.aprog.tools.Tools.join;
import static net.sourceforge.aprog.tools.Tools.set;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jrewrite.InteractiveRewrite.Context.Item;

import net.sourceforge.aprog.tools.IllegalInstantiationException;
import net.sourceforge.aprog.tools.Tools;

/**
 * @author codistmonk (creation 2014-06-03)
 */
public final class InteractiveRewrite {
	
	private InteractiveRewrite() {
		throw new IllegalInstantiationException();
	}
	
	/**
	 * @param commandLineArguments
	 * <br>Unused
	 */
	public static final void main(final String[] commandLineArguments) {
		assert test1();
		assert test2();
		assert test3();
	}
	
	public static final boolean test3() {
		final Session session = new Session();
		
		session.assume("identity", template(v("E"), equality("E", "E")));
		
		session.printTo(System.out);
		
		{
			session.prove("symmetry of equality", template(v("a", "b"), rule(equality("a", "b"), equality("b", "a"))));
			
			session.printTo(System.out);
			
			session.introduce();
			session.printTo(System.out);
			session.introduce();
			session.printTo(System.out);
			session.introduce();
			session.printTo(System.out);
			
			session.bind("identity", "E", session.getExpression("#2"));
			session.express(-1);
			session.printTo(System.out);
			
			session.rewrite("#6", -1, set(0));
			
			session.printTo(System.out);
		}
		
		session.assume("trueness1", template(v("E"), rule(equality("E", "true"), "E")));
		session.assume("trueness2", template(v("E"), rule("E", equality("E", "true"))));
		session.assume("definition of 0", nat("0"));
		session.assume("definition of S", template(v("n"), rule(nat("n"), nat(s("n")))));
		session.assume("definition of recursivity", template(v("P"), rule(apply1("P", "0")
				, rule(template(v("n"), rule(nat("n"), rule(apply1("P", "n"), apply1("P", s("n")))))
						, template(v("n"), apply1("P", "n"))))));
		session.assume("definition of 1", equality("1", s("0")));
		
		session.printTo(System.out);
		
		{
			session.prove("nat1", nat("1"));
			
			session.bind("symmetry of equality", "a", "1");
			session.bind(-1, "b", s("0"));
			session.express(-1);
			session.apply("S 0=>1", -1, "definition of 1");
			
			session.bind("definition of S", "n", "0");
			session.express(-1);
			session.apply(-1, "definition of 0");
			session.rewrite("S 0=>1", -1, set(0));

			session.printTo(System.out);
		}
		
		session.assume("right neutrality of 0", template(v("a")
				, rule(nat("a"), equality(plus("a", "0"), "a"))));
		session.assume("right recursivity of addition", template(v("a", "b")
				, rule(nat("a"), rule(nat("b"), equality(plus("a", s("b")), s(plus("a", "b")))))));
		
		session.printTo(System.out);
		
		{
			session.prove(template(v("a"), rule(nat("a"), equality(plus("a", "1"), plus("1", "a")))));
			session.assume("definition of P", template(v("a"), equality(apply1("P", "a"), equality(plus("a", "1"), plus("1", "a")))));
			
			session.printTo(System.out);
			
			session.prove(apply1("P", "0"));
			session.bind("definition of P", "a", "0");
			session.express("expression of P 0 (a)", -1);
			
			session.bind("right recursivity of addition", "a", "0");
			session.bind(-1, "b", "0");
			session.express(-1);
			session.apply(-1, "definition of 0");
			session.apply("S0+0", -1, "definition of 0");
			session.bind("right neutrality of 0", "a", "0");
			session.express(-1);
			session.apply("0+0=>0", -1, "definition of 0");
			session.bind("symmetry of equality", "a", "1");
			session.bind(-1, "b", s("0"));
			session.express(-1);
			session.apply("S 0=>1", -1, "definition of 1");
			session.rewrite("0+0=>0", "S0+0", set(0));
			session.rewrite("S 0=>1", -1, set(0, 1));
			session.rewrite("expression of P 0 (b)", -1, "expression of P 0 (a)", set(0, 1));
			session.bind("right neutrality of 0", "a", "1");
			session.express(-1);
			session.apply(-1, "nat1");
			session.rewrite("expression of P 0 (c)", -1, "expression of P 0 (b)", set(0));
			session.bind("identity", "E", "1");
			session.express("1=1", -1);
			session.bind("trueness2", "E", equality("1", "1"));
			session.express(-1);
			session.apply(-1, "1=1");
			session.rewrite("expression of P 0 (d)", -1, "expression of P 0 (c)", set(0));
			session.bind("trueness1", "E", apply1("P", "0"));
			session.express(-1);
			session.apply(-1, "expression of P 0 (d)");
			
			session.printTo(System.out);
			
			{
				session.prove(template(v("n"), rule(nat("n"), rule(apply1("P", "n"), apply1("P", s("n"))))));
				session.printTo(System.out);
				session.introduce();
				session.printTo(System.out);
				session.introduce();
				session.printTo(System.out);
				session.introduce();
				session.bind("definition of P", "a", "n");
				session.express("expression of P n (a)", -1);
				session.bind("definition of P", "a", s("n"));
				session.express("expression of P (S n) (a)", -1);
				
				session.printTo(System.out);
			}
		}
		
		return session.isGoalReached();
	}
	
	public static final List<Object> apply1(final Object function, final Object argument) {
		return list(function, " ", argument);
	}
	
	public static final List<Object> plus(final Object expression1, final Object expression2) {
		return list(expression1, "+", expression2);
	}
	
	public static final boolean test2() {
		final Session context = new Session();
		
		context.assume(nat("0"));
		context.assume(template(v("n"), rule(nat("n"), nat(s("n")))));
		
		context.printTo(System.out);
		
		{
			context.prove(template(v("n"), rule(nat("n"), nat(s(s("n"))))));
			
			context.printTo(System.out);
			
			{
				context.introduce();
				
				context.printTo(System.out);
				
				{
					context.introduce();
					
					context.printTo(System.out);
					
					context.bind("#1", "n", "n");
					context.express(-1);
					
					context.printTo(System.out);
					
					context.apply(-1, "#5");
					
					context.printTo(System.out);
					
					context.bind("#1", "n", s("n"));
					context.express(-1);
					context.apply(-1, "#9");
					
					context.printTo(System.out);
				}
			}
		}
		
		return context.isGoalReached();
	}
	
	public static final boolean test1() {
		final Session context = new Session();
		
		for (int i = 0; i <= 9; ++i) {
			context.assume("digit" + i, list("" + i, ":", "N"));
		}
		
		context.assume("concatN", template(v("x", "y"), rule(nat("x"), rule(nat("y"), nat(list("x", "y"))))));
		
		context.printTo(System.out);
		
		{
			context.prove(nat(list("4", "2")));
			
			context.printTo(System.out);
			
			context.bind("concatN", "x", "4");
			
			context.printTo(System.out);
			
			context.bind(-1, "y", "2");
			
			context.printTo(System.out);
			
			context.express(-1);
			
			context.printTo(System.out);
			
			context.apply(-1, "digit4");
			
			context.printTo(System.out);
			
			context.apply(-1, "digit2");
			
			context.printTo(System.out);
		}
		
		context.printTo(System.out);
		
		return context.isGoalReached();
	}
	
	public static final List<Object> s(final Object expression) {
		return apply1("S", expression);
	}
	
	public static final List<Object> nat(final Object expression) {
		return list(expression, ":", "N");
	}
	
	public static final List<Object> list(final Object... list) {
		return Arrays.asList(list);
	}
	
	public static final Rule rule(final Object condition, final Object expression) {
		return new Rule(condition, expression);
	}
	
	public static final Template template(final Collection<String> variables, final Object expression) {
		return new Template(variables, expression);
	}
	
	public static final Collection<String> v(final String... variables) {
		return Tools.set(variables);
	}
	
	@SuppressWarnings("unchecked")
	public static final String deepToString(final Object object) {
		if (object instanceof List) {
			final StringBuilder resultBuilder = new StringBuilder();
			
			for (final Object element : (List<Object>) object) {
				if (element instanceof List) {
					resultBuilder.append('(').append(deepToString(element)).append(')');
				} else {
					resultBuilder.append(deepToString(element));
				}
			}
			
			return resultBuilder.toString();
		}
		
		return object.toString();
	}
	
	public static final Object deepReplace(final String variable, final Object value, final Object target) {
		return deepRewrite(new Rewriter() {
			
			@Override
			public final Object rewrite(final Object target) {
				return value;
			}
			
			@Override
			public final boolean canRewrite(final Object target) {
				return target.equals(variable);
			}
			
			@Override
			public final boolean canRewrite(final Template target) {
				return !target.getVariables().contains(variable);
			}
			
			/**
			 * {@value}.
			 */
			private static final long serialVersionUID = -670474044151868974L;
			
		}, target);
	}
	
	@SuppressWarnings("unchecked")
	public static final Object deepRewrite(final Rewriter rewriter, final Object target) {
		if (rewriter.canRewrite(target)) {
			return rewriter.rewrite(target);
		}
		
		if (target instanceof Rule) {
			final Rule rule = (Rule) target;
			
			return new Rule(deepRewrite(rewriter, rule.getCondition())
					, deepRewrite(rewriter, rule.getExpression()));
		}
		
		if (target instanceof Template) {
			final Template template = (Template) target;
			
			if (!rewriter.canRewrite(template)) {
				return template;
			}
			
			return new Template(template.getVariables(), deepRewrite(rewriter, template.getExpression()));
			
		}
		
		if (target instanceof Iterable) {
			Collection<Object> result;
			
			try {
				result = (Collection<Object>) target.getClass().newInstance();
			} catch (final Exception exception) {
				result = new ArrayList<>();
			}
			
			for (final Object subtarget : (Iterable<?>) target) {
				result.add(deepRewrite(rewriter, subtarget));
			}
			
			return result;
		}
		
		return target;
	}
	
	public static final String EQUALITY_OPERATOR = " = ";
	
	public static final boolean isProposition(final Object expression) {
		return expression instanceof Rule || isEquality(expression);
	}
	
	public static final boolean isEquality(final Object expression) {
		if (expression instanceof List) {
			@SuppressWarnings("unchecked")
			final List<Object> list = (List<Object>) expression;
			
			if (list.size() == 3 && EQUALITY_OPERATOR.equals(list.get(1))) {
				return true;
			}
		}
		
		return false;
	}
	
	public static final List<Object> equality(final Object expression1, final Object expression2) {
		return list(expression1, EQUALITY_OPERATOR, expression2);
	}
	
	public static final List<Object> castToEquality(final Object expression) {
		@SuppressWarnings("unchecked")
		final List<Object> result = (List<Object>) expression;
		
		if (!isEquality(expression)) {
			throw new IllegalArgumentException();
		}
		
		return result;
	}
	
	/**
	 * @author codistmonk (creation 2014-06-03)
	 */
	public static final class Rule implements Serializable {
		
		private final Object condition;
		
		private final Object expression;
		
		public Rule(final Object condition, final Object expression) {
			this.condition = condition;
			this.expression = expression;
		}
		
		public final Object apply(final Object test) {
			if (this.getCondition().equals(test)) {
				return this.getExpression();
			}
			
			throw new IllegalArgumentException();
		}
		
		public final Object getCondition() {
			return this.condition;
		}
		
		public final Object getExpression() {
			return this.expression;
		}
		
		@Override
		public final int hashCode() {
			return this.getCondition().hashCode() + this.getExpression().hashCode();
		}
		
		@Override
		public final boolean equals(final Object object) {
			final Rule that = cast(this.getClass(), object);
			
			return that != null && this.getCondition().equals(that.getCondition())
					&& this.getExpression().equals(that.getExpression());
		}

		@Override
		public final String toString() {
			return "(" + deepToString(this.getCondition()) + ") -> (" + deepToString(this.getExpression()) + ")";
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = 128535452491808168L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-06-03)
	 */
	public static final class Template implements Serializable {
		
		private final Collection<String> variables;
		
		private final Object expression;
		
		public Template(final Collection<String> variables, final Object expression) {
			this.variables = variables;
			this.expression = expression;
		}
		
		public final Template bind(final String variable, final Object value) {
			if (!this.getVariables().contains(variable)) {
				throw new IllegalArgumentException();
			}
			
			final Object resultExpression = deepReplace(variable, value, this.getExpression());
			final Collection<String> resultVariables = new LinkedHashSet<String>(this.getVariables());
			
			resultVariables.remove(variable);
			
			return new Template(resultVariables, resultExpression);
		}
		
		public final Collection<String> getVariables() {
			return this.variables;
		}
		
		public final Object getExpression() {
			return this.expression;
		}
		
		@Override
		public final int hashCode() {
			return this.getVariables().hashCode() + this.getExpression().hashCode();
		}
		
		@Override
		public final boolean equals(final Object object) {
			final Template that = cast(this.getClass(), object);
			
			return that != null
					&& this.getVariables().equals(that.getVariables())
					&& this.getExpression().equals(that.getExpression());
		}
		
		@Override
		public final String toString() {
			final String string = this.getVariables().toString();
			
			return "{" + string.substring(1, string.length() - 1) + "} => (" + deepToString(this.getExpression()) + ")";
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = -1650284933791899885L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-06-03)
	 */
	public static final class Context implements Serializable {
		
		private final Context parent;
		
		private final List<Item> items;
		
		private final Map<String, Item> map;
		
		private final Object goal;
		
		private boolean goalReached;
		
		public Context() {
			this(null);
		}
		
		public Context(final Context parent) {
			this(parent, Special.TRUE);
		}
		
		public Context(final Context parent, final Object goal) {
			this.parent = parent;
			this.items = new ArrayList<>();
			this.map = new HashMap<>();
			this.goal = goal;
			this.goalReached = Special.TRUE.equals(goal);
		}
		
		public final Context getParent() {
			return this.parent;
		}
		
		public final Context introduce() {
			final Context result;
			
			if (this.goal instanceof Rule) {
				final Rule goal = (Rule) this.goal;
				result = new Context(this, goal.getExpression());
				
				result.assume(goal.getCondition());
			} else if (this.goal instanceof Template) {
				final Template template = (Template) this.goal;
				
				if (template.getVariables().isEmpty()) {
					final Rule goal = (Rule) template.getExpression();
					result = new Context(this, goal.getExpression());
					
					result.assume(goal.getCondition());
				} else {
					final String variable = template.getVariables().iterator().next();
					result = new Context(this, template.bind(variable, variable));
					
					result.assume(variable);
				}
			} else {
				throw new IllegalArgumentException();
			}
			
			final String name = this.newName();
			
			this.addItem(name, new Item(equality(name, this.goal), result));
			
			return result;
		}
		
		// BEGIN DEDUCTIONS
		
		public final void express(final int templateRuleIndex) {
			this.express(this.newName(), (Template) this.getExpression(templateRuleIndex));
		}
		
		public final void express(final String templateRuleName) {
			this.express(this.newName(), (Template) this.getExpression(templateRuleName));
		}
		
		public final void express(final String name, final int templateRuleIndex) {
			this.express(name, (Template) this.getExpression(templateRuleIndex));
		}
		
		public final void express(final String name, final String templateRuleName) {
			this.express(name, (Template) this.getExpression(templateRuleName));
		}
		
		private final void express(final String name, final Template ruleTemplate) {
			if (!isProposition(ruleTemplate.getExpression()) || !ruleTemplate.getVariables().isEmpty()) {
				throw new IllegalArgumentException();
			}
			
			this.addItem(name, new Item(equality(name, ruleTemplate.getExpression()), Special.DEDUCTION));
		}
		
		public final void bind(final int templateRuleIndex, final String variable, final Object value) {
			this.bind(this.newName(), templateRuleIndex, variable, value);
		}
		
		public final void bind(final String templateRuleName, final String variable, final Object value) {
			this.bind(this.newName(), templateRuleName, variable, value);
		}
		
		public final void bind(final String name, final int ruleTemplateIndex, final String variable, final Object value) {
			this.bind(name, (Template) this.getExpression(ruleTemplateIndex), variable, value);
		}
		
		public final void bind(final String name, final String ruleTemplateName, final String variable, final Object value) {
			this.bind(name, (Template) this.getExpression(ruleTemplateName), variable, value);
		}
		
		private final void bind(final String name, final Template ruleTemplate, final String variable, final Object value) {
			if (!isProposition(ruleTemplate.getExpression())) {
				throw new IllegalArgumentException();
			}
			
			this.addItem(name, new Item(equality(name, ruleTemplate.bind(variable, value)), Special.DEDUCTION));
		}
		
		public final void apply(final int ruleIndex, final int testIndex) {
			this.apply(this.newName(), ruleIndex, testIndex);
		}
		
		public final void apply(final int ruleIndex, final String testName) {
			this.apply(this.newName(), ruleIndex, testName);
		}
		
		public final void apply(final String ruleName, final int testIndex) {
			this.apply(this.newName(), ruleName, testIndex);
		}
		
		public final void apply(final String ruleName, final String testName) {
			this.apply(this.newName(), ruleName, testName);
		}
		
		public final void apply(final String name, final int ruleIndex, final int testIndex) {
			this.apply(name, (Rule) this.getExpression(ruleIndex), this.getExpression(testIndex));
		}
		
		public final void apply(final String name, final int ruleIndex, final String testName) {
			this.apply(name, (Rule) this.getExpression(ruleIndex), this.getExpression(testName));
		}
		
		public final void apply(final String name, final String ruleName, final int testIndex) {
			this.apply(name, (Rule) this.getExpression(ruleName), this.getExpression(testIndex));
		}
		
		public final void apply(final String name, final String ruleName, final String testName) {
			this.apply(name, (Rule) this.getExpression(ruleName), this.getExpression(testName));
		}
		
		private final void apply(final String name, final Rule rule, final Object expression) {
			final Object newExpression = rule.apply(expression);
			this.addItem(name, new Item(equality(name, newExpression), Special.DEDUCTION));
		}
		
		// END DEDUCTIONS
		
		public final void assume(final Object expression) {
			this.assume(this.newName(), expression);
		}
		
		public final void assume(final String name, final Object expression) {
			this.addItem(name, new Item(equality(name, expression), Special.AXIOM));
		}
		
		public final Context prove(final Object goal) {
			return this.prove(this.newName(), goal);
		}
		
		public final Context prove(final String name, final Object goal) {
			final Context result = new Context(this, goal);
			
			this.addItem(name, new Item(equality(name, goal), result));
			
			return result;
		}
		
		public final void rewrite(final int equalityIndex, final int targetIndex, final Set<Integer> indices) {
			this.rewrite(this.newName(), equalityIndex, targetIndex, indices);
		}
		
		public final void rewrite(final String name, final int equalityIndex, final int targetIndex, final Set<Integer> indices) {
			this.rewrite(name, this.getExpression(equalityIndex), this.getExpression(targetIndex), indices);
		}
		
		public final void rewrite(final String equalityName, final int targetIndex, final Set<Integer> indices) {
			this.rewrite(this.newName(), equalityName, targetIndex, indices);
		}
		
		public final void rewrite(final String name, final String equalityName, final int targetIndex, final Set<Integer> indices) {
			this.rewrite(name, this.getExpression(equalityName), this.getExpression(targetIndex), indices);
		}
		
		public final void rewrite(final int equalityIndex, final String targetName, final Set<Integer> indices) {
			this.rewrite(this.newName(), equalityIndex, targetName, indices);
		}
		
		public final void rewrite(final String name, final int equalityIndex, final String targetName, final Set<Integer> indices) {
			this.rewrite(name, this.getExpression(equalityIndex), this.getExpression(targetName), indices);
		}
		
		public final void rewrite(final String equalityName, final String targetName, final Set<Integer> indices) {
			this.rewrite(this.newName(), equalityName, targetName, indices);
		}
		
		public final void rewrite(final String name, final String equalityName, final String targetName, final Set<Integer> indices) {
			this.rewrite(name, this.getExpression(equalityName), this.getExpression(targetName), indices);
		}
		
		private final void rewrite(final String name, final Object source, final Object target, final Set<Integer> indices) {
			final List<Object> equality = castToEquality(source);
			final Object pattern = equality.get(0);
			final boolean[] rewritten = { false };
			final Object newFact = deepRewrite(new Rewriter() {
				
				private int index;
				
				@Override
				public final Object rewrite(final Object target) {
					return equality.get(2);
				}
				
				@Override
				public final boolean canRewrite(final Object target) {
					final boolean result = pattern.equals(target) && indices.contains(this.index++);
					
					rewritten[0] |= result;
					
					return result;
				}
				
				@Override
				public final boolean canRewrite(final Template target) {
					// TODO Auto-generated method stub
					return true;
				}
				
				/**
				 * {@value}.
				 */
				private static final long serialVersionUID = 6362319794903172199L;
				
			}, target);
			
			if (rewritten[0]) {
				this.addItem(name, new Item(equality(name, newFact), Special.DEDUCTION));
			}
		}
		
		public final boolean isGoalReached() {
			return this.goalReached;
		}
		
		public final Item getItem(final int index) {
			final int n = this.getItemCount();
			final int normalizedIndex = (n + index) % n;
			
			if (this.getParent() != null) {
				final int parentItemCount = this.getParent().getItemCount();
				
				if (normalizedIndex < parentItemCount) {
					return this.getParent().getItem(normalizedIndex);
				}
				
				return this.items.get(normalizedIndex - parentItemCount);
			}
			
			return this.items.get(normalizedIndex);
		}
		
		public final Item getItem(final String name) {
			final Item item = this.map.get(name);
			
			return item == null && this.getParent() != null ? this.getParent().getItem(name) : item;
		}
		
		public final List<Object> getDefinition(final int index) {
			return this.getItem(index).getDefinition();
		}
		
		public final List<Object> getDefinition(final String name) {
			return this.getItem(name).getDefinition();
		}
		
		public final Object getExpression(final int index) {
			return this.getDefinition(index).get(2);
		}
		
		public final Object getExpression(final String name) {
			return this.getDefinition(name).get(2);
		}
		
		public final int getDepth() {
			return this.getParent() == null ? 0 : 1 + this.getParent().getDepth();
		}
		
		public final void printTo(final PrintStream output) {
			output.println();
			
			final String indent = join("", Collections.nCopies(this.getDepth(), "\t").toArray());
			
			for (final Item item : this.items) {
				final Object proof = item.getProof();
				final String proofStatus;
				
				if (Special.DEDUCTION.equals(proof)) {
					proofStatus = "OK";
				} else if (proof instanceof Context && ((Context) proof).isGoalReached()) {
					proofStatus = "OK";
				} else {
					proofStatus = "??";
				}
				
				output.println(indent + proofStatus + " " + deepToString(item.getDefinition()));
			}
			
			{
				final String proofStatus = this.isGoalReached() ? "OK" : "??";
				
				output.println();
				output.println(indent + proofStatus + " " + deepToString(this.goal) + " (GOAL)");
			}
			
		}
		
		public final int getItemCount() {
			return (this.getParent() == null ? 0 : this.getParent().getItemCount()) + this.items.size();
		}
		
		private final void addItem(final String name, final Item item) {
			this.items.add(item);
			this.map.put(name, item);
			
			this.goalReached |= this.goal.equals(item.getDefinition().get(2));
		}
		
		private final String newName() {
			return "#" + this.getItemCount();
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = 4514650811365801131L;
		
		/**
		 * @author codistmonk (creation 2014-06-03)
		 */
		public static final class Item implements Serializable {
			
			private final List<Object> definition;
			
			private final Object proof;
			
			public Item(final List<Object> definition, final Object proof) {
				this.definition = definition;
				this.proof = proof;
			}
			
			public final List<Object> getDefinition() {
				return this.definition;
			}
			
			public final Object getProof() {
				return this.proof;
			}
			
			/**
			 * {@value}.
			 */
			private static final long serialVersionUID = -952832066418595523L;
			
		}
		
		/**
		 * @author codistmonk (creation 2014-06-03)
		 */
		public static enum Special {
			
			TRUE, FALSE, AXIOM, DEDUCTION;
			
		}
		
	}
	
	/**
	 * @author codistmonk (creation 2014-06-04)
	 */
	public static final class Session implements Serializable {
		
		private final Context rootContext = new Context();
		
		private Context currentContext = this.rootContext;
		
		public final void introduce() {
			this.currentContext = this.currentContext.introduce();
			this.popContext();
		}
		
		public final void express(final int templateRuleIndex) {
			this.currentContext.express(templateRuleIndex);
			this.popContext();
		}
		
		public final void express(final String templateRuleName) {
			this.currentContext.express(templateRuleName);
			this.popContext();
		}
		
		public final void express(final String name, final int templateRuleIndex) {
			this.currentContext.express(name, templateRuleIndex);
			this.popContext();
		}
		
		public final void express(final String name, final String templateRuleName) {
			this.currentContext.express(name, templateRuleName);
			this.popContext();
		}
		
		public final void bind(final int templateRuleIndex, final String variable,
				final Object value) {
			this.currentContext.bind(templateRuleIndex, variable, value);
			this.popContext();
		}
		
		public final void bind(final String templateRuleName, final String variable,
				final Object value) {
			this.currentContext.bind(templateRuleName, variable, value);
			this.popContext();
		}
		
		public final void bind(final String name, final int ruleTemplateIndex,
				final String variable, final Object value) {
			this.currentContext.bind(name, ruleTemplateIndex, variable, value);
			this.popContext();
		}
		
		public final void bind(final String name, final String ruleTemplateName,
				final String variable, final Object value) {
			this.currentContext.bind(name, ruleTemplateName, variable, value);
			this.popContext();
		}
		
		public final void apply(final int ruleIndex, final int testIndex) {
			this.currentContext.apply(ruleIndex, testIndex);
			this.popContext();
		}
		
		public final void apply(final int ruleIndex, final String testName) {
			this.currentContext.apply(ruleIndex, testName);
			this.popContext();
		}
		
		public final void apply(final String ruleName, final int testIndex) {
			this.currentContext.apply(ruleName, testIndex);
			this.popContext();
		}
		
		public final void apply(final String ruleName, final String testName) {
			this.currentContext.apply(ruleName, testName);
			this.popContext();
		}
		
		public final void apply(final String name, final int ruleIndex, final int testIndex) {
			this.currentContext.apply(name, ruleIndex, testIndex);
			this.popContext();
		}
		
		public final void apply(final String name, final int ruleIndex, final String testName) {
			this.currentContext.apply(name, ruleIndex, testName);
			this.popContext();
		}
		
		public final void apply(final String name, final String ruleName, final int testIndex) {
			this.currentContext.apply(name, ruleName, testIndex);
			this.popContext();
		}
		
		public final void apply(final String name, final String ruleName, final String testName) {
			this.currentContext.apply(name, ruleName, testName);
			this.popContext();
		}
		
		public final void assume(final Object expression) {
			this.currentContext.assume(expression);
			this.popContext();
		}
		
		public final void assume(final String name, final Object expression) {
			this.currentContext.assume(name, expression);
			this.popContext();
		}
		
		public final void prove(final Object goal) {
			this.currentContext = this.currentContext.prove(goal);
			this.popContext();
		}
		
		public final void prove(final String name, final Object goal) {
			this.currentContext = this.currentContext.prove(name, goal);
			this.popContext();
		}
		
		public final void rewrite(final int equalityIndex, final int targetIndex,
				final Set<Integer> indices) {
			this.currentContext.rewrite(equalityIndex, targetIndex, indices);
			this.popContext();
		}
		
		public final void rewrite(final String name, final int equalityIndex,
				final int targetIndex, final Set<Integer> indices) {
			this.currentContext.rewrite(name, equalityIndex, targetIndex, indices);
			this.popContext();
		}
		
		public final void rewrite(final String equalityName, final int targetIndex,
				final Set<Integer> indices) {
			this.currentContext.rewrite(equalityName, targetIndex, indices);
			this.popContext();
		}
		
		public final void rewrite(final String name, final String equalityName,
				final int targetIndex, final Set<Integer> indices) {
			this.currentContext.rewrite(name, equalityName, targetIndex, indices);
			this.popContext();
		}
		
		public final void rewrite(final int equalityIndex, final String targetName,
				final Set<Integer> indices) {
			this.currentContext.rewrite(equalityIndex, targetName, indices);
			this.popContext();
		}
		
		public final void rewrite(final String name, final int equalityIndex,
				final String targetName, final Set<Integer> indices) {
			this.currentContext.rewrite(name, equalityIndex, targetName, indices);
			this.popContext();
		}
		
		public final void rewrite(final String equalityName, final String targetName,
				final Set<Integer> indices) {
			this.currentContext.rewrite(equalityName, targetName, indices);
			this.popContext();
		}
		
		public final void rewrite(final String name, final String equalityName,
				final String targetName, final Set<Integer> indices) {
			this.currentContext.rewrite(name, equalityName, targetName, indices);
			this.popContext();
		}
		
		public final boolean isGoalReached() {
			return this.currentContext.isGoalReached();
		}
		
		public final Item getItem(final int index) {
			return this.currentContext.getItem(index);
		}
		
		public final Item getItem(final String name) {
			return this.currentContext.getItem(name);
		}
		
		public final List<Object> getDefinition(final int index) {
			return this.currentContext.getDefinition(index);
		}
		
		public final List<Object> getDefinition(final String name) {
			return this.currentContext.getDefinition(name);
		}
		
		public final Object getExpression(final int index) {
			return this.currentContext.getExpression(index);
		}
		
		public final Object getExpression(final String name) {
			return this.currentContext.getExpression(name);
		}
		
		public final int getDepth() {
			return this.currentContext.getDepth();
		}
		
		public final void printTo(final PrintStream output) {
			this.currentContext.printTo(output);
		}
		
		public final int getItemCount() {
			return this.currentContext.getItemCount();
		}
		
		private final void popContext() {
			while (this.currentContext.isGoalReached() && this.currentContext.getParent() != null) {
				this.currentContext = this.currentContext.getParent();
			}
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = -8755435237357100769L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-06-04)
	 */
	public static interface Rewriter extends Serializable {
		
		public abstract boolean canRewrite(Template target);
		
		public abstract boolean canRewrite(Object target);
		
		public abstract Object rewrite(Object target);
		
	}
	
}
