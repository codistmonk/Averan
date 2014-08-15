package averan.tactics;

import static averan.tactics.SessionTools.*;

import averan.core.Composite;
import averan.core.Expression;
import averan.core.Rewriter;
import averan.modules.Standard;

import net.sourceforge.aprog.tools.IllegalInstantiationException;

/**
 * @author codistmonk (creation 2014-08-12)
 */
public final class StandardTools {
	
	private StandardTools() {
		throw new IllegalInstantiationException();
	}
	
	public static final void rewriteRight(final String sourceName, final String equalityName) {
		final Expression source = proposition(sourceName);
		final Composite equality = proposition(equalityName);
		final Expression left = equality.get(0);
		final Expression right = equality.get(2);
		
		claim(source.accept(new Rewriter().rewrite(right, left)));
		
		{
			bind(Standard.SYMMETRY_OF_EQUALITY, left, right);
			apply(factName(-1), equalityName);
			rewrite(sourceName, factName(-1));
		}
	}
	
}
