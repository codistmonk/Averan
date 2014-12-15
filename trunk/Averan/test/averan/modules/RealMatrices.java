package averan.modules;

import static averan.core.ExpressionTools.$;
import static averan.core.SessionTools.*;
import static averan.io.ExpressionParser.$$;
import static averan.modules.Reals.real;
import static averan.modules.Standard.*;
import averan.core.Composite;
import averan.core.Expression;
import averan.core.Module;
import averan.core.Session;
import averan.core.Module.Symbol;
import averan.io.SessionExporter;
import averan.io.SessionScaffold;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;

import net.sourceforge.aprog.tools.IllegalInstantiationException;

/**
 * @author codistmonk (creation 2014)
 */
public final class RealMatrices {
	
	private RealMatrices() {
		throw new IllegalInstantiationException();
	}
	
	public static final Module MODULE = new Module(Reals.MODULE, RealMatrices.class.getName());
	
	static {
		new SessionScaffold(MODULE) {
			
			@Override
			public final void buildSession() {
				suppose("definition_of_matrices",
						$$("∀X,m,n (X∈≀M_(m,n) = ('rowCount'_X = m ∧ 'columnCount'_X = n ∧ ∀i,j (X_(i,j)∈ℝ)))"));
				suppose("type_of_matrices",
						$$("∀X ((X∈≀M)=(X∈≀M_(('rowCount'_X),('columnCount'_X))))"));
				suppose("definition_of_matrix_equality",
						$$("∀X,Y ((X∈≀M) → ((Y∈≀M) → ((X=Y) = (∀i,j ((X)_(i,j)=(Y_(i,j)))))))"));
				suppose("definition_of_matrix_scalarization",
						$$("∀X ((X∈≀M_(1,1)) → (⟨X⟩=X_(1,1)))"));
				suppose("definition_of_matrix_addition",
						$$("∀X,Y,i,j (((X+Y)_(i,j) = (X_(i,j))+(Y_(i,j))))"));
				suppose("type_of_matrix_addition",
						$$("∀X,Y ((X∈≀M) → ((Y∈≀M) → ((X+Y)∈≀M)))"));
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
				suppose("type_of_matrix_multiplication",
						$$("∀X,Y ((X∈≀M) → ((Y∈≀M) → ((XY)∈≀M)))"));
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
				
				claim("matrix_element_is_real",
						$$("∀X ((X∈≀M) → (∀i,j ((X)_(i,j)∈ℝ)))"));
				{
					introduce();
					introduce();
					introduce();
					introduce();
					
					final Symbol x = parameter("X");
					bind("type_of_matrices", x);
					rewrite(conditionName(-1), factName(-1));
					bind("definition_of_matrices", x, $("rowCount", "_", x), $("columnCount", "_", x));
					rewrite(factName(-2), factName(-1));
					bind(factName(-1));
					bind(factName(-1), parameter("i"), parameter("j"));
				}
				
				// TODO claim
				admit("right_distributivity_on_sum",
						$$("∀X,Y,n,i ((((Σ_(i=0)^n) X)Y)=((Σ_(i=0)^n) (XY)))"));
				admit("left_distributivity_on_sum",
						$$("∀X,Y,n,i ((X ((Σ_(i=0)^n) Y))=((Σ_(i=0)^n) (XY)))"));
				admit("commutativity_of_sum",
						$$("∀X,m,n,i,j ((((Σ_(i=0)^m) ((Σ_(j=0)^n) X)))=(((Σ_(j=0)^n) ((Σ_(i=0)^m) X))))"));
				
				claimAssociativityOfMatrixAddition();
				claimCommutativityOfMatrixAddition();
//				claimAssociativityOfMatrixMultiplication();
				
			}
			
			private static final long serialVersionUID = 8185469030596522271L;
			
		};
	}
	
	public static final void claimAssociativityOfMatrixMultiplication() {
		claim("associativity_of_matrix_multiplication",
				$$("∀X,Y,Z (X∈≀M → (Y∈≀M → (Z∈≀M → ((X(YZ))=((XY)Z)))))"));
		{
			final Symbol x = introduce();
			final Symbol y = introduce();
			final Symbol z = introduce();
			
			introduce();
			introduce();
			introduce();
			
			final Expression xy = $(x, y);
			final Expression yz = $(y, z);
			final Expression xyZ = $(xy, z);
			final Expression xYZ = $(x, yz);
			
			bind("definition_of_matrix_equality", xyZ, xYZ);
			
			autoApplyLastFact();
			autoApplyLastFact();
			claim(((Composite) fact(-1)).get(2));
			{
				final Symbol i = introduce();
				final Symbol j = introduce();
				final Symbol k = session().getCurrentModule().new Symbol("k");
				final Symbol l = session().getCurrentModule().new Symbol("l");
				
				bind("definition_of_matrix_multiplication_columnCount", x, y);
				bind("definition_of_matrix_multiplication", xy, z, i, j, k);
				rewrite(factName(-1), factName(-2));
				bind("definition_of_matrix_multiplication", x, y, i, k, l);
				rewrite(factName(-2), factName(-1));
				bind("definition_of_matrix_multiplication", x, yz, i, j, l);
				bind("definition_of_matrix_multiplication", y, z, l, j, k);
				rewrite(factName(-2), factName(-1));
			}
		}
	}
	
	public static final void claimCommutativityOfMatrixAddition() {
		claim("commutativity_of_matrix_addition",
				$$("∀X,Y (X∈≀M ∧ Y∈≀M) → ((X+Y)=(Y+X))"));
		{
			final Symbol x = introduce();
			final Symbol y = introduce();
			introduce();
			bind(conditionName(-1));
			final Expression xy = $(x, "+", y);
			final Expression yx = $(y, "+", x);
			bind("definition_of_matrix_equality", xy, yx);
			autoApplyLastFact();
			autoApplyLastFact();
			claim(((Composite) fact(-1)).get(2));
			{
				final Symbol i = introduce();
				final Symbol j = introduce();
				
				bind("definition_of_matrix_addition", x, y, i, j);
				bind("definition_of_matrix_addition", y, x, i, j);
				
				claim($(((Composite) fact(-2)).get(2), "=", ((Composite) fact(-1)).get(2)));
				{
					final Expression xij = $(x, "_", $(i, ",", j));
					final Expression yij = $(y, "_", $(i, ",", j));
					
					bind("commutativity_of_addition", xij, yij);
					
					{
						final String lastModuleName = factName(-1);
						final Module lastModule = fact(-1);
						
						claimLastFactApplied(lastModuleName, lastModule);
						{
							bind("matrix_element_is_real", x);
							autoApplyLastFact();
							bind(factName(-1), i, j);
							apply(lastModuleName, factName(-1));
						}
					}
					
					{
						final String lastModuleName = factName(-1);
						final Module lastModule = fact(-1);
						
						claimLastFactApplied(lastModuleName, lastModule);
						{
							bind("matrix_element_is_real", y);
							autoApplyLastFact();
							bind(factName(-1), i, j);
							apply(lastModuleName, factName(-1));
						}
					}
					
					rewrite(factName(-3), factName(-1));
					rewriteRight(factName(-1), factName(-3));
				}
				
				rewriteRight(factName(-1), factName(-2));
			}
		}
	}
	
	public static final void autoApplyLastFact() {
		final String lastModuleName = factName(-1);
		final Module lastModule = fact(-1);
		
		claimLastFactApplied(lastModuleName, lastModule);
		{
			Reals.proveWithBindAndApply(lastModule.getConditions().get(0));
			apply(lastModuleName, factName(-1));
		}
	}

	public static void claimLastFactApplied(final String lastModuleName, final Module lastModule) {
		final ArrayList<Symbol> newParameters = new ArrayList<>(lastModule.getParameters());
		final ArrayList<Expression> newConditions = new ArrayList<>(lastModule.getConditions());
		final ArrayList<Expression> newFacts = new ArrayList<>(lastModule.getFacts());
		
		if (!newParameters.isEmpty()) {
			throw new IllegalStateException();
		}
		
		if (newConditions.isEmpty()) {
			bind(factName(-1));
			
			return;
		}
		
		newConditions.remove(0);
		
		final Expression applied;
		
		if (newConditions.isEmpty() && newFacts.size() == 1) {
			applied = newFacts.get(0);
		} else {
			applied = new Module(lastModule.getParent(), session().newPropositionName(), lastModule.getTrustedModules(),
					newParameters, newConditions, newFacts);
		}
		
		claim(applied);
	}
	
	public static final Composite realMatrix(final Expression expression) {
		return $(expression, "∈", "≀M");
	}
	
	public static final void claimAssociativityOfMatrixAddition() {
		final Session session = session();
		final String matrixFactName = "associativity_of_matrix_addition";
		final String realFactName = "associativity_of_addition";
		final Session s = new Session(new Module(session.getCurrentModule(), matrixFactName));
		final Module realFact = ((Module) session.getProposition(realFactName)).canonical();
		final Collection<Expression> remainingConditions = new HashSet<>(realFact.getConditions());
		
		lookForRealParameters:
		for (final Symbol parameter : realFact.getParameters()) {
			final Expression typeOfParameter = real(parameter);
			
			for (final Expression condition : realFact.getConditions()) {
				if (typeOfParameter.equals(condition)) {
					s.suppose(realMatrix(s.getCurrentContext().parameter(parameter.toString().toUpperCase(Locale.ENGLISH))));
					remainingConditions.remove(condition);
					continue lookForRealParameters;
				}
			}
			
			s.getCurrentContext().parameter(parameter.toString());
		}
		
		for (final Expression e : s.getCurrentModule().bind(realFact).getFacts()) {
			s.claim(e);
			{
				final Composite g = (Composite) s.getCurrentGoal();
				
				s.bind("definition_of_matrix_equality", (Expression) g.get(0), g.get(2));
				
				{
					Expression lastFact = s.getFact(-1);
					
					while (lastFact instanceof Module) {
						final Module lastModule = (Module) lastFact;
						Reals.proveWithBindAndApply(s, lastModule.getConditions().get(0));
						s.apply(s.getFactName(-2), s.getFactName(-1));
						lastFact = s.getFact(-1);
					}
					
					s.claim(((Composite) lastFact).get(2));
				}
				
				{
					s.introduce();
					s.introduce();
					
					final Symbol x = s.getParameter("X");
					final Symbol y = s.getParameter("Y");
					final Symbol z = s.getParameter("Z");
					final Symbol i = s.getParameter("i");
					final Symbol j = s.getParameter("j");
					final Expression xij = $(x, "_", $(i, ",", j));
					final Expression yij = $(y, "_", $(i, ",", j));
					final Expression zij = $(z, "_", $(i, ",", j));
					final Expression xy = $(x, "+", y);
					final Expression yz = $(y, "+", z);
					
					s.bind("definition_of_matrix_addition", x, yz, i, j);
					s.bind("definition_of_matrix_addition", y, z, i, j);
					s.rewrite(s.getFactName(-2), s.getFactName(-1));
					final String leftExpansion = s.getFactName(-1);
					
					s.bind("definition_of_matrix_addition", xy, z, i, j);
					s.bind("definition_of_matrix_addition", x, y, i, j);
					s.rewrite(s.getFactName(-2), s.getFactName(-1));
					final String rightExpansion = s.getFactName(-1);
					
					s.bind(realFactName, xij, yij, zij);
					{
						s.bind("matrix_element_is_real", x);
						s.apply(s.getFactName(-1), matrixFactName + ".1");
						s.bind(s.getFactName(-1), i, j);
						s.apply(s.getFactName(-4), s.getFactName(-1));
						
						s.bind("matrix_element_is_real", y);
						s.apply(s.getFactName(-1), matrixFactName + ".2");
						s.bind(s.getFactName(-1), i, j);
						s.apply(s.getFactName(-4), s.getFactName(-1));
						
						s.bind("matrix_element_is_real", z);
						s.apply(s.getFactName(-1), matrixFactName + ".3");
						s.bind(s.getFactName(-1), i, j);
						s.apply(s.getFactName(-4), s.getFactName(-1));
					}
					
					s.rewrite(leftExpansion, s.getFactName(-1));
					rewriteRight(s, s.getFactName(-1), rightExpansion);
				}
				
				rewriteRight(s, s.getFactName(-1), s.getFactName(-2));
			}
		}
		
		session.getCurrentModule().new Claim(matrixFactName, s.getCurrentModule()).execute();
		session.tryToPop();
//		new SessionExporter(s).exportSession();
	}
	
}
