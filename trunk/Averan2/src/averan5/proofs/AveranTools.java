package averan5.proofs;

import static java.util.stream.Collectors.toList;
import static net.sourceforge.aprog.tools.Tools.cast;
import static net.sourceforge.aprog.tools.Tools.last;

import averan5.expressions.Id;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import net.sourceforge.aprog.tools.IllegalInstantiationException;

/**
 * @author codistmonk (creation 2015-04-11)
 */
public final class AveranTools {
	
	private AveranTools() {
		throw new IllegalInstantiationException();
	}
	
	public static final String FORALL = $("∀");
	
	public static final String IMPLIES = $("→");
	
	public static final String EQUALS = $("=");
	
	public static final String GIVEN = $("|");
	
	public static final String AND = $(",");
	
	public static final String AT = $("@");
	
	private static final List<Deduction> stack = new ArrayList<>();
	
	public static final Deduction push() {
		return push("");
	}
	
	public static final Deduction push(final String deductionName) {
		return push(new Deduction(null, deductionName));
	}
	
	public static final Deduction push(final Deduction result) {
		stack.add(result);
		
		return result;
	}
	
	public static final Deduction pop() {
		return stack.remove(stack.size() - 1);
	}
	
	public static final Deduction pop(final Deduction deduction) {
		while (deduction != pop()) {
			// NOP;
		}
		
		return deduction;
	}
	
	public static final Deduction deduction() {
		return last(stack);
	}
	
	public static final Object forall(final String name) {
		final Object result = $new(name);
		
		deduction().forall(result);
		
		return result;
	}
	
	public static final void suppose(final Object proposition) {
		suppose(newName(), proposition);
	}
	
	public static final void suppose(final String propositionName, final Object proposition) {
		deduction().suppose(propositionName, proposition);
	}
	
	public static final void apply(final String ruleName, final String conditionName) {
		apply(null, ruleName, conditionName);
	}
	
	public static final void apply(final String propositionName, final String ruleName, final String conditionName) {
		conclude(new ModusPonens(propositionName, ruleName, conditionName));
	}
	
	public static final void substitute(final Object target,
			final Map<Object, Object> equalities, final int... indices) {
		substitute(null, target, equalities, indices);
	}
	
	public static final void substitute(final String propositionName, final Object target,
			final Map<Object, Object> equalities, final int... indices) {
		conclude(new Substitution(propositionName, target, equalities, indices(indices)));
	}
	
	public static final void bind(final String targetName, final Object... values) {
		subdeduction();
		
		final int n = values.length;
		
		bind(targetName, values[0]);
		
		for (int i = 1; i < n; ++i) {
			bind(name(-1), values[i]);
		}
		
		set(conclude().getMessage(), "Bind", targetName, "with", Arrays.asList(values));
	}
	
	public static final <T, C extends Collection<T>> C set(final C collection, @SuppressWarnings("unchecked") final T... elements) {
		collection.clear();
		collection.addAll(Arrays.asList(elements));
		
		return collection;
	}
	
	public static final void bind(final String targetName, final Object value) {
		bind(null, targetName, value);
	}
	
	public static final void bind(final String propositionName, final String targetName, final Object value) {
		conclude(new Binding(propositionName, targetName, value));
	}
	
	public static final void subdeduction() {
		subdeduction(newName());
	}
	
	public static final void subdeduction(final String propositionName) {
		push(new Deduction(deduction(), propositionName));
	}
	
	public static final Deduction conclude() {
		return conclude(pop());
	}
	
	public static final <P extends Proof> P conclude(final P proof) {
		deduction().conclude(proof);
		
		return proof;
	}
	
	public static final String name(final int index) {
		return deduction().getPropositionName(index);
	}
	
	public static final Object proposition(final String name) {
		return deduction().getProposition(name);
	}
	
	public static final Object proposition(final int index) {
		return deduction().getProposition(name(index));
	}
	
	public static final String newName() {
		return deduction().newPropositionName();
	}
	
	public static final Object $new(final String name) {
		return $(new Id(name));
	}
	
	@SuppressWarnings("unchecked")
	public static final <T> T $(final Object... objects) {
		if (objects.length == 1) {
			return (T) objects[0];
		}
		
		if (objects.length <= 1) {
			return (T) Arrays.asList(objects);
		}
		
		return (T) Arrays.stream(objects).map(AveranTools::$).collect(toList());
	}
	
	public static final List<Object> $forall(final Object variableOrName) {
		return $(FORALL, $(variableOrName));
	}
	
	public static final List<Object> $forall(final Object variableOrName, final Object... scoped) {
		return $($forall(variableOrName), $(scoped));
	}
	
	public static final List<Object> $equality(final Object left, final Object right) {
		return $(left, EQUALS, right);
	}
	
	public static final Object $rule(final Object... propositions) {
		return $rightAssociativeOperation(IMPLIES, propositions);
	}
	
	public static final Object $rightAssociativeOperation(final Object operator, final Object... operands) {
		final int n = operands.length;
		Object result = operands[n - 1];
		
		for (int i = n - 2; 0 <= i; --i) {
			result = $(operands[i], operator, result);
		}
		
		return result;
	}
	
	public static final List<Object> join(final Object separator, final Object... objects) {
		return join(separator, Arrays.asList(objects));
	}
	
	public static final List<Object> join(final Object separator, final Iterable<Object> objects) {
		final Collection<?> collection = cast(Collection.class, objects);
		final List<Object> result = collection == null ? new ArrayList<>() : new ArrayList<>(2 * collection.size() + 1);
		final Iterator<Object> i = objects.iterator();
		
		if (i.hasNext()) {
			result.add(i.next());
			
			while (i.hasNext()) {
				result.add(separator);
				result.add(i.next());
			}
		}
		
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public static final <K, V> Map<K, V> map(final Object... keyAndValues) {
		final Map<K, V> result = new LinkedHashMap<>();
		final int n = keyAndValues.length;
		
		for (int i = 0; i < n; i += 2) {
			result.put((K) keyAndValues[i + 0], (V) keyAndValues[i + 1]);
		}
		
		
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public static final <E> List<E> indices(final int... indices) {
		return (List<E>) Arrays.stream(indices).mapToObj(Integer::valueOf).collect(toList());
	}
	
	public static final <T> Iterable<T> iterable(final Stream<T> stream) {
		return () -> stream.iterator();
	}
	
	public static final void checkArgument(final boolean check, final String message) {
		if (!check) {
			throw new IllegalArgumentException(message);
		}
	}
	
	public static final void checkState(final boolean check, final String message) {
		if (!check) {
			throw new IllegalStateException(message);
		}
	}
	
	public static final Object checkProposition(final String name) {
		return checkProposition(name, deduction());
	}
	
	public static final Object checkProposition(final String name, final Deduction context) {
		final Object result = context.getProposition(name);
		
		checkArgument(result != null, "Missing proposition: " + name);
		
		return result;
	}
	
	public static final List<Object> checkRule(final String name) {
		return checkRule(name, deduction());
	}
	
	@SuppressWarnings("unchecked")
	public static final List<Object> checkRule(final String name, final Deduction context) {
		final Object result = checkProposition(name, context);
		
		checkArgument(isRule(result), "Not a rule: " + result);
		
		return (List<Object>) result;
	}
	
	public static final List<Object> checkEquality(final String name) {
		return checkEquality(name, deduction());
	}
	
	@SuppressWarnings("unchecked")
	public static final List<Object> checkEquality(final String name, final Deduction context) {
		final Object result = checkProposition(name, context);
		
		checkArgument(isEquality(result), "Not an equality: " + result);
		
		return (List<Object>) result;
	}
	
	public static final List<Object> checkSubstitution(final String name) {
		return checkSubstitution(name, deduction());
	}
	
	@SuppressWarnings("unchecked")
	public static final List<Object> checkSubstitution(final String name, final Deduction context) {
		final Object result = checkProposition(name, context);
		
		checkArgument(isSubstitution(result), "Not a substitution: " + result);
		
		return (List<Object>) result;
	}
	
	public static final List<Object> checkBlock(final String name) {
		return checkBlock(name, deduction());
	}
	
	@SuppressWarnings("unchecked")
	public static final List<Object> checkBlock(final String name, final Deduction context) {
		final Object result = checkProposition(name, context);
		
		checkArgument(isBlock(result), "Not a block: " + result);
		
		return (List<Object>) result;
	}
	
	public static final boolean isRule(final Object object) {
		final List<?> expression = cast(List.class, object);
		
		return expression != null
				&& expression.size() == 3
				&& IMPLIES.equals(expression.get(1));
	}
	
	public static final Object condition(final Object rule) {
		return list(rule).get(0);
	}
	
	public static final Object conclusion(final Object rule) {
		return list(rule).get(2);
	}
	
	public static final boolean isEquality(final Object object) {
		final List<?> expression = cast(List.class, object);
		
		return expression != null
				&& expression.size() == 3
				&& EQUALS.equals(expression.get(1));
	}
	
	public static final Object left(final Object binaryOperation) {
		return list(binaryOperation).get(0);
	}
	
	public static final Object right(final Object binaryOperation) {
		return list(binaryOperation).get(2);
	}
	
	public static final boolean isSubstitution(final Object object) {
		final List<?> expression = cast(List.class, object);
		
		return expression != null
				&& expression.size() == 5
				&& GIVEN.equals(expression.get(1))
				&& AT.equals(expression.get(3));
	}
	
	public static final Object target(final Object substitution) {
		return list(substitution).get(0);
	}
	
	public static final Object equalities(final Object substitution) {
		return list(substitution).get(2);
	}
	
	public static final Object indices(final Object substitution) {
		return list(substitution).get(4);
	}
	
	public static final boolean isQuantification(final Object object) {
		final List<?> expression = cast(List.class, object);
		
		return expression != null
				&& expression.size() == 2
				&& FORALL.equals(expression.get(0));
	}
	
	public static final Object variable(final Object block) {
		return quantifiedVariable(quantification(block));
	}
	
	public static final Object quantifiedVariable(final Object quantitication) {
		return list(quantitication).get(1);
	}
	
	public static final boolean isBlock(final Object object) {
		final List<?> expression = cast(List.class, object);
		
		return expression != null
				&& expression.size() == 2
				&& isQuantification(expression.get(0));
	}
	
	@SuppressWarnings("unchecked")
	public static final List<Object> quantification(final Object block) {
		final Object result = list(block).get(0);
		
		checkArgument(isQuantification(result), "Not a quantification: " + result);
		
		return (List<Object>) result;
	}
	
	public static final Object scope(final Object block) {
		return list(block).get(1);
	}
	
	@SuppressWarnings("unchecked")
	public static final List<Object> list(final Object object) {
		return (List<Object>) object;
	}
	
	public static final void abort() {
		throw new RuntimeException("Aborted");
	}
	
}
