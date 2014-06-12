package jrewrite2;

import java.util.Collections;
import java.util.Set;

import jrewrite2.Template.Variable;

/**
 * @author codistmonk (creation 2014-06-10)
 */
public final class Rewriter implements Visitor {
	
	private final Expression pattern;
	
	private final Expression replacement;
	
	private final Set<Integer> indices;
	
	private int index;
	
	public Rewriter(final Expression pattern, final Expression replacement) {
		this(pattern, replacement, Collections.<Integer> emptySet());
	}
	
	public Rewriter(final Expression pattern, final Expression replacement, final Set<Integer> indices) {
		this.pattern = pattern;
		this.replacement = replacement;
		this.indices = indices;
	}
	
	@Override
	public final Expression visit(final Symbol symbol) {
		Expression result = this.visit((Expression) symbol);
		
		if (result == null) {
			result = symbol;
		}
		
		return result;
	}
	
	@Override
	public final Expression visit(final Variable variable) {
		Expression result = this.visit((Expression) variable);
		
		if (result == null) {
			result = variable;
		}
		
		return result;
	}
	
	@Override
	public final Expression visitBeforeChildren(final Composite composite) {
		return this.visit(composite);
	}
	
	@Override
	public final Composite visitAfterChildren(final Composite composite,
			final Object[] childrenVisitResults) {
		if (!newCompositeNeeded(composite, childrenVisitResults)) {
			return composite;
		}
		
		return newComposite(childrenVisitResults);
	}
	
	@Override
	public final Expression visitBeforeChildren(final Rule rule) {
		return this.visit(rule);
	}
	
	@Override
	public final Rule visitAfterChildren(final Rule rule,
			final Object[] childrenVisitResults) {
		if (!newCompositeNeeded(rule.getComposite(), childrenVisitResults)) {
			return rule;
		}
		
		return new Rule(newComposite(childrenVisitResults));
	}
	
	@Override
	public Expression visitBeforeChildren(final Equality equality) {
		return this.visit(equality);
	}
	
	@Override
	public final Equality visitAfterChildren(final Equality equality,
			final Object[] childrenVisitResults) {
		if (!newCompositeNeeded(equality.getComposite(), childrenVisitResults)) {
			return equality;
		}
		
		return new Equality(newComposite(childrenVisitResults));
	}
	
	@Override
	public final Expression visitBeforeChildren(final Template template) {
		return this.visit(template);
	}
	
	@Override
	public final Template visitAfterChildren(final Template template,
			final Object[] childrenVisitResults) {
		final Expression newProposition = (Expression) childrenVisitResults[0];
		
		return newProposition == template.getProposition() ? template
				: new Template(template.getVariableName(), newProposition, template.new Variable());
	}
	
	private final Expression visit(final Expression object) {
		return this.pattern.equals(object)
				&& (this.indices.contains(this.index++) || this.indices.isEmpty()) ? this.replacement : null;
	}
	
	/**
	 * {@value}.
	 */
	private static final long serialVersionUID = 9150325302376037034L;
	
	public static final boolean newCompositeNeeded(final Composite composite
			, final Object[] childrenVisitResults) {
		final int n = composite.getChildCount();
		
		for (int i = 0; i < n; ++i) {
			if (composite.getChild(i) != childrenVisitResults[i]) {
				return true;
			}
		}
		
		return false;
	}
	
	public static final Composite newComposite(final Object[] childrenVisitResults) {
		final int n = childrenVisitResults.length;
		final Expression[] expressions = new Expression[n];
		
		for (int i = 0; i < n; ++i) {
			expressions[i] = (Expression) childrenVisitResults[i];
		}
		
		return new Composite(expressions);
	}
	
}
