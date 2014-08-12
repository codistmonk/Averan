package averan.tactics;

import static averan.tactics.SessionTools.apply;
import static averan.tactics.SessionTools.bind;
import static averan.tactics.SessionTools.claim;
import static averan.tactics.SessionTools.rewrite;
import static averan.tactics.SessionTools.session;
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
		final Session session = session();
		final Composite equality = session.getProposition(equalityName);
		
		claim(session.getProposition(sourceName).accept(new Rewriter().rewrite(equality.get(2), equality.get(0))));
		
		{
			final String ruleName = session.getCurrentContext().getModule().newPropositionName();
			
			bind(ruleName, Standard.SYMMETRY_OF_EQUALITY, (Expression) equality.get(0), equality.get(2));
			
			final String reversedEqualityName = session.getCurrentContext().getModule().newPropositionName();
			
			apply(reversedEqualityName, ruleName, equalityName);
			
			rewrite(sourceName, reversedEqualityName);
		}
	}
	
}
