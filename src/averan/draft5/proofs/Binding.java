package averan.draft5.proofs;

import static averan.draft5.expressions.Expressions.*;
import static averan.draft5.proofs.Stack.*;

import java.util.Arrays;
import java.util.List;

import averan.draft5.expressions.Expressions;

/**
 * @author codistmonk (creation 2015-04-11)
 */
public final class Binding extends Proof.Abstract {
	
	private final String blockName;
	
	private Object value;
	
	public Binding(final String blockName, final Object value) {
		this(null, blockName, value);
	}
	
	public Binding(final String provedPropositionName, final String blockName, final Object value) {
		super(provedPropositionName, Arrays.asList("By binding", blockName, "with", value));
		this.blockName = blockName;
		this.value = value;
	}
	
	public final String getBlockName() {
		return this.blockName;
	}
	
	public final Object getValue() {
		return this.value;
	}
	
	@Override
	public final Object getProvedPropositionFor(final Deduction context) {
		final List<Object> block = list(unifiable(checkBlock(this.getBlockName(), context)));
		
		return Substitution.substituteIn(scope(block), map(variable(block), this.getValue()), indices());
	}
	
	@Override
	public final Binding lock() {
		this.value = Expressions.lock(this.getValue());
		
		return (Binding) super.lock();
	}
	
	private static final long serialVersionUID = 5987805106367286343L;
	
}