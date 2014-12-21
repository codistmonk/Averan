package averan.modules;

import static averan.core.ExpressionTools.*;
import static averan.core.Module.ROOT;
import static averan.core.SessionTools.*;
import static net.sourceforge.aprog.tools.Tools.cast;
import static net.sourceforge.aprog.tools.Tools.ignore;
import averan.core.Composite;
import averan.core.Expression;
import averan.core.Module;
import averan.core.Pattern;
import averan.core.Session;
import averan.core.Rewriter;
import averan.core.Module.Bind;
import averan.core.Module.Symbol;
import averan.core.Pattern.Any;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import net.sourceforge.aprog.tools.IllegalInstantiationException;
import net.sourceforge.aprog.tools.Pair;

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
		rewriteRight((String) null, sourceName, equalityName, indices);
	}
	
	public static final void rewriteRight(final String factName, final String sourceName, final String equalityName, final Integer... indices) {
		rewriteRight(session(), factName, sourceName, equalityName, indices);
	}
	
	public static final void rewriteRight(final Session session, final String sourceName, final String equalityName, final Integer... indices) {
		rewriteRight(session, null, sourceName, equalityName, indices);
	}
	
	public static final void rewriteRight(final Session session, final String factName, final String sourceName, final String equalityName, final Integer... indices) {
		final Expression source = session.getProposition(sourceName);
		final Composite equality = session.getProposition(equalityName);
		
		session.claim(factName == null ? session.newPropositionName() : factName,
				source.accept(new Rewriter().rewrite(equality.get(2), equality.get(0)).atIndices(Arrays.asList(indices))));
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
	
	public static final void proveUsingBindAndApply(final Expression expression) {
		proveUsingBindAndApply(session(), expression);
	}
	
	public static final void proveUsingBindAndApply(final Session session, final Expression expression) {
		Pair<String, Pattern> justificationNameAndPattern = justificationFor(session, expression);
		
		if (justificationNameAndPattern == null) {
			throw new IllegalArgumentException("No justification found for: " + expression);
		}
		
		final String justificationName = justificationNameAndPattern.getFirst();
		final Pattern pattern = justificationNameAndPattern.getSecond();
		final Expression justification = session.getProposition(justificationName);
		final Module module = cast(Module.class, justification);
		
		if (module == null) {
			recall(session, justificationName);
		} else if (!Standard.ELIMINATION_OF_FALSE.equals(justificationName)) {
			session.claim(expression);
			
			{
				final Module context = session.getCurrentModule();
				final Bind bind = context.new Bind(context, justificationName);
				
				for (final Map.Entry<Any.Key, Expression> entry : pattern.getBindings().entrySet()) {
					bind.bind(entry.getKey().toString(), entry.getValue());
				}
				
				bind.execute();
				
				for (Module last = cast(Module.class, session.getFact(-1)); last != null && !last.getConditions().isEmpty(); last = cast(Module.class, session.getFact(-1))) {
					final Expression condition = last.getConditions().get(0);
					
					if (expression.equals(condition)) {
						throw new IllegalStateException("Circularity: " + condition + " needed to prove itself");
					}
					
					proveUsingBindAndApply(session, condition);
					session.apply(session.getFactName(-2), session.getFactName(-1));
				}
			}
		}
	}
	
	public static final Pair<String, Pattern> justificationFor(final Module context, final Expression target) {
		Pair<String, Pattern> result = null;
		
		for (Module c = context; c != null && result == null; c = c.getParent()) {
			result = justificationFor(target, c.getFacts(), c.getFactIndices());
			
			if (result == null) {
				result = justificationFor(target, c.getConditions(), c.getConditionIndices());
			}
		}
		
		return result;
	}
	
	public static final Pair<String, Pattern> justificationFor(final Session session, final Expression target) {
		Pair<String, Pattern> result = justificationFor(session.getCurrentModule(), target);
		
		if (result != null) {
			return result;
		}
		
//		for (final Module module : session.getTrustedModules()) {
//			result = justificationFor(module, target);
//			
//			if (result != null) {
//				return result;
//			}
//		}
		
		return null;
	}
	
	public static final Pattern patternFor(final Module context, final Expression expression) {
		final Rewriter rewriter = new Rewriter();
		
		scheduleAnyfyParameters(cast(Module.class, expression), rewriter);
		
		for (Module c = context; c != null; c = c.getParent()) {
			scheduleAnyfyParameters(c, rewriter);
		}
		
		return new Pattern(expression.accept(rewriter));
	}
	
	public static final void scheduleAnyfyParameters(final Module module, final Rewriter rewriter) {
		if (module == null) {
			return;
		}
		
		for (final Symbol symbol : module.getParameters()) {
			rewriter.rewrite(symbol, new Pattern.Any(new Pattern.Any.Key(symbol)));
		}
	}
	
	private static final Pair<String, Pattern> justificationFor(final Expression target,
			final List<Expression> propositions, final Map<String, Integer> propositionIndices) {
		for (final Map.Entry<String, Integer> entry : propositionIndices.entrySet()) {
			final Expression proposition = propositions.get(entry.getValue());
			
			if (target.equals(proposition)) {
				return new Pair<>(entry.getKey(), new Pattern(target));
			}
			
			Module module = cast(Module.class, proposition);
			
			if (module != null) {
				module = module.canonical();
				final List<Expression> facts = module.getFacts();
				final Pattern pattern = patternFor(module, facts.get(facts.size() - 1));
				
				if (pattern.equals(target)) {
					return new Pair<>(entry.getKey(), pattern);
				}
			}
		}
		
		return null;
	}
	
}
