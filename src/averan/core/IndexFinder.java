package averan.core;

import averan.core.Module.Symbol;
import averan.core.Pattern.Any;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import multij.tools.Pair;

/**
 * @author codistmonk (creation 2014-08-22)
 */
public final class IndexFinder implements Visitor<List<Pair<Integer, Pattern>>> {
	
	private final Pattern pattern;
	
	private final boolean waitForTopLevelRHS;
	
	private int subindex;
	
	private int level;
	
	private boolean active;
	
	private final List<Pair<Integer, Pattern>> result;
	
	private final Map<Object, Integer> indices;
	
	public IndexFinder(final Pattern pattern) {
		this(false, pattern);
	}
	
	public IndexFinder(final boolean waitForTopLevelRHS, final Pattern pattern) {
		this.pattern = pattern;
		this.waitForTopLevelRHS = waitForTopLevelRHS;
		this.subindex = -1;
		this.level = -1;
		this.active = !this.waitForTopLevelRHS;
		this.result = new ArrayList<>();
		this.indices = new HashMap<>();
	}
	
	public final Pattern getPattern() {
		return this.pattern;
	}
	
	@Override
	public final List<Pair<Integer, Pattern>> visit(final Any any) {
		return this.beginVisit(any).endVisit(any);
	}
	
	@Override
	public final List<Pair<Integer, Pattern>> visit(final Composite composite) {
		return this.beginVisit(composite)
				.findIndicesIn(composite.getChildren())
				.endVisit(composite);
	}
	
	@Override
	public final List<Pair<Integer, Pattern>> visit(final Symbol symbol) {
		return this.beginVisit(symbol).endVisit(symbol);
	}
	
	@Override
	public final List<Pair<Integer, Pattern>> visit(final Module module) {
		return this.beginVisit(module)
				.findIndicesIn(module.getParameters())
				.findIndicesIn(module.getConditions())
				.findIndicesIn(module.getFacts())
				.endVisit(module);
	}
	
	private final IndexFinder beginVisit(final Expression expression) {
		++this.level;
		
		this.computeResult(this.getPattern().equals(expression));
		
		return this;
	}
	
	private final List<Pair<Integer, Pattern>> endVisit(final Expression expression) {
		if (this.level == 1 && this.subindex == 1 && Module.EQUAL.equals(expression)) {
			this.active = true;
		}
		
		--this.level;
		
		return this.result;
	}
	
	private final boolean isActive() {
		return this.active;
	}
	
	private final List<Pair<Integer, Pattern>> computeResult(final boolean match) {
		if (match) {
			final Pattern patternCopy = this.getPattern().copy();
			final Integer patternIndex = this.indices.compute(patternCopy.getBindings(),
					(k, v) -> v == null ? 0 : v + 1);
			
			if (this.isActive()) {
				this.result.add(new Pair<>(patternIndex, patternCopy));
			}
		}
		
		return this.result;
	}
	
	private final IndexFinder findIndicesIn(final List<? extends Expression> list) {
		final int n = list.size();
		
		for (int i = 0; i < n; ++i) {
			this.subindex = i;
			list.get(i).accept(this);
		}
		
		this.subindex = -1;
		
		return this;
	}
	
	/**
	 * {@value}.
	 */
	private static final long serialVersionUID = -6666401837567106389L;
	
}