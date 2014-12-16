package averan.modules;

import static averan.core.ExpressionTools.$;
import static averan.core.SessionTools.*;
import static averan.io.ExpressionParser.$$;
import static averan.modules.Standard.*;

import averan.core.Composite;
import averan.core.Expression;
import averan.core.Module;
import averan.core.Module.Symbol;
import averan.io.SessionScaffold;

import java.util.ArrayList;

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
				admit("right_distributivity_over_sum",
						$$("∀X,Y,n,i ((((Σ_(i=0)^n) X)Y)=((Σ_(i=0)^n) (XY)))"));
				admit("left_distributivity_over_sum",
						$$("∀X,Y,n,i ((X ((Σ_(i=0)^n) Y))=((Σ_(i=0)^n) (XY)))"));
				admit("commutativity_of_sum_nesting",
						$$("∀X,m,n,i,j ((((Σ_(i=0)^m) ((Σ_(j=0)^n) X)))=(((Σ_(j=0)^n) ((Σ_(i=0)^m) X))))"));
				admit("distributivity_of_sum_over_addition",
						$$("∀X,Y,n,i (((Σ_(i=0)^n) (X+Y))=(((Σ_(i=0)^n) X)+((Σ_(i=0)^n) Y)))"));
				
				claimAssociativityOfMatrixAddition();
				claimCommutativityOfMatrixAddition();
				claimAssociativityOfMatrixMultiplication();
				claimLeftDistributivityOfMatrixMultiplicationOverAddition();
				claimRightDistributivityOfMatrixMultiplicationOverAddition();
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
			final Expression xYZ = $(x, yz);
			final Expression xyZ = $(xy, z);
			
			bind("definition_of_matrix_equality", xYZ, xyZ);
			
			autoApplyLastFact();
			autoApplyLastFact();
			claim(((Composite) fact(-1)).get(2));
			{
				final Symbol i = introduce();
				final Symbol j = introduce();
				final Symbol k = session().getCurrentModule().new Symbol("k");
				final Symbol l = session().getCurrentModule().new Symbol("l");
				final Expression xil = $(x, "_", $(i, ",", l));
				final Expression ylk = $(y, "_", $(l, ",", k));
				final Expression zkj = $(z, "_", $(k, ",", j));
				final Expression colsXMin1 = $($("columnCount", "_", x), "-", "1");
				final Expression colsYMin1 = $($("columnCount", "_", y), "-", "1");
				
				bind("definition_of_matrix_multiplication_columnCount", x, y);
				bind("definition_of_matrix_multiplication", xy, z, i, j, k);
				rewrite(factName(-1), factName(-2));
				bind("definition_of_matrix_multiplication", x, y, i, k, l);
				rewrite(factName(-2), factName(-1));
				bind("right_distributivity_over_sum", (Expression) $(xil, ylk), zkj, colsXMin1, l);
				rewrite(factName(-2), factName(-1));
				bind("commutativity_of_sum_nesting", (Expression) $($(xil, ylk), zkj), colsXMin1, colsYMin1, l, k);
				rewriteRight(factName(-2), factName(-1));
				
				String xyZijName = factName(-1);
				
				bind("associativity_of_multiplication", xil, ylk, zkj);
				applyLastFactOnMatrixElementRealness(x, i, l);
				applyLastFactOnMatrixElementRealness(y, l, k);
				applyLastFactOnMatrixElementRealness(z, k, j);
				
				rewriteRight(xyZijName, factName(-1));
				
				xyZijName = factName(-1);
				
				bind("definition_of_matrix_multiplication", x, yz, i, j, l);
				bind("definition_of_matrix_multiplication", y, z, l, j, k);
				rewrite(factName(-2), factName(-1));
				bind("left_distributivity_over_sum", xil, (Expression) $(ylk, zkj), colsYMin1, k);
				rewrite(factName(-2), factName(-1));
				
				rewriteRight(factName(-1), xyZijName);
			}
			
			rewriteRight(factName(-1), factName(-2));
		}
	}
	
	public static final void claimAssociativityOfMatrixAddition() {
		claim("associativity_of_matrix_addition",
				$$("∀X,Y,Z (X∈≀M → (Y∈≀M → (Z∈≀M → ((X+(Y+Z))=((X+Y)+Z)))))"));
		{
			final Symbol x = introduce();
			final Symbol y = introduce();
			final Symbol z = introduce();
			
			introduce();
			introduce();
			introduce();
			
			final Expression xy = $(x, "+", y);
			final Expression yz = $(y, "+", z);
			final Expression xYZ = $(x, "+", yz);
			final Expression xyZ = $(xy, "+", z);
			
			bind("definition_of_matrix_equality", xYZ, xyZ);
			
			autoApplyLastFact();
			autoApplyLastFact();
			claim(((Composite) fact(-1)).get(2));
			{
				final Symbol i = introduce();
				final Symbol j = introduce();
				final Expression xij = $(x, "_", $(i, ",", j));
				final Expression yij = $(y, "_", $(i, ",", j));
				final Expression zij = $(z, "_", $(i, ",", j));
				
				bind("definition_of_matrix_addition", x, yz, i, j);
				bind("definition_of_matrix_addition", y, z, i, j);
				rewrite(factName(-2), factName(-1));
				final String xYZFactName = factName(-1);
				final Expression xYZij = ((Composite) fact(-1)).get(2);
				
				bind("definition_of_matrix_addition", xy, z, i, j);
				bind("definition_of_matrix_addition", x, y, i, j);
				rewrite(factName(-2), factName(-1));
				final Expression xyZij = ((Composite) fact(-1)).get(2);
				final String xyZFactName = factName(-1);
				
				claim($(xYZij, "=", xyZij));
				{
					bind("associativity_of_addition", xij, yij, zij);
					applyLastFactOnMatrixElementRealness(x, i, j);
					applyLastFactOnMatrixElementRealness(y, i, j);
					applyLastFactOnMatrixElementRealness(z, i, j);
				}
				
				rewrite(xYZFactName, factName(-1));
				rewriteRight(factName(-1), xyZFactName);
			}
			
			rewriteRight(factName(-1), factName(-2));
		}
	}
	
	public static final void claimCommutativityOfMatrixAddition() {
		claim("commutativity_of_matrix_addition",
				$$("∀X,Y (X∈≀M → (Y∈≀M → ((X+Y)=(Y+X))))"));
		{
			final Symbol x = introduce();
			final Symbol y = introduce();
			introduce();
			introduce();
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
	
	public static final void applyLastFactOnMatrixElementRealness(
			final Symbol matrix, final Symbol i, final Symbol j) {
		claim(((Module) fact(-1)).getConditions().get(0));
		{
			bind("matrix_element_is_real", matrix);
			autoApplyLastFact();
			bind(factName(-1), i, j);
		}
		apply(factName(-2), factName(-1));
	}
	
	public static final void claimLeftDistributivityOfMatrixMultiplicationOverAddition() {
		claim("left_distributivity_of_matrix_multiplication_over_addition",
				$$("∀X,Y,Z ((X∈≀M) → ((Y∈≀M) → ((Z∈≀M) → ((X(Y+Z))=(XY+XZ)))))"));
		{
			final Symbol x = introduce();
			final Symbol y = introduce();
			final Symbol z = introduce();
			
			introduce();
			introduce();
			introduce();
			
			final Expression yz = $(y, "+", z);
			final Expression xy = $(x, y);
			final Expression xz = $(x, z);
			final Expression xYZ = $(x, yz);
			final Expression xyXZ = $(xy, "+", xz);
			
			bind("definition_of_matrix_equality", xYZ, xyXZ);
			autoApplyLastFact();
			autoApplyLastFact();
			
			claim(((Composite) fact(-1)).get(2));
			{
				final Symbol i = introduce();
				final Symbol j = introduce();
				final Symbol k = session().getCurrentModule().new Symbol("k");
				final Expression xik = $(x, "_", $(i, ",", k));
				final Expression ykj = $(y, "_", $(k, ",", j));
				final Expression zkj = $(z, "_", $(k, ",", j));
				
				bind("definition_of_matrix_multiplication", x, yz, i, j, k);
				bind("definition_of_matrix_addition", y, z, k, j);
				rewrite(factName(-2), factName(-1));
				
				String xYZFactName = factName(-1);
				
				bind("left_distributivity_of_multiplication_over_addition", xik, ykj, zkj);
				applyLastFactOnMatrixElementRealness(x, i, k);
				applyLastFactOnMatrixElementRealness(y, k, j);
				applyLastFactOnMatrixElementRealness(z, k, j);
				
				rewrite(xYZFactName, factName(-1));
				
				xYZFactName = factName(-1);
				
				bind("definition_of_matrix_addition", xy, xz, i, j);
				bind("definition_of_matrix_multiplication", x, y, i, j, k);
				rewrite(factName(-2), factName(-1));
				bind("definition_of_matrix_multiplication", x, z, i, j, k);
				rewrite(factName(-2), factName(-1));
				
				bind("distributivity_of_sum_over_addition", (Expression) $(xik, ykj), $(xik, zkj), $($("columnCount", "_", x), "-", "1"), k);
				rewriteRight(factName(-2), factName(-1));
				
				rewriteRight(xYZFactName, factName(-1));
			}
			
			rewriteRight(factName(-1), factName(-2));
		}
	}
	
	public static final void claimRightDistributivityOfMatrixMultiplicationOverAddition() {
		claim("right_distributivity_of_matrix_multiplication_over_addition",
				$$("∀X,Y,Z ((X∈≀M) → ((Y∈≀M) → ((Z∈≀M) → (((X+Y)Z)=(XZ+YZ)))))"));
		{
			final Symbol x = introduce();
			final Symbol y = introduce();
			final Symbol z = introduce();
			
			introduce();
			introduce();
			introduce();
			
			final Expression xy = $(x, "+", y);
			final Expression xz = $(x, z);
			final Expression yz = $(y, z);
			final Expression xyZ = $(xy, z);
			final Expression xzYZ = $(xz, "+", yz);
			
			bind("definition_of_matrix_equality", xyZ, xzYZ);
			autoApplyLastFact();
			autoApplyLastFact();
			
			claim(((Composite) fact(-1)).get(2));
			{
				final Symbol i = introduce();
				final Symbol j = introduce();
				final Symbol k = session().getCurrentModule().new Symbol("k");
				final Expression xik = $(x, "_", $(i, ",", k));
				final Expression yik = $(y, "_", $(i, ",", k));
				final Expression zkj = $(z, "_", $(k, ",", j));
				
				bind("definition_of_matrix_multiplication", xy, z, i, j, k);
				bind("definition_of_matrix_addition", x, y, i, k);
				rewrite(factName(-2), factName(-1));
				
				String xYZFactName = factName(-1);
				
				bind("right_distributivity_of_multiplication_over_addition", xik, yik, zkj);
				applyLastFactOnMatrixElementRealness(x, i, k);
				applyLastFactOnMatrixElementRealness(y, i, k);
				applyLastFactOnMatrixElementRealness(z, k, j);
				
				rewrite(xYZFactName, factName(-1));
				
				bind("definition_of_matrix_addition_columnCount", x, y);
				final String columnCountXYFactName = factName(-1);
				rewrite(factName(-2), factName(-1));
				
				xYZFactName = factName(-1);
				
				bind("definition_of_matrix_addition", xz, yz, i, j);
				bind("definition_of_matrix_multiplication", x, z, i, j, k);
				rewrite(factName(-2), factName(-1));
				bind("definition_of_matrix_multiplication", y, z, i, j, k);
				
				claim($($("columnCount", "_", y), "=", $("columnCount", "_", x)));
				{
					bind("commutativity_of_matrix_addition", x, y);
					autoApplyLastFact();
					autoApplyLastFact();
					rewrite(columnCountXYFactName, factName(-1));
					bind("definition_of_matrix_addition_columnCount", y, x);
					rewrite(factName(-2), factName(-1));
				}
				rewrite(factName(-2), factName(-1));
				
				rewrite(factName(-4), factName(-1));
				bind("distributivity_of_sum_over_addition",
						(Expression) $(xik, zkj), $(yik, zkj), $($("columnCount", "_", x), "-", "1"), k);
				rewriteRight(factName(-2), factName(-1));
				
				rewriteRight(xYZFactName, factName(-1));
			}
			
			rewriteRight(factName(-1), factName(-2));
		}
	}
	
}
