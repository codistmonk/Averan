package jrewrite3.demo;

import static java.awt.Color.BLACK;
import static java.awt.Color.WHITE;
import static jrewrite3.core.ExpressionTools.$;
import static jrewrite3.demo.ExpressionParser.$$;

import java.io.ByteArrayOutputStream;

import org.scilab.forge.jlatexmath.TeXFormula;

import jrewrite3.core.Composite;
import jrewrite3.core.Expression;
import jrewrite3.core.Module;
import jrewrite3.core.Module.Symbol;
import jrewrite3.core.Rewriter;
import jrewrite3.core.Session;
import jrewrite3.modules.Standard;

import net.sourceforge.aprog.tools.IllegalInstantiationException;

/**
 * @author codistmonk (creation 2014-08-08)
 */
public final class Demo2 {
	
	private Demo2() {
		throw new IllegalInstantiationException();
	}
	
	public static final Module MODULE = new Module(Standard.MODULE);
	
	static {
		final Session session = new Session(MODULE);
		
		block:
		try {
			session.suppose("definition_of_conjunction",
					$$("∀P,Q (P → (Q → (P ∧ Q)))"));
			session.suppose("definition_of_proposition_equality",
					$$("∀P,Q ((P=Q) = ((P→Q) ∧ (Q→P)))"));
			session.suppose("definition_of_negation",
					$$("∀P (¬P = (P→`false))"));
			session.suppose("definition_of_existence",
					$$("∀P,x (∃x (P x)) = ¬(∀y ¬(P y))"));
			session.suppose("definition_of_intersection",
					$$("∀A,B,x (x∈A∩B) = (x∈A ∧ x∈B)"));
			session.suppose("definition_of_summation",
					$$("∀i,a,b,e,s ((s=((Σ_(i=a)^b) e)) → (((b<a) → (s=0)) ∧ ((a≤b) → (s=(s{b=(b-1)})+(e{i=b})))))"));
			session.suppose("definition_of_matrices",
					$$("∀X,m,n (X∈≀M_(m,n) = (`rowCount_X = m ∧ `columnCount_X = n ∧ ∀i,j (X_(i,j)∈ℝ)))"));
			session.suppose("definition_of_matrix_size_equality",
					$$("∀X,Y ((`size_X=`size_Y) = ((`columnCount_X = `columnCount_Y) ∧ (`rowCount_X = `rowCount_Y)))"));
			session.suppose("definition_of_matrix_equality",
					$$("∀X,Y ((X=Y) = (∀i,j ((X)_(i,j)=(Y_(i,j)))))"));
			session.suppose("definition_of_matrix_addition",
					$$("∀X,Y ((`size_X=`size_Y) → (∀i,j ((X+Y)_(i,j)=(X_(i,j))+(Y_(i,j)))))"));
			session.suppose("definition_of_matrix_subtraction",
					$$("∀X,Y ((`size_X=`size_Y) → (∀i,j ((X-Y)_(i,j)=(X_(i,j))-(Y_(i,j)))))"));
			session.suppose("definition_of_matrix_multiplication",
					$$("∀X,Y,n (((`columnCount_X = n) ∧ (`rowCount_Y = n)) → (∀i,j,k ((XY)_(i,j)=((Σ_(k=0)^(n-1)) (X_(i,k))(Y_(k,j))))))"));
			session.suppose("definition_of_transposition",
					$$("∀X (∀i,j (Xᵀ_(i,j)=X_(j,i)))"));
			session.suppose("definition_of_transposition_rowCount",
					$$("∀X (`rowCount_(Xᵀ)=`columnCount_X)"));
			session.suppose("definition_of_transposition_columnCount",
					$$("∀X (`columnCount_(Xᵀ)=`rowCount_X)"));
			
			session.suppose("definition_of_U_rowCount",
					$$("∀n (`rowCount_(U_n)=n)"));
			session.suppose("definition_of_U_columnCount",
					$$("∀n (`columnCount_(U_n)=1)"));
			session.suppose("definition_of_U",
					$$("∀n (0<n → (∀i (U_n_(i,1)=1/n)))"));
			
			session.admit("commutativity_of_multiplication",
					$$("∀x,y ((x∈ℝ) → ((y∈ℝ) → ((xy)=(yx))))"));
			
			claimCommutativityOfConjunction(session);
			claimTranspositionOfAddition(session);
			claimTranspositionOfMultiplication(session);
			
			session.suppose("definition_of_replicated_means",
					$$("∀X,n ((`columnCount_X=n) → (M_X)=X(U_n)(U_n)ᵀ)"));
			session.suppose("definition_of_problem_dimension",
					$$("0<D"));
			session.suppose("definition_of_class_count",
					$$("1<N"));
			session.suppose("definition_of_class_means",
					$$("∀i,j,n ((n=`columnCount_(C_j)) → (((M_C)_(i,j))=((C_j)(U_n))_(i,1)))"));
			session.suppose("definition_of_class_rowCount",
					$$("∀i ((`rowCount_(C_i)) = D)"));
			session.suppose("definition_of_V",
					$$("V = `Var_(M_C)"));
			session.suppose("definition_of_S",
					$$("∀i (S = (Σ_(i=0)^(N-1) (`Var_(C_i))))"));
			
			// TODO claim
			session.admit("simplified_definition_of_variance",
					$$("∀X ((`Var_X)=(XXᵀ)-((M_X)(M_X)ᵀ))"));
			session.admit("simplified_definition_of_objective",
					$$("∀w,i ((J_w)=⟨wᵀVw⟩/⟨wᵀSw⟩)"));
			session.admit("equation_to_solve_to_optimize_objective",
					$$("∀w (((SwwᵀV)=(VwwᵀS)) → `optimality_(J_w))"));
			session.admit("regularization",
					$$("∀B,ω,w ((w=Bω) → (((SwwᵀV)=(VwwᵀS)) → `constrainedOptimality_(J_(Bω))))"));
		} finally {
			session.new Exporter(-1).exportSession();
		}
		
		{
			final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			
			session.new Exporter(new TexPrinter(buffer), 0).exportSession();
			
			new TeXFormula(buffer.toString()).createPNG(0, 18F, "view.png", WHITE, BLACK);
		}
	}
	
	public static final void claimTranspositionOfMultiplication(final Session session) {
		session.claim("transposition_of_multiplication",
				$$("∀X,Y ((X∈≀M_(`rowCount_X,`columnCount_X)) → ((Y∈≀M_(`rowCount_Y,`columnCount_Y)) → ((`columnCount_X=`rowCount_Y) → ((XY)ᵀ=YᵀXᵀ))))"));
		
		{
			session.introduce();
			session.introduce();
			session.introduce();
			session.introduce();
			session.introduce();
			
			final Symbol x = session.getParameter("X");
			final Symbol y = session.getParameter("Y");
			final Expression xt = $(x, "ᵀ");
			final Expression yt = $(y, "ᵀ");
			final Expression xy = $(x, y);
			final Expression xyt = $(xy, "ᵀ");
			final Expression ytxt = $(yt, xt);
			
			session.bind("definition_of_matrix_equality", xyt, ytxt);
			
			session.claim(((Composite) session.getLastFact()).get(2));
			
			{
				session.introduce();
				session.introduce();
				
				final Symbol i = session.getParameter("i");
				final Symbol j = session.getParameter("j");
				final Symbol k = session.getCurrentModule().new Symbol("k");
				final Expression rowCountX = $("rowCount", "_", x);
				final Expression columnCountX = $("columnCount", "_", x);
				final Expression rowCountY = $("rowCount", "_", y);
				final Expression columnCountY = $("columnCount", "_", y);
				
				session.bind(Standard.IDENTITY, k);
				session.bind("definition_of_transposition", xy, i, j);
				session.bind("definition_of_matrix_multiplication", x, y, columnCountX);
				session.claim(((Module) session.getLastFact()).getConditions().get(0));
				
				{
					session.bind(Standard.IDENTITY, columnCountX);
					session.rewrite(session.getLastFactName(), "transposition_of_multiplication#2", 0);
				}
				
				session.apply("transposition_of_multiplication#4#2", session.getLastFactName());
				session.bind(session.getLastFactName(), j, i, k);
				session.rewrite("transposition_of_multiplication#4#1", session.getLastFactName());
				
				session.bind("definition_of_matrix_multiplication", yt, xt, $("columnCount", "_", x));
				session.claim(((Module) session.getLastFact()).getConditions().get(0));
				
				{
					session.bind("definition_of_transposition_columnCount", y);
					rewriteRight(session, session.getLastFactName(), "transposition_of_multiplication#2");
					session.bind("definition_of_transposition_rowCount", x);
				}
				
				session.apply("transposition_of_multiplication#4#7", session.getLastFactName());
				session.bind(session.getLastFactName(), i, j, k);
				
				session.bind("definition_of_transposition", x);
				session.bind(session.getLastFactName(), k, j);
				session.rewrite("transposition_of_multiplication#4#10", session.getLastFactName());
				
				session.bind("definition_of_transposition", y);
				session.bind(session.getLastFactName(), i, k);
				session.rewrite("transposition_of_multiplication#4#13", session.getLastFactName());
				
				final Expression xjk = $(x, "_", $(j, ",", k));
				final Expression yki = $(y, "_", $(k, ",", i));
				final Expression xjkyki = $(xjk, yki);
				final Expression ykixjk = $(yki, xjk);
				
				session.claim($(ykixjk, "=", xjkyki));
				
				{
					session.claim($(xjk, "∈", "ℝ"));
					
					{
						session.bind("definition_of_matrices", x, rowCountX, columnCountX);
						session.rewrite("transposition_of_multiplication#0", session.getLastFactName());
						session.bind(session.getLastFactName());
						session.bind(session.getLastFactName(), j, k);
					}
					
					session.claim($(yki, "∈", "ℝ"));
					
					{
						session.bind("definition_of_matrices", y, rowCountY, columnCountY);
						session.rewrite("transposition_of_multiplication#1", session.getLastFactName());
						session.bind(session.getLastFactName());
						session.bind(session.getLastFactName(), k, i);
					}
					
					session.bind("commutativity_of_multiplication", yki, xjk);
					session.apply(session.getLastFactName(), "transposition_of_multiplication#4#17#1");
					session.apply(session.getLastFactName(), "transposition_of_multiplication#4#17#0");
				}
				
				session.rewrite("transposition_of_multiplication#4#16", session.getLastFactName());
				rewriteRight(session, "transposition_of_multiplication#4#6", session.getLastFactName());
				
			}
			
			rewriteRight(session, session.getLastFactName(), "transposition_of_multiplication#3");
		}
	}
	
	public static final void claimTranspositionOfAddition(final Session session) {
		session.claim("transposition_of_addition", $$("∀X,Y ((`size_X=`size_Y) → ((X+Y)ᵀ=Xᵀ+Yᵀ))"));
		
		{
			session.introduce();
			session.introduce();
			session.introduce();
			
			final Symbol x = session.getParameter("X");
			final Symbol y = session.getParameter("Y");
			final Expression xt = $(x, "ᵀ");
			final Expression yt = $(y, "ᵀ");
			
			session.bind("definition_of_transposition", (Expression) $(x, "+", y));
			session.bind("definition_of_matrix_addition", x, y);
			session.apply("transposition_of_addition#2", "transposition_of_addition#0");
			
			{
				final Module m = (Module) session.getCurrentContext().getModule().getProposition("transposition_of_addition#1");
				final Symbol i = m.getParameter("i");
				final Symbol j = m.getParameter("j");
				
				session.bind("transposition_of_addition#3", j, i);
				
				{
					session.bind("definition_of_transposition", x, i, j);
					rewriteRight(session, "transposition_of_addition#4", "transposition_of_addition#5");
				}
				
				{
					session.bind("definition_of_transposition", y, i, j);
					rewriteRight(session, "transposition_of_addition#6", "transposition_of_addition#7");
				}
				
				session.rewrite("transposition_of_addition#1", "transposition_of_addition#8");
			}
			
			session.bind("definition_of_matrix_addition", xt, yt);
			
			session.claim($($("size", "_", xt), "=", $("size", "_", yt)));
			
			{
				session.bind("definition_of_matrix_size_equality", xt, yt);
				session.bind("definition_of_matrix_size_equality", x, y);
				session.rewrite("transposition_of_addition#0", "transposition_of_addition#11#1");
				session.bind("definition_of_transposition_rowCount", x);
				session.bind("definition_of_transposition_columnCount", x);
				session.bind("definition_of_transposition_rowCount", y);
				session.bind("definition_of_transposition_columnCount", y);
				rewriteRight(session, "transposition_of_addition#11#2", "transposition_of_addition#11#3");
				rewriteRight(session, "transposition_of_addition#11#7", "transposition_of_addition#11#4");
				rewriteRight(session, "transposition_of_addition#11#8", "transposition_of_addition#11#5");
				rewriteRight(session, "transposition_of_addition#11#9", "transposition_of_addition#11#6");
				
				final Module conjunction1110 = session.getProposition("transposition_of_addition#11#10");
				
				session.bind("commutativity_of_conjunction", conjunction1110.getFacts().get(0), conjunction1110.getFacts().get(1));
				session.rewrite("transposition_of_addition#11#10", "transposition_of_addition#11#11");
				rewriteRight(session, "transposition_of_addition#11#12", "transposition_of_addition#11#0");
			}
			
			session.apply("transposition_of_addition#10", "transposition_of_addition#11");
			
			{
				final Module m = (Module) session.getCurrentContext().getModule().getProposition("transposition_of_addition#9");
				final Symbol i = m.getParameter("i");
				final Symbol j = m.getParameter("j");
				
				session.bind("transposition_of_addition#12", i, j);
				rewriteRight(session, "transposition_of_addition#9", "transposition_of_addition#13");
			}
			
			session.bind("definition_of_matrix_equality", (Expression) $($(x, "+", y), "ᵀ"), $(xt, "+", yt));
			rewriteRight(session, "transposition_of_addition#14", "transposition_of_addition#15");
		}
	}
	
	public static final void claimCommutativityOfConjunction(final Session session) {
		session.claim("commutativity_of_conjunction",
				$$("∀P,Q ((P ∧ Q) = (Q ∧ P))"));
		
		{
			session.introduce();
			session.introduce();
			
			final Symbol p = session.getParameter("P");
			final Symbol q = session.getParameter("Q");
			final Expression pq = $(p, "&", q);
			final Expression qp = $(q, "&", p);
			final Expression pq2qp = $(pq, "->", qp);
			final Expression qp2pq = $(qp, "->", pq);
			
			session.bind("definition_of_proposition_equality", pq, qp);
			
			session.claim(pq2qp);
			{
				session.introduce();
				session.bind("commutativity_of_conjunction#1#0");
			}
			
			session.claim(qp2pq);
			
			{
				session.introduce();
				session.bind("commutativity_of_conjunction#2#0");
			}
			
			session.claim($(pq2qp, "&", qp2pq));
			
			{
				session.recall("commutativity_of_conjunction#1");
				session.recall("commutativity_of_conjunction#2");
			}
			
			rewriteRight(session, "commutativity_of_conjunction#3", "commutativity_of_conjunction#0");
		}
	}
	
	public static final void rewriteRight(final Session session, final String sourceName, final String equalityName) {
		final Composite equality = session.getProposition(equalityName);
		
		session.claim(session.getProposition(sourceName).accept(new Rewriter().rewrite(equality.get(2), equality.get(0))));
		
		{
			final String ruleName = session.getCurrentContext().getModule().newPropositionName();
			
			session.bind(ruleName, Standard.SYMMETRY_OF_EQUALITY, (Expression) equality.get(0), equality.get(2));
			
			final String reversedEqualityName = session.getCurrentContext().getModule().newPropositionName();
			
			session.apply(reversedEqualityName, ruleName, equalityName);
			
			session.rewrite(sourceName, reversedEqualityName);
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
