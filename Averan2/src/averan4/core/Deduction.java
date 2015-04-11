package averan4.core;

import static net.sourceforge.aprog.tools.Tools.last;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * @author codistmonk (creation 2015-04-11)
 */
public final class Deduction implements Proof {
	
	private final Deduction parent;
	
	private final Collection<List<Object>> parameters;
	
	private final Map<String, List<Object>> propositions;
	
	private final List<String> propositionNames;
	
	private final Map<String, Proof> proofs;
	
	public Deduction(final Deduction parent) {
		this.parent = parent;
		this.parameters = new LinkedHashSet<>();
		this.propositions = new HashMap<>();
		this.propositionNames = new ArrayList<>();
		this.proofs = new HashMap<>();
	}
	
	public final Deduction getParent() {
		return this.parent;
	}
	
	public final Collection<List<Object>> getParameters() {
		return this.parameters;
	}
	
	public final Map<String, List<Object>> getPropositions() {
		return this.propositions;
	}
	
	public final List<String> getPropositionNames() {
		return this.propositionNames;
	}
	
	public final String getPropositionName(final int index) {
		Demo.checkArgument(index < 0);
		
		final int i = this.getPropositionNames().size() + index;
		
		return 0 <= i ? this.getPropositionNames().get(i) : this.getParent().getPropositionName(i);
	}
	
	public final Map<String, Proof> getProofs() {
		return this.proofs;
	}
	
	public final List<Object> getProposition(final String propositionName) {
		final List<Object> candidate = this.getPropositions().get(propositionName);
		
		return candidate != null || this.getParent() == null ? candidate : this.getParent().getProposition(propositionName);
	}
	
	public final Deduction forall(final List<Object> parameter) {
		if (!this.getParameters().add(parameter)) {
			throw new IllegalArgumentException();
		}
		
		return this;
	}
	
	public final Deduction suppose(final String propositionName, final List<Object> proposition) {
		if (this.getProposition(propositionName) != null) {
			throw new IllegalArgumentException();
		}
		
		this.getPropositions().put(propositionName, proposition);
		this.getPropositionNames().add(propositionName);
		
		return this;
	}
	
	public final Deduction conclude(final String propositionName, final Proof proof) {
		this.suppose(propositionName, proof.propositionFor(this));
		this.getProofs().put(propositionName, proof);
		
		return this;
	}
	
	public final List<String> getConditionNames() {
		final List<String> result = new ArrayList<>(this.getPropositionNames());
		
		result.removeAll(this.getProofs().keySet());
		
		return result;
	}
	
	public final List<String> getConclusionNames() {
		final List<String> result = new ArrayList<>(this.getPropositionNames());
		
		result.retainAll(this.getProofs().keySet());
		
		return result;
	}
	
	@Override
	public final List<Object> propositionFor(final Deduction context) {
		final List<String> conditionNames = this.getConditionNames();
		List<Object> result = new ArrayList<>(2 * conditionNames.size() + 1);
		
		for (final String conditionName : conditionNames) {
			result.add(conditionName);
			result.add(Demo.IMPLIES);
		}
		
		result.add(this.getProposition(last(this.getConclusionNames())));
		
		for (final ListIterator<List<Object>> i = new ArrayList(this.getParameters()).listIterator(this.getParameters().size()); i.hasPrevious();) {
			result = Demo.forall(i.previous(), result);
		}
		
		return result;
	}
	
	@Override
	public final String toString() {
		return "Deduced in " + this.getProofs().size() + " step(s)";
	}
	
	private static final long serialVersionUID = -1040410980387761070L;
	
}