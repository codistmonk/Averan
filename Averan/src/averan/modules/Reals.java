package averan.modules;

import static averan.core.ExpressionTools.*;
import static averan.core.SessionTools.*;
import static averan.io.ExpressionParser.$$;
import static averan.modules.Standard.*;
import static net.sourceforge.aprog.tools.Tools.append;
import static net.sourceforge.aprog.tools.Tools.cast;

import averan.core.Composite;
import averan.core.Expression;
import averan.core.IndexFinder;
import averan.core.Module;
import averan.core.Pattern.Any.Key;
import averan.core.Module.Bind;
import averan.core.Module.Symbol;
import averan.core.Pattern;
import averan.core.Rewriter;
import averan.core.Session;
import averan.modules.Standard;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.aprog.tools.IllegalInstantiationException;
import net.sourceforge.aprog.tools.Pair;

/**
 * @author codistmonk (creation 2014-08-28)
 */
public final class Reals {
	
	private Reals() {
		throw new IllegalInstantiationException();
	}
	
	private static int proveWithBindAndApplyDepth = 4;
	
	public static final Module MODULE = new Module(Standard.MODULE, Reals.class.getName());
	
	public static final Map<String, RewriteHint[]> hints = new HashMap<>();
	
	public static final Symbol ZERO = $$("0");
	
	public static final Symbol ONE = $$("1");
	
	static {
		pushNewSession(MODULE);
		
		try {
			suppose("type_of_0", natural(ZERO));
			suppose("type_of_1", natural(ONE));
			suppose("mathematical_induction", $$("∀P,i (P{i=0} → (i∈ℕ → (P → (P{i=(i+1)})) → (∀n (P{i=n}))))"));
			suppose("naturals_are_reals", $$("∀x ((x∈ℕ) → (x∈ℝ))"));
			suppose("definition_of_natural_range",
					$$("∀n ((n∈ℕ) → (∀i ((i∈ℕ_n) → (i∈ℕ ∧ i<n))))"));
			
			suppose("type_of_inverse", $$("∀x ((x∈ℝ) → ((1/x)∈ℝ))"));
			suppose("type_of_division", $$("∀x,y ((x∈ℝ) → ((y∈ℝ) → ((x/y)∈ℝ)))"));
			
			suppose("type_of_addition", $$("∀x,y ((x∈ℝ) → ((y∈ℝ) → ((x+y)∈ℝ)))"));
			suppose("commutativity_of_addition", $$("∀x,y ((x∈ℝ) → ((y∈ℝ) → ((x+y)=(y+x))))"));
			suppose("associativity_of_addition", $$("∀x,y,z ((x∈ℝ) → ((y∈ℝ) → ((z∈ℝ) → (x+(y+z)=x+y+z))))"));
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
				
				proveWithBindAndApply($(pxzy, "=", pypxz));
				rewrite(factName(-2), factName(-1), 1);
				proveWithBindAndApply($(pypxz, "=", pyxz));
				rewrite(factName(-2), factName(-1));
				proveWithBindAndApply($(pyx, "=", pxy));
				rewrite(factName(-2), factName(-1));
			}
			
			final RewriteHint[] additionHints = {
					new RewriteHint("commutativity_of_addition", true),
					new RewriteHint("associativity_of_addition", false),
					new RewriteHint("ordering_of_terms", true),
			};
			
			hints.put("addition", additionHints);
			
			suppose("definition_of_1", $$("∀x ((x∈ℝ) → (x1=x))"));
			suppose("type_of_multiplication", $$("∀x,y ((x∈ℝ) → ((y∈ℝ) → ((xy)∈ℝ)))"));
			suppose("commutativity_of_multiplication", $$("∀x,y ((x∈ℝ) → ((y∈ℝ) → ((xy)=(yx))))"));
			suppose("associativity_of_multiplication", $$("∀x,y,z ((x∈ℝ) → ((y∈ℝ) → ((z∈ℝ) → (x(yz)=xyz))))"));
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
				
				proveWithBindAndApply($(pxzy, "=", pypxz));
				rewrite(factName(-2), factName(-1), 1);
				proveWithBindAndApply($(pypxz, "=", pyxz));
				rewrite(factName(-2), factName(-1));
				proveWithBindAndApply($(pyx, "=", pxy));
				rewrite(factName(-2), factName(-1));
			}
			
			final RewriteHint[] multiplicationHints = {
					new RewriteHint("definition_of_1", false),
					new RewriteHint("commutativity_of_multiplication", true),
					new RewriteHint("associativity_of_multiplication", false),
					new RewriteHint("ordering_of_factors", true),
			};
			
			hints.put("multiplication", multiplicationHints);
			
			final RewriteHint[] additionAndMultiplicationHints = append(additionHints, multiplicationHints);
			
			suppose("definition_of_0", $$("∀x ((x∈ℝ) → (x+0=x))"));
			suppose("definition_of_opposite", $$("∀x ((x∈ℝ) → (x+(-x)=0))"));
			suppose("type_of_opposite", $$("∀x ((x∈ℝ) → ((-x)∈ℝ))"));
			suppose("definition_of_subtraction", $$("∀x,y ((x∈ℝ) → ((y∈ℝ) → ((x-y)=(x+(-y)))))"));
			suppose("opposite_of_multiplication", $$("∀x,y ((x∈ℝ) → ((y∈ℝ) → (((-x)y)=(-(xy)))))"));
			
			final RewriteHint[] subtractionHints = {
					new RewriteHint("definition_of_subtraction", false),
					new RewriteHint("opposite_of_multiplication", false),
			};
			
			hints.put("subtraction", subtractionHints);
			
			suppose("left_distributivity_of_multiplication_over_addition",
					$$("∀x,y,z ((x∈ℝ) → ((y∈ℝ) → ((z∈ℝ) → ((x(y+z))=(xy+xz)))))"));
			
			claim("right_distributivity_of_multiplication_over_addition",
					$$("∀x,y,z ((x∈ℝ) → ((y∈ℝ) → ((z∈ℝ) → (((x+y)z)=(xz+yz)))))"));
			{
				final Symbol x = introduce();
				final Symbol y = introduce();
				final Symbol z = introduce();
				introduce();
				introduce();
				introduce();
				
				final Composite goal = goal();
				
				bind(IDENTITY, (Expression) goal.get(0));
				proveWithBindAndApply($($($(x, "+", y), z), "=", $(z, $(x, "+", y))));
				rewrite(factName(-2), factName(-1), 1);
				proveWithBindAndApply($($(z, $(x, "+", y)), "=", $($(z, x), "+", $(z, y))));
				rewrite(factName(-2), factName(-1));
				canonicalize(((Composite) fact(-1)).get(2), additionAndMultiplicationHints);
				rewrite(factName(-2), factName(-1));
			}
			
			suppose("left_distributivity_of_multiplication_over_subtraction",
					$$("∀x,y,z ((x∈ℝ) → ((y∈ℝ) → ((z∈ℝ) → ((x(y-z))=(xy-xz)))))"));
			
			suppose("right_distributivity_of_multiplication_over_subtraction",
					$$("∀x,y,z ((x∈ℝ) → ((y∈ℝ) → ((z∈ℝ) → (((x-y)z)=(xz-yz)))))"));
			
			final RewriteHint[] distributivityHints = {
					new RewriteHint("left_distributivity_of_multiplication_over_addition", false),
					new RewriteHint("right_distributivity_of_multiplication_over_addition", false),
			};
			
			hints.put("distributivity", distributivityHints);
			
			final RewriteHint[] arithmeticHints = append(additionAndMultiplicationHints,
					append(subtractionHints, distributivityHints));
			
			hints.put("arithmetic", arithmeticHints);
			
			claim("subtract_1_add_1",
					$$("∀x ((x∈ℝ) → (((x-1)+1)=x))"));
			{
				final Symbol x = introduce();
				
				introduce();
				
				final Expression left = ((Composite) goal()).get(0);
				
				bind("naturals_are_reals", ONE);
				apply(factName(-1), "type_of_1");
				proveEquality(equality(left, $(x, "+", $(ONE, "+", $("-", ONE)))), arithmeticHints);
				bind("definition_of_opposite", ONE);
				apply(factName(-1), factName(-3));
				rewrite(factName(-3), factName(-1));
				bind("definition_of_0", x);
				apply(factName(-1), conditionName(-1));
				rewrite(factName(-3), factName(-1));
			}
			
			suppose("definition_of_sum_0",
					$$("∀X,i (((Σ_(i=0)^0) X)=(X{i=0}))"));
			suppose("definition_of_sum_n",
					$$("∀X,n,i (((Σ_(i=0)^n) X)=((Σ_(i=0)^(n-1)) X)+(X{i=n}))"));
			// TODO claim
			admit("type_of_sum",
					$$("∀X,n,i (((i∈ℕ_(n+1)) → (X∈ℝ)) → (((Σ_(i=0)^n) X)∈ℝ))"));
			admit("left_distributivity_over_sum",
					$$("∀X,Y,n,i ((X ((Σ_(i=0)^n) Y))=((Σ_(i=0)^n) (XY)))"));
			admit("right_distributivity_over_sum",
					$$("∀X,Y,n,i ((((Σ_(i=0)^n) X)Y)=((Σ_(i=0)^n) (XY)))"));
			admit("commutativity_of_sum_nesting",
					$$("∀X,m,n,i,j ((((Σ_(i=0)^m) ((Σ_(j=0)^n) X)))=(((Σ_(j=0)^n) ((Σ_(i=0)^m) X))))"));
			admit("distributivity_of_sum_over_addition",
					$$("∀X,Y,n,i (((Σ_(i=0)^n) (X+Y))=(((Σ_(i=0)^n) X)+((Σ_(i=0)^n) Y)))"));
			admit("distributivity_of_sum_over_subtraction",
					$$("∀X,Y,n,i (((Σ_(i=0)^n) (X-Y))=(((Σ_(i=0)^n) X)-((Σ_(i=0)^n) Y)))"));
		} finally {
			popSession();
		}
	}
	
	public static final Expression natural(final Expression expression) {
		return $(expression, "∈", "ℕ");
	}
	
	public static final Expression real(final Expression expression) {
		return $(expression, "∈", "ℝ");
	}
	
	public static final Expression inverse(final Expression expression) {
		return $("1", "/", expression);
	}
	
	public static final int getProveWithBindAndApplyDepth() {
		return proveWithBindAndApplyDepth;
	}
	
	public static final void setProveWithBindAndApplyDepth(final int proveWithBindAndApplyDepth) {
		Reals.proveWithBindAndApplyDepth = proveWithBindAndApplyDepth;
	}
	
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
		
		for (final Module trustedModule : context.getTrustedModules()) {
			result.addAll(0, findJustificationsIn(trustedModule, goal));
		}
		
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
		
		for (Module context = session.getCurrentModule(); context != null; context = context.getParent()) {
			result.addAll(0, findJustificationsIn(context, goal));
		}
		
		return result;
	}
	
	public static final boolean proveWithBindAndApply(final Expression goal) {
		return proveWithBindAndApply(goal, getProveWithBindAndApplyDepth());
	}
	
	public static final boolean proveWithBindAndApply(final Expression goal, final int depth) {
		return proveWithBindAndApply(session(), goal, depth);
	}
	
	public static final boolean proveWithBindAndApply(final Session session, final Expression goal) {
		return proveWithBindAndApply(session, goal, getProveWithBindAndApplyDepth());
	}
	
	public static final boolean proveWithBindAndApply(final Session session, final Expression goal, final int depth) {
		if (depth <= 0) {
			return false;
		}
		
		final List<Pair<String, Pattern>> justifications = findJustificationsIn(session, goal);
		final int n = justifications.size();
		
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
								s.tryToPop();
								
								continue tryHint;
							}
						}
					}
				}
		} while (!done);
		
		final Expression newFact = lastFactOf(s.getCurrentModule());
		
		session.getCurrentModule().new Claim(newFact, s.getCurrentModule()).execute();
		session.tryToPop();
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
					if (!proveWithBindAndApply(session, bound.getConditions().get(0), getProveWithBindAndApplyDepth())) {
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
	
	public static final void proveEquality(final Expression expression, final RewriteHint... hints) {
		proveEquality((String) null, expression, hints);
	}
	
	public static final void proveEquality(final String factName, final Expression expression, final RewriteHint... hints) {
		proveEquality(session(), factName, expression, hints);
	}
	
	public static final void proveEquality(final Session session, final Expression expression, final RewriteHint... hints) {
		proveEquality(session, null, expression, hints);
	}
	
	public static final void proveEquality(final Session session, final String factName, final Expression expression, final RewriteHint... hints) {
		session.claim(factName == null ? session.newPropositionName() : factName, expression);
		{
			while (session.getCurrentGoal() instanceof Module) {
				session.introduce();
			}
			
			final Composite equality = (Composite) session.getCurrentGoal();
			final Module context = session.getCurrentModule();
			
			canonicalize(session, equality.get(0), hints);
			
			if (context != session.getCurrentModule()) {
				return;
			}
			
			canonicalize(session, equality.get(2), hints);
			
			if (context != session.getCurrentModule()) {
				return;
			}
			
			rewriteRight(session, factName(-2), factName(-1));
		}
	}
	
	/**
	 * @author codistmonk (creation 2014-08-28)
	 */
	public static final class RewriteHint implements Serializable {
		
		private final String propositionName;
		
		private final boolean infinite;
		
		private final Pattern leftPattern;
		
		private final Expression rightTemplate;
		
		public RewriteHint(final String propositionName, final boolean infinite) {
			this(session(), propositionName, infinite);
		}
		
		public RewriteHint(final Session session, final String propositionName, final boolean infinite) {
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
		
		@Override
		public final String toString() {
			return this.getPropositionName();
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = -6577146621435043597L;
		
	}
	
}
