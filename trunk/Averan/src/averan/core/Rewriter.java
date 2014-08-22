package averan.core;

import static net.sourceforge.aprog.tools.Tools.cast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;

import net.sourceforge.aprog.tools.Tools;
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
			
			Tools.debugPrint(composite, childVisits);
			if (!equals2(composite.getChildren(), childVisits)) {
				Tools.debugPrint(composite, childVisits);
				return new Composite(childVisits);
			}
		}
		
		return compositeVisit;
	}
	
	public static final boolean equals2(final List<? extends Expression> list1,
			final List<? extends Expression> list2) {
		final int n = list1.size();
		
		if (n != list2.size()) {
			return false;
		}
		
		for (int i = 0; i < n; ++i) {
			final Expression expression1 = list1.get(i);
			final Expression expression2 = list2.get(i);
			
			if (!expression1.equals(expression2)) {
				return false;
			}
			
			Tools.debugPrint(expression1, expression1.getClass());
			Tools.debugPrint(expression2, expression2.getClass());
			
			if ((expression1 instanceof Pattern.Any) != (expression2 instanceof Pattern.Any)) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * @author codistmonk (creation 2014-08-22)
	 *
	 * @param <T>
	 */
	public static abstract class Zipper<T> implements Visitor<T> {
		
		private Expression other;
		
		@Override
		public final T visit(final Composite composite) {
			final Composite otherComposite = cast(Composite.class, this.other);
			
			if (otherComposite == null) {
				return null;
			}
			
			final List<T> childVisits = new ArrayList<>();
			
			{
				final List<Expression> compositeChildren = composite.getChildren();
				final List<Expression> otherChildren = otherComposite.getChildren();
				final int n = compositeChildren.size();
				
				if (otherChildren.size() != n) {
					return null;
				}
				
				for (int i = 0; i < n; ++i) {
					this.other = otherChildren.get(i);
					final T childVisit = compositeChildren.get(i).accept(this);
					
					if (childVisit == null) {
						return null;
					}
					
					childVisits.add(childVisit);
				}
			}
			
			return this.endVisit(composite, childVisits);
		}
		
		@Override
		public final T visit(final Module module) {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public final T visit(final Symbol symbol) {
			
			// TODO Auto-generated method stub
			return null;
		}
		
		protected abstract T endVisit(Composite composite, List<T> childVisits);
		
	}
	
	@Override
	public final Expression visit(final Pattern.Any any) {
		final Expression result = this.tryToRewrite(any);
		
		Tools.debugPrint(any, result);
		
		return result;
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
		
		if (module == moduleVisit && (!equals2(module.getParameters(), parameterVisits.get()) ||
				!equals2(module.getConditions(), conditionVisits.get()) ||
				!equals2(module.getFacts(), factVisits.get()))) {
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
