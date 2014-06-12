package jrewrite2;

import java.io.Serializable;

/**
 * @author codistmonk (creation 2014-06-10)
 */
public abstract interface Visitor extends Serializable {
	
	public abstract Object visit(Symbol symbol);
	
	public abstract Object visit(Template.Variable variable);
	
	public abstract Object visitBeforeChildren(Composite composite);
	
	public abstract Object visitAfterChildren(Composite composite, Object[] childrenVisitResults);
	
	public abstract Object visitBeforeChildren(Rule rule);
	
	public abstract Object visitAfterChildren(Rule rule, Object[] childrenVisitResults);
	
	public abstract Object visitBeforeChildren(Equality equality);
	
	public abstract Object visitAfterChildren(Equality equality, Object[] childrenVisitResults);
	
	public abstract Object visitBeforeChildren(Template template);
	
	public abstract Object visitAfterChildren(Template template, Object[] childrenVisitResults);
	
}
