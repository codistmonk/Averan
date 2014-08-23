package averan.modules;

import static averan.core.ExpressionTools.*;
import static averan.core.Module.ROOT;
import static averan.core.SessionTools.*;
import static net.sourceforge.aprog.tools.Tools.ignore;

import averan.core.Composite;
import averan.core.Expression;
import averan.core.Module;
import averan.core.Session;
import averan.core.Rewriter;
import averan.core.Module.Symbol;

import net.sourceforge.aprog.tools.IllegalInstantiationException;

/**
 * @author codistmonk (creation 2014-08-04)
 */
public final class Standard {
	
	private Standard() {
		throw new IllegalInstantiationException();
	}
	
	/**
	 * {@value}.
	 */
	public static final String IDENTITY = "identity";
	
	/**
	 * {@value}.
	 */
	public static final String SYMMETRY_OF_EQUALITY = "symmetry_of_equality";

	/**
	 * {@value}.
	 */
	public static final String TRUTHNESS_OF_TRUE = "truthness_of_true";
	
	/**
	 * {@value}.
	 */
	public static final String ELIMINATION_OF_FALSE = "elimination_of_false";
	
	/**
	 * {@value}.
	 */
	public static final String RECALL = "recall";
	
	public static final Module MODULE = new Module(ROOT);
	
	public static final Symbol TRUE = MODULE.new Symbol("true");
	
	public static final Symbol FALSE = MODULE.new Symbol("false");
	
	static {
		final Session session = new Session(MODULE);
		
		session.claim(IDENTITY, $(forAll("x"), equality("x", "x")));
		{
			final Symbol x = session.introduceAndGet();
			
			session.substitute(substitution(x));
			session.rewrite(session.getFactName(-1), session.getFactName(-1));
		}
		
		session.claim(SYMMETRY_OF_EQUALITY, $(forAll("x", "y"), $(equality("x", "y"), "->", equality("y", "x"))));
		{
			final Symbol x = session.introduceAndGet();
			final Symbol y = session.introduceAndGet();
			
			ignore(y);
			
			session.introduce();
			session.bind(IDENTITY, x);
			session.rewrite(session.getFactName(-1), session.getConditionName(-1), 0);
		}
		
		session.admit(TRUTHNESS_OF_TRUE, TRUE);
		
		session.admit(ELIMINATION_OF_FALSE, $(forAll("P"), $(FALSE, "->", "P")));
		
		session.claim(RECALL, $(forAll("P"), $("P", "->", "P")));
		{
			final Symbol p = session.introduceAndGet();
			
			session.introduce();
			session.bind(IDENTITY, p);
			session.rewrite(session.getConditionName(-1), session.getFactName(-1));
		}
	}
	
	public static final void rewriteRight(final String sourceName, final String equalityName, final Integer... indices) {
		rewriteRight(session(), sourceName, equalityName, indices);
	}
	
	public static final void rewriteRight(final Session session, final String sourceName, final String equalityName, final Integer... indices) {
		final Expression source = session.getProposition(sourceName);
		final Composite equality = session.getProposition(equalityName);
		
		session.claim(source.accept(new Rewriter().rewrite(equality.get(2), equality.get(0))));
		{
			unifyAndApply(session, SYMMETRY_OF_EQUALITY, equalityName);
			session.rewrite(sourceName, session.getFactName(-1), indices);
		}
	}
	
	public static final void recall(final String propositionName) {
		recall(session(), propositionName);
	}
	
	public static final void recall(final Session session, final String propositionName) {
		session.claim(session.getProposition(propositionName));
		{
			unifyAndApply(session, RECALL, propositionName);
		}
	}
	
}
