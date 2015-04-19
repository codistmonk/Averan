package averan5.deductions;

import static net.sourceforge.aprog.tools.Tools.*;

import averan5.core.Goal;
import averan5.deductions.StandardTest.ExpressionCombiner;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;

import net.sourceforge.aprog.tools.IllegalInstantiationException;
import net.sourceforge.aprog.tools.Pair;
import net.sourceforge.aprog.tools.Tools;

/**
 * @author codistmonk (creation 2015-04-18)
 */
public final class AutoDeduce {
	
	private AutoDeduce() {
		throw new IllegalInstantiationException();
	}
	
	public static final boolean autoDeduce(final Object goal) {
		final Goal g = Goal.deduce(goal);
		
		try {
			g.intros();
			
			g.conclude();
			
			return true;
		} catch (final Exception exception) {
			return false;
		}
	}
	
	public static final Pair<String, Object> justify(final Object goal) {
		return null;
	}
	
	public static final Object unify(final Object object1, final Object object2) {
		return Unify.INSTANCE.apply(object1, object2);
	}
	
	/**
	 * @author codistmonk (creation 2015-04-19)
	 */
	public static final class Unify implements ExpressionCombiner {
		
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
		
	}
	
	/**
	 * @author codistmonk (creation 2015-04-19)
	 */
	public static final class Unifier implements Serializable {
		
		private Collection<Unifier> unifiers;
		
		private Object[] object;
		
		public Unifier() {
			this.unifiers = new HashSet<>();
			this.object = new Object[1];
			
			this.unifiers.add(this);
		}
		
		public final boolean unifies(final Object object) {
			if (this == object) {
				return true;
			}
			
			if (object == null) {
				return false;
			}
			
			final Object thisObject = this.object[0];
			final Unifier that = cast(this.getClass(), object);
			
			if (that != null) {
				final Object thatObject = that.object[0];
				final boolean merge = thisObject != null && thatObject != null;
				
				if (merge && !thisObject.equals(thatObject)) {
					return false;
				}
				
				final Unifier absorber, absorbed;
				
				if (merge || thatObject == null) {
					absorber = this;
					absorbed = that;
				} else {
					absorber = that;
					absorbed = this;
				}
				
				for (final Unifier unifier : absorbed.unifiers) {
					absorber.unifiers.add(unifier);
					unifier.unifiers = absorber.unifiers;
					unifier.object = absorber.object;
				}
				
				return true;
			}
			
			{
				if (thisObject == null) {
					this.object[0] = object;
					
					return true;
				}
				
				return Tools.equals(thisObject, object);
			}
		}
		
		private static final long serialVersionUID = 4343191681740782407L;
		
	}
	
}
