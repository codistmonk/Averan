package averan.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;

import averan.core.Module.Statement;
import averan.core.Module.Symbol;

/**
 * @author codistmonk (creation 2014-08-01)
 */
public final class Rewriter implements Visitor<Expression> {
	
	private final Statement statement;
	
	private final Map<Expression, Expression> rewrites;
	
	private final Set<Integer> indices;
	
	private int index;
	
	public Rewriter() {
		this(null);
	}
	
	public Rewriter(final Statement statement) {
		this.statement = statement;
		this.rewrites = new LinkedHashMap<>();
		this.indices = new TreeSet<>();
		
		this.reset();
	}
	
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
	
	public final Rewriter atIndices(final Collection<Integer> indices) {
		this.getIndices().addAll(indices);
		
		return this;
	}
	
	@Override
	public final Expression visit(final Composite composite) {
		final Expression compositeVisit = this.tryToRewrite(composite);
		
		if (composite == compositeVisit) {
			final List<Expression> childVisits = composite.childrenAcceptor(this).get();
			
			if (!composite.getChildren().equals(childVisits)) {
				return new Composite(childVisits);
			}
		}
		
		return compositeVisit;
	}
	
	@Override
	public final Expression visit(final Symbol symbol) {
		return this.tryToRewrite(symbol);
	}
	
	@Override
	public final Expression visit(final Module module) {
		final Expression moduleVisit = this.tryToRewrite(module);
		final Supplier<List<Expression>> parameterVisits = module.parametersAcceptor(this);
		final Supplier<List<Expression>> conditionVisits = module.conditionsAcceptor(this);
		final Supplier<List<Expression>> factVisits = module.factsAcceptor(this);
		
		if (module == moduleVisit && (!module.getParameters().equals(parameterVisits.get()) ||
				!module.getConditions().equals(conditionVisits.get()) ||
				!module.getFacts().equals(factVisits.get()))) {
			final List<Symbol> oldParameters = module.getParameters();
			final List<Expression> replacedParameters = parameterVisits.get();
			final int n = oldParameters.size();
			final List<Symbol> newParameters = new ArrayList<>();
			
			for (int i = 0; i < n; ++i) {
				final Symbol parameter = oldParameters.get(i);
				
				if (parameter == replacedParameters.get(i)) {
					newParameters.add(parameter);
				}
			}
			
			final Module result = new Module(module.getParent(), module.getName(), newParameters,
					conditionVisits.get(), factVisits.get());
			
			result.getConditionIndices().putAll(module.getConditionIndices());
			result.getFactIndices().putAll(module.getFactIndices());
			result.getStatements().addAll(Collections.nCopies(module.getFacts().size(), this.statement));
			
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
