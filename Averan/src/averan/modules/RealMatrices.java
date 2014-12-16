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
//				suppose("definition_of_matrices",
//						$$("∀X,m,n (X∈≀M_(m,n) = ('rowCount'_X = m ∧ 'columnCount'_X = n ∧ ∀i,j (X_(i,j)∈ℝ)))"));
				suppose("type_of_matrix_element",
						$$("∀X,m,n ((X∈≀M_(m,n)) → (∀i,j (X_(i,j)∈ℝ)))"));
				suppose("definition_of_matrix_rowCount",
						$$("∀X,m,n ((X∈≀M_(m,n)) → ('rowCount'_X = m))"));
				suppose("definition_of_matrix_columnCount",
						$$("∀X,m,n ((X∈≀M_(m,n)) → ('columnCount'_X = n))"));
				suppose("definition_of_matrix_equality",
						$$("∀X,Y,m,n ((X∈≀M_(m,n)) → ((Y∈≀M_(m,n)) → ((X=Y) = (∀i,j ((X)_(i,j)=(Y_(i,j)))))))"));
				suppose("definition_of_matrix_scalarization",
						$$("∀X ((X∈≀M_(1,1)) → (⟨X⟩=X_(1,1)))"));
				suppose("definition_of_matrix_addition",
						$$("∀X,Y,m,n ((X∈≀M_(m,n)) → ((Y∈≀M_(m,n)) → (∀i,j ((X+Y)_(i,j) = (X_(i,j))+(Y_(i,j))))))"));
				suppose("type_of_matrix_addition",
						$$("∀X,Y,m,n ((X∈≀M_(m,n)) → ((Y∈≀M_(m,n)) → ((X+Y)∈≀M_(m,n))))"));
				suppose("definition_of_matrix_subtraction",
						$$("∀X,Y,m,n ((X∈≀M_(m,n)) → ((Y∈≀M_(m,n)) → (∀i,j ((X-Y)_(i,j) = (X_(i,j))-(Y_(i,j))))))"));
				suppose("type_of_matrix_subtraction",
						$$("∀X,Y,m,n ((X∈≀M_(m,n)) → ((Y∈≀M_(m,n)) → ((X-Y)∈≀M_(m,n))))"));
				suppose("definition_of_matrix_multiplication",
						$$("∀X,Y,m,n,o,i,j,k ((X∈≀M_(m,n)) → ((Y∈≀M_(n,o)) → ((XY)_(i,j)=((Σ_(k=0)^(n-1)) ((X_(i,k))(Y_(k,j)))))))"));
				suppose("type_of_matrix_multiplication",
						$$("∀X,Y,m,n,o ((X∈≀M_(m,n)) → ((Y∈≀M_(n,o)) → ((XY)∈≀M_(m,o))))"));
				suppose("definition_of_transposition",
						$$("∀X,m,n ((X∈≀M_(m,n)) → (∀i,j ((Xᵀ)_(i,j) = (X_(j,i)))))"));
				suppose("type_of_transposition",
						$$("∀X,m,n ((X∈≀M_(m,n)) → ((Xᵀ)∈≀M_(n,m)))"));
				
				claimAssociativityOfMatrixAddition2();
				claimCommutativityOfMatrixAddition2();
				claimAssociativityOfMatrixMultiplication2();
				
				if (true) return;
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
				suppose("definition_of_matrix_addition_rowCount_2",
						$$("∀X,Y ('rowCount'_(X+Y) = 'rowCount'_Y)"));
				suppose("definition_of_matrix_addition_columnCount_2",
						$$("∀X,Y ('columnCount'_(X+Y) = 'columnCount'_Y)"));
				suppose("definition_of_matrix_subtraction",
						$$("∀X,Y,i,j (((X-Y)_(i,j) = (X_(i,j))-(Y_(i,j))))"));
				suppose("type_of_matrix_subtraction",
						$$("∀X,Y ((X∈≀M) → ((Y∈≀M) → ((X-Y)∈≀M)))"));
				suppose("definition_of_matrix_subtraction_rowCount",
						$$("∀X,Y ('rowCount'_(X-Y) = 'rowCount'_X)"));
				suppose("definition_of_matrix_subtraction_columnCount",
						$$("∀X,Y ('columnCount'_(X-Y) = 'columnCount'_X)"));
				suppose("definition_of_matrix_subtraction_rowCount_2",
						$$("∀X,Y ('rowCount'_(X-Y) = 'rowCount'_Y)"));
				suppose("definition_of_matrix_subtraction_columnCount_2",
						$$("∀X,Y ('columnCount'_(X-Y) = 'columnCount'_Y)"));
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
				suppose("type_of_transposition",
						$$("∀X ((X∈≀M) → ((Xᵀ)∈≀M))"));
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
				
				claimAssociativityOfMatrixAddition();
				claimCommutativityOfMatrixAddition();
				claimAssociativityOfMatrixMultiplication();
				claimLeftDistributivityOfMatrixMultiplicationOver("addition", "+");
				claimRightDistributivityOfMatrixMultiplicationOver("addition", "+");
				claimLeftDistributivityOfMatrixMultiplicationOver("subtraction", "-");
				claimRightDistributivityOfMatrixMultiplicationOver("subtraction", "-");
				
				claimTranspositionOf("addition", "+");
				claimTranspositionOf("subtraction", "-");
				claimTranspositionOfMultiplication();
			}
			
			private static final long serialVersionUID = 8185469030596522271L;
			
		};
	}
	
	public static final void claimTranspositionOf(final String operation, final String operator) {
		claim("transposition_of_" + operation,
				$$("∀X,Y ((X∈≀M) → ((Y∈≀M) → ((X" + operator + "Y)ᵀ=Xᵀ" + operator + "Yᵀ)))"));
		{
			final Symbol x = introduce();
			final Symbol y = introduce();
			final Expression xt = transpose(x);
			final Expression yt = transpose(y);
			final Expression xy = $(x, operator, y);
			final Expression xyT = transpose(xy);
			final Expression xtyt = $(xt, operator, yt);
			
			introduce();
			introduce();
			
			bind("definition_of_matrix_equality", xyT, xtyt);
			autoApplyLastFact();
			autoApplyLastFact();
			
			claim(((Composite) fact(-1)).get(2));
			{
				final Symbol i = introduce();
				final Symbol j = introduce();
				
				bind("definition_of_transposition", xy, i, j);
				bind("definition_of_matrix_" + operation, x, y, j, i);
				rewrite(factName(-2), factName(-1));
				
				final String xyTFactName = factName(-1);
				
				bind("definition_of_matrix_" + operation, xt, yt, i, j);
				bind("definition_of_transposition", x, i, j);
				rewrite(factName(-2), factName(-1));
				bind("definition_of_transposition", y, i, j);
				rewrite(factName(-2), factName(-1));
				
				rewriteRight(xyTFactName, factName(-1));
			}
			
			rewriteRight(factName(-1), factName(-2));
		}
	}
	
	public static final void claimTranspositionOfMultiplication() {
		claim("transposition_of_multiplication",
				$$("∀X,Y ((X∈≀M) → ((Y∈≀M) → ((XY)ᵀ=YᵀXᵀ)))"));
		{
			final Symbol x = introduce();
			final Symbol y = introduce();
			final Expression xt = transpose(x);
			final Expression yt = transpose(y);
			final Expression xy = $(x, y);
			final Expression xyT = transpose(xy);
			final Expression ytxt = $(yt, xt);
			
			introduce();
			introduce();
			
			bind("definition_of_matrix_equality", xyT, ytxt);
			autoApplyLastFact();
			autoApplyLastFact();
			
			claim(((Composite) fact(-1)).get(2));
			{
				final Symbol i = introduce();
				final Symbol j = introduce();
				final Symbol k = session().getCurrentModule().new Symbol("k");
				final Expression xjk = $(x, "_", $(j, ",", k));
				final Expression yki = $(y, "_", $(k, ",", i));
				
				bind("definition_of_transposition", xy, i, j);
				bind("definition_of_matrix_multiplication", x, y, j, i, k);
				rewrite(factName(-2), factName(-1));
				
				final String xyTFactName = factName(-1);
				
				bind("definition_of_matrix_multiplication", yt, xt, i, j, k);
				bind("definition_of_transposition", x, k, j);
				rewrite(factName(-2), factName(-1));
				bind("definition_of_transposition", y, i, k);
				rewrite(factName(-2), factName(-1));
				
				final String ytxtFactName = factName(-1);
				
				bind("commutativity_of_multiplication", yki, xjk);
				applyLastFactOnMatrixElementRealness(y, k, i);
				applyLastFactOnMatrixElementRealness(x, j, k);
				rewrite(ytxtFactName, factName(-1));
				if (true) return;
				
				rewriteRight(xyTFactName, factName(-1));
			}
			
			rewriteRight(factName(-1), factName(-2));
		}
	}
	
	public static final Composite transpose(final Expression expression) {
		return $(expression, "ᵀ");
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
	
	public static final Composite realMatrix(final Expression expression, final Symbol m, final Symbol n) {
		return $(expression, "∈", $("≀M", "_", $(m, ",", n)));
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
	
	public static final void applyLastFactOnMatrixElementRealness(
			final Symbol matrix, final Symbol m, final Symbol n, final Symbol i, final Symbol j) {
		claim(((Module) fact(-1)).getConditions().get(0));
		{
			bind("type_of_matrix_element", matrix, m, n);
			autoApplyLastFact();
			bind(factName(-1), i, j);
		}
		apply(factName(-2), factName(-1));
	}
	
	public static final void claimLeftDistributivityOfMatrixMultiplicationOver(
			final String operation, final String operator) {
		claim("left_distributivity_of_matrix_multiplication_over_" + operation,
				$$("∀X,Y,Z ((X∈≀M) → ((Y∈≀M) → ((Z∈≀M) → ((X(Y" + operator + "Z))=(XY" + operator + "XZ)))))"));
		{
			final Symbol x = introduce();
			final Symbol y = introduce();
			final Symbol z = introduce();
			
			introduce();
			introduce();
			introduce();
			
			final Expression yz = $(y, operator, z);
			final Expression xy = $(x, y);
			final Expression xz = $(x, z);
			final Expression xYZ = $(x, yz);
			final Expression xyXZ = $(xy, operator, xz);
			
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
				bind("definition_of_matrix_" + operation, y, z, k, j);
				rewrite(factName(-2), factName(-1));
				
				String xYZFactName = factName(-1);
				
				bind("left_distributivity_of_multiplication_over_" + operation, xik, ykj, zkj);
				applyLastFactOnMatrixElementRealness(x, i, k);
				applyLastFactOnMatrixElementRealness(y, k, j);
				applyLastFactOnMatrixElementRealness(z, k, j);
				
				rewrite(xYZFactName, factName(-1));
				
				xYZFactName = factName(-1);
				
				bind("definition_of_matrix_" + operation, xy, xz, i, j);
				bind("definition_of_matrix_multiplication", x, y, i, j, k);
				rewrite(factName(-2), factName(-1));
				bind("definition_of_matrix_multiplication", x, z, i, j, k);
				rewrite(factName(-2), factName(-1));
				
				bind("distributivity_of_sum_over_" + operation, (Expression) $(xik, ykj), $(xik, zkj), $($("columnCount", "_", x), "-", "1"), k);
				rewriteRight(factName(-2), factName(-1));
				
				rewriteRight(xYZFactName, factName(-1));
			}
			
			rewriteRight(factName(-1), factName(-2));
		}
	}
	
	public static final void claimRightDistributivityOfMatrixMultiplicationOver(
			final String operation, final String operator) {
		claim("right_distributivity_of_matrix_multiplication_over_" + operation,
				$$("∀X,Y,Z ((X∈≀M) → ((Y∈≀M) → ((Z∈≀M) → (((X" + operator + "Y)Z)=(XZ" + operator + "YZ)))))"));
		{
			final Symbol x = introduce();
			final Symbol y = introduce();
			final Symbol z = introduce();
			
			introduce();
			introduce();
			introduce();
			
			final Expression xy = $(x, operator, y);
			final Expression xz = $(x, z);
			final Expression yz = $(y, z);
			final Expression xyZ = $(xy, z);
			final Expression xzYZ = $(xz, operator, yz);
			
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
				bind("definition_of_matrix_" + operation, x, y, i, k);
				rewrite(factName(-2), factName(-1));
				
				String xYZFactName = factName(-1);
				
				bind("right_distributivity_of_multiplication_over_" + operation, xik, yik, zkj);
				applyLastFactOnMatrixElementRealness(x, i, k);
				applyLastFactOnMatrixElementRealness(y, i, k);
				applyLastFactOnMatrixElementRealness(z, k, j);
				
				rewrite(xYZFactName, factName(-1));
				
				bind("definition_of_matrix_" + operation + "_columnCount", x, y);
				final String columnCountXYFactName = factName(-1);
				rewrite(factName(-2), factName(-1));
				
				xYZFactName = factName(-1);
				
				bind("definition_of_matrix_" + operation, xz, yz, i, j);
				bind("definition_of_matrix_multiplication", x, z, i, j, k);
				rewrite(factName(-2), factName(-1));
				bind("definition_of_matrix_multiplication", y, z, i, j, k);
				
				claim($($("columnCount", "_", y), "=", $("columnCount", "_", x)));
				{
					bind("definition_of_matrix_" + operation + "_columnCount_2", x, y);
					rewrite(columnCountXYFactName, factName(-1));
				}
				rewrite(factName(-2), factName(-1));
				
				rewrite(factName(-4), factName(-1));
				bind("distributivity_of_sum_over_" + operation,
						(Expression) $(xik, zkj), $(yik, zkj), $($("columnCount", "_", x), "-", "1"), k);
				rewriteRight(factName(-2), factName(-1));
				
				rewriteRight(xYZFactName, factName(-1));
			}
			
			rewriteRight(factName(-1), factName(-2));
		}
	}
	
	public static final void claimAssociativityOfMatrixAddition2() {
		claim("associativity_of_matrix_addition",
				$$("∀X,Y,Z,m,n ((X∈≀M_(m,n)) → ((Y∈≀M_(m,n)) → ((Z∈≀M_(m,n)) → ((X+(Y+Z))=((X+Y)+Z)))))"));
		{
			final Symbol x = introduce();
			final Symbol y = introduce();
			final Symbol z = introduce();
			final Symbol m = introduce();
			final Symbol n = introduce();
			
			introduce();
			introduce();
			introduce();
			
			final Expression xy = $(x, "+", y);
			final Expression yz = $(y, "+", z);
			final Expression xYZ = $(x, "+", yz);
			final Expression xyZ = $(xy, "+", z);
			
			bind("definition_of_matrix_equality", xYZ, xyZ, m, n);
			autoApplyLastFact();
			autoApplyLastFact();
			
			claim(((Composite) fact(-1)).get(2));
			{
				final Symbol i = introduce();
				final Symbol j = introduce();
				final Expression xij = $(x, "_", $(i, ",", j));
				final Expression yij = $(y, "_", $(i, ",", j));
				final Expression zij = $(z, "_", $(i, ",", j));
				
				bind("definition_of_matrix_addition", x, yz, m, n);
				autoApplyLastFact();
				autoApplyLastFact();
				bind(factName(-1), i, j);
				String xYZFactName = factName(-1);
				bind("definition_of_matrix_addition", y, z, m, n);
				autoApplyLastFact();
				autoApplyLastFact();
				bind(factName(-1), i, j);
				rewrite(xYZFactName, factName(-1));
				xYZFactName = factName(-1);
				bind("associativity_of_addition", xij, yij, zij);
				applyLastFactOnMatrixElementRealness(x, m, n, i, j);
				applyLastFactOnMatrixElementRealness(y, m, n, i, j);
				applyLastFactOnMatrixElementRealness(z, m, n, i, j);
				rewrite(xYZFactName, factName(-1));
				xYZFactName = factName(-1);
				
				bind("definition_of_matrix_addition", xy, z, m, n);
				autoApplyLastFact();
				autoApplyLastFact();
				bind(factName(-1), i, j);
				String xyZFactName = factName(-1);
				bind("definition_of_matrix_addition", x, y, m, n);
				autoApplyLastFact();
				autoApplyLastFact();
				bind(factName(-1), i, j);
				rewrite(xyZFactName, factName(-1));
				
				rewriteRight(xYZFactName, factName(-1));
			}
			
			rewriteRight(factName(-1), factName(-2));
		}
	}
	
	public static final void claimCommutativityOfMatrixAddition2() {
		claim("commutativity_of_matrix_addition",
				$$("∀X,Y,m,n ((X∈≀M_(m,n)) → ((Y∈≀M_(m,n)) → ((X+Y)=(Y+X))))"));
		{
			final Symbol x = introduce();
			final Symbol y = introduce();
			final Symbol m = introduce();
			final Symbol n = introduce();
			
			introduce();
			introduce();
			
			final Expression xy = $(x, "+", y);
			final Expression yx = $(y, "+", x);
			
			bind("definition_of_matrix_equality", xy, yx, m, n);
			autoApplyLastFact();
			autoApplyLastFact();
			
			claim(((Composite) fact(-1)).get(2));
			{
				final Symbol i = introduce();
				final Symbol j = introduce();
				final Expression xij = $(x, "_", $(i, ",", j));
				final Expression yij = $(y, "_", $(i, ",", j));
				
				bind("definition_of_matrix_addition", x, y, m, n);
				autoApplyLastFact();
				autoApplyLastFact();
				bind(factName(-1), i, j);
				String xyFactName = factName(-1);
				bind("commutativity_of_addition", xij, yij);
				applyLastFactOnMatrixElementRealness(x, m, n, i, j);
				applyLastFactOnMatrixElementRealness(y, m, n, i, j);
				rewrite(xyFactName, factName(-1));
				xyFactName = factName(-1);
				
				bind("definition_of_matrix_addition", y, x, m, n);
				autoApplyLastFact();
				autoApplyLastFact();
				bind(factName(-1), i, j);
				
				rewriteRight(xyFactName, factName(-1));
			}
			
			rewriteRight(factName(-1), factName(-2));
		}
	}
	
	public static final void claimAssociativityOfMatrixMultiplication2() {
		claim("associativity_of_matrix_multiplication",
				$$("∀X,Y,Z,m,n,o,p ((X∈≀M_(m,n)) → ((Y∈≀M_(n,o)) → ((Z∈≀M_(o,p)) → ((X(YZ))=((XY)Z)))))"));
		{
			final Symbol x = introduce();
			final Symbol y = introduce();
			final Symbol z = introduce();
			final Symbol m = introduce();
			final Symbol n = introduce();
			final Symbol o = introduce();
			final Symbol p = introduce();
			
			introduce();
			introduce();
			introduce();
			
			final Expression xy = $(x, y);
			final Expression yz = $(y, z);
			final Expression xYZ = $(x, yz);
			final Expression xyZ = $(xy, z);
			
			claim(realMatrix(xy, m, o));
			{
				bind("type_of_matrix_multiplication", x, y, m, n, o);
				autoApplyLastFact();
				autoApplyLastFact();
			}
			
			claim(realMatrix(yz, n, p));
			{
				bind("type_of_matrix_multiplication", y, z, n, o, p);
				autoApplyLastFact();
				autoApplyLastFact();
			}
			
			bind("definition_of_matrix_equality", xYZ, xyZ, m, p);
			
			claim(((Module) fact(-1)).getConditions().get(0));
			{
				bind("type_of_matrix_multiplication", x, yz, m, n, p);
				autoApplyLastFact();
				autoApplyLastFact();
			}
			apply(factName(-2), factName(-1));
			
			claim(((Module) fact(-1)).getConditions().get(0));
			{
				bind("type_of_matrix_multiplication", xy, z, m, o, p);
				autoApplyLastFact();
				autoApplyLastFact();
			}
			apply(factName(-2), factName(-1));
			
			claim(((Composite) fact(-1)).get(2));
			{
				final Symbol i = introduce();
				final Symbol j = introduce();
				final Symbol k = session().getCurrentModule().new Symbol("k");
				final Symbol l = session().getCurrentModule().new Symbol("l");
				final Expression xik = $(x, "_", $(i, ",", k));
				final Expression ykl = $(y, "_", $(k, ",", l));
				final Expression zlj = $(z, "_", $(l, ",", j));
				final Expression xikykl = $(xik, ykl);
				final Expression yklzlj = $(ykl, zlj);
				
				bind("definition_of_matrix_multiplication", x, yz, m, n, p, i, j, k);
				autoApplyLastFact();
				autoApplyLastFact();
				String xYZFactName = factName(-1);
				bind("definition_of_matrix_multiplication", y, z, n, o, p, k, j, l);
				autoApplyLastFact();
				autoApplyLastFact();
				rewrite(xYZFactName, factName(-1));
				
				bind("left_distributivity_over_sum", xik, yklzlj, $(o, "-", "1"), l);
				rewrite(factName(-2), factName(-1));
				xYZFactName = factName(-1);
				bind("associativity_of_multiplication", xik, ykl, zlj);
				applyLastFactOnMatrixElementRealness(x, m, n, i, k);
				applyLastFactOnMatrixElementRealness(y, n, o, k, l);
				applyLastFactOnMatrixElementRealness(z, o, p, l, j);
				rewrite(xYZFactName, factName(-1));
				xYZFactName = factName(-1);
				
				bind("definition_of_matrix_multiplication", xy, z, m, o, p, i, j, l);
				autoApplyLastFact();
				autoApplyLastFact();
				final String xyZFactName = factName(-1);
				bind("definition_of_matrix_multiplication", x, y, m, n, o, i, l, k);
				autoApplyLastFact();
				autoApplyLastFact();
				rewrite(xyZFactName, factName(-1));
				
				bind("right_distributivity_over_sum", xikykl, zlj, $(n, "-", "1"), k);
				rewrite(factName(-2), factName(-1));
				bind("commutativity_of_sum_nesting", (Expression) $(xikykl, zlj), $(o, "-", "1"), $(n, "-", "1"), l, k);
				rewrite(factName(-2), factName(-1));
				
				rewriteRight(xYZFactName, factName(-1));
			}
			
			rewriteRight(factName(-1), factName(-2));
		}
	}
	
}
