package jrewrite2;

import static net.sourceforge.aprog.tools.Tools.cast;
import static net.sourceforge.aprog.tools.Tools.debugPrint;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author codistmonk (creation 2014-06-10)
 */
public final class Context implements Serializable {
	
	private final Context parent;
	
	private final List<Fact> facts;
	
	private final Map<String, Integer> factIndices;
	
	private Expression goal;
	
	private boolean goalReached;
	
	public Context() {
		this(null, TRUE);
	}
	
	public Context(final Context parent, final Expression goal) {
		this.parent = parent;
		this.goal = goal;
		this.facts = new ArrayList<>();
		this.factIndices = new LinkedHashMap<>();
		this.goalReached = TRUE.equals(goal) || ASSUMED.equals(goal);
	}
	
	public final Context getParent() {
		return this.parent;
	}
	
	public final Expression getGoal() {
		return this.goal;
	}
	
	public final boolean isGoalReached() {
		return this.goalReached;
	}
	
	public final int getFactCount() {
		return this.facts.size();
	}
	
	public final Fact getFact(final int index) {
		return this.facts.get(index);
	}
	
	public final int getFactIndex(final String key) {
		return this.factIndices.get(key);
	}
	
	public final void assume(final String key, final Expression proposition) {
		this.accept(key, proposition, ASSUMED);
	}
	
	public final Context prove(final String key, final Expression proposition) {
		final Context result = new Context(this, proposition);
		
		this.addFact(key, new Fact(proposition, result));
		
		return result;
	}
	
	public final void introduce(final String key) {
		final Rule rule = cast(Rule.class, this.getGoal());
		
		if (rule != null) {
			this.goal = rule.getExpression();
			
			this.accept(key, rule.getCondition(), TRUE);
			
			return;
		}
		
		final Template template = cast(Template.class, this.getGoal());
		
		if (template != null) {
			this.goal = template.getProposition();
			final Symbol variable = new Symbol(template.getVariableName());
			
			this.accept(key, new Equality(variable, variable), TRUE);
			
			return;
		}
		
		throw new IllegalStateException();
	}
	
	public final void bind(final String key, final int templateIndex, final Expression expression) {
		debugPrint("TODO");
		// TODO
	}
	
	public final void rewrite(final String key, final int factIndex
			, final Expression pattern, final Expression replacement, final Set<Integer> indices) {
		debugPrint("TODO");
		// TODO
	}
	
	public final void apply(final String key, final int ruleIndex, final int conditionIndex) {
		final Rule rule = (Rule) this.getFact(ruleIndex).getProposition();
		
		if (!rule.getCondition().equals(this.getFact(conditionIndex).getProposition())) {
			throw new IllegalArgumentException();
		}
		
		this.accept(key, rule.getExpression(), TRUE);
	}
	
	public final void printTo(final PrintStream output) {
		output.println();
		
		for (final Map.Entry<String, Integer> entry : this.factIndices.entrySet()) {
			output.println("(" + entry.getKey() + ") " + this.getFact(entry.getValue()).getProposition());
		}
		
		output.println();
		
		output.println("(goal) " + this.getGoal());
	}
	
	private final void accept(final String key, final Expression proposition, final Symbol justification) {
		this.addFact(key, new Fact(proposition, new Context(null, justification)));
	}
	
	private final void addFact(final String key, final Fact fact) {
		final String actualKey = key != null ? key : this.newKey();
		
		this.factIndices.put(actualKey, this.getFactCount());
		this.facts.add(fact);
		
		if (this.getGoal().equals(fact.getProposition())) {
			this.goalReached = true;
		}
	}
	
	private final String newKey() {
		return "#" + this.getFactCount();
	}
	
	/**
	 * {@value}.
	 */
	private static final long serialVersionUID = 2082834263764356892L;
	
	public static final Symbol ASSUMED = new Symbol("Assumed");
	
	public static final Symbol TRUE = new Symbol("True");
	
	public static final Symbol FALSE = new Symbol("False");
	
	/**
	 * @author codistmonk (creation 2014-06-10)
	 */
	public static final class Fact implements Serializable {
		
		private final Expression proposition;
		
		private final Context proof;
		
		public Fact(final Expression proposition, final Context proof) {
			this.proposition = proposition;
			this.proof = proof;
		}
		
		public final Expression getProposition() {
			return this.proposition;
		}
		
		public final Context getProof() {
			return this.proof;
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = 1869338402483804728L;
		
	}
	
}
