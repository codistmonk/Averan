package averan.draft1.core;

import static multij.tools.Tools.cast;

import java.util.ArrayList;
import java.util.List;

/**
 * @author codistmonk (creation 2014-08-22)
 *
 * @param <T>
 */
public abstract class Zipper<T> implements Visitor<T> {
	
	private final T defaultResult;
	
	private Expression other;
	
	protected Zipper(final T defaultResult, final Expression other) {
		this.defaultResult = defaultResult;
		this.other = other;
	}
	
	@Override
	public final T visit(final Composite composite) {
		final Composite otherComposite = cast(Composite.class, this.getOther());
		
		if (otherComposite == null) {
			return this.getDefaultResult();
		}
		
		final List<T> childVisits = zip(composite.getChildren(), otherComposite.getChildren());
		
		this.other = otherComposite;
		
		return childVisits == null ? this.getDefaultResult() : this.endVisit(composite, childVisits);
	}
	
	@Override
	public final T visit(final Module module) {
		final Module otherModule = cast(Module.class, this.getOther());
		
		if (otherModule == null) {
			return this.getDefaultResult();
		}
		
		final List<T> parameterVisits = this.zip(module.getParameters(), otherModule.getParameters());
		
		if (parameterVisits == null) {
			return this.getDefaultResult();
		}
		
		final List<T> conditionVisits = this.zip(module.getConditions(), otherModule.getConditions());
		
		if (conditionVisits == null) {
			return this.getDefaultResult();
		}
		
		final List<T> factVisits = this.zip(module.getFacts(), otherModule.getFacts());
		
		if (factVisits == null) {
			return this.getDefaultResult();
		}
		
		this.other = otherModule;
		
		return this.endVisit(module, parameterVisits, conditionVisits, factVisits);
	}
	
	public final T getDefaultResult() {
		return this.defaultResult;
	}
	
	protected final Expression getOther() {
		return this.other;
	}
	
	protected abstract T endVisit(Composite composite, List<T> childVisits);
	
	protected abstract T endVisit(Module module, List<T> parameterVisits, List<T> conditionVisits, List<T> factVisits);
	
	private final List<T> zip(final List<? extends Expression> list, final List<? extends Expression> otherList) {
		final List<T> result = new ArrayList<>();
		
		final int n = list.size();
		
		if (otherList.size() != n) {
			return null;
		}
		
		for (int i = 0; i < n; ++i) {
			this.other = otherList.get(i);
			final T childVisit = list.get(i).accept(this);
			
			if (childVisit == this.getDefaultResult()) {
				return null;
			}
			
			result.add(childVisit);
		}
		
		return result;
	}
	
	/**
	 * {@value}.
	 */
	private static final long serialVersionUID = -4813152648556072535L;
	
}