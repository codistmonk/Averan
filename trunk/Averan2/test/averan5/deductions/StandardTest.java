package averan5.deductions;

import static averan5.core.AveranTools.*;
import static averan5.deductions.Standard.*;
import static java.util.stream.Collectors.toList;
import static net.sourceforge.aprog.tools.Tools.*;
import static org.junit.Assert.*;
import averan5.core.Binding;
import averan5.core.ModusPonens;
import averan5.core.Deduction;
import averan5.core.Goal;
import averan5.core.Proof;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Test;

/**
 * @author codistmonk (creation 2015-04-13)
 */
public final class StandardTest {
	
	@Test
	public final void areEqualTest() {
		{
			assertTrue(areEqual("a", "a"));
			assertFalse(areEqual("a", "b"));
			assertTrue(areEqual($forall("a", "a"), $forall("b", "b")));
			assertTrue(areEqual($equality($forall("a", "a"), $forall("a", "a")), $equality($forall("b", "b"), $forall("b", "b"))));
			assertTrue(areEqual($forall("a", "a"), $forall("b", "a")));
		}
		
		{
			assertFalse(areEqual($new("a"), $new("a")));
			assertFalse(areEqual($new("a"), $new("b")));
			
			{
				final Object a = $new("a");
				final Object b = $new("b");
				
				assertTrue(areEqual($forall(a, a), $forall(b, b)));
			}
		}
	}
	
	@Test
	public final void testRewrite() {
		build(() -> {
			supposeRewrite();
			
			suppose($equality("a", "b"));
			
			final Goal goal = Goal.deduce($equality("b", "b"));
			
			rewrite(name(-1), name(-1));
			
			goal.conclude();
		});
	}
	
	@Test
	public final void testRewriteRight() {
		build(() -> {
			supposeRewrite();
			deduceIdentity();
			deduceCommutativityOfEquality();
			
			suppose($equality("a", "b"));
			
			final Goal goal = Goal.deduce($equality("a", "a"));
			
			rewriteRight(name(-1), name(-1));
			
			goal.conclude();
		});
	}
	
	@Test
	public final void testDeduceIdentity() {
		build(() -> {
			supposeRewrite();
			deduceIdentity();
			
			final Goal goal = Goal.deduce($equality("a", "a"));
			
			bind("identity", $("a"));
			
			goal.conclude();
		});
	}
	
	@Test
	public final void testDeduceRecall() {
		build(() -> {
			supposeRewrite();
			deduceIdentity();
			deduceRecall();
			
			suppose($("a"));
			
			final Goal goal = Goal.deduce($rule("a", "a"));
			
			bind("recall", $("a"));
			
			goal.conclude();
		});
	}
	
	@Test
	public final void testJustify1() {
		build(() -> {
			supposeRewrite();
			deduceIdentity();
			deduceRecall();
			
			suppose($("a"));
			
			final Goal goal = Goal.deduce($("a"));
			
			conclude(justify(goal.getProposition()).get(0));
			
			goal.conclude();
		});
	}
	
	@Test
	public final void testJustify2() {
		build(() -> {
			supposeRewrite();
			deduceIdentity();
			deduceRecall();
			
			suppose($forall("a", "a"));
			
			final Goal goal = Goal.deduce($forall("b", "b"));
			
			conclude(justify(goal.getProposition()).get(0));
			
			goal.conclude();
		});
	}
	
	@Test
	public final void testJustify3() {
		build(() -> {
			suppose($rule("a", "b"));
			suppose($("a"));
			
			final Goal goal = Goal.deduce($("b"));
			
			conclude(justify(goal.getProposition()).get(0));
			
			goal.conclude();
		});
	}
	
	@Test
	public final void testJustify4() {
		build(() -> {
			suppose($rule("a", "b"));
			suppose($rule("b", "c"));
			suppose($("a"));
			
			final Goal goal = Goal.deduce($("c"));
			
			conclude(justify(goal.getProposition()).get(0));
			
			goal.conclude();
		});
	}
	
	@Test
	public final void testJustify5() {
		build(() -> {
			suppose($rule("a", "b", "c"));
			suppose($("a"));
			suppose($("b"));
			
			final Goal goal = Goal.deduce($("c"));
			
			conclude(justify(goal.getProposition()).get(0));
			
			goal.conclude();
		});
	}
	
	@Test
	public final void testJustify6() {
		build(() -> {
			supposeRewrite();
			deduceIdentity();
			deduceRecall();
			
			final Goal goal = Goal.deduce($("b"));
			
			assertEquals(Collections.emptyList(), justify(goal.getProposition()));
		});
	}
	
	@Test
	public final void testJustify7() {
		build(() -> {
			suppose($forall("a", "a"));
			
			final Goal goal = Goal.deduce($("b"));
			
			conclude(justify(goal.getProposition()).get(0));
			
			goal.conclude();
		});
	}
	
	@Test
	public final void testJustify8() {
		build(() -> {
			suppose($forall("b", $rule("a", "b")));
			suppose($("a"));
			
			final Goal goal = Goal.deduce($("c"));
			
			conclude(justify(goal.getProposition()).get(0));
			
			goal.conclude();
		});
	}
	
	public static final List<Proof> justify(final Object goal) {
		final List<Proof> result = new ArrayList<>();
		Deduction deduction = deduction();
		
		while (deduction != null) {
			final List<String> propositionNames = deduction.getPropositionNames();
			
			for (int i = propositionNames.size() - 1; 0 <= i; --i) {
				final String propositionName = propositionNames.get(i);
				final Object proposition = deduction.getProposition(propositionName);
				
				{
					final LayeredMap<Object, Object> bindings = new LayeredMap.Default<>();
					final int n = canBeImplied(goal, proposition, bindings);
					
					if (0 <= n) {
						debugPrint();
						debugPrint(proposition, "|-", goal);
						debugPrint(n, bindings.toMap(new HashMap<>()));
						
						if (!bindings.isEmpty()) {
							debugPrint(rewriteBound(proposition, new HashMap<>(), bindings.getMaps().iterator()));
						}
					}
				}
				
				if (areEqual(goal, proposition)) {
					result.add(new Recall(propositionName));
				}
				
				if (isRule(proposition)) {
					final int n = implies(proposition, goal);
					
					if (0 <= n) {
						Object tmp = proposition;
						String tmpName = propositionName;
						boolean ok = true;
						
						if (0 < n) {
							subdeduction();
						}
						
						for (int j = 0; j <= n && ok; ++j) {
							final List<Proof> conditionJustifications = justify(condition(tmp));
							
							if (!conditionJustifications.isEmpty()) {
								final Proof justification = conditionJustifications.get(0);
								final Recall recall = cast(Recall.class, justification);
								
								if (recall == null) {
									subdeduction();
									
									conclude(justification);
									apply(tmpName, name(-1));
									
									if (n == 0) {
										return set(result, pop());
									}
									
									conclude();
								} else {
									if (n == 0) {
										return set(result, new ModusPonens(tmpName, recall.getPropositionName()));
									}
									
									apply(tmpName, recall.getPropositionName());
								}
								
								tmp = proposition(-1);
								tmpName = name(-1);
							} else {
								ok = false;
							}
						}
						
						if (ok) {
							return set(result, pop());
						} else {
							if (0 < n) {
								pop();
							}
						}
					}
				}
				
				if (isBlock(proposition)) {
					final Object variable = variable(proposition);
					final Map<Object, Object> bindings = map(variable, null);
					
					if (areEqual2(goal, scope(proposition), bindings)) {
						result.add(new Binding(propositionName, bindings.get(variable)));
					}
				}
			}
			
			deduction = deduction.getParent();
		}
		
		return result;
	}
	
	public static final Object rewriteBound(final Object expression, final Map<Object, Object> bindings, final Iterator<Map<Object, Object>> bindingIterator) {
		{
			final Object value = bindings.get(expression);
			
			if (value != null) {
				return value;
			}
		}
		
		if (bindingIterator != null) {
			if (isBlock(expression)) {
				final Object variable = variable(expression);
				final Map<Object, Object> map = bindingIterator.next();
				final Object value = map.get(variable);
				
				checkArgument(map.size() == 1 && map.containsKey(variable), "Expected 1 binding for variable " + variable + " but got " + map);
				
				bindings.putAll(map);
				
				return value != null ? rewriteBound(scope(expression), bindings, bindingIterator) :
					$forall(variable, rewriteBound(scope(expression), bindings, bindingIterator));
			}
			
			if (isRule(expression)) {
				return $rule(rewriteBound(condition(expression), bindings, null), rewriteBound(conclusion(expression), bindings, bindingIterator));
			}
		}
		
		final List<Object> list = cast(List.class, expression);
		
		if (list != null) {
			return list.stream().map(e -> rewriteBound(e, bindings, null)).collect(toList());
		}
		
		return expression;
	}
	
	public static final boolean areEqual2(final Object expression1, final Object expression2, final Map<Object, Object> bindings) {
		if (areEqual(expression1, expression2)) {
			return true;
		}
		
		if (bindings.containsKey(expression2)) {
			final Object value = bindings.get(expression2);
			
			if (value == null) {
				bindings.put(expression2, expression1);
				
				return true;
			}
			
			return areEqual2(expression1, value, bindings);
		}
		
		return false;
	}
	
	public static final int implies(final Object rule, final Object goal) {
		Object tmp = rule;
		int result = 0;
		
		while (isRule(tmp)) {
			final Object conclusion = conclusion(tmp);
			
			if (areEqual(goal, conclusion)) {
				return result;
			}
			
			++result;
			tmp = conclusion;
		}
		
		return -1;
	}
	
	public static final int canBeImplied(final Object goal, final Object proposition, final LayeredMap<Object, Object> bindings) {
		if (areEqual(goal, proposition)) {
			return 0;
		}
		
		{
			final Map<Object, Object> tmp = bindings.toMap(new HashMap<>());
			
			if (areEqual2(goal, proposition, tmp)) {
				tmp.forEach(bindings::set);
				
				return 0;
			}
		}
		
		Object p = proposition;
		
		if (isBlock(proposition)) {
			bindings.push(new HashMap<>()).put(variable(proposition), null);
			
			final int protoresult = canBeImplied(goal, scope(proposition), bindings);
			
			if (0 <= protoresult) {
				return protoresult;
			}
			
			p = scope(proposition);
		}
		
		if (isRule(p)) {
			final int protoresult = canBeImplied(goal, conclusion(p), bindings);
			
			if (0 <= protoresult) {
				return 1 + protoresult;
			}
		}
		
		return -1;
	}
	
	public static final void autoDeduce(final String propositionName, final List<Object> goal) {
		// TODO
	}
	
	public static final Deduction build(final Runnable deductionBuilder) {
		return build(deductionBuilder, 2);
	}
	
	public static final Deduction build(final Runnable deductionBuilder, final int debugDepth) {
		return Standard.build(getCallerMethodName(), deductionBuilder, debugDepth);
	}
	
	/**
	 * @author codistmonk (creation 2015-04-17)
	 *
	 * @param <K>
	 * @param <V>
	 */
	public static abstract interface LayeredMap<K, V> extends Serializable {
		
		public abstract List<Map<K, V>> getMaps();
		
		public default boolean isEmpty() {
			for (final Map<K, V> map : this.getMaps()) {
				if (!map.isEmpty()) {
					return false;
				}
			}
			
			return true;
		}
		
		public default V get(final Object key) {
			for (final ListIterator<Map<K, V>> i = this.getMaps().listIterator(this.getDepth()); i.hasPrevious();) {
				final Map<K, V> map = i.previous();
				
				if (map.containsKey(key)) {
					return map.get(key);
				}
			}
			
			return null;
		}
		
		public default V set(final K key, final V value) {
			for (final ListIterator<Map<K, V>> i = this.getMaps().listIterator(this.getDepth()); i.hasPrevious();) {
				final Map<K, V> map = i.previous();
				
				if (map.containsKey(key)) {
					return map.put(key, value);
				}
			}
			
			return this.put(key, value);
		}
		
		public default V put(final K key, final V value) {
			return this.top().put(key, value);
		}
		
		public default int getDepth() {
			return this.getMaps().size();
		}
		
		public default LayeredMap<K, V> push(final Map<K, V> map) {
			this.getMaps().add(map);
			
			return this;
		}
		
		public default Map<K, V> pop() {
			return this.getMaps().remove(this.getDepth() - 1);
		}
		
		public default Map<K, V> top() {
			return last(this.getMaps());
		}
		
		public default Map<K, V> toMap(final Map<K, V> result) {
			this.getMaps().forEach(result::putAll);
			
			return result;
		}
		
		/**
		 * @author codistmonk (creation 2015-04-17)
		 *
		 * @param <K>
		 * @param <V>
		 */
		public static final class Default<K, V> implements LayeredMap<K, V> {
			
			private final List<Map<K, V>> maps = new ArrayList<>();
			
			@Override
			public final List<Map<K, V>> getMaps() {
				return this.maps;
			}
			
			private static final long serialVersionUID = 5217882640924788682L;
			
		}
		
	}
	
	/**
	 * @author codistmonk (creation 2015-04-14)
	 */
	public static final class Recall implements Proof {
		
		private final String propositionName;
		
		public Recall(final String propositionName) {
			this.propositionName = propositionName;
		}
		
		public final String getPropositionName() {
			return this.propositionName;
		}
		
		@Override
		public final Deduction concludeIn(final Deduction context) {
			push(context);
			
			try {
				bind("recall", context.getProposition(this.getPropositionName()));
				apply(name(-1), this.getPropositionName());
			} finally {
				pop(context);
			}
			
			return context;
		}
		
		@Override
		public final String toString() {
			return "Recall " + this.getPropositionName();
		}
		
		private static final long serialVersionUID = 3450261358246212849L;
		
	}
	
}
