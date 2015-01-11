package averan3.deductions;

import static averan3.core.Composite.EQUALS;
import static averan3.core.Session.*;

import averan3.core.Variable;

import net.sourceforge.aprog.tools.IllegalInstantiationException;

/**
 * @author codistmonk (creation 2015-01-11)
 */
public final class AutoDeduce3 {
	
	private AutoDeduce3() {
		throw new IllegalInstantiationException();
	}
	
	public static final void deduceFundamentalPropositions() {
		deduce("identity");
		{
			final Variable x = introduce("x");
			
			substitute($$(x, $(), $()));
			rewrite(name(-1), name(-1));
			conclude();
		}
		
		deduce("symmetry_of_equality");
		{
			final Variable x = introduce("x");
			final Variable y = introduce("y");
			
			suppose($(x, EQUALS, y));
			bind("identity", x);
			rewrite(name(-1), name(-2), 0);
			conclude();
		}
		
		deduce("recall");
		{
			final Variable p = introduce("P");
			
			suppose(p);
			bind("identity", p);
			rewrite(name(-2), name(-1));
			conclude();
		}
	}

}
