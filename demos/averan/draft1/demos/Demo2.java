package averan.draft1.demos;

import static averan.draft1.core.ExpressionTools.*;
import static averan.draft1.core.SessionTools.*;
import static averan.draft1.io.ExpressionParser.$$;
import static averan.draft1.modules.Reals.real;
import static averan.draft1.modules.Standard.*;
import averan.draft1.core.Composite;
import averan.draft1.core.Expression;
import averan.draft1.core.Module;
import averan.draft1.core.Session;
import averan.draft1.core.Module.Symbol;
import averan.draft1.io.SessionExporter;
import averan.draft1.io.SessionScaffold;
import averan.draft1.modules.RealMatrices;
import averan.draft1.modules.Reals;
import averan.draft1.modules.Standard;
import averan.draft1.modules.Reals.RewriteHint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import multij.tools.Tools;

/**
 * @author codistmonk (creation 2014-08-08)
 */
public final class Demo2 {
	
	public static final Module MODULE = new Module(Standard.MODULE);
	
	static {
		new SessionScaffold(MODULE) {
			
			@Override
			public final void buildSession() {
				trust(RealMatrices.MODULE);
				
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
				suppose("definition_of_matrix_size_equality",
						$$("∀X,Y (('size'_X='size'_Y) = (('columnCount'_X = 'columnCount'_Y) ∧ ('rowCount'_X = 'rowCount'_Y)))"));
				suppose("definition_of_matrix_equality_2",
						$$("∀X,Y ((X∈≀M) → ((Y∈≀M) → ((X=Y) = (∀i,j ((X)_(i,j)=(Y_(i,j)))))))"));
				
//				claimMatrixFactMirroringRealFact("associativity_of_matrix_addition", "associativity_of_addition");
				
//				Session.breakSession();
				
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
						$$("∀X,n (('columnCount'_X=n) → (M_X)=n X(U_n)(U_n)ᵀ)"));
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
//				admit("simplified_definition_of_variance",
//						$$("∀X (('Var'_X)=(XXᵀ)-((M_X)(M_X)ᵀ))"));
//				admit("simplified_definition_of_objective",
//						$$("∀w,i ((J_w)=⟨wᵀVw⟩/⟨wᵀSw⟩)"));
//				admit("equation_to_solve_to_optimize_objective",
//						$$("∀w (((SwwᵀV)=(VwwᵀS)) → 'optimality' (J_w))"));
//				admit("regularization",
//						$$("∀B,ω,w ((w=Bω) → (((SwwᵀV)=(VwwᵀS)) → 'constrainedOptimality' (J_(Bω))))"));
			}
			
			/**
			 * {@value}.
			 */
			private static final long serialVersionUID = 5581129338624729719L;
			
		};
		
//		{
//			final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
//			
//			new SessionExporter(session, new TexPrinter(buffer)
//				.hint($("ᵀ"), new DisplayHint(1500, "", "", 0))
//				.hint($("optimality"), new DisplayHint(50, "", "\\;", 1))
//				.hint($("constrainedOptimality"), new DisplayHint(50, "", "\\;", 1))
//			, 0).exportSession();
//			
//			new TeXFormula(buffer.toString()).createPNG(0, 18F, "view.png", WHITE, BLACK);
//		}
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
				bind(conditionName(-1));
			}
			
			claim(qp2pq);
			{
				introduce();
				bind(conditionName(-1));
			}
			
			claim($(pq2qp, "&", qp2pq));
			{
				recall(factName(-2));
				recall(factName(-2));
			}
			
			rewriteRight(factName(-1), factName(-4));
		}
	}
	
	/**
	 * @param commandLineArguments
	 * <br>Unused
	 */
	public static final void main(final String[] commandLineArguments) {
		// NOP
	}
	
}
