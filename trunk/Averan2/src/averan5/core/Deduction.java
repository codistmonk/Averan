package averan5.core;

import static averan5.core.AveranTools.*;
import static net.sourceforge.aprog.tools.Tools.last;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * @author codistmonk (creation 2015-04-11)
 */
public final class Deduction extends Proof.Abstract {
	
	private final Deduction parent;
	
	private final Collection<Object> parameters;
	
	private final Map<String, Object> propositions;
	
	private final List<String> propositionNames;
	
	private final Map<String, Proof> proofs;
	
	public Deduction(final Deduction parent) {
		this(parent, parent == null ? "" : Integer.toString(parent.getPropositionNames().size() + 1));
	}
	
	public Deduction(final Deduction parent, final String provedPropositionName) {
		super(provedPropositionName, new ArrayList<>(Arrays.asList("By deduction in", null, "step(s)")));
		this.parent = parent;
		this.parameters = new LinkedHashSet<>();
		this.propositions = new HashMap<>();
		this.propositionNames = new ArrayList<>();
		this.proofs = new HashMap<>();
		
		this.getMessage().set(1, new Object() {
			
			@Override
			public final String toString() {
				return Integer.toString(getProofs().size());
			}
			
		});
	}
	
	public final Deduction getParent() {
		return this.parent;
	}
	
	public final Collection<Object> getParameters() {
		return this.parameters;
	}
	
	public final Map<String, Object> getPropositions() {
		return this.propositions;
	}
	
	public final List<String> getPropositionNames() {
		return this.propositionNames;
	}
	
	public final String newPropositionName() {
		return this.getProvedPropositionName() + "." + (this.getPropositions().size() + 1);
	}
	
	public final String getPropositionName(final int index) {
		checkArgument(index < 0, "Not negative: " + index);
		
		final int i = this.getPropositionNames().size() + index;
		
		return 0 <= i ? this.getPropositionNames().get(i) : this.getParent().getPropositionName(i);
	}
	
	public final Map<String, Proof> getProofs() {
		return this.proofs;
	}
	
	public final List<Object> getProposition(final String propositionName) {
		final List<Object> candidate = list(this.getPropositions().get(propositionName));
		
		return candidate != null || this.getParent() == null ? candidate : this.getParent().getProposition(propositionName);
	}
	
	public final Deduction forall(final List<Object> parameter) {
		if (!this.getParameters().add(parameter)) {
			throw new IllegalArgumentException();
		}
		
		return this;
	}
	
	public final Deduction suppose(final String propositionName, final Object proposition) {
		if (this.getProposition(propositionName) != null) {
			throw new IllegalArgumentException();
		}
		
		this.getPropositions().put(propositionName, list(proposition));
		this.getPropositionNames().add(propositionName);
		
		return this;
	}
	
	public final Deduction conclude() {
		if (this.getParent() != null) {
			this.getParent().conclude(this);
		}
		
		return this;
	}
	
	public final Deduction conclude(final Proof proof) {
		this.suppose(proof.getProvedPropositionName(), proof.getProvedPropositionFor(this));
		this.getProofs().put(proof.getProvedPropositionName(), proof);
		
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
	
	public final List<Object> getProvedProposition() {
		return this.getProvedPropositionFor(this);
	}
	
	@Override
	public final List<Object> getProvedPropositionFor(final Deduction context) {
		checkArgument(!this.getConclusionNames().isEmpty(), "Nothing to conclude");
		
		final List<Object> conclusion = this.getProposition(last(this.getConclusionNames()));
		List<Object> result = conclusion;
		
		{
			final List<String> conditionNames = this.getConditionNames();
			
			if (!conditionNames.isEmpty()) {
				result = new ArrayList<>(2 * conditionNames.size() + 1);
				
				for (final String conditionName : conditionNames) {
					result.add(this.getProposition(conditionName));
					result.add(IMPLIES);
				}
				
				result.add(conclusion);
			}
		}
		
		{
			final ArrayList<Object> parameters = new ArrayList<>(this.getParameters());
			
			for (final ListIterator<Object> i = parameters.listIterator(parameters.size()); i.hasPrevious();) {
				result = $forall(i.previous(), result);
			}
		}
		
		return result;
	}
	
	private static final long serialVersionUID = -1040410980387761070L;
	
}