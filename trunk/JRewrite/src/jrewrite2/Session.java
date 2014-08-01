package jrewrite2;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import jrewrite2.Context.Fact;

/**
 * @author codistmonk (creation 2014-06-10)
 */
public final class Session implements Serializable {
	
	private final Context rootContext = new Context();
	
	private Context currentContext = this.rootContext;
	
	private final Map<String, Object> metadata = new LinkedHashMap<>();
	
	public final Context getRootContext() {
		return this.rootContext;
	}
	
	public final Context getCurrentContext() {
		return this.currentContext;
	}
	
	public final Map<String, Object> getMetadata() {
		return this.metadata;
	}
	
	public final Expression getGoal() {
		return this.getCurrentContext().getGoal();
	}
	
	public final boolean isGoalReached() {
		return this.getCurrentContext().isGoalReached();
	}
	
	public final int getLocalFactCount() {
		return this.getCurrentContext().getLocalFactCount();
	}
	
	public final int getFactCount() {
		return this.getCurrentContext().getFactCount();
	}
	
	public final Expression getProposition(final String key) {
		return this.getFact(key).getProposition();
	}
	
	public final Expression getProposition(final int index) {
		return this.getFact(index).getProposition();
	}
	
	public final Fact getFact(final String key) {
		return this.getFact(this.getFactIndex(key));
	}
	
	public final Fact getFact(final int index) {
		return this.getCurrentContext().getFact(index);
	}
	
	public final int getFactIndex(final String key) {
		return this.getCurrentContext().getFactIndex(key);
	}
	
	public final void assume(final Expression proposition) {
		this.assume(null, proposition);
	}
	
	public final void assume(final String key, final Expression proposition) {
		this.getCurrentContext().assume(key, proposition);
		this.pop();
	}
	
	public final Context prove(final Expression proposition) {
		return this.prove(null, proposition);
	}
	
	public final Context prove(final String key, final Expression proposition) {
		final Context result = this.getCurrentContext().prove(key, proposition);
		this.currentContext = result;
		
		this.pop();
		
		return result;
	}
	
	public final void introduce() {
		this.introduce(null);
	}
	
	public final void introduce(final String key) {
		this.getCurrentContext().introduce(key);
		this.pop();
	}
	
	public final void bind(final String templateKey, final Expression expression) {
		this.bind(null, templateKey, expression);
	}
	
	public final void bind(final int templateIndex, final Expression expression) {
		this.bind(null, templateIndex, expression);
	}
	
	public final void bind(final String key, final String templateKey, final Expression expression) {
		this.bind(key, this.getFactIndex(templateKey), expression);
	}
	
	public final void bind(final String key, final int templateIndex, final Expression expression) {
		this.getCurrentContext().bind(key, templateIndex, expression);
		this.pop();
	}
	
	public final void rewriteLeft(final String factKey, final String equalityKey) {
		this.rewriteLeft(null, factKey, equalityKey, Collections.<Integer> emptySet());
	}
	
	public final void rewriteLeft(final String factKey, final int equalityIndex) {
		this.rewriteLeft(null, factKey, equalityIndex, Collections.<Integer> emptySet());
	}
	
	public final void rewriteLeft(final int factIndex, final String equalityKey) {
		this.rewriteLeft(null, factIndex, equalityKey, Collections.<Integer> emptySet());
	}
	
	public final void rewriteLeft(final int factIndex, final int equalityIndex) {
		this.rewriteLeft(null, factIndex, equalityIndex, Collections.<Integer> emptySet());
	}
	
	public final void rewriteLeft(final String key, final String factKey, final String equalityKey) {
		this.rewriteLeft(key, this.getFactIndex(factKey), this.getFactIndex(equalityKey)
				, Collections.<Integer> emptySet());
	}
	
	public final void rewriteLeft(final String key, final String factKey, final int equalityIndex) {
		this.rewriteLeft(key, this.getFactIndex(factKey), equalityIndex, Collections.<Integer> emptySet());
	}
	
	public final void rewriteLeft(final String key, final int factIndex, final String equalityKey) {
		this.rewriteLeft(key, factIndex, this.getFactIndex(equalityKey), Collections.<Integer> emptySet());
	}
	
	public final void rewriteLeft(final String key, final int factIndex, final int equalityIndex) {
		this.rewriteLeft(key, factIndex, equalityIndex, Collections.<Integer> emptySet());
	}
	
	public final void rewriteLeft(final String factKey,
			final String equalityKey, final Set<Integer> indices) {
		this.rewriteLeft(null, factKey, equalityKey, indices);
	}
	
	public final void rewriteLeft(final String factKey,
			final int equalityIndex, final Set<Integer> indices) {
		this.rewriteLeft(null, factKey, equalityIndex, indices);
	}
	
	public final void rewriteLeft(final int factIndex,
			final String equalityKey, final Set<Integer> indices) {
		this.rewriteLeft(null, factIndex, equalityKey, indices);
	}
	
	public final void rewriteLeft(final int factIndex,
			final int equalityIndex, final Set<Integer> indices) {
		this.rewriteLeft(null, factIndex, equalityIndex, indices);
	}
	
	public final void rewriteLeft(final String key, final String factKey,
			final String equalityKey, final Set<Integer> indices) {
		this.rewriteLeft(key, this.getFactIndex(factKey), this.getFactIndex(equalityKey), indices);
	}
	
	public final void rewriteLeft(final String key, final String factKey,
			final int equalityIndex, final Set<Integer> indices) {
		this.rewriteLeft(key, this.getFactIndex(factKey), equalityIndex, indices);
	}
	
	public final void rewriteLeft(final String key, final int factIndex,
			final String equalityKey, final Set<Integer> indices) {
		this.rewriteLeft(key, factIndex, this.getFactIndex(equalityKey), indices);
	}
	
	public final void rewriteLeft(final String key, final int factIndex,
			final int equalityIndex, final Set<Integer> indices) {
		this.getCurrentContext().rewriteLeft(key, factIndex, equalityIndex, indices);
		this.pop();
	}
	
	public final void rewriteRight(final String factKey, final String equalityKey) {
		this.rewriteRight(null, factKey, equalityKey, Collections.<Integer> emptySet());
	}
	
	public final void rewriteRight(final String factKey, final int equalityIndex) {
		this.rewriteRight(null, factKey, equalityIndex, Collections.<Integer> emptySet());
	}
	
	public final void rewriteRight(final int factIndex, final String equalityKey) {
		this.rewriteRight(null, factIndex, equalityKey, Collections.<Integer> emptySet());
	}
	
	public final void rewriteRight(final int factIndex, final int equalityIndex) {
		this.rewriteRight(null, factIndex, equalityIndex, Collections.<Integer> emptySet());
	}
	
	public final void rewriteRight(final String key, final String factKey,
			final String equalityKey) {
		this.rewriteRight(key, this.getFactIndex(factKey), this.getFactIndex(equalityKey)
				, Collections.<Integer> emptySet());
	}
	
	public final void rewriteRight(final String key, final String factKey,
			final int equalityIndex) {
		this.rewriteRight(key, this.getFactIndex(factKey), equalityIndex, Collections.<Integer> emptySet());
	}
	
	public final void rewriteRight(final String key, final int factIndex,
			final String equalityKey) {
		this.rewriteRight(key, factIndex, equalityKey, Collections.<Integer> emptySet());
	}
	
	public final void rewriteRight(final String key, final int factIndex,
			final int equalityIndex) {
		this.rewriteRight(key, factIndex, equalityIndex, Collections.<Integer> emptySet());
	}
	
	public final void rewriteRight(final String factKey,
			final String equalityKey, final Set<Integer> indices) {
		this.rewriteRight(null, factKey, equalityKey, indices);
	}
	
	public final void rewriteRight(final String factKey,
			final int equalityIndex, final Set<Integer> indices) {
		this.rewriteRight(null, factKey, equalityIndex, indices);
	}
	
	public final void rewriteRight(final int factIndex,
			final String equalityKey, final Set<Integer> indices) {
		this.rewriteRight(null, factIndex, equalityKey, indices);
	}
	
	public final void rewriteRight(final int factIndex,
			final int equalityIndex, final Set<Integer> indices) {
		this.rewriteRight(null, factIndex, equalityIndex, indices);
	}
	
	public final void rewriteRight(final String key, final String factKey,
			final String equalityKey, final Set<Integer> indices) {
		this.rewriteRight(key, this.getFactIndex(factKey), this.getFactIndex(equalityKey), indices);
	}
	
	public final void rewriteRight(final String key, final String factKey,
			final int equalityIndex, final Set<Integer> indices) {
		this.rewriteRight(key, this.getFactIndex(factKey), equalityIndex, indices);
	}
	
	public final void rewriteRight(final String key, final int factIndex,
			final String equalityKey, final Set<Integer> indices) {
		this.rewriteRight(key, factIndex, this.getFactIndex(equalityKey), indices);
	}
	
	public final void rewriteRight(final String key, final int factIndex,
			final int equalityIndex, final Set<Integer> indices) {
		final int normalizedFactIndex = this.getNormalizedIndex(factIndex);
		final int normalizedEqualityIndex = this.getNormalizedIndex(equalityIndex);
		final Equality equality = (Equality) this.getProposition(normalizedEqualityIndex);
		
		this.bind(SYMMETRY_OF_EQUALITY, equality.getLeft());
		this.bind(-1, equality.getRight());
		this.apply(-1, normalizedEqualityIndex);
		this.rewriteLeft(key, normalizedFactIndex, -1, indices);
	}
	
	public final void apply(final String ruleKey, final String conditionKey) {
		this.apply(null, ruleKey, conditionKey);
	}
	
	public final void apply(final String ruleKey, final int conditionIndex) {
		this.apply(null, ruleKey, conditionIndex);
	}
	
	public final void apply(final int ruleIndex, final String conditionKey) {
		this.apply(null, ruleIndex, this.getFactIndex(conditionKey));
	}
	
	public final void apply(final int ruleIndex, final int conditionIndex) {
		this.apply(null, ruleIndex, conditionIndex);
	}
	
	public final void apply(final String key, final String ruleKey, final String conditionKey) {
		this.apply(key, this.getFactIndex(ruleKey), this.getFactIndex(conditionKey));
	}
	
	public final void apply(final String key, final String ruleKey, final int conditionIndex) {
		this.apply(key, this.getFactIndex(ruleKey), conditionIndex);
	}
	
	public final void apply(final String key, final int ruleIndex, final String conditionKey) {
		this.apply(key, ruleIndex, this.getFactIndex(conditionKey));
	}
	
	public final void apply(final String key, final int ruleIndex, final int conditionIndex) {
		this.getCurrentContext().apply(key, ruleIndex, conditionIndex);
		this.pop();
	}
	
	public final void undo() {
		if (this.getLocalFactCount() == 0) {
			this.currentContext = this.getCurrentContext().getParent();
		}
		
		this.getCurrentContext().undo();
	}
	
	public final int getNormalizedIndex(final int index) {
		return this.getCurrentContext().getNormalizedIndex(index);
	}
	
	public final int getDepth() {
		return this.getCurrentContext().getDepth();
	}
	
	public final void printTo(final PrintStream output) {
		this.printTo(output, false);
	}
	
	public final void printTo(final PrintStream output, final boolean printProofs) {
		this.getCurrentContext().printTo(output, printProofs);
	}
	
	public final void pop() {
		while (this.isGoalReached() && this.getCurrentContext().getParent() != null) {
			this.currentContext = this.getCurrentContext().getParent();
		}
	}
	
	/**
	 * {@value}.
	 */
	private static final long serialVersionUID = -3276320733153503278L;
	
	/**
	 * {@value}.
	 */
	public static final String IDENTITY = "identity";
	
	/**
	 * {@value}.
	 */
	public static final String SYMMETRY_OF_EQUALITY = "symmetry_of_equality";
	
}