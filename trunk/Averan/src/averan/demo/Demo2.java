package averan.demo;

import static averan.core.ExpressionTools.$;
import static averan.demo.ExpressionParser.$$;
import static java.awt.Color.BLACK;
import static java.awt.Color.WHITE;
import static net.sourceforge.aprog.tools.Tools.ignore;
import static net.sourceforge.aprog.tools.Tools.unchecked;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.scilab.forge.jlatexmath.TeXFormula;

import averan.core.Composite;
import averan.core.Expression;
import averan.core.Module;
import averan.core.Rewriter;
import averan.core.Session;
import averan.core.Module.Symbol;
import averan.modules.Standard;
import net.sourceforge.aprog.tools.IllegalInstantiationException;
import net.sourceforge.aprog.tools.Tools;

/**
 * @author codistmonk (creation 2014-08-11)
 */
abstract class SessionTools {
	
	protected SessionTools() {
		throw new IllegalInstantiationException();
	}
	
	private static final Map<Class<?>, Session> sessions = new HashMap<>();
	
	@SuppressWarnings("unchecked")
	protected static final <E extends Expression> E introduce() {
		final Session session = getOrCreateSession();
		final List<Symbol> parameters = session.getCurrentModule().getParameters();
		final List<Expression> conditions = session.getCurrentModule().getConditions();
		final int oldParameterCount = parameters.size();
		final int oldConditionCount = conditions.size();
		
		session.introduce();
		
		if (oldParameterCount < parameters.size()) {
			return (E) session.getParameter(-1);
		}
		
		if (oldConditionCount < conditions.size()) {
			return session.getCondition(-1);
		}
		
		throw new IllegalStateException();
	}
	
	protected static final void suppose(final Expression condition) {
		getOrCreateSession().suppose(condition);
	}
	
	protected static final void suppose(final String conditionName, final Expression proposition) {
		getOrCreateSession().suppose(conditionName, proposition);
	}
	
	protected static final void admit(final Expression fact) {
		getOrCreateSession().admit(fact);
	}
	
	protected static final void admit(final String factName, final Expression fact) {
		getOrCreateSession().admit(factName, fact);
	}
	
	protected static final Session getOrCreateSession() {
		final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		
		for (int i = stackTrace.length - 1; 0 <= i; --i) {
			try {
				final Class<?> cls = Class.forName(stackTrace[i].getClassName());
				final Field moduleField = cls.getDeclaredField("MODULE");
				
				return sessions.compute(cls, (k, v) -> v == null ?
						new Session((Module) getStaticFieldValue(moduleField)) : v);
			} catch (final ClassNotFoundException exception) {
				exception.printStackTrace();
			} catch (final NoSuchFieldException exception) {
				ignore(exception);
			}
		}
		
		throw new IllegalStateException();
	}
	
	@SuppressWarnings("unchecked")
	public static final <T> T getStaticFieldValue(final Field field) {
		try {
			field.setAccessible(true);
			
			return (T) field.get(null);
		} catch (final Exception exception) {
			throw unchecked(exception);
		}
	}
	
}

/**
 * @author codistmonk (creation 2014-08-08)
 */
public final class Demo2 extends SessionTools {
	
	public static final Module MODULE = new Module(Standard.MODULE);
	
	static {
		final Session session = getOrCreateSession();
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
					$$("∀X,Y ((`size_X=`size_Y) → (∀i,j ((X+Y)_(i,j)=(X_(i,j))+(Y_(i,j)))))"));
			suppose("definition_of_matrix_subtraction",
					$$("∀X,Y ((`size_X=`size_Y) → (∀i,j ((X-Y)_(i,j)=(X_(i,j))-(Y_(i,j)))))"));
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
			
			claimCommutativityOfConjunction(session);
			claimTranspositionOfAddition(session);
			claimTranspositionOfMultiplication(session);
			
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
					$$("∀w (((SwwᵀV)=(VwwᵀS)) → `optimality_(J_w))"));
			admit("regularization",
					$$("∀B,ω,w ((w=Bω) → (((SwwᵀV)=(VwwᵀS)) → `constrainedOptimality_(J_(Bω))))"));
		} catch (final BreakSessionException exception) {
			sessionBreakPoint = exception.getStackTrace()[1].toString();
		} finally {
			session.new Exporter(-1).exportSession();
			
			System.out.println(sessionBreakPoint);
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
			
			session.bind("definition_of_matrix_equality", xyt, ytxt);
			
			session.claim(((Composite) session.getFact(-1)).get(2));
			
			{
				introduce();
				introduce();
				
				final Symbol i = session.getParameter("i");
				final Symbol j = session.getParameter("j");
				final Symbol k = session.getCurrentModule().parameter("k");
				final Expression rowCountX = $("rowCount", "_", x);
				final Expression columnCountX = $("columnCount", "_", x);
				final Expression rowCountY = $("rowCount", "_", y);
				final Expression columnCountY = $("columnCount", "_", y);
				
				session.bind(Standard.IDENTITY, k);
				session.bind("definition_of_transposition", xy, i, j);
				session.bind("definition_of_matrix_multiplication", x, y, columnCountX);
				session.claim(((Module) session.getFact(-1)).getConditions().get(0));
				
				{
					session.bind(Standard.IDENTITY, columnCountX);
					session.rewrite(session.getFactName(-1), "transposition_of_multiplication#2", 0);
				}
				
				session.apply("transposition_of_multiplication#4#2", session.getFactName(-1));
				session.bind(session.getFactName(-1), j, i, k);
				session.rewrite("transposition_of_multiplication#4#1", session.getFactName(-1));
				
				session.bind("definition_of_matrix_multiplication", yt, xt, $("columnCount", "_", x));
				session.claim(((Module) session.getFact(-1)).getConditions().get(0));
				
				{
					session.bind("definition_of_transposition_columnCount", y);
					rewriteRight(session, session.getFactName(-1), "transposition_of_multiplication#2");
					session.bind("definition_of_transposition_rowCount", x);
				}
				
				session.apply("transposition_of_multiplication#4#7", session.getFactName(-1));
				session.bind(session.getFactName(-1), i, j, k);
				
				session.bind("definition_of_transposition", x);
				session.bind(session.getFactName(-1), k, j);
				session.rewrite("transposition_of_multiplication#4#10", session.getFactName(-1));
				
				session.bind("definition_of_transposition", y);
				session.bind(session.getFactName(-1), i, k);
				session.rewrite("transposition_of_multiplication#4#13", session.getFactName(-1));
				
				final Expression xjk = $(x, "_", $(j, ",", k));
				final Expression yki = $(y, "_", $(k, ",", i));
				final Expression xjkyki = $(xjk, yki);
				final Expression ykixjk = $(yki, xjk);
				
				session.claim($(ykixjk, "=", xjkyki));
				
				{
					session.claim($(xjk, "∈", "ℝ"));
					
					{
						session.bind("definition_of_matrices", x, rowCountX, columnCountX);
						session.rewrite("transposition_of_multiplication#0", session.getFactName(-1));
						session.bind(session.getFactName(-1));
						session.bind(session.getFactName(-1), j, k);
					}
					
					session.claim($(yki, "∈", "ℝ"));
					
					{
						session.bind("definition_of_matrices", y, rowCountY, columnCountY);
						session.rewrite("transposition_of_multiplication#1", session.getFactName(-1));
						session.bind(session.getFactName(-1));
						session.bind(session.getFactName(-1), k, i);
					}
					
					session.bind("commutativity_of_multiplication", yki, xjk);
					session.apply(session.getFactName(-1), "transposition_of_multiplication#4#17#1");
					session.apply(session.getFactName(-1), "transposition_of_multiplication#4#17#0");
				}
				
				session.rewrite("transposition_of_multiplication#4#16", session.getFactName(-1));
				rewriteRight(session, "transposition_of_multiplication#4#6", session.getFactName(-1));
				
			}
			
			rewriteRight(session, session.getFactName(-1), "transposition_of_multiplication#3");
		}
	}
	
	public static final void claimTranspositionOfAddition(final Session session) {
		session.claim("transposition_of_addition", $$("∀X,Y ((`size_X=`size_Y) → ((X+Y)ᵀ=Xᵀ+Yᵀ))"));
		
		{
			introduce();
			introduce();
			introduce();
			
			final Symbol x = session.getParameter("X");
			final Symbol y = session.getParameter("Y");
			final Expression xt = $(x, "ᵀ");
			final Expression yt = $(y, "ᵀ");
			final Expression xy = $(x, "+", y);
			final Expression xyt = $(xy, "ᵀ");
			final Expression xtyt = $(xt, "+", yt);
			
			session.bind("definition_of_matrix_equality", xyt, xtyt);
			session.claim(((Composite) session.getFact(-1)).get(2));
			
			{
				introduce();
				introduce();
				
				final Symbol i = session.getParameter("i");
				final Symbol j = session.getParameter("j");
				
				session.bind("definition_of_transposition", xy, i, j);
				session.claim($(((Composite) session.getFact(-1)).get(2), "=", ((Composite) session.getCurrentGoal()).get(2)));
				
				{
					session.bind("definition_of_matrix_addition", x, y);
					session.apply(session.getFactName(-1), "transposition_of_addition#0");
					session.bind(session.getFactName(-1), j, i);
					
					session.bind("definition_of_transposition", x, i, j);
					rewriteRight(session, session.getFactName(-2), session.getFactName(-1));
					
					session.bind("definition_of_transposition", y, i, j);
					rewriteRight(session, session.getFactName(-2), session.getFactName(-1));
					
					final Composite eq1 = session.getFact(-1);
					final Composite eq2 = (Composite) session.getCurrentGoal();
					
					session.claim($(eq2.get(2), "=", eq1.get(2)));
					
					{
						session.bind("definition_of_matrix_addition", xt, yt);
						session.claim(((Module) session.getFact(-1)).getConditions().get(0));
						
						{
							session.bind("definition_of_matrix_size_equality", xt, yt);
							session.claim(((Composite) session.getFact(-1)).get(2));
							
							{
								session.claim(((Module) session.getCurrentGoal()).getFacts().get(0));
								
								{
									session.bind("definition_of_matrix_size_equality", x, y);
									session.rewrite("transposition_of_addition#0", session.getFactName(-1));
									session.bind(session.getFactName(-1));
									session.bind("definition_of_transposition_columnCount", x);
									session.rewrite(session.getFactName(-1), session.getFactName(-2));
									session.bind("definition_of_transposition_columnCount", y);
									rewriteRight(session, session.getFactName(-2), session.getFactName(-1));
								}
								
								session.claim(((Module) session.getCurrentGoal()).getFacts().get(1));
								
								{
									session.bind("definition_of_matrix_size_equality", x, y);
									session.rewrite("transposition_of_addition#0", session.getFactName(-1));
									session.bind(session.getFactName(-1));
									session.bind("definition_of_transposition_rowCount", x);
									session.rewrite(session.getFactName(-1), session.getFactName(-3));
									session.bind("definition_of_transposition_rowCount", y);
									rewriteRight(session, session.getFactName(-2), session.getFactName(-1));
								}
							}
							
							rewriteRight(session, session.getFactName(-1), session.getFactName(-2));
						}
						
						session.apply(session.getFactName(-2), session.getFactName(-1));
						session.bind(session.getFactName(-1), i, j);
					}
					
					rewriteRight(session, session.getFactName(-2), session.getFactName(-1));
				}
				
				session.rewrite(session.getFactName(-2), session.getFactName(-1));
			}
			
			rewriteRight(session, session.getFactName(-1), session.getFactName(-2));
		}
	}
	
	public static final void claimCommutativityOfConjunction(final Session session) {
		session.claim("commutativity_of_conjunction",
				$$("∀P,Q ((P ∧ Q) = (Q ∧ P))"));
		
		{
			introduce();
			introduce();
			
			final Symbol p = session.getParameter("P");
			final Symbol q = session.getParameter("Q");
			final Expression pq = $(p, "&", q);
			final Expression qp = $(q, "&", p);
			final Expression pq2qp = $(pq, "->", qp);
			final Expression qp2pq = $(qp, "->", pq);
			
			session.bind("definition_of_proposition_equality", pq, qp);
			
			session.claim(pq2qp);
			{
				introduce();
				session.bind("commutativity_of_conjunction#1#0");
			}
			
			session.claim(qp2pq);
			
			{
				introduce();
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
