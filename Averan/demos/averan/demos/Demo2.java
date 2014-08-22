package averan.demos;

import static averan.core.ExpressionTools.$;
import static averan.core.SessionTools.*;
import static averan.io.ExpressionParser.$$;
import static averan.modules.Standard.*;
import static java.awt.Color.BLACK;
import static java.awt.Color.WHITE;
import static java.util.stream.Collectors.toList;
import averan.core.Composite;
import averan.core.Expression;
import averan.core.Module;
import averan.core.Module.Symbol;
import averan.core.Pattern;
import averan.core.Session;
import averan.core.Visitor;
import averan.io.SessionExporter;
import averan.io.TexPrinter;
import averan.io.TexPrinter.DisplayHint;
import averan.modules.Standard;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import net.sourceforge.aprog.tools.Tools;

import org.scilab.forge.jlatexmath.TeXFormula;

/**
 * @author codistmonk (creation 2014-08-08)
 */
public final class Demo2 {
	
	public static final Module MODULE = new Module(Standard.MODULE);
	
	static {
		String sessionBreakPoint = "";
		
		try {
			{
				admit("associativity_of_addition",
						$$("∀x,y,z ((x+(y+z))=((x+y)+z))"));
				admit("associativity_of_multiplication",
						$$("∀x,y,z ((x(yz))=((xy)z))"));
				admit("right_distributivity_of_multiplication_over_addition",
						$$("∀a,b,c ((a∈ℝ) → ((b∈ℝ) → ((c∈ℝ) → (((a+b)c)=((ac)+(bc))))))"));
				admit("left_distributivity_of_multiplication_over_addition",
						$$("∀a,b,c ((a∈ℝ) → ((b∈ℝ) → ((c∈ℝ) → ((a(b+c))=((ab)+(ac))))))"));
				admit("right_distributivity_of_multiplication_over_subtraction",
						$$("∀a,b,c ((a∈ℝ) → ((b∈ℝ) → ((c∈ℝ) → (((a-b)c)=((ac)-(bc))))))"));
				admit("left_distributivity_of_multiplication_over_subtraction",
						$$("∀a,b,c ((a∈ℝ) → ((b∈ℝ) → ((c∈ℝ) → ((a(b-c))=((ab)-(ac))))))"));
				admit("commutativity_of_multiplication",
						$$("∀x,y ((x∈ℝ) → ((y∈ℝ) → ((xy)=(yx))))"));
				admit("commutativity_of_addition",
						$$("∀x,y ((x∈ℝ) → ((y∈ℝ) → ((x+y)=(y+x))))"));
				
//				canonicalize(session(), $$("c+(a+(ba))d"),
				canonicalize(session(), $$("(c+a)+b"),
						new Inversion("associativity_of_addition"),
						new Inversion("commutativity_of_addition")
//						new Inversion("commutativity_of_multiplication")
				);
				
				BreakSessionException.breakSession();
			}
			
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
					$$("∀X,Y,i,j,k ((XY)_(i,j)=((Σ_(k=0)^(('columnCount'_X)-1)) (X_(i,k))(Y_(k,j))))"));
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
			suppose("definition_of_subtraction",
					$$("∀x,y ((x∈ℝ) → ((y∈ℝ) → ((x-y)=(x+('-'y)))))"));
			admit("commutativity_of_multiplication",
					$$("∀x,y ((x∈ℝ) → ((y∈ℝ) → ((xy)=(yx))))"));
			admit("type_of_multiplication",
					$$("∀x,y ((x∈ℝ) → ((y∈ℝ) → ((xy)∈ℝ)))"));
			admit("commutativity_of_addition",
					$$("∀x,y ((x∈ℝ) → ((y∈ℝ) → ((x+y)=(y+x))))"));
			admit("type_of_addition",
					$$("∀x,y ((x∈ℝ) → ((y∈ℝ) → ((x+y)∈ℝ)))"));
			admit("associativity_of_addition",
					$$("∀x,y,z ((x+(y+z))=((x+y)+z))"));
			admit("associativity_of_multiplication",
					$$("∀x,y,z ((x(yz))=((xy)z))"));
			admit("right_distributivity_of_multiplication_over_addition",
					$$("∀a,b,c ((a∈ℝ) → ((b∈ℝ) → ((c∈ℝ) → (((a+b)c)=((ac)+(bc))))))"));
			admit("left_distributivity_of_multiplication_over_addition",
					$$("∀a,b,c ((a∈ℝ) → ((b∈ℝ) → ((c∈ℝ) → ((a(b+c))=((ab)+(ac))))))"));
			admit("right_distributivity_of_multiplication_over_subtraction",
					$$("∀a,b,c ((a∈ℝ) → ((b∈ℝ) → ((c∈ℝ) → (((a-b)c)=((ac)-(bc))))))"));
			admit("left_distributivity_of_multiplication_over_subtraction",
					$$("∀a,b,c ((a∈ℝ) → ((b∈ℝ) → ((c∈ℝ) → ((a(b-c))=((ab)-(ac))))))"));
			
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
	
	public static final void canonicalize(final Session session, final Expression expression, final AlgebraicProperty... transformationRules) {
		final AlgebraicCanonicalizer canonicalizer = new AlgebraicCanonicalizer(session).addRules(transformationRules);
		Expression oldExpression = expression;
		Expression newExpression = expression.accept(canonicalizer);
		
		while (oldExpression != newExpression) {
			Tools.debugPrint(oldExpression, newExpression);
			
			oldExpression = newExpression;
			newExpression = newExpression.accept(canonicalizer);
		}
		
		Tools.debugPrint(oldExpression, newExpression);
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
		public final Expression visit(final Composite composite) {
			final Expression compositeVisit = this.tryToReplace(composite);
			
			if (compositeVisit != composite) {
				return compositeVisit;
			}
			
			final List<Expression> childVisits = composite.childrenAcceptor(this).get();
			
			if (childVisits.equals(composite.getChildren())) {
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
			
			if (module.getParameters().equals(parameterVisits)
					&& module.getConditions().equals(conditionVisits)
					&& module.getFacts().equals(factVisits)) {
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
		
		public final Pattern newPattern(final Session session) {
			final Expression proposition = session.getProposition(this.getJustification());
			
			if (Module.isEquality(proposition)) {
				return Pattern.anyfy(proposition);
			}
			
			if (proposition instanceof Module) {
				final List<Expression> facts = ((Module) proposition).canonical().getFacts();
				
				if (facts.isEmpty()) {
					throw new IllegalArgumentException();
				}
				
				final Expression lastFact = facts.get(facts.size() - 1);
				
				if (!Module.isEquality(lastFact)) {
					throw new IllegalArgumentException();
				}
				
				return Pattern.anyfy(lastFact);
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
	
}
