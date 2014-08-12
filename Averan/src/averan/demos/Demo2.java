package averan.demos;

import static averan.io.ExpressionParser.$$;
import static averan.tactics.ExpressionTools.$;
import static averan.tactics.SessionTools.*;
import static java.awt.Color.BLACK;
import static java.awt.Color.WHITE;

import java.io.ByteArrayOutputStream;

import org.scilab.forge.jlatexmath.TeXFormula;

import averan.core.Composite;
import averan.core.Expression;
import averan.core.Module;
import averan.core.Rewriter;
import averan.core.Module.Symbol;
import averan.io.Exporter;
import averan.io.TexPrinter;
import averan.io.TexPrinter.DisplayHint;
import averan.modules.Standard;
import averan.tactics.Session;

/**
 * @author codistmonk (creation 2014-08-08)
 */
public final class Demo2 {
	
	public static final Module MODULE = new Module(Standard.MODULE);
	
	static {
		String sessionBreakPoint = "";
		
		try {
			suppose("definition_of_conjunction",
					$$("∀P,Q (P → (Q → (P ∧ Q)))"));
			suppose("definition_of_proposition_equality",
					$$("∀P,Q ((P=Q) = ((P→Q) ∧ (Q→P)))"));
			suppose("definition_of_negation",
					$$("∀P (¬P = (P→`false))"));
			suppose("definition_of_existence",
					$$("∀P,x (∃x (P x)) = ¬(∀y ¬(P y))"));
			suppose("definition_of_intersection",
					$$("∀A,B,x (x∈A∩B) = (x∈A ∧ x∈B)"));
			suppose("definition_of_summation",
					$$("∀i,a,b,e,s ((s=((Σ_(i=a)^b) e)) → (((b<a) → (s=0)) ∧ ((a≤b) → (s=(s{b=(b-1)})+(e{i=b})))))"));
			suppose("definition_of_matrices",
					$$("∀X,m,n (X∈≀M_(m,n) = (`rowCount_X = m ∧ `columnCount_X = n ∧ ∀i,j (X_(i,j)∈ℝ)))"));
			suppose("definition_of_matrix_size_equality",
					$$("∀X,Y ((`size_X=`size_Y) = ((`columnCount_X = `columnCount_Y) ∧ (`rowCount_X = `rowCount_Y)))"));
			suppose("definition_of_matrix_equality",
					$$("∀X,Y ((X=Y) = (∀i,j ((X)_(i,j)=(Y_(i,j)))))"));
			suppose("definition_of_matrix_scalarization",
					$$("∀X ((X∈≀M_(1,1)) → (⟨X⟩=X_(1,1)))"));
			suppose("definition_of_matrix_addition",
					$$("∀X,Y,i,j (((X+Y)_(i,j)=(X_(i,j))+(Y_(i,j))))"));
			suppose("definition_of_matrix_subtraction",
					$$("∀X,Y,i,j (((X-Y)_(i,j)=(X_(i,j))-(Y_(i,j))))"));
			suppose("definition_of_matrix_multiplication",
					$$("∀X,Y,n (((`columnCount_X = n) ∧ (`rowCount_Y = n)) → (∀i,j,k ((XY)_(i,j)=((Σ_(k=0)^(n-1)) (X_(i,k))(Y_(k,j))))))"));
			suppose("definition_of_transposition",
					$$("∀X (∀i,j (Xᵀ_(i,j)=X_(j,i)))"));
			suppose("definition_of_transposition_rowCount",
					$$("∀X (`rowCount_(Xᵀ)=`columnCount_X)"));
			suppose("definition_of_transposition_columnCount",
					$$("∀X (`columnCount_(Xᵀ)=`rowCount_X)"));
			
			suppose("definition_of_U_rowCount",
					$$("∀n (`rowCount_(U_n)=n)"));
			suppose("definition_of_U_columnCount",
					$$("∀n (`columnCount_(U_n)=1)"));
			suppose("definition_of_U",
					$$("∀n (0<n → (∀i (U_n_(i,1)=1/n)))"));
			
			admit("commutativity_of_multiplication",
					$$("∀x,y ((x∈ℝ) → ((y∈ℝ) → ((xy)=(yx))))"));
			
			claimCommutativityOfConjunction();
			claimTranspositionOfAddition();
			claimTranspositionOfMultiplication();
			
			suppose("definition_of_replicated_means",
					$$("∀X,n ((`columnCount_X=n) → (M_X)=X(U_n)(U_n)ᵀ)"));
			suppose("definition_of_problem_dimension",
					$$("0<D"));
			suppose("definition_of_class_count",
					$$("1<N"));
			suppose("definition_of_class_means",
					$$("∀i,j,n ((n=`columnCount_(C_j)) → (((M_C)_(i,j))=((C_j)(U_n))_(i,1)))"));
			suppose("definition_of_class_rowCount",
					$$("∀i ((`rowCount_(C_i)) = D)"));
			suppose("definition_of_V",
					$$("V = `Var_(M_C)"));
			suppose("definition_of_S",
					$$("∀i (S = (Σ_(i=0)^(N-1) (`Var_(C_i))))"));
			
			// TODO claim
			admit("simplified_definition_of_variance",
					$$("∀X ((`Var_X)=(XXᵀ)-((M_X)(M_X)ᵀ))"));
			admit("simplified_definition_of_objective",
					$$("∀w,i ((J_w)=⟨wᵀVw⟩/⟨wᵀSw⟩)"));
			admit("equation_to_solve_to_optimize_objective",
					$$("∀w (((SwwᵀV)=(VwwᵀS)) → `optimality (J_w))"));
			admit("regularization",
					$$("∀B,ω,w ((w=Bω) → (((SwwᵀV)=(VwwᵀS)) → `constrainedOptimality (J_(Bω))))"));
		} catch (final BreakSessionException exception) {
			sessionBreakPoint = exception.getStackTrace()[1].toString();
		} finally {
			new Exporter(session(), -1).exportSession();
			
			System.out.println(sessionBreakPoint);
		}
		
		{
			final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			
			new Exporter(session(), new TexPrinter(buffer)
				.hint($("optimality"), new DisplayHint(50, "", "\\;", 1))
				.hint($("constrainedOptimality"), new DisplayHint(50, "", "\\;", 1))
			, 0).exportSession();
			
			new TeXFormula(buffer.toString()).createPNG(0, 18F, "view.png", WHITE, BLACK);
		}
	}
	
	public static final void claimTranspositionOfMultiplication() {
		claim("transposition_of_multiplication",
				$$("∀X,Y ((X∈≀M_(`rowCount_X,`columnCount_X)) → ((Y∈≀M_(`rowCount_Y,`columnCount_Y)) → ((`columnCount_X=`rowCount_Y) → ((XY)ᵀ=YᵀXᵀ))))"));
		
		{
			final Symbol x = introduce();
			final Symbol y = introduce();
			
			introduce();
			introduce();
			introduce();
			
			final Expression xt = $(x, "ᵀ");
			final Expression yt = $(y, "ᵀ");
			final Expression xy = $(x, y);
			final Expression xyt = $(xy, "ᵀ");
			final Expression ytxt = $(yt, xt);
			
			bind("definition_of_matrix_equality", xyt, ytxt);
			
			claim(((Composite) fact(-1)).get(2));
			
			{
				final Symbol i = introduce();
				final Symbol j = introduce();
				final Symbol k = parameter("k");
				final Expression rowCountX = $("rowCount", "_", x);
				final Expression columnCountX = $("columnCount", "_", x);
				final Expression rowCountY = $("rowCount", "_", y);
				final Expression columnCountY = $("columnCount", "_", y);
				
				bind(Standard.IDENTITY, k);
				bind("definition_of_transposition", xy, i, j);
				bind("definition_of_matrix_multiplication", x, y, columnCountX);
				claim(((Module) fact(-1)).getConditions().get(0));
				
				{
					bind(Standard.IDENTITY, columnCountX);
					rewrite(factName(-1), "transposition_of_multiplication#2", 0);
				}
				
				apply("transposition_of_multiplication#4#2", factName(-1));
				bind(factName(-1), j, i, k);
				rewrite("transposition_of_multiplication#4#1", factName(-1));
				
				bind("definition_of_matrix_multiplication", yt, xt, $("columnCount", "_", x));
				claim(((Module) fact(-1)).getConditions().get(0));
				
				{
					bind("definition_of_transposition_columnCount", y);
					rewriteRight(factName(-1), "transposition_of_multiplication#2");
					bind("definition_of_transposition_rowCount", x);
				}
				
				apply("transposition_of_multiplication#4#7", factName(-1));
				bind(factName(-1), i, j, k);
				
				bind("definition_of_transposition", x);
				bind(factName(-1), k, j);
				rewrite("transposition_of_multiplication#4#10", factName(-1));
				
				bind("definition_of_transposition", y);
				bind(factName(-1), i, k);
				rewrite("transposition_of_multiplication#4#13", factName(-1));
				
				final Expression xjk = $(x, "_", $(j, ",", k));
				final Expression yki = $(y, "_", $(k, ",", i));
				final Expression xjkyki = $(xjk, yki);
				final Expression ykixjk = $(yki, xjk);
				
				claim($(ykixjk, "=", xjkyki));
				
				{
					claim($(xjk, "∈", "ℝ"));
					
					{
						bind("definition_of_matrices", x, rowCountX, columnCountX);
						rewrite("transposition_of_multiplication#0", factName(-1));
						bind(factName(-1));
						bind(factName(-1), j, k);
					}
					
					claim($(yki, "∈", "ℝ"));
					
					{
						bind("definition_of_matrices", y, rowCountY, columnCountY);
						rewrite("transposition_of_multiplication#1", factName(-1));
						bind(factName(-1));
						bind(factName(-1), k, i);
					}
					
					bind("commutativity_of_multiplication", yki, xjk);
					apply(factName(-1), "transposition_of_multiplication#4#17#1");
					apply(factName(-1), "transposition_of_multiplication#4#17#0");
				}
				
				rewrite("transposition_of_multiplication#4#16", factName(-1));
				rewriteRight("transposition_of_multiplication#4#6", factName(-1));
				
			}
			
			rewriteRight(factName(-1), "transposition_of_multiplication#3");
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
	
}
