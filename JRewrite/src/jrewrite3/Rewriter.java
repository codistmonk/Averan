package jrewrite3;

import static net.sourceforge.aprog.tools.Tools.cast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;

import jrewrite3.Module.Symbol;

/**
 * @author codistmonk (creation 2014-08-01)
 */
public final class Rewriter implements Visitor<Expression> {
	
	private final Map<Expression, Expression> rewrites = new LinkedHashMap<>();
	
	private final Set<Integer> indices = new TreeSet<>();
	
	private int index = -1;
	
	public final Map<Expression, Expression> getRewrites() {
		return this.rewrites;
	}
	
	public final Set<Integer> getIndices() {
		return this.indices;
	}
	
	public final Rewriter reset() {
		this.index = -1;
		
		return this;
	}
	
	public final Rewriter rewrite(final Expression pattern, final Expression replacement) {
		this.getRewrites().put(pattern, replacement);
		
		return this;
	}
	
	@Override
	public final Expression beginVisit(final Composite composite) {
		return this.tryToRewrite(composite);
	}
	
	@Override
	public final Expression endVisit(final Composite composite, final Expression compositeVisit,
			final Supplier<List<Expression>> childVisits) {
		if (composite == compositeVisit && !composite.getChildren().equals(childVisits.get())) {
			return new Composite(childVisits.get());
		}
		
		return compositeVisit;
	}
	
	@Override
	public final Expression visit(final Symbol symbol) {
		return this.tryToRewrite(symbol);
	}
	
	@Override
	public final Expression beginVisit(final Module module) {
		return this.tryToRewrite(module);
	}
	
	@Override
	public final Expression endVisit(final Module module, final Expression moduleVisit,
			final Supplier<List<Expression>> parameterVisits,
			final Supplier<List<Expression>> conditionVisits,
			final Supplier<List<Expression>> factVisits) {
		if (module == moduleVisit && (!module.getParameters().equals(parameterVisits.get()) ||
				!module.getConditions().equals(conditionVisits.get()) ||
				!module.getFacts().equals(factVisits.get()))) {
			final List<Symbol> newVariables = new ArrayList<>();
			
			for (final Expression expression : parameterVisits.get()) {
				final Symbol variable = cast(Symbol.class, expression);
				
				if (variable != null && module == variable.getModule()) {
					newVariables.add(variable);
				}
			}
			
			final Module result = new Module(module.getParent(), newVariables,
					conditionVisits.get(), factVisits.get());
			
			result.getConditionIndices().putAll(module.getConditionIndices());
			result.getFactIndices().putAll(module.getFactIndices());
			// TODO pass "rewrite" command as proof
			result.getProofs().addAll(Collections.nCopies(module.getFacts().size(), null));
			
			return result;
		}
		
		return moduleVisit;
	}
	
	private final Expression tryToRewrite(final Expression expression) {
		final Expression replacement = this.getRewrites().get(expression);
		
		if (replacement == null) {
			return expression;
		}
		
		return this.getIndices().isEmpty() || this.getIndices().contains(++this.index)
				? replacement : expression;
	}
	
	/**
	 * {@value}.
	 */
	private static final long serialVersionUID = 2140855852605397128L;
	
}
