package averan.modules;

import static averan.core.ExpressionTools.*;
import static averan.core.Module.ROOT;
import static averan.core.SessionTools.claim;
import static averan.core.SessionTools.session;
import static net.sourceforge.aprog.tools.Tools.ignore;

import averan.core.Composite;
import averan.core.Expression;
import averan.core.Module;
import averan.core.Session;
import averan.core.Module.Bind;
import averan.core.Rewriter;
import averan.core.Visitor;
import averan.core.Module.Symbol;
import averan.core.Pattern;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
		
		claim(source.accept(new Rewriter().rewrite(equality.get(2), equality.get(0))));
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
	
	public static final void unifyAndApply(final String moduleName, final String conditionName) {
		unifyAndApply(session(), moduleName, conditionName);
	}
	
	public static final void unifyAndApply(final Session session, final String moduleName, final String conditionName) {
		final Module module = session.getProposition(moduleName);
		final Pattern anyfiedCondition = anyfy(module.getConditions().get(0));
		
		if (!anyfiedCondition.equals(session.getProposition(conditionName))) {
			throw new IllegalArgumentException("Failed to unify (" + conditionName + ") with " + anyfiedCondition.getTemplate());
		}
		
		computeBind(session.getCurrentModule(), module, moduleName, anyfiedCondition).execute();
		
		session.apply(session.getFactName(-1), conditionName);
	}
	
	private static final Bind computeBind(final Module context, final Module module, final String moduleName, final Pattern anyfiedCondition) {
		final Bind result = context.new Bind(context, moduleName);
		
		for (final Symbol parameter : module.getParameters()) {
			final String parameterName = parameter.toString();
			
			result.bind(parameterName, anyfiedCondition.get(parameterName));
		}
		
		return result;
	}
	
	public static final Pattern anyfy(final Expression expression) {
		final Map<String, List<Module>> parameterNameUsage = new HashMap<>();
		final Pattern result = new Pattern(expression.accept(new Visitor<Expression>() {
			
			@Override
			public final Expression visit(final Composite composite) {
				return new Composite(composite.childrenAcceptor(this).get());
			}
			
			@Override
			public final Expression visit(final Symbol symbol) {
				return Pattern.any(symbol.toString());
			}
			
			@Override
			public final Expression visit(final Module module) {
				final Module result = new Module(module.getParent(), module.getName(),
						new ArrayList<>(module.getParameters()), new ArrayList<>(module.getConditions()), new ArrayList<>(module.getFacts()));
				final Rewriter rewriter = new Rewriter();
				
				{
					final List<Symbol> parameters = result.getParameters();
					final int n = parameters.size();
					
					for (int i = 0; i < n; ++i) {
						final Symbol parameter = parameters.get(i);
						final List<Module> usage = parameterNameUsage.compute(parameter.toString(), (k, v) -> v == null ? new ArrayList<>() : v );
						
						usage.add(result);
						
						rewriter.rewrite(parameter, Pattern.any(parameter.toString(), usage.size()));
					}
				}
				
				{
					final List<Expression> conditions = result.getConditions();
					final int n = conditions.size();
					
					for (int i = 0; i < n; ++i) {
						conditions.set(i, conditions.get(i).accept(rewriter));
					}
				}
				
				{
					final List<Expression> facts = result.getFacts();
					final int n = facts.size();
					
					for (int i = 0; i < n; ++i) {
						facts.set(i, facts.get(i).accept(rewriter));
					}
				}
				
				return result;
			}
			
			/**
			 * {@value}.
			 */
			private static final long serialVersionUID = -7233860227717949061L;
			
		}));
		
		return result;
	}
	
}
