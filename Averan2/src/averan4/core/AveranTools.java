package averan4.core;

import static java.util.stream.Collectors.toList;
import static net.sourceforge.aprog.tools.Tools.cast;
import static net.sourceforge.aprog.tools.Tools.last;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.sourceforge.aprog.tools.IllegalInstantiationException;

/**
 * @author codistmonk (creation 2015-04-11)
 */
public final class AveranTools {
	
	private AveranTools() {
		throw new IllegalInstantiationException();
	}
	
	public static final List<Object> FORALL = $("\\/");
	
	public static final List<Object> IMPLIES = $("->");
	
	public static final List<Object> EQUALS = $("=");
	
	public static final List<Object> GIVEN = $("|");
	
	public static final List<Object> AND = $(",");
	
	public static final List<Object> AT = $("@");
	
	private static final List<Deduction> stack = new ArrayList<>();
	
	public static final Deduction push(final Deduction result) {
		stack.add(result);
		
		return result;
	}
	
	public static final Deduction push() {
		return push(new Deduction(null));
	}
	
	public static final Deduction pop() {
		return stack.remove(stack.size() - 1);
	}
	
	public static final Deduction deduction() {
		return last(stack);
	}
	
	public static final List<Object> forall(final String name) {
		final List<Object> result = $new(name);
		
		deduction().forall(result);
		
		return result;
	}
	
	public static final void suppose(final String propositionName, final List<Object> proposition) {
		deduction().suppose(propositionName, proposition);
	}
	
	public static final void conclude(final String propositionName, final Proof proof) {
		deduction().conclude(propositionName, proof);
	}
	
	public static final void apply(final String propositionName, final String ruleName, final String conditionName) {
		deduction().conclude(propositionName, new ModusPonens(ruleName, conditionName));
	}
	
	public static final void substitute(final String propositionName, final List<Object> target,
			final Map<List<Object>, List<Object>> equalities, final Collection<Integer> indices) {
		deduction().conclude(propositionName, new Substitution(target, equalities, indices));
	}
	
	public static final void bind(final String propositionName, final String targetName, final List<Object> value) {
		deduction().conclude(propositionName, new Binding(targetName, value));
	}
	
	public static final String name(final int index) {
		return deduction().getPropositionName(index);
	}
	
	public static final List<Object> proposition(final int index) {
		return deduction().getProposition(name(index));
	}
	
	public static final List<Object> $new(final String name) {
		return $(new Id(name));
	}
	
	@SuppressWarnings("unchecked")
	public static final List<Object> $(final Object... objects) {
		if (objects.length == 1 && objects[0] instanceof List) {
			return (List<Object>) objects[0];
		}
		
		if (objects.length <= 1) {
			return Arrays.asList(objects);
		}
		
		return Arrays.stream(objects).map(AveranTools::$).collect(toList());
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
	
	public static final List<Object> $rule(final Object... propositions) {
		return join(IMPLIES, propositions);
	}
	
	public static final List<Object> join(final Object separator, final Object... objects) {
		return join(separator, Arrays.asList(objects));
	}
	
	public static final List<Object> join(final Object separator, final Iterable<Object> objects) {
		@SuppressWarnings("unchecked")
		final Collection<Object> collection = cast(Collection.class, objects);
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
	
	public static final Collection<Integer> indices(final int... indices) {
		return Arrays.stream(indices).mapToObj(Integer::valueOf).collect(Collectors.toCollection(TreeSet::new));
	}
	
	public static final <T> Iterable<T> iterable(final Stream<T> stream) {
		return () -> stream.iterator();
	}
	
	public static final void checkArgument(final boolean check) {
		if (!check) {
			throw new IllegalArgumentException();
		}
	}
	
}
