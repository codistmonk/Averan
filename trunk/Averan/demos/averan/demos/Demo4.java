package averan.demos;

import static averan.core.ExpressionTools.*;
import static averan.core.SessionTools.*;
import static averan.io.ExpressionParser.$$;
import static averan.modules.Standard.*;
import static java.awt.Color.BLACK;
import static java.awt.Color.WHITE;
import static net.sourceforge.aprog.tools.Tools.append;
import static net.sourceforge.aprog.tools.Tools.cast;

import averan.core.Composite;
import averan.core.Expression;
import averan.core.Module;
import averan.core.Pattern.Any.Key;
import averan.core.Module.Bind;
import averan.core.Module.Symbol;
import averan.core.Pattern;
import averan.core.Rewriter;
import averan.core.Session;
import averan.demos.Demo2.BreakSessionException;
import averan.io.SessionExporter;
import averan.io.TexPrinter;
import averan.modules.Reals.IndexFinder;
import averan.modules.Standard;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.scilab.forge.jlatexmath.TeXFormula;

import net.sourceforge.aprog.tools.IllegalInstantiationException;
import net.sourceforge.aprog.tools.Pair;

/**
 * @author codistmonk (creation 2014-08-28)
 */
public final class Demo4 {
	
	private Demo4() {
		throw new IllegalInstantiationException();
	}
	
	public static final Module MODULE = new Module(Standard.MODULE, Demo4.class.getName());
	
	public static final Expression anyfy(final Module module) {
		final Rewriter rewriter = new Rewriter();
		
		for (final Symbol parameter : module.getParameters()) {
			rewriter.rewrite(parameter, new Pattern.Any(new Pattern.Any.Key(parameter)));
		}
		
		return module.accept(rewriter);
	}
	
	public static final List<Pair<String, Pattern>> findJustificationsIn(final Module context, final Expression goal) {
		final List<Pair<String, Pattern>> result = new ArrayList<>();
		
		result.addAll(0, findJustificationsIn(context.getFacts(), context.getFactIndices(), goal));
		result.addAll(0, findJustificationsIn(context.getConditions(), context.getConditionIndices(), goal));
		
		return result;
	}
	
	public static final List<Pair<String, Pattern>> findJustificationsIn(final List<Expression> propositions,
			final Map<String, Integer> propositionIndices, final Expression goal) {
		final List<Pair<String, Pattern>> result = new ArrayList<>();
		
		for (final Map.Entry<String, Integer> entry : propositionIndices.entrySet()) {
			final Expression contextFact = propositions.get(entry.getValue());
			
			{
				final Pattern justificationPattern = new Pattern(contextFact);
				
				if (justificationPattern.equals(goal)) {
					result.add(new Pair<>(entry.getKey(), justificationPattern));
					continue;
				}
			}
			
			{
				Module module = cast(Module.class, contextFact);
				
				if (module != null) {
					module = ((Module) anyfy(module)).canonical();
					
					for (final Expression moduleFact : module.getFacts()) {
						final Pattern pattern = new Pattern(moduleFact);
						
						if (pattern.equals(goal)) {
							final Pattern justificationPattern = new Pattern(module);
							
							justificationPattern.getBindings().putAll(pattern.getBindings());
							
							result.add(new Pair<>(entry.getKey(), justificationPattern));
							
							break;
						}
					}
				}
			}
		}
		
		return result;
	}
	
	public static final List<Pair<String, Pattern>> findJustificationsFor(final Expression goal) {
		return findJustificationsIn(session(), goal);
	}
	
	public static final List<Pair<String, Pattern>> findJustificationsIn(final Session session, final Expression goal) {
		final List<Pair<String, Pattern>> result = new ArrayList<>();
		
		{
			for (Module context = session.getCurrentModule(); context != null; context = context.getParent()) {
				result.addAll(0, findJustificationsIn(context, goal));
			}
			
			for (final Module context : session.getTrustedModules()) {
				result.addAll(0, findJustificationsIn(context, goal));
			}
		}
		
		return result;
	}
	
	public static final boolean proveWithBindAndApply(final Expression goal, final int depth) {
		return proveWithBindAndApply(session(), goal, depth);
	}
	
	public static final boolean proveWithBindAndApply(final Session session, final Expression goal, final int depth) {
		if (depth <= 0) {
			return false;
		}
		
		final List<Pair<String, Pattern>> justifications = findJustificationsIn(session, goal);
		final int n = justifications.size();
		
//		Tools.debugPrint(goal, justifications);
		
		for (int i = n - 1; 0 <= i; --i) {
			final Pair<String, Pattern> justification = justifications.get(i);
			final Module module = cast(Module.class, justification.getSecond().getTemplate());
			
			if (module == null) {
				recall(session, justification.getFirst());
				return true;
			}
			
			if (module.getConditions().isEmpty()) {
				bindPattern(session, justification);
				return true;
			}
		}
		
		tryToBindAndProveConditions:
		for (int i = n - 1; 0 <= i; --i) {
			final Pair<String, Pattern> justification = justifications.get(i);
			final Module module = cast(Module.class, justification.getSecond().getTemplate());
			
			if (module != null && !module.getConditions().isEmpty()) {
				session.claim(goal);
				{
					bindPattern(session, justification);
					
					Module bound = session.getFact(-1);
					
					while (bound != null && !bound.getConditions().isEmpty()) {
						final Expression condition = bound.getConditions().get(0);
						
						if (proveWithBindAndApply(session, condition, depth - 1)) {
							session.apply(session.getFactName(-2), session.getFactName(-1));
							
							bound = cast(Module.class, session.getFact(-1));
						} else {
							session.abort();
							
							continue tryToBindAndProveConditions;
						}
					}
				}
				
				return true;
			}
		}
		
		return false;
	}
	
	public static final void bindPattern(final Session session, final Pair<String, Pattern> justification) {
		final Bind bind = session.getCurrentModule().new Bind(session.getCurrentModule(), justification.getFirst());
		
		for (final Map.Entry<Pattern.Any.Key, Expression> binding : justification.getSecond().getBindings().entrySet()) {
			bind.bind(binding.getKey().toString(), binding.getValue());
		}
		
		bind.execute();
	}
	
	public static final void canonicalize(final Expression expression, final RewriteHint... hints) {
		canonicalize(session(), expression, hints);
	}
	
	public static final void canonicalize(final Session session, final Expression expression,
			final RewriteHint... hints) {
		final Session s = new Session(new Module(session.getCurrentModule(), session.newPropositionName()));
		
		s.bind(IDENTITY, expression);
		
		final int sOldFactCount = s.getCurrentModule().getFacts().size();
		boolean done;
		
		do {
			done = true;
			
			tryHint:
				for (final RewriteHint hint : hints) {
					final List<Pair<Integer, Pattern>> pairs = s.getFact(-1).accept(new IndexFinder(true, hint.getLeftPattern()));
					
					for (final Pair<Integer, Pattern> pair : pairs) {
						final Expression oldFact = s.getFact(-1);
						final String  oldFactName = s.getFactName(-1);
						final Session tmp = new Session(new Module(s.getCurrentModule(), s.newPropositionName()));
						
						if (tryToRewrite(tmp, oldFactName, hint, pair)) {
							final Expression newFact = lastFactOf(tmp.getCurrentModule());
							
							if (!hint.isInfinite() || newFact.toString().compareTo(oldFact.toString()) < 0) {
								done = false;
								s.getCurrentModule().new Claim(newFact, tmp.getCurrentModule()).execute();
								continue tryHint;
							}
						}
					}
				}
		} while (!done);
		
		if (sOldFactCount < s.getCurrentModule().getFacts().size()) {
			final Expression newFact = lastFactOf(s.getCurrentModule());
			
			session.getCurrentModule().new Claim(newFact, s.getCurrentModule()).execute();
		}
	}
	
	public static boolean tryToRewrite(final Session session, final String toRewriteName,
			final RewriteHint hint, final Pair<Integer, Pattern> pair) {
		final Pattern pattern = pair.getSecond();
		
		{
			final Module context = session.getCurrentModule();
			final Bind bind = context.new Bind(context, hint.getPropositionName());
			
			for (final Map.Entry<Key, Expression> binding : pattern.getBindings().entrySet()) {
				bind.bind(binding.getKey().toString(), binding.getValue());
			}
			
			bind.execute();
		}
		
		Module bound = cast(Module.class, session.getFact(-1));
		String boundName = session.getFactName(-1);
		
		if (bound != null) {
			session.claim(lastFactOf(bound.canonical()));
			{
				while (bound != null && !bound.getConditions().isEmpty()) {
					if (!proveWithBindAndApply(session, bound.getConditions().get(0), 3)) {
						return false;
					}
					
					session.apply(boundName, session.getFactName(-1));
					bound = cast(Module.class, session.getFact(-1));
					boundName = session.getFactName(-1);
				}
			}
		}
		
		session.rewrite(toRewriteName, boundName, pair.getFirst());
		
		return true;
	}
	
	public static final Expression lastFactOf(final Module module) {
		final List<Expression> facts = module.getFacts();
		
		return facts.get(facts.size() - 1);
	}
	
	public static final void proveEquality(final Composite equality, final RewriteHint... hints) {
		proveEquality(session(), equality, hints);
	}
	
	public static final void proveEquality(final Session session, final Composite equality, final RewriteHint... hints) {
		canonicalize(equality.get(0), hints);
		canonicalize(equality.get(2), hints);
		rewriteRight(factName(-2), factName(-1));
	}
	
	static {
		new SessionScaffold() {
			
			@Override
			public final void run() {
				claim("test1", $$("∀x,y (x → ((x→y) → y))"));
				{
					introduce();
					introduce();
					introduce();
					introduce();
					proveWithBindAndApply(goal(), 2);
				}
				
				admit("type_of_addition", $$("∀x,y ((x∈ℝ) → ((y∈ℝ) → ((x+y)∈ℝ)))"));
				admit("commutativity_of_addition", $$("∀x,y ((x∈ℝ) → ((y∈ℝ) → ((x+y)=(y+x))))"));
				admit("associativity_of_addition", $$("∀x,y,z ((x∈ℝ) → ((y∈ℝ) → ((z∈ℝ) → (x+(y+z)=x+y+z))))"));
				claim("ordering_of_terms", $$("∀x,y,z ((x∈ℝ) → ((y∈ℝ) → ((z∈ℝ) → ((x+z+y)=(x+y+z)))))"));
				{
					final Symbol x = introduce();
					final Symbol y = introduce();
					final Symbol z = introduce();
					introduce();
					introduce();
					introduce();
					
					final Composite goal = goal();
					
					bind(IDENTITY, (Expression) goal.get(0));
					
					final Composite pxz = $(x, "+", z);
					final Composite pxzy = $(pxz, "+", y);
					final Composite pypxz = $(y, "+", pxz);
					final Composite pyx = $(y, "+", x);
					final Composite pyxz = $(pyx, "+", z);
					final Composite pxy = $(x, "+", y);
					
					proveWithBindAndApply($(pxzy, "=", pypxz), 3);
					rewrite(factName(-2), factName(-1), 1);
					proveWithBindAndApply($(pypxz, "=", pyxz), 3);
					rewrite(factName(-2), factName(-1));
					proveWithBindAndApply($(pyx, "=", pxy), 3);
					rewrite(factName(-2), factName(-1));
				}
				
				final RewriteHint[] additionHints = {
						new RewriteHint(session(), "commutativity_of_addition", true),
						new RewriteHint(session(), "associativity_of_addition", false),
						new RewriteHint(session(), "ordering_of_terms", true),
				};
				
				claim("test2", $$("∀x,y,z ((x∈ℝ) → ((y∈ℝ) → ((z∈ℝ) → ((x+z+y)=(z+y+x)))))"));
				{
					introduce();
					introduce();
					introduce();
					introduce();
					introduce();
					introduce();
					
					proveEquality(goal(), additionHints);
				}
				
				admit("type_of_multiplication", $$("∀x,y ((x∈ℝ) → ((y∈ℝ) → ((xy)∈ℝ)))"));
				admit("commutativity_of_multiplication", $$("∀x,y ((x∈ℝ) → ((y∈ℝ) → ((xy)=(yx))))"));
				admit("associativity_of_multiplication", $$("∀x,y,z ((x∈ℝ) → ((y∈ℝ) → ((z∈ℝ) → (x(yz)=xyz))))"));
				claim("ordering_of_factors", $$("∀x,y,z ((x∈ℝ) → ((y∈ℝ) → ((z∈ℝ) → ((xzy)=(xyz)))))"));
				{
					final Symbol x = introduce();
					final Symbol y = introduce();
					final Symbol z = introduce();
					introduce();
					introduce();
					introduce();
					
					final Composite goal = goal();
					
					bind(IDENTITY, (Expression) goal.get(0));
					
					final Composite pxz = $(x, z);
					final Composite pxzy = $(pxz, y);
					final Composite pypxz = $(y, pxz);
					final Composite pyx = $(y, x);
					final Composite pyxz = $(pyx, z);
					final Composite pxy = $(x, y);
					
					proveWithBindAndApply($(pxzy, "=", pypxz), 3);
					rewrite(factName(-2), factName(-1), 1);
					proveWithBindAndApply($(pypxz, "=", pyxz), 3);
					rewrite(factName(-2), factName(-1));
					proveWithBindAndApply($(pyx, "=", pxy), 3);
					rewrite(factName(-2), factName(-1));
				}
				
				final RewriteHint[] multiplicationHints = {
						new RewriteHint(session(), "commutativity_of_multiplication", true),
						new RewriteHint(session(), "associativity_of_multiplication", false),
						new RewriteHint(session(), "ordering_of_factors", true),
				};
				
				claim("test3", $$("∀x,y,z ((x∈ℝ) → ((y∈ℝ) → ((z∈ℝ) → ((xzy)=(zyx)))))"));
				{
					introduce();
					introduce();
					introduce();
					introduce();
					introduce();
					introduce();
					
					proveEquality(goal(), multiplicationHints);
				}
				
				final RewriteHint[] additionAndMultiplicationHints = append(additionHints, multiplicationHints);
				
				claim("test4", $$("∀a,b,c,d ((a∈ℝ) → ((b∈ℝ) → ((c∈ℝ) → ((d∈ℝ) → ((dc+ba)=(ab+cd))))))"));
				{
					introduce();
					introduce();
					introduce();
					introduce();
					introduce();
					introduce();
					introduce();
					introduce();
					
					proveEquality(goal(), additionAndMultiplicationHints);
				}
			}
			
			/**
			 * {@value}.
			 */
			private static final long serialVersionUID = -2527396009076173030L;
			
		};
	}
	
	/**
	 * @param commandLineArguments
	 * <br>Unused
	 */
	public static void main(final String[] commandLineArguments) {
		// NOP
	}
	
	/**
	 * @author codistmonk (creation 2014-08-28)
	 */
	public static final class RewriteHint implements Serializable {
		
		private final Session session;
		
		private final String propositionName;
		
		private final boolean infinite;
		
		private final Pattern leftPattern;
		
		private final Expression rightTemplate;
		
		public RewriteHint(final Session session, final String propositionName, final boolean infinite) {
			this.session = session;
			this.propositionName = propositionName;
			this.infinite = infinite;
			
			final Expression proposition = session.getProposition(propositionName);
			
			if (Module.isEquality(proposition)) {
				final Composite equality = (Composite) proposition;
				
				this.leftPattern = new Pattern(equality.get(0));
				this.rightTemplate = equality.get(2);
			} else if (proposition instanceof Module) {
				final Module module = (Module) anyfy(((Module) proposition).canonical());
				final List<Expression> facts = module.getFacts();
				final Composite equality = (Composite) facts.get(facts.size() - 1);
				
				if (!Module.isEquality(equality)) {
					throw new IllegalArgumentException();
				}
				
				this.leftPattern = new Pattern(equality.get(0));
				this.rightTemplate = equality.get(2);
			} else {
				throw new IllegalArgumentException();
			}
		}
		
		public final String getPropositionName() {
			return this.propositionName;
		}
		
		public final Pattern getLeftPattern() {
			return this.leftPattern;
		}
		
		public final Expression getRightTemplate() {
			return this.rightTemplate;
		}
		
		public final boolean isInfinite() {
			return this.infinite;
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = -6577146621435043597L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-08-28)
	 */
	public static abstract class SessionScaffold implements Serializable {
		
		public SessionScaffold() {
			final Session session = session();
			String sessionBreakPoint = "";
			
			try {
				this.run();
			} catch (final BreakSessionException exception) {
				sessionBreakPoint = exception.getStackTrace()[1].toString();
			} finally {
				new SessionExporter(session, 0).exportSession();
				
				System.out.println(sessionBreakPoint);
			}
			
			{
				final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
				
				new SessionExporter(session, new TexPrinter(buffer)
				, 1 < session.getStack().size() ? 0 : 1).exportSession();
				
//				System.out.println(buffer.toString());
				
				new TeXFormula(buffer.toString()).createPNG(0, 18F, "view.png", WHITE, BLACK);
			}
		}
		
		public abstract void run();
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = -8607265458958375768L;
		
	}
	
}
