package averan4.core;

import static averan4.core.AveranTools.*;

import java.util.List;

/**
 * @author codistmonk (creation 2015-04-11)
 */
public final class Binding implements Proof {
	
	private final String blockName;
	
	private final List<Object> value;
	
	public Binding(final String blockName, final List<Object> value) {
		this.blockName = blockName;
		this.value = value;
	}
	
	public final String getBlockName() {
		return this.blockName;
	}
	
	public final List<Object> getValue() {
		return this.value;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public final List<Object> propositionFor(final Deduction context) {
		final List<Object> block = context.getProposition(this.getBlockName());
		
		checkArgument(block.size() == 2);
		
		final List<Object> quantification = (List<Object>) block.get(0);
		
		checkArgument(quantification.size() == 2 && FORALL.equals(quantification.get(0)));
		
		return Substitution.substituteIn((List<Object>) block.get(1), map(quantification.get(1), this.getValue()), indices());
	}
	
	private static final long serialVersionUID = 5987805106367286343L;
	
}