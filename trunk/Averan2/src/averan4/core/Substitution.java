package averan4.core;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * @author codistmonk (creation 2015-04-11)
 */
public final class Substitution implements Proof {
	
	private final List<Object> target;
	
	private final Map<List<Object>, List<Object>> equalities;
	
	private final Collection<Integer> indices;
	
	public Substitution(final List<Object> target, final Map<List<Object>, List<Object>> equalities, final Collection<Integer> indices) {
		this.target = target;
		this.equalities = equalities;
		this.indices = indices instanceof TreeSet ? indices : new TreeSet<>(indices);
	}
	
	public final List<Object> getTarget() {
		return this.target;
	}
	
	public final Map<List<Object>, List<Object>> getEqualities() {
		return this.equalities;
	}
	
	public final Collection<Integer> getIndices() {
		return this.indices;
	}
	
	@Override
	public final List<Object> propositionFor(final Deduction context) {
		final List<Object> substitution = Demo.expression(this.getTarget(),
				Demo.GIVEN, Demo.join(Demo.AND, Demo.iterable(this.getEqualities().entrySet().stream().map(e -> Demo.equality(e.getKey(), e.getValue())))),
				Demo.AT, new ArrayList<>(this.getIndices()));
		final List<Object> substituted = substituteIn(this.getTarget(), this.getEqualities(), this.getIndices());
		
		return Demo.equality(substitution, substituted);
	}
	
	@Override
	public final String toString() {
		return "Substitution in " + this.getTarget() + " using " + this.getEqualities() + " at indices " + this.getIndices();
	}
	
	private static final long serialVersionUID = -5039934017175763847L;
	
	public static final List<Object> substituteIn(final List<Object> target,
			final Map<List<Object>, List<Object>> equalities, final Collection<Integer> indices) {
		return substituteIn(target, equalities, indices, new int[] { -1 });
	}
	
	@SuppressWarnings("unchecked")
	private static final List<Object> substituteIn(final List<Object> target,
			final Map<List<Object>, List<Object>> equalities, final Collection<Integer> indices, final int[] index) {
		final List<Object> replacement = equalities.get(target);
		
		if (replacement != null && (indices.isEmpty() || indices.contains(++index[0]))) {
			return replacement;
		}
		
		return target.stream().map(
				e -> e instanceof List ? substituteIn((List<Object>) e, equalities, indices, index) : e).collect(toList());
	}
	
}