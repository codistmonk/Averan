package averan.demos;

import static averan.core.ExpressionTools.*;
import static averan.core.StructureMatcher.listsMatch;
import static averan.core.SessionTools.*;
import static averan.io.ExpressionParser.$$;
import static averan.modules.Standard.*;
import static java.awt.Color.BLACK;
import static java.awt.Color.WHITE;
import static java.util.stream.Collectors.toList;
import static net.sourceforge.aprog.tools.Tools.cast;
import averan.core.Composite;
import averan.core.Expression;
import averan.core.Module;
import averan.core.Module.Bind;
import averan.core.Module.Symbol;
import averan.core.Pattern;
import averan.core.Pattern.Any;
import averan.core.Rewriter;
import averan.core.Session;
import averan.core.Visitor;
import averan.io.SessionExporter;
import averan.io.TexPrinter;
import averan.io.TexPrinter.DisplayHint;
import averan.modules.Reals;
import averan.modules.Standard;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.aprog.tools.IllegalInstantiationException;
import net.sourceforge.aprog.tools.Pair;

import org.scilab.forge.jlatexmath.TeXFormula;

/**
 * @author codistmonk (creation 2014-08-08)
 */
public final class Demo2 {
	
	public static final Module MODULE = new Module(Standard.MODULE);
	
	public static final AlgebraicProperty[] REAL_ALGEBRA_RULES = {
			new Noninversion("definition_of_subtraction"),
			new Noninversion("opposite_of_multiplication"),
			new Noninversion("right_distributivity_of_multiplication_over_addition"),
			new Noninversion("associativity_of_addition"),
			new Inversion("ordering_of_terms"),
			new Inversion("commutativity_of_addition"),
			new Noninversion("associativity_of_multiplication"),
			new Inversion("commutativity_of_multiplication"),
			new Inversion("ordering_of_factors"),
			new Noninversion("left_distributivity_of_multiplication_over_addition")
	};
	
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
	
	public static final Pair<String, Pattern> justificationFor(final Session session, final Expression target) {
		Pair<String, Pattern> result = justificationFor(session.getCurrentModule(), target);
		
		if (result != null) {
			return result;
		}
		
		for (final Module module : session.getTrustedModules()) {
			result = justificationFor(module, target);
			
			if (result != null) {
				return result;
			}
		}
		
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
	
	public static final void claimRealEquality(final String factName, final Expression goal) {
		claimRealEquality(session(), factName, goal);
	}
	
	public static final void claimRealEquality(final Session session, final String factName, final Expression goal) {
		session.claim(factName == null ? session.newPropositionName() : factName, goal);
		{
			{
				Expression g = session.getCurrentGoal();
				
				while (g instanceof Module) {
					session.introduce();
					g = session.getCurrentGoal();
				}
			}
			
			final Composite equality = (Composite) session.getCurrentGoal();
			
			canonicalize(equality.get(0), REAL_ALGEBRA_RULES);
			canonicalize(equality.get(2), REAL_ALGEBRA_RULES);
			rewriteRight(factName(-2), factName(-1));
		}
	}
	
	static {
		String sessionBreakPoint = "";
		
		try {
			session().trust(Reals.MODULE);
			
			claimRealEquality("test", $(forAll("a", "b", "c", "d"),
					$(real($("a")), "->", $(real($("b")), "->", $(real($("c")), "->", $(real($("d")), "->",
							$$("(c+(a-(ba))d)=((ad)-(abd)+c)")))))));
			
			suppose("definition_of_conjunction",
					$$("∀P,Q (P → (Q → (P ∧ Q)))"));
			suppose("definition_of_proposition_equality",
					$$("∀P,Q ((P=Q) = ((P→Q) ∧ (Q→P)))"));
			suppose("definition_of_negation",
					$$("∀P (¬P = (P→'false'))"));
			suppose("definition_of_existence",
					$$("∀P,x (∃x (P x)) = ¬(∀y ¬(P y))"));
			suppose("definition_of_intersection",
					$$("∀A,B,x (x∈A∩B) = (x∈A ∧ x∈B)"));
			suppose("definition_of_summation",
					$$("∀i,a,b,e,s ((s=((Σ_(i=a)^b) e)) → (((b<a) → (s=0)) ∧ ((a≤b) → (s=(s{b=(b-1)})+(e{i=b})))))"));
			suppose("definition_of_matrices",
					$$("∀X,m,n (X∈≀M_(m,n) = ('rowCount'_X = m ∧ 'columnCount'_X = n ∧ ∀i,j (X_(i,j)∈ℝ)))"));
			suppose("definition_of_matrix_size_equality",
					$$("∀X,Y (('size'_X='size'_Y) = (('columnCount'_X = 'columnCount'_Y) ∧ ('rowCount'_X = 'rowCount'_Y)))"));
			suppose("definition_of_matrix_equality",
					$$("∀X,Y ((X=Y) = (∀i,j ((X)_(i,j)=(Y_(i,j)))))"));
			suppose("definition_of_matrix_scalarization",
					$$("∀X ((X∈≀M_(1,1)) → (⟨X⟩=X_(1,1)))"));
			suppose("definition_of_matrix_addition",
					$$("∀X,Y,i,j (((X+Y)_(i,j) = (X_(i,j))+(Y_(i,j))))"));
			suppose("definition_of_matrix_addition_rowCount",
					$$("∀X,Y ('rowCount'_(X+Y) = 'rowCount'_X)"));
			suppose("definition_of_matrix_addition_columnCount",
					$$("∀X,Y ('columnCount'_(X+Y) = 'columnCount'_X)"));
			suppose("definition_of_matrix_subtraction",
					$$("∀X,Y,i,j (((X-Y)_(i,j) = (X_(i,j))-(Y_(i,j))))"));
			suppose("definition_of_matrix_subtraction_rowCount",
					$$("∀X,Y ('rowCount'_(X-Y) = 'rowCount'_X)"));
			suppose("definition_of_matrix_subtraction_columnCount",
					$$("∀X,Y ('columnCount'_(X-Y) = 'columnCount'_X)"));
			suppose("definition_of_matrix_multiplication",
					$$("∀X,Y,i,j,k ((XY)_(i,j)=((Σ_(k=0)^(('columnCount'_X)-1)) ((X_(i,k))(Y_(k,j)))))"));
			suppose("definition_of_matrix_multiplication_rowCount",
					$$("∀X,Y ('rowCount'_(XY) = 'rowCount'_X)"));
			suppose("definition_of_matrix_multiplication_columnCount",
					$$("∀X,Y ('columnCount'_(XY) = 'columnCount'_Y)"));
			suppose("definition_of_transposition",
					$$("∀X (∀i,j (Xᵀ_(i,j)=X_(j,i)))"));
			suppose("definition_of_transposition_rowCount",
					$$("∀X ('rowCount'_(Xᵀ)='columnCount'_X)"));
			suppose("definition_of_transposition_columnCount",
					$$("∀X ('columnCount'_(Xᵀ)='rowCount'_X)"));
			
			suppose("definition_of_U_rowCount",
					$$("∀n ('rowCount'_(U_n)=n)"));
			suppose("definition_of_U_columnCount",
					$$("∀n ('columnCount'_(U_n)=1)"));
			suppose("definition_of_U",
					$$("∀n (0<n → (∀i (U_n_(i,1)=1/n)))"));
			
			claimCommutativityOfConjunction();
			claimTranspositionOfAddition();
			claimTranspositionOfSubtraction();
			claimTranspositionOfMultiplication();
			
			suppose("definition_of_replicated_means",
					$$("∀X,n (('columnCount'_X=n) → (M_X)=X(U_n)(U_n)ᵀ)"));
			suppose("definition_of_problem_dimension",
					$$("0<D"));
			suppose("definition_of_class_count",
					$$("1<N"));
			suppose("definition_of_class_means",
					$$("∀i,j,n ((n='columnCount'_(C_j)) → (((M_C)_(i,j))=((C_j)(U_n))_(i,1)))"));
			suppose("definition_of_class_rowCount",
					$$("∀i (('rowCount'_(C_i)) = D)"));
			suppose("definition_of_V",
					$$("V = 'Var'_(M_C)"));
			suppose("definition_of_S",
					$$("∀i (S = (Σ_(i=0)^(N-1) ('Var'_(C_i))))"));
			suppose("definition_of_variance",
					$$("∀X (('Var'_X)=(X-(M_X))(X-(M_X))ᵀ)"));
			
			// TODO claim
			claim("simplified_definition_of_variance",
					$$("∀X (('Var'_X)=(XXᵀ)-((M_X)(M_X)ᵀ))"));
			{
				final Symbol x = introduce();
				final Expression xt = $(x, "ᵀ");
				final Expression mx = $("M", "_", x);
				final Expression mxt = $($("M", "_", x), "ᵀ");
				
				bind("definition_of_variance", x);
				bind("transposition_of_subtraction", x, mx);
				rewrite(factName(-2), factName(-1));
			}
//			admit("simplified_definition_of_variance",
//					$$("∀X (('Var'_X)=(XXᵀ)-((M_X)(M_X)ᵀ))"));
//			admit("simplified_definition_of_objective",
//					$$("∀w,i ((J_w)=⟨wᵀVw⟩/⟨wᵀSw⟩)"));
//			admit("equation_to_solve_to_optimize_objective",
//					$$("∀w (((SwwᵀV)=(VwwᵀS)) → 'optimality' (J_w))"));
//			admit("regularization",
//					$$("∀B,ω,w ((w=Bω) → (((SwwᵀV)=(VwwᵀS)) → 'constrainedOptimality' (J_(Bω))))"));
		} catch (final BreakSessionException exception) {
			sessionBreakPoint = exception.getStackTrace()[1].toString();
		} finally {
			new SessionExporter(session(), -1).exportSession();
			
			System.out.println(sessionBreakPoint);
		}
		
		{
			final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			
			new SessionExporter(session(), new TexPrinter(buffer)
				.hint($("ᵀ"), new DisplayHint(1500, "", "", 0))
				.hint($("optimality"), new DisplayHint(50, "", "\\;", 1))
				.hint($("constrainedOptimality"), new DisplayHint(50, "", "\\;", 1))
			, 0).exportSession();
			
			new TeXFormula(buffer.toString()).createPNG(0, 18F, "view.png", WHITE, BLACK);
		}
	}
	
	public static final void canonicalize(final Expression expression, final AlgebraicProperty... transformationRules) {
		canonicalize(session(), expression, transformationRules);
	}
	
	public static final void canonicalize(final Session session, final Expression expression, final AlgebraicProperty... transformationRules) {
		boolean keepGoing = true;
		final Module module = new Module(session.getCurrentModule());
		final Session s = new Session(module);
		
		s.bind(IDENTITY, expression);
		
		while (keepGoing) {
			keepGoing = false;
			
			for (final AlgebraicProperty transformationRule : transformationRules) {
				final Composite proposition = s.getFact(-1);
				final String propositionName = s.getFactName(-1);
				final List<Pair<Integer, Pattern>> indices = proposition.accept(new IndexFinder(true, transformationRule.newLeftPattern(session)));
				
				for (final Pair<Integer, Pattern> pair : indices) {
					final Integer index = pair.getFirst();
					final Pattern pattern = pair.getSecond();
					
					if (!transformationRule.bindAndApply(s, pattern)) {
						continue;
					}
					
					s.rewrite(propositionName, s.getFactName(-1), index);
					
					final Expression last = s.getFact(-1);
					
					if (transformationRule instanceof Inversion && 0 <= last.toString().compareTo(proposition.toString())) {
						recall(s, propositionName);
					} else {
						keepGoing = true;
						break;
					}
				}
			}
		}
		
		session.getCurrentModule().new Claim(module.getFacts().get(module.getFacts().size() - 1), module).execute();
	}
	
	public static final void claimTranspositionOfMultiplication() {
		claim("transposition_of_multiplication",
				$$("∀X,Y ((X∈≀M_('rowCount'_X,'columnCount'_X)) → ((Y∈≀M_('rowCount'_Y,'columnCount'_Y)) → (('columnCount'_X='rowCount'_Y) → ((XY)ᵀ=YᵀXᵀ))))"));
		{
			final Symbol x = introduce();
			final Symbol y = introduce();
			
			introduce("type_of_X");
			introduce("type_of_Y");
			introduce("size_compatibility");
			
			final Expression xy = $(x, y);
			final Expression xyt = $($(x, y), "ᵀ");
			final Expression xt = $(x, "ᵀ");
			final Expression yt = $(y, "ᵀ");
			final Expression ytxt = $(yt, xt);
			final Expression rowCountX = $("rowCount", "_", x);
			final Expression columnCountX = $("columnCount", "_", x);
			final Expression rowCountY = $("rowCount", "_", y);
			final Expression columnCountY = $("columnCount", "_", y);
			final Expression columnCountYT = $("columnCount", "_", yt);
			
			bind("definition_of_matrix_equality", xyt, ytxt);
			claim(((Composite) fact(-1)).get(2));
			{
				final Symbol i = introduce();
				final Symbol j = introduce();
				final Symbol k = parameter("k");
				
				bind("definition_of_transposition", xy, i, j);
				claim($(((Composite) fact(-1)).get(2), "=", ((Composite) getCurrentGoal()).get(2)));
				{
					bind("definition_of_matrix_multiplication", x, y, j, i, k);
					
					final Expression xjk = $(x, "_", $(j, ",", k));
					final Expression yki = $(y, "_", $(k, ",", i));
					
					claim($($(xjk, yki), "=", $(yki, xjk)));
					{
						claim(real(xjk));
						{
							bind("definition_of_matrices", x, rowCountX, columnCountX);
							rewrite("type_of_X", factName(-1));
							bind(factName(-1));
							bind(factName(-1), j, k);
						}
						
						claim(real(yki));
						{
							bind("definition_of_matrices", y, rowCountY, columnCountY);
							rewrite("type_of_Y", factName(-1));
							bind(factName(-1));
							bind(factName(-1), k, i);
						}
						
						bind("commutativity_of_multiplication", xjk, yki);
						apply(factName(-1), factName(-3));
						apply(factName(-1), factName(-3));
					}
					
					rewrite(factName(-2), factName(-1));
					
					bind("definition_of_transposition", x, k, j);
					rewriteRight(factName(-2), factName(-1));
					
					bind("definition_of_transposition", y, i, k);
					rewriteRight(factName(-2), factName(-1));
					
					bind("definition_of_matrix_multiplication", yt, xt, i, j, k);
					
					claim($(columnCountYT, "=", columnCountX));
					{
						bind("definition_of_transposition_columnCount", y);
						rewriteRight(factName(-1), "size_compatibility");
					}
					rewrite(factName(-2), factName(-1));
					
					rewriteRight(factName(-4), factName(-1));
				}
				
				rewrite(factName(-2), factName(-1));
			}
			
			rewriteRight(factName(-1), factName(-2));
		}
	}
	
	public static final void claimTranspositionOfAddition() {
		claim("transposition_of_addition", $$("∀X,Y ((X+Y)ᵀ=Xᵀ+Yᵀ)"));
		
		{
			final Symbol x = introduce();
			final Symbol y = introduce();
			
			final Expression xt = $(x, "ᵀ");
			final Expression yt = $(y, "ᵀ");
			final Expression xy = $(x, "+", y);
			final Expression xyt = $(xy, "ᵀ");
			final Expression xtyt = $(xt, "+", yt);
			
			bind("definition_of_matrix_equality", xyt, xtyt);
			claim(((Composite) fact(-1)).get(2));
			
			{
				final Symbol i = introduce();
				final Symbol j = introduce();
				
				bind("definition_of_transposition", xy, i, j);
				claim($(((Composite) fact(-1)).get(2), "=", ((Composite) getCurrentGoal()).get(2)));
				
				{
					bind("definition_of_matrix_addition", x, y, j, i);
					bind("definition_of_transposition", x, i, j);
					rewriteRight(factName(-2), factName(-1));
					
					bind("definition_of_transposition", y, i, j);
					rewriteRight(factName(-2), factName(-1));
					
					bind("definition_of_matrix_addition", xt, yt, i, j);
					
					rewriteRight(factName(-2), factName(-1));
				}
				
				rewrite(factName(-2), factName(-1));
			}
			
			rewriteRight(factName(-1), factName(-2));
		}
	}
	
	public static final void claimTranspositionOfSubtraction() {
		claim("transposition_of_subtraction", $$("∀X,Y ((X-Y)ᵀ=Xᵀ-Yᵀ)"));
		
		{
			final Symbol x = introduce();
			final Symbol y = introduce();
			
			final Expression xt = $(x, "ᵀ");
			final Expression yt = $(y, "ᵀ");
			final Expression xy = $(x, "-", y);
			final Expression xyt = $(xy, "ᵀ");
			final Expression xtyt = $(xt, "-", yt);
			
			bind("definition_of_matrix_equality", xyt, xtyt);
			claim(((Composite) fact(-1)).get(2));
			
			{
				final Symbol i = introduce();
				final Symbol j = introduce();
				
				bind("definition_of_transposition", xy, i, j);
				claim($(((Composite) fact(-1)).get(2), "=", ((Composite) getCurrentGoal()).get(2)));
				
				{
					bind("definition_of_matrix_subtraction", x, y, j, i);
					bind("definition_of_transposition", x, i, j);
					rewriteRight(factName(-2), factName(-1));
					
					bind("definition_of_transposition", y, i, j);
					rewriteRight(factName(-2), factName(-1));
					
					bind("definition_of_matrix_subtraction", xt, yt, i, j);
					
					rewriteRight(factName(-2), factName(-1));
				}
				
				rewrite(factName(-2), factName(-1));
			}
			
			rewriteRight(factName(-1), factName(-2));
		}
	}
	
	public static final void claimCommutativityOfConjunction() {
		claim("commutativity_of_conjunction",
				$$("∀P,Q ((P ∧ Q) = (Q ∧ P))"));
		
		{
			final Symbol p = introduce();
			final Symbol q = introduce();
			final Expression pq = $(p, "&", q);
			final Expression qp = $(q, "&", p);
			final Expression pq2qp = $(pq, "->", qp);
			final Expression qp2pq = $(qp, "->", pq);
			
			bind("definition_of_proposition_equality", pq, qp);
			
			claim(pq2qp);
			{
				introduce();
				bind("commutativity_of_conjunction#1#0");
			}
			
			claim(qp2pq);
			
			{
				introduce();
				bind("commutativity_of_conjunction#2#0");
			}
			
			claim($(pq2qp, "&", qp2pq));
			
			{
				recall("commutativity_of_conjunction#1");
				recall("commutativity_of_conjunction#2");
			}
			
			rewriteRight("commutativity_of_conjunction#3", "commutativity_of_conjunction#0");
		}
	}
	
	/**
	 * @param commandLineArguments
	 * <br>Unused
	 */
	public static final void main(final String[] commandLineArguments) {
		// NOP
	}
	
	/**
	 * @author codistmonk (creation 2014-08-11)
	 */
	public static final class BreakSessionException extends RuntimeException {
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = -6958775377557504848L;
		
		public static final void breakSession() {
			throw new BreakSessionException();
		}
		
	}
	
	public static final Expression real(final Expression expression) {
		return $(expression, "∈", "ℝ");
	}
	
	/**
	 * @author codistmonk (creation 2014-08-22)
	 */
	public static final class IndexFinder implements Visitor<List<Pair<Integer, Pattern>>> {
		
		private final Pattern pattern;
		
		private final boolean waitForTopLevelRHS;
		
		private int subindex;
		
		private int level;
		
		private boolean active;
		
		private final List<Pair<Integer, Pattern>> result;
		
		private final Map<Object, Integer> indices;
		
		public IndexFinder(final Pattern pattern) {
			this(false, pattern);
		}
		
		public IndexFinder(final boolean waitForTopLevelRHS, final Pattern pattern) {
			this.pattern = pattern;
			this.waitForTopLevelRHS = waitForTopLevelRHS;
			this.subindex = -1;
			this.level = -1;
			this.active = !this.waitForTopLevelRHS;
			this.result = new ArrayList<>();
			this.indices = new HashMap<>();
		}
		
		public final Pattern getPattern() {
			return this.pattern;
		}
		
		@Override
		public final List<Pair<Integer, Pattern>> visit(final Any any) {
			return this.beginVisit(any).endVisit(any);
		}
		
		@Override
		public final List<Pair<Integer, Pattern>> visit(final Composite composite) {
			return this.beginVisit(composite)
					.findIndicesIn(composite.getChildren())
					.endVisit(composite);
		}
		
		@Override
		public final List<Pair<Integer, Pattern>> visit(final Symbol symbol) {
			return this.beginVisit(symbol).endVisit(symbol);
		}
		
		@Override
		public final List<Pair<Integer, Pattern>> visit(final Module module) {
			return this.beginVisit(module)
					.findIndicesIn(module.getParameters())
					.findIndicesIn(module.getConditions())
					.findIndicesIn(module.getFacts())
					.endVisit(module);
		}
		
		private final IndexFinder beginVisit(final Expression expression) {
			++this.level;
			
			this.computeResult(this.getPattern().equals(expression));
			
			return this;
		}
		
		private final List<Pair<Integer, Pattern>> endVisit(final Expression expression) {
			if (this.level == 1 && this.subindex == 1 && Module.EQUAL.equals(expression)) {
				this.active = true;
			}
			
			--this.level;
			
			return this.result;
		}
		
		private final boolean isActive() {
			return this.active;
		}
		
		private final List<Pair<Integer, Pattern>> computeResult(final boolean match) {
			if (match) {
				final Pattern patternCopy = this.getPattern().copy();
				final Integer patternIndex = this.indices.compute(patternCopy.getBindings(),
						(k, v) -> v == null ? 0 : v + 1);
				
				if (this.isActive()) {
					this.result.add(new Pair<>(patternIndex, patternCopy));
				}
			}
			
			return this.result;
		}
		
		private final IndexFinder findIndicesIn(final List<? extends Expression> list) {
			final int n = list.size();
			
			for (int i = 0; i < n; ++i) {
				this.subindex = i;
				list.get(i).accept(this);
			}
			
			this.subindex = -1;
			
			return this;
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = -6666401837567106389L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-08-22)
	 */
	public static final class AlgebraicCanonicalizer implements Visitor<Expression> {
		
		private final Session session;
		
		private final Collection<AlgebraicProperty> transformationRules;
		
		public AlgebraicCanonicalizer(Session session) {
			this.session = session;
			this.transformationRules = new ArrayList<>();
		}

		public final AlgebraicCanonicalizer addRules(final AlgebraicProperty... transformationRules) {
			for (final AlgebraicProperty transformationRule : transformationRules) {
				this.transformationRules.add(transformationRule);
			}
			
			return this;
		}
		
		@Override
		public final Expression visit(final Any any) {
			throw new IllegalArgumentException();
		}
		
		@Override
		public final Expression visit(final Composite composite) {
			final Expression compositeVisit = this.tryToReplace(composite);
			
			if (compositeVisit != composite) {
				return compositeVisit;
			}
			
			final List<Expression> childVisits = composite.childrenAcceptor(this).get();
			
			if (listsMatch(composite.getChildren(), childVisits)) {
				return composite;
			}
			
			return new Composite(childVisits);
		}
		
		@Override
		public final Expression visit(final Symbol symbol) {
			return this.tryToReplace(symbol);
		}
		
		@Override
		public final Expression visit(final Module module) {
			final Expression moduleVisit = this.tryToReplace(module);
			
			if (moduleVisit != module) {
				return moduleVisit;
			}
			
			@SuppressWarnings("unchecked")
			final List<Symbol> parameterVisits = (List) module.parametersAcceptor(this).get()
					.stream().filter(e -> module.getParameters().contains(e)).collect(toList());
			final List<Expression> conditionVisits = module.conditionsAcceptor(this).get();
			final List<Expression> factVisits = module.factsAcceptor(this).get();
			
			if (listsMatch(module.getParameters(), parameterVisits)
					&& listsMatch(module.getConditions(), conditionVisits)
					&& listsMatch(module.getFacts(), factVisits)) {
				return module;
			}
			
			return new Module(module.getParent(), module.getName(),
					parameterVisits, conditionVisits, factVisits);
		}
		
		private final Expression tryToReplace(final Expression expression) {
			for (final AlgebraicProperty transformationRule : this.transformationRules) {
				final Pattern pattern = transformationRule.newPattern(this.session);
				final Composite equality = (Composite) pattern.getTemplate();
				
				if (equality.get(0).equals(expression)) {
					final Expression result = pattern.express(equality.get(2));
					
					if (!(transformationRule instanceof Inversion)
							|| result.toString().compareTo(expression.toString()) < 0) {
						return result;
					}
				}
			}
			
			return expression;
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = -4871598360704641832L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-08-22)
	 */
	public static abstract class AlgebraicProperty implements Serializable {
		
		private final String justification;
		
		protected AlgebraicProperty(final String justification) {
			this.justification = justification;
		}
		
		public final String getJustification() {
			return this.justification;
		}
		
		public final boolean bindAndApply(final Session session, final Pattern pattern) {
			final Session s = new Session(new Module(session.getCurrentModule(), session.newPropositionName()));
			
			Module module = cast(Module.class, s.getProposition(this.getJustification()));
			
			if (module != null) {
				final Bind bind = s.getCurrentModule().new Bind(s.getCurrentModule(), this.getJustification());
				
				for (Map.Entry<Any.Key, Expression> binding : pattern.getBindings().entrySet()) {
					bind.bind(binding.getKey().getName().toString(), binding.getValue());
				}
				
				bind.execute();
				
				module = cast(Module.class, s.getFact(-1));
				
				while (module != null && !module.getConditions().isEmpty()) {
					final Expression condition = module.getConditions().get(0);
					final int oldFactCount = s.getCurrentModule().getFacts().size();
					proveUsingBindAndApply(s, condition);
					final int newFactCount = s.getCurrentModule().getFacts().size();
					
					if (oldFactCount == newFactCount) {
						return false;
					}
					
					s.apply(s.getFactName(-2), s.getFactName(-1));
					module = cast(Module.class, s.getFact(-1));
				}
			}
			
			session.getCurrentModule().new Claim(session.newPropositionName(), s.getFact(-1), s.getCurrentModule()).execute();
			
			return true;
		}
		
		public final Pattern newPattern(final Session session) {
			final Expression proposition = session.getProposition(this.getJustification());
			
			if (Module.isEquality(proposition)) {
				return new Pattern(proposition);
			}
			
			if (proposition instanceof Module) {
				final Module canonicalModule = ((Module) proposition).canonical();
				final List<Expression> facts = canonicalModule.getFacts();
				
				if (facts.isEmpty()) {
					throw new IllegalArgumentException();
				}
				
				final Expression lastFact = facts.get(facts.size() - 1);
				
				if (!Module.isEquality(lastFact)) {
					throw new IllegalArgumentException();
				}
				
				return this.anyfy(canonicalModule, lastFact);
			}
			
			throw new IllegalArgumentException();
		}
		
		private final Pattern anyfy(final Module context, final Expression expression) {
			final Rewriter rewriter = new Rewriter();
			
			for (final Symbol parameter : context.getParameters()) {
				rewriter.rewrite(parameter, Pattern.any(parameter.toString()));
			}
			
			return new Pattern(expression.accept(rewriter));
		}
		
		public final Pattern newLeftPattern(final Session session) {
			final Expression proposition = session.getProposition(this.getJustification());
			
			if (Module.isEquality(proposition)) {
				return new Pattern(((Composite) proposition).getChildren().get(0));
			}
			
			if (proposition instanceof Module) {
				final Module canonicalModule = ((Module) proposition).canonical();
				final List<Expression> facts = canonicalModule.getFacts();
				
				if (facts.isEmpty()) {
					throw new IllegalArgumentException();
				}
				
				final Composite lastFact = (Composite) facts.get(facts.size() - 1);
				
				if (!Module.isEquality(lastFact)) {
					throw new IllegalArgumentException();
				}
				
				final Rewriter rewriter = new Rewriter();
				
				for (final Symbol parameter : canonicalModule.getParameters()) {
					rewriter.rewrite(parameter, Pattern.any(parameter.toString()));
				}
				
				return new Pattern(lastFact.getChildren().get(0).accept(rewriter));
			}
			
			throw new IllegalArgumentException();
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = 1878172301362441979L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-08-22)
	 */
	public static final class Inversion extends AlgebraicProperty {
		
		public Inversion(final String justification) {
			super(justification);
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = -3885973523639244860L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-08-22)
	 */
	public static final class Noninversion extends AlgebraicProperty {
		
		public Noninversion(final String justification) {
			super(justification);
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = 3917509705666449092L;
		
	}
	
}
