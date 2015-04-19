package averan5.deductions;

import net.sourceforge.aprog.tools.Tools;

/**
 * @author codistmonk (creation 2015-04-19)
 */
public final class Unify implements ExpressionCombiner {
	
	@Override
	public final Object visit(final Object expression1, final Object expression2) {
		if (expression1 instanceof Unifier) {
			return ((Unifier) expression1).unifies(expression2) ? expression1 : null;
		}
		
		if (expression2 instanceof Unifier) {
			return ((Unifier) expression2).unifies(expression1) ? expression2 : null;
		}
		
		return Tools.equals(expression1, expression2) ? expression1 : null;
	}
	
	private static final long serialVersionUID = 3182367276867731182L;
	
	public static final Unify INSTANCE = new Unify();
	
	public static final Object unify(final Object object1, final Object object2) {
		return INSTANCE.apply(object1, object2);
	}
	
}