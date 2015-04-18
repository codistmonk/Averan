package averan5.deductions;

import averan5.core.Goal;

import net.sourceforge.aprog.tools.IllegalInstantiationException;

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
	
}
