package averan5.core;

import static averan5.core.AveranTools.*;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * @author codistmonk (creation 2015-04-11)
 */
public final class Substitution extends Proof.Abstract {
	
	private final Object target;
	
	private final Map<List<Object>, List<Object>> equalities;
	
	private final Collection<Integer> indices;
	
	public Substitution(final String provedPropositionName, final List<Object> target,
			final Map<List<Object>, List<Object>> equalities, final Collection<Integer> indices) {
		super(provedPropositionName, Arrays.asList("By substituting in", target, "using", equalities, "at", null));
		this.target = target;
		this.equalities = equalities;
		this.indices = indices instanceof TreeSet ? indices : new TreeSet<>(indices);
		
		this.getMessage().set(5, this.getIndices());
	}
	
	public final Object getTarget() {
		return this.target;
	}
	
	public final Map<List<Object>, List<Object>> getEqualities() {
		return this.equalities;
	}
	
	public final Collection<Integer> getIndices() {
		return this.indices;
	}
	
	@Override
	public final Object getProvedPropositionFor(final Deduction context) {
		final Object substitution = $(this.getTarget(),
				GIVEN, join(AND, iterable(
						this.getEqualities().entrySet().stream().map(e -> $equality(e.getKey(), e.getValue())))),
				AT, new ArrayList<>(this.getIndices()));
		final Object substituted = substituteIn(this.getTarget(), this.getEqualities(), this.getIndices());
		
		return $equality(substitution, substituted);
	}
	
	private static final long serialVersionUID = -5039934017175763847L;
	
	public static final List<Object> substituteIn(final Object target,
			final Map<List<Object>, List<Object>> equalities, final Collection<Integer> indices) {
		return substituteIn(target, equalities, indices, new int[] { -1 });
	}
	
	@SuppressWarnings("unchecked")
	private static final List<Object> substituteIn(final Object target,
			final Map<List<Object>, List<Object>> equalities, final Collection<Integer> indices, final int[] index) {
		final List<Object> replacement = equalities.get(target);
		
		if (replacement != null && (indices.isEmpty() || indices.contains(++index[0]))) {
			return replacement;
		}
		
		return list(target).stream().map(
				e -> e instanceof List ? substituteIn((List<Object>) e, equalities, indices, index) : e).collect(toList());
	}
	
}