package jrewrite3;

import static net.sourceforge.aprog.tools.Tools.cast;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import jrewrite3.Module.Variable;

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
	
	@Override
	public final Expression visitBeforeChildren(final Composite composite) {
		return this.tryToRewrite(composite);
	}
	
	@Override
	public final Expression visitAfterChildren(final Composite composite, final Expression beforeVisit,
			final List<Expression> childVisits) {
		if (composite == beforeVisit && !composite.getChildren().equals(childVisits)) {
			return new Composite(childVisits);
		}
		
		return beforeVisit;
	}
	
	@Override
	public final Expression visit(final Variable variable) {
		return this.tryToRewrite(variable);
	}
	
	@Override
	public final Expression visitBeforeVariables(final Module module) {
		return this.tryToRewrite(module);
	}
	
	public final Expression visitAfterFacts(final Module module, final Expression beforeVisit,
			final List<Expression> variableVisits,
			final List<Expression> conditionVisits,
			final List<Expression> factVisits) {
		if (module == beforeVisit && (!module.getVariables().equals(variableVisits) ||
				!module.getConditions().equals(conditionVisits) ||
				!module.getFacts().equals(factVisits))) {
			final List<Variable> newVariables = new ArrayList<>();
			
			for (final Expression expression : variableVisits) {
				final Variable variable = cast(Variable.class, expression);
				
				if (variable != null && module == variable.getModule()) {
					newVariables.add(variable);
				}
			}
			
			return new Module(module.getParent(), newVariables, conditionVisits, factVisits);
		}
		
		return beforeVisit;
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
