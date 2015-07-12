package averan.draft1.core;

import java.util.List;

import averan.draft1.core.Module.Symbol;
import averan.draft1.core.Pattern.Any;

/**
 * @author codistmonk (creation 2014-08-22)
 */
public final class StructureMatcher extends Zipper<Boolean> {
	
	public StructureMatcher(final Expression other) {
		super(false, other);
	}
	
	@Override
	public final Boolean visit(final Any any) {
		return this.getOther() instanceof Any && any.equals(this.getOther());
	}
	
	@Override
	public final Boolean visit(final Symbol symbol) {
		return symbol.equals(this.getOther());
	}
	
	@Override
	protected final Boolean endVisit(final Composite composite, final List<Boolean> childVisits) {
		return true;
	}
	
	@Override
	protected final Boolean endVisit(final Module module, final List<Boolean> parameterVisits,
			final List<Boolean> conditionVisits, final List<Boolean> factVisits) {
		return true;
	}
	
	/**
	 * {@value}.
	 */
	private static final long serialVersionUID = 8361812973058798238L;
	
	public static final boolean listsMatch(final List<? extends Expression> list1,
			final List<? extends Expression> list2) {
		final int n = list1.size();
		
		if (n != list2.size()) {
			return false;
		}
		
		for (int i = 0; i < n; ++i) {
			if (!list1.get(i).accept(new StructureMatcher(list2.get(i)))) {
				return false;
			}
		}
		
		return true;
	}
	
}