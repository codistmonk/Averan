package averan.demos;

import static averan.core.ExpressionTools.*;
import static averan.core.SessionTools.*;
import static averan.io.ExpressionParser.$$;
import static averan.modules.Reals.*;
import static averan.modules.Standard.*;
import static java.awt.Color.BLACK;
import static java.awt.Color.WHITE;
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
import averan.core.Session;
import averan.demos.Demo2.BreakSessionException;
import averan.io.SessionExporter;
import averan.io.TexPrinter;
import averan.modules.Reals;
import averan.modules.Reals.RewriteHint;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.HashMap;
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
	
	public static final Module MODULE = new Module(Reals.MODULE, Demo4.class.getName());
	
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
					final List<Pair<Integer, Pattern>> pairs = s.getFact(-1).accept(new IndexFinder(true, hint.getLeftPattern(s)));
					
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
		
		if (sOldFactCount < s.getCurrentModule().getFacts().size()) {
			final Expression newFact = lastFactOf(s.getCurrentModule());
			
			session.getCurrentModule().new Claim(newFact, s.getCurrentModule()).execute();
			session.tryToPop();
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
	
	private static int proveWithBindAndApplyDepth = 4;
	
	public static final int getProveWithBindAndApplyDepth() {
		return proveWithBindAndApplyDepth;
	}
	
	public static final void setProveWithBindAndApplyDepth(final int proveWithBindAndApplyDepth) {
		Demo4.proveWithBindAndApplyDepth = proveWithBindAndApplyDepth;
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
	
	public static final Map<String, RewriteHint[]> hints = new HashMap<>();
	
	static {
		new SessionScaffold() {
			
			@Override
			public final void run() {
				claim("Demo4.test1", $$("∀x,y (x → ((x→y) → y))"));
				{
					introduce();
					introduce();
					introduce();
					introduce();
					proveWithBindAndApply(goal());
				}
				
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
				
				claim("Demo4.test2", $$("∀x,y,z ((x∈ℝ) → ((y∈ℝ) → ((z∈ℝ) → ((x+z+y)=(z+y+x)))))"));
				{
					introduce();
					introduce();
					introduce();
					introduce();
					introduce();
					introduce();
					
					proveEquality(goal(), additionHints);
				}
				
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
						new RewriteHint("commutativity_of_multiplication", true),
						new RewriteHint("associativity_of_multiplication", false),
						new RewriteHint("ordering_of_factors", true),
				};
				
				hints.put("multiplication", multiplicationHints);
				
				claim("Demo4.test3", $$("∀x,y,z ((x∈ℝ) → ((y∈ℝ) → ((z∈ℝ) → ((xzy)=(zyx)))))"));
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
				
				claim("Demo4.test4", $$("∀a,b,c,d ((a∈ℝ) → ((b∈ℝ) → ((c∈ℝ) → ((d∈ℝ) → ((dc+ba)=(ab+cd))))))"));
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
				
				suppose("definition_of_subtraction", $$("∀x,y ((x∈ℝ) → ((y∈ℝ) → ((x-y)=(x+(-y)))))"));
				suppose("type_of_opposite", $$("∀x ((x∈ℝ) → ((-x)∈ℝ))"));
				suppose("opposite_of_multiplication", $$("∀x,y ((x∈ℝ) → ((y∈ℝ) → (((-x)y)=(-(xy)))))"));
				
				final RewriteHint[] subtractionHints = {
						new RewriteHint("definition_of_subtraction", false),
						new RewriteHint("opposite_of_multiplication", false),
				};
				
				hints.put("subtraction", subtractionHints);
				
				claim("Demo4.test5", $$("∀a,b,c,d ((a∈ℝ) → ((b∈ℝ) → ((c∈ℝ) → ((d∈ℝ) → ((dc+(a-ba))=(cd-ab+a))))))"));
				{
					introduce();
					introduce();
					introduce();
					introduce();
					introduce();
					introduce();
					introduce();
					introduce();
					
					proveEquality(goal(), append(additionAndMultiplicationHints, subtractionHints));
				}
				
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
				
				final RewriteHint[] distributivityHints = {
						new RewriteHint("left_distributivity_of_multiplication_over_addition", false),
						new RewriteHint("right_distributivity_of_multiplication_over_addition", false),
				};
				
				hints.put("distributivity", distributivityHints);
				
				final RewriteHint[] arithmeticHints = append(additionAndMultiplicationHints,
						append(subtractionHints, distributivityHints));
				
				hints.put("arithmetic", arithmeticHints);
				
				claim("Demo4.test6", $$("∀a,b,c,d ((a∈ℝ) → ((b∈ℝ) → ((c∈ℝ) → ((d∈ℝ) → ((c+(a-ba)d)=(c-adb+da))))))"));
				{
					introduce();
					introduce();
					introduce();
					introduce();
					introduce();
					introduce();
					introduce();
					introduce();
					
					proveEquality(goal(), arithmeticHints);
				}
			}
			
			/**
			 * {@value}.
			 */
			private static final long serialVersionUID = -2527396009076173030L;
			
		};
	}
	
//	static boolean debug = false;
	
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
	public static abstract class SessionScaffold implements Serializable {
		
		public SessionScaffold() {
			final Session session = pushSession(new Session(MODULE));
			String sessionBreakPoint = "";
			
			try {
				this.run();
			} catch (final BreakSessionException exception) {
				sessionBreakPoint = exception.getStackTrace()[1].toString();
			} finally {
				popSession();
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
