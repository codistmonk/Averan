package jrewrite;

import static net.sourceforge.aprog.tools.Tools.cast;
import static net.sourceforge.aprog.tools.Tools.join;

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
		test2();
	}
	
	public static final void test2() {
		final Context context = new Context();
		
		context.assume(nat("0"));
		context.define(template(v("n"), rule(nat("n"), nat(list("S ", "n")))));
		
		System.out.println();
		context.printTo(System.out);
		
		{
			final Context proofContext1 = context.prove(template(v("n"), rule(nat("n"), nat(list("S ", list("S ", "n"))))));
			
			proofContext1.printTo(System.out);
			
			{
				final Context proofContext2 = proofContext1.introduce();
				
				System.out.println();
				proofContext2.printTo(System.out);
				
				{
					final Context proofContext3 = proofContext2.introduce();
					
					System.out.println();
					proofContext3.printTo(System.out);
					
					proofContext3.bind("#1", "n", "n");
					proofContext3.express(-1);
					
					System.out.println();
					proofContext3.printTo(System.out);
					
					proofContext3.apply(-1, "#5");
					
					System.out.println();
					proofContext3.printTo(System.out);
					
					proofContext3.bind("#1", "n", list("S ", "n"));
					proofContext3.express(-1);
					proofContext3.apply(-1, "#9");
					
					System.out.println();
					proofContext3.printTo(System.out);
				}
				
				System.out.println();
				proofContext2.printTo(System.out);
			}
			
			System.out.println();
			proofContext1.printTo(System.out);
		}
		
		System.out.println();
		context.printTo(System.out);
	}
	
	public static final void test1() {
		final Context context = new Context();
		
		for (int i = 0; i <= 9; ++i) {
			context.assume("digit" + i, list("" + i, ":", "N"));
		}
		
		context.define("concatN", template(v("x", "y"), rule(nat("x"), rule(nat("y"), nat(list("x", "y"))))));
		
		context.printTo(System.out);
		
		System.out.println();
		
		{
			final Context proofContext1 = context.prove(nat(list("4", "2")));
			
			proofContext1.printTo(System.out);
			
			proofContext1.bind("concatN", "x", "4");
			
			proofContext1.printTo(System.out);
			
			proofContext1.bind(-1, "y", "2");
			
			proofContext1.printTo(System.out);
			
			proofContext1.express(-1);
			
			proofContext1.printTo(System.out);
			
			proofContext1.apply(-1, "digit4");
			
			proofContext1.printTo(System.out);
			
			proofContext1.apply(-1, "digit2");
			
			proofContext1.printTo(System.out);
		}
		
		context.printTo(System.out);
	}
	
	public static final List<Object> list(final Object... list) {
		return Arrays.asList(list);
	}
	
	public static final List<Object> nat(final Object expression) {
		return list(expression, ":", "N");
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
	
	public static final String deepToString(final Object object) {
		if (object instanceof List) {
			final StringBuilder resultBuilder = new StringBuilder();
			
			for (final Object element : (List) object) {
				resultBuilder.append(deepToString(element));
			}
			
			return resultBuilder.toString();
		}
		
		return object.toString();
	}
	
	@SuppressWarnings("unchecked")
	public static final Object replace(final String variable, final Object value, final Object target) {
		if (target.equals(variable)) {
			return value;
		}
		
		if (target instanceof Rule) {
			final Rule rule = (Rule) target;
			
			return new Rule(replace(variable, value, rule.getCondition()), replace(variable, value, rule.getExpression()));
		}
		
		if (target instanceof Template) {
			final Template template = (Template) target;
			
			if (template.getVariables().contains(variable)) {
				return template;
			}
			
			return new Template(template.getVariables(), replace(variable, value, template.getExpression()));
			
		}
		
		if (target instanceof Iterable) {
			Collection<Object> result;
			
			try {
				result = (Collection<Object>) target.getClass().newInstance();
			} catch (final Exception exception) {
				result = new ArrayList<>();
			}
			
			for (final Object subtarget : (Iterable<?>) target) {
				result.add(replace(variable, value, subtarget));
			}
			
			return result;
		}
		
		return target;
	}
	
	public static final List<Object> equality(final String name, final Object expression) {
		return list(name, "=", expression);
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
			
			final Object resultExpression = replace(variable, value, this.getExpression());
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
		
		public final void express(final String name, final String templateRuleName) {
			this.express(name, (Template) this.getExpression(templateRuleName));
		}
		
		private final void express(final String name, final Template ruleTemplate) {
			if (!(ruleTemplate.getExpression() instanceof Rule) || !ruleTemplate.getVariables().isEmpty()) {
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
			if (!(ruleTemplate.getExpression() instanceof Rule)) {
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
		
		public final void define(final Template template) {
			this.define(this.newName(), template);
		}
		
		public final void define(final String name, final Template template) {
			this.addItem(name, new Item(equality(name, template) , Special.TEMPLATE));
		}
		
		public final boolean isGoalReached() {
			return this.goalReached;
		}
		
		public final Item getItem(final int index) {
			final int n = this.getItemCount();
			final int normalizedIndex = (n + index) % n;
			
			if (this.parent != null) {
				final int parentItemCount = this.parent.getItemCount();
				
				if (normalizedIndex < parentItemCount) {
					return this.parent.getItem(normalizedIndex);
				}
				
				return this.items.get(normalizedIndex - parentItemCount);
			}
			
			return this.items.get(normalizedIndex);
		}
		
		public final Item getItem(final String name) {
			final Item item = this.map.get(name);
			
			return item == null && this.parent != null ? this.parent.getItem(name) : item;
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
			return this.parent == null ? 0 : 1 + this.parent.getDepth();
		}
		
		public final void printTo(final PrintStream output) {
			final String indent = join("", Collections.nCopies(this.getDepth(), "\t").toArray());
			
			for (final Item item : this.items) {
				final Object proof = item.getProof();
				final String proofStatus;
				
				if (Special.TEMPLATE.equals(proof) || Special.DEDUCTION.equals(proof)) {
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
				
				output.println(indent + proofStatus + " " + deepToString(this.goal) + " (GOAL)");
			}
			
		}
		
		public final int getItemCount() {
			return (this.parent == null ? 0 : this.parent.getItemCount()) + this.items.size();
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
			
			TRUE, FALSE, TEMPLATE, AXIOM, DEDUCTION;
			
		}
		
	}
	
}
