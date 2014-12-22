package averan.modules;

import static averan.core.ExpressionTools.$;
import static averan.core.Session.breakSession;
import static averan.core.SessionTools.*;
import static averan.io.ExpressionParser.$$;
import static averan.modules.Reals.ONE;
import static averan.modules.Reals.ZERO;
import static averan.modules.Standard.*;
import averan.core.Composite;
import averan.core.Expression;
import averan.core.Module;
import averan.core.Session;
import averan.core.SessionTools;
import averan.core.Module.Symbol;
import averan.io.SessionScaffold;

import java.util.ArrayList;

import net.sourceforge.aprog.tools.IllegalInstantiationException;
import net.sourceforge.aprog.tools.Tools;

/**
 * @author codistmonk (creation 2014)
 */
public final class RealMatrices {
	
	private RealMatrices() {
		throw new IllegalInstantiationException();
	}
	
	public static final Module MODULE = new Module(Reals.MODULE, RealMatrices.class.getName());
	
	static {
		new SessionScaffold(MODULE, 1, null) {
			
			@Override
			public final void buildSession() {
				suppose("type_of_matrix_element",
						$$("∀X,m,n ((m∈ℕ) → ((n∈ℕ) → ((X∈≀M_(m,n)) → (∀i,j (X_(i,j)∈ℝ)))))"));
				suppose("definition_of_matrix_equality",
						$$("∀X,Y,m,n ((m∈ℕ) → ((n∈ℕ) → ((X∈≀M_(m,n)) → ((Y∈≀M_(m,n)) → ((X=Y) = (∀i,j ((X)_(i,j)=(Y_(i,j)))))))))"));
				suppose("definition_of_matrix_scalarization",
						$$("∀X ((X∈≀M_(1,1)) → (⟨X⟩=X_(0,0)))"));
				claimTypeOfMatrixScalarization();
				suppose("definition_of_matrix_addition",
						$$("∀X,Y,m,n ((X∈≀M_(m,n)) → ((Y∈≀M_(m,n)) → (∀i,j ((X+Y)_(i,j) = (X_(i,j))+(Y_(i,j))))))"));
				suppose("type_of_matrix_addition",
						$$("∀X,Y,m,n ((X∈≀M_(m,n)) → ((Y∈≀M_(m,n)) → ((X+Y)∈≀M_(m,n))))"));
				suppose("definition_of_matrix_subtraction",
						$$("∀X,Y,m,n ((X∈≀M_(m,n)) → ((Y∈≀M_(m,n)) → (∀i,j ((X-Y)_(i,j) = (X_(i,j))-(Y_(i,j))))))"));
				suppose("type_of_matrix_subtraction",
						$$("∀X,Y,m,n ((X∈≀M_(m,n)) → ((Y∈≀M_(m,n)) → ((X-Y)∈≀M_(m,n))))"));
				suppose("definition_of_matrix_multiplication",
						$$("∀X,Y,m,n,o ((X∈≀M_(m,n)) → ((Y∈≀M_(n,o)) → (∀i,j,k ((XY)_(i,j)=((Σ_(k=0)^(n-1)) ((X_(i,k))(Y_(k,j))))))))"));
				suppose("type_of_matrix_multiplication",
						$$("∀X,Y,m,n,o ((X∈≀M_(m,n)) → ((Y∈≀M_(n,o)) → ((XY)∈≀M_(m,o))))"));
				suppose("definition_of_transposition",
						$$("∀X,m,n ((X∈≀M_(m,n)) → (∀i,j ((Xᵀ)_(i,j) = (X_(j,i)))))"));
				suppose("type_of_transposition",
						$$("∀X,m,n ((X∈≀M_(m,n)) → ((Xᵀ)∈≀M_(n,m)))"));
				// TODO claim
				suppose("definition_of_matrix_scalar_multiplication",
						$$("∀X,Y,m,n ((X∈ℝ) → ((Y∈≀M_(m,n)) → (∀i,j ((XY)_(i,j))=X(Y_(i,j)))))"));
				suppose("type_of_matrix_scalar_multiplication",
						$$("∀X,Y,m,n ((X∈ℝ) → ((Y∈≀M_(m,n)) → ((XY)∈≀M_(m,n))))"));
				suppose("commutativity_of_matrix_scalar_multiplication",
						$$("∀X,Y,m,n ((X∈ℝ) → ((Y∈≀M_(m,n)) → ((XY)=(YX))))"));
				suppose("associativity_of_matrix_scalar_multiplication",
						$$("∀X,Y,Z,m,n,o ((X∈ℝ) → ((Y∈≀M_(m,n)) → ((Z∈≀M_(n,o)) → ((X(YZ))=((XY)Z)))))"));
				
				claimAssociativityOfMatrixAddition();
				claimCommutativityOfMatrixAddition();
				claimAssociativityOfMatrixMultiplication();
				claimLeftDistributivityOfMatrixMultiplicationOver("addition", "+");
				claimRightDistributivityOfMatrixMultiplicationOver("addition", "+");
				claimLeftDistributivityOfMatrixMultiplicationOver("subtraction", "-");
				claimRightDistributivityOfMatrixMultiplicationOver("subtraction", "-");
				
				claimTranspositionOf("addition", "+");
				claimTranspositionOf("subtraction", "-");
				breakSession();
				claimTranspositionOfMultiplication();
			}
			
			private static final long serialVersionUID = 8185469030596522271L;
			
		};
	}
	
	public static final void breakSession() {
		pushSession(session());
		Session.breakSession();
	}
	
	public static final Composite transpose(final Expression expression) {
		return $(expression, "ᵀ");
	}
	
	public static final void autoApplyLastFact() {
		final String lastModuleName = factName(-1);
		final Module lastModule = fact(-1);
		
		claimApplied(lastModule);
		{
			Reals.proveWithBindAndApply(lastModule.getConditions().get(0));
			apply(lastModuleName, factName(-1));
		}
	}
	
	public static final void claimApplied(final Module lastModule) {
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
	
	public static final Composite realMatrix(final Object expression, final Object m, final Object n) {
		return $(expression, "∈", $("≀M", "_", $(m, ",", n)));
	}
	
	public static final void applyLastFactOnMatrixElementRealness(
			final Symbol matrix, final Symbol m, final Symbol n, final Symbol i, final Symbol j) {
		claim(lastModuleCondition());
		{
			bind("type_of_matrix_element", matrix, m, n);
			autoApplyLastFact();
			autoApplyLastFact();
			autoApplyLastFact();
			bind(factName(-1), i, j);
		}
		apply(factName(-2), factName(-1));
	}
	
	public static final void claimAssociativityOfMatrixAddition() {
		claim("associativity_of_matrix_addition",
				$$("∀X,Y,Z,m,n ((m∈ℕ) → ((n∈ℕ) → ((X∈≀M_(m,n)) → ((Y∈≀M_(m,n)) → ((Z∈≀M_(m,n)) → ((X+(Y+Z))=((X+Y)+Z)))))))"));
		{
			final Symbol x = introduce();
			final Symbol y = introduce();
			final Symbol z = introduce();
			final Symbol m = introduce();
			final Symbol n = introduce();
			
			introduce();
			introduce();
			introduce();
			introduce();
			introduce();
			
			final Expression xy = $(x, "+", y);
			final Expression yz = $(y, "+", z);
			final Expression xYZ = $(x, "+", yz);
			final Expression xyZ = $(xy, "+", z);
			
			claimLastFact(() -> {
				bind("definition_of_matrix_equality", xYZ, xyZ, m, n);
				autoApplyLastFact();
				autoApplyLastFact();
				autoApplyLastFact();
				autoApplyLastFact();
			});
			
			claim(lastEqualityRight());
			{
				final Symbol i = introduce();
				final Symbol j = introduce();
				final Expression xij = $(x, "_", $(i, ",", j));
				final Expression yij = $(y, "_", $(i, ",", j));
				final Expression zij = $(z, "_", $(i, ",", j));
				
				claimLastFact(() -> {
					bind("definition_of_matrix_addition", x, yz, m, n);
					autoApplyLastFact();
					autoApplyLastFact();
					bind(factName(-1), i, j);
					claimLastFact(() -> {
						bind("definition_of_matrix_addition", y, z, m, n);
						autoApplyLastFact();
						autoApplyLastFact();
						bind(factName(-1), i, j);
					});
					rewrite(factName(-2), factName(-1));
					claimLastFact(() -> {
						bind("associativity_of_addition", xij, yij, zij);
						applyLastFactOnMatrixElementRealness(x, m, n, i, j);
						applyLastFactOnMatrixElementRealness(y, m, n, i, j);
						applyLastFactOnMatrixElementRealness(z, m, n, i, j);
					});
					rewrite(factName(-2), factName(-1));
				});
				
				claimLastFact(() -> {
					bind("definition_of_matrix_addition", xy, z, m, n);
					autoApplyLastFact();
					autoApplyLastFact();
					bind(factName(-1), i, j);
					claimLastFact(() -> {
						bind("definition_of_matrix_addition", x, y, m, n);
						autoApplyLastFact();
						autoApplyLastFact();
						bind(factName(-1), i, j);
					});
					rewrite(factName(-2), factName(-1));
				});
				
				rewriteRight(factName(-2), factName(-1));
			}
			
			rewriteRight(factName(-1), factName(-2));
		}
	}
	
	public static final void claimCommutativityOfMatrixAddition() {
		claim("commutativity_of_matrix_addition",
				$$("∀X,Y,m,n ((m∈ℕ) → ((n∈ℕ) → ((X∈≀M_(m,n)) → ((Y∈≀M_(m,n)) → ((X+Y)=(Y+X))))))"));
		{
			final Symbol x = introduce();
			final Symbol y = introduce();
			final Symbol m = introduce();
			final Symbol n = introduce();
			
			introduce();
			introduce();
			introduce();
			introduce();
			
			final Expression xy = $(x, "+", y);
			final Expression yx = $(y, "+", x);
			
			claimLastFact(() -> {
				bind("definition_of_matrix_equality", xy, yx, m, n);
				autoApplyLastFact();
				autoApplyLastFact();
				autoApplyLastFact();
				autoApplyLastFact();
			});
			
			claim(lastEqualityRight());
			{
				final Symbol i = introduce();
				final Symbol j = introduce();
				final Expression xij = $(x, "_", $(i, ",", j));
				final Expression yij = $(y, "_", $(i, ",", j));
				
				claimLastFact(() -> {
					bind("definition_of_matrix_addition", x, y, m, n);
					autoApplyLastFact();
					autoApplyLastFact();
					bind(factName(-1), i, j);
					claimLastFact(() -> {
						bind("commutativity_of_addition", xij, yij);
						applyLastFactOnMatrixElementRealness(x, m, n, i, j);
						applyLastFactOnMatrixElementRealness(y, m, n, i, j);
					});
					rewrite(factName(-2), factName(-1));
				});
				
				claimLastFact(() -> {
					bind("definition_of_matrix_addition", y, x, m, n);
					autoApplyLastFact();
					autoApplyLastFact();
					bind(factName(-1), i, j);
				});
				
				rewriteRight(factName(-2), factName(-1));
			}
			
			rewriteRight(factName(-1), factName(-2));
		}
	}
	
	public static final void claimAssociativityOfMatrixMultiplication() {
		claim("associativity_of_matrix_multiplication",
				$$("∀X,Y,Z,m,n,o,p ((m∈ℕ) → ((n∈ℕ) → ((o∈ℕ) → ((p∈ℕ) → ((X∈≀M_(m,n)) → ((Y∈≀M_(n,o)) → ((Z∈≀M_(o,p)) → ((X(YZ))=((XY)Z)))))))))"));
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
			introduce();
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
			
			claimLastFact(() -> {
				bind("definition_of_matrix_equality", xYZ, xyZ, m, p);
				autoApplyLastFact();
				autoApplyLastFact();
				claim(lastModuleCondition());
				{
					bind("type_of_matrix_multiplication", x, yz, m, n, p);
					autoApplyLastFact();
					autoApplyLastFact();
				}
				apply(factName(-2), factName(-1));
				claim(lastModuleCondition());
				{
					bind("type_of_matrix_multiplication", xy, z, m, o, p);
					autoApplyLastFact();
					autoApplyLastFact();
				}
				apply(factName(-2), factName(-1));
			});
			
			claim(lastEqualityRight());
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
				
				claimLastFact(() -> {
					bind("definition_of_matrix_multiplication", x, yz, m, n, p);
					autoApplyLastFact();
					autoApplyLastFact();
					bind(factName(-1), i, j, k);
					claimLastFact(() -> {
						bind("definition_of_matrix_multiplication", y, z, n, o, p);
						autoApplyLastFact();
						autoApplyLastFact();
						bind(factName(-1), k, j, l);
					});
					rewrite(factName(-2), factName(-1));
					bind("left_distributivity_over_sum", xik, yklzlj, $(o, "-", "1"), l);
					rewrite(factName(-2), factName(-1));
					claimLastFact(() -> {
						bind("associativity_of_multiplication", xik, ykl, zlj);
						applyLastFactOnMatrixElementRealness(x, m, n, i, k);
						applyLastFactOnMatrixElementRealness(y, n, o, k, l);
						applyLastFactOnMatrixElementRealness(z, o, p, l, j);
					});
					rewrite(factName(-2), factName(-1));
				});
				
				claimLastFact(() -> {
					bind("definition_of_matrix_multiplication", xy, z, m, o, p);
					autoApplyLastFact();
					autoApplyLastFact();
					bind(factName(-1), i, j, l);
					claimLastFact(() -> {
						bind("definition_of_matrix_multiplication", x, y, m, n, o);
						autoApplyLastFact();
						autoApplyLastFact();
						bind(factName(-1), i, l, k);
					});
					rewrite(factName(-2), factName(-1));
					bind("right_distributivity_over_sum", xikykl, zlj, $(n, "-", "1"), k);
					rewrite(factName(-2), factName(-1));
					bind("commutativity_of_sum_nesting", (Expression) $(xikykl, zlj), $(o, "-", "1"), $(n, "-", "1"), l, k);
					rewrite(factName(-2), factName(-1));
				});
				
				rewriteRight(factName(-2), factName(-1));
			}
			
			rewriteRight(factName(-1), factName(-2));
		}
	}
	
	public static final void claimLeftDistributivityOfMatrixMultiplicationOver(
			final String operation, final String operator) {
		claim("left_distributivity_of_matrix_multiplication_over_" + operation,
				$$("∀X,Y,Z,m,n,o ((m∈ℕ) → ((n∈ℕ) → ((o∈ℕ) → ((X∈≀M_(m,n)) → ((Y∈≀M_(n,o)) → ((Z∈≀M_(n,o)) → ((X(Y" + operator + "Z))=(XY" + operator + "XZ))))))))"));
		{
			final Symbol x = introduce();
			final Symbol y = introduce();
			final Symbol z = introduce();
			final Symbol m = introduce();
			final Symbol n = introduce();
			final Symbol o = introduce();
			
			introduce();
			introduce();
			introduce();
			introduce();
			introduce();
			introduce();
			
			final Expression yz = $(y, operator, z);
			final Expression xy = $(x, y);
			final Expression xz = $(x, z);
			final Expression xYZ = $(x, yz);
			final Expression xyXZ = $(xy, operator, xz);
			
			claim(realMatrix(xy, m, o));
			{
				bind("type_of_matrix_multiplication", x, y, m, n, o);
				autoApplyLastFact();
				autoApplyLastFact();
			}
			
			claim(realMatrix(xz, m, o));
			{
				bind("type_of_matrix_multiplication", x, z, m, n, o);
				autoApplyLastFact();
				autoApplyLastFact();
			}
			
			proveUsingBindAndApply(realMatrix(yz, n, o));
			
			claim(realMatrix(xYZ, m, o));
			{
				bind("type_of_matrix_multiplication", x, yz, m, n, o);
				autoApplyLastFact();
				autoApplyLastFact();
			}
			
			proveUsingBindAndApply(realMatrix(xyXZ, m, o));
			
			claimLastFact(() -> {
				bind("definition_of_matrix_equality", xYZ, xyXZ, m, o);
				autoApplyLastFact();
				autoApplyLastFact();
				autoApplyLastFact();
				autoApplyLastFact();
			});
			
			claim(lastEqualityRight());
			{
				final Symbol i = introduce();
				final Symbol j = introduce();
				final Symbol k = session().getCurrentModule().new Symbol("k");
				final Expression xik = $(x, "_", $(i, ",", k));
				final Expression ykj = $(y, "_", $(k, ",", j));
				final Expression zkj = $(z, "_", $(k, ",", j));
				
				claimLastFact(() -> {
					bind("definition_of_matrix_multiplication", x, yz, m, n, o);
					autoApplyLastFact();
					autoApplyLastFact();
					bind(factName(-1), i, j, k);
					claimLastFact(() -> {
						claimLastFact(() -> {
							bind("definition_of_matrix_" + operation, y, z, n, o);
							autoApplyLastFact();
							autoApplyLastFact();
							bind(factName(-1), k, j);
						});
						rewrite(factName(-2), factName(-1));
					});
					claimLastFact(() -> {
						claimLastFact(() -> {
							bind("left_distributivity_of_multiplication_over_" + operation, xik, ykj, zkj);
							applyLastFactOnMatrixElementRealness(x, m, n, i, k);
							applyLastFactOnMatrixElementRealness(y, n, o, k, j);
							applyLastFactOnMatrixElementRealness(z, n, o, k, j);
						});
						rewrite(factName(-2), factName(-1));
					});
					bind("distributivity_of_sum_over_" + operation, (Expression) $(xik, ykj), $(xik, zkj), $(n, "-", "1"), k);
					rewrite(factName(-2), factName(-1));
				});
				
				claimLastFact(() -> {
					bind("definition_of_matrix_" + operation, xy, xz, m, o);
					autoApplyLastFact();
					autoApplyLastFact();
					bind(factName(-1), i, j);
					claimLastFact(() -> {
						bind("definition_of_matrix_multiplication", x, y, m, n, o);
						autoApplyLastFact();
						autoApplyLastFact();
						bind(factName(-1), i, j, k);
					});
					rewrite(factName(-2), factName(-1));
					claimLastFact(() -> {
						bind("definition_of_matrix_multiplication", x, z, m, n, o);
						autoApplyLastFact();
						autoApplyLastFact();
						bind(factName(-1), i, j, k);
					});
					rewrite(factName(-2), factName(-1));
				});
				
				rewriteRight(factName(-2), factName(-1));
			}
			
			rewriteRight(factName(-1), factName(-2));
		}
	}
	
	public static final void claimRightDistributivityOfMatrixMultiplicationOver(
			final String operation, final String operator) {
		claim("right_distributivity_of_matrix_multiplication_over_" + operation,
				$$("∀X,Y,Z,m,n,o ((m∈ℕ) → ((n∈ℕ) → ((o∈ℕ) → ((X∈≀M_(m,n)) → ((Y∈≀M_(m,n)) → ((Z∈≀M_(n,o)) → (((X" + operator + "Y)Z)=(XZ" + operator + "YZ))))))))"));
		{
			final Symbol x = introduce();
			final Symbol y = introduce();
			final Symbol z = introduce();
			final Symbol m = introduce();
			final Symbol n = introduce();
			final Symbol o = introduce();
			
			introduce();
			introduce();
			introduce();
			introduce();
			introduce();
			introduce();
			
			final Expression yz = $(y, z);
			final Expression xy = $(x, operator, y);
			final Expression xz = $(x, z);
			final Expression xyZ = $(xy, z);
			final Expression xzYZ = $(xz, operator, yz);
			
			claim(realMatrix(yz, m, o));
			{
				bind("type_of_matrix_multiplication", y, z, m, n, o);
				autoApplyLastFact();
				autoApplyLastFact();
			}
			
			claim(realMatrix(xz, m, o));
			{
				bind("type_of_matrix_multiplication", x, z, m, n, o);
				autoApplyLastFact();
				autoApplyLastFact();
			}
			
			proveUsingBindAndApply(realMatrix(xy, m, n));
			
			claim(realMatrix(xyZ, m, o));
			{
				bind("type_of_matrix_multiplication", xy, z, m, n, o);
				autoApplyLastFact();
				autoApplyLastFact();
			}
			
			proveUsingBindAndApply(realMatrix(xzYZ, m, o));
			
			claimLastFact(() -> {
				bind("definition_of_matrix_equality", xyZ, xzYZ, m, o);
				autoApplyLastFact();
				autoApplyLastFact();
				autoApplyLastFact();
				autoApplyLastFact();
			});
			
			claim(lastEqualityRight());
			{
				final Symbol i = introduce();
				final Symbol j = introduce();
				final Symbol k = session().getCurrentModule().new Symbol("k");
				final Expression xik = $(x, "_", $(i, ",", k));
				final Expression yik = $(y, "_", $(i, ",", k));
				final Expression zkj = $(z, "_", $(k, ",", j));
				
				claimLastFact(() -> {
					bind("definition_of_matrix_multiplication", xy, z, m, n, o);
					autoApplyLastFact();
					autoApplyLastFact();
					bind(factName(-1), i, j, k);
					claimLastFact(() -> {
						bind("definition_of_matrix_" + operation, x, y, m, n);
						autoApplyLastFact();
						autoApplyLastFact();
						bind(factName(-1), i, k);
					});
					rewrite(factName(-2), factName(-1));
					claimLastFact(() -> {
						bind("right_distributivity_of_multiplication_over_" + operation, xik, yik, zkj);
						applyLastFactOnMatrixElementRealness(x, m, n, i, k);
						applyLastFactOnMatrixElementRealness(y, m, n, i, k);
						applyLastFactOnMatrixElementRealness(z, n, o, k, j);
					});
					rewrite(factName(-2), factName(-1));
					bind("distributivity_of_sum_over_" + operation, (Expression) $(xik, zkj), $(yik, zkj), $(n, "-", "1"), k);
					rewrite(factName(-2), factName(-1));
				});
				
				claimLastFact(() -> {
					bind("definition_of_matrix_" + operation, xz, yz, m, o);
					autoApplyLastFact();
					autoApplyLastFact();
					bind(factName(-1), i, j);
					claimLastFact(() -> {
						bind("definition_of_matrix_multiplication", x, z, m, n, o);
						autoApplyLastFact();
						autoApplyLastFact();
						bind(factName(-1), i, j, k);
					});
					rewrite(factName(-2), factName(-1));
					claimLastFact(() -> {
						bind("definition_of_matrix_multiplication", y, z, m, n, o);
						autoApplyLastFact();
						autoApplyLastFact();
						bind(factName(-1), i, j, k);
					});
					rewrite(factName(-2), factName(-1));
				});
				
				rewriteRight(factName(-2), factName(-1));
			}
			
			rewriteRight(factName(-1), factName(-2));
		}
	}
	
	public static final void claimTranspositionOf(final String operation, final String operator) {
		claim("transposition_of_" + operation,
				$$("∀X,Y,m,n ((m∈ℕ) → ((n∈ℕ) → ((X∈≀M_(m,n)) → ((Y∈≀M_(m,n)) → ((X" + operator + "Y)ᵀ=Xᵀ" + operator + "Yᵀ)))))"));
		{
			final Symbol x = introduce();
			final Symbol y = introduce();
			final Symbol m = introduce();
			final Symbol n = introduce();
			final Expression xt = transpose(x);
			final Expression yt = transpose(y);
			final Expression xy = $(x, operator, y);
			final Expression xyT = transpose(xy);
			final Expression xtyt = $(xt, operator, yt);
			
			introduce();
			introduce();
			introduce();
			introduce();
			
			claim(realMatrix(xt, n, m));
			{
				bind("type_of_transposition", x, m, n);
				autoApplyLastFact();
			}
			
			claim(realMatrix(yt, n, m));
			{
				bind("type_of_transposition", y, m, n);
				autoApplyLastFact();
			}
			
			proveUsingBindAndApply(realMatrix(xtyt, n, m));
			
			claimLastFact(() -> {
				bind("definition_of_matrix_equality", xyT, xtyt, n, m);
				autoApplyLastFact();
				autoApplyLastFact();
				autoApplyLastFact();
				autoApplyLastFact();
			});
			
			claim(lastEqualityRight());
			{
				final Symbol i = introduce();
				final Symbol j = introduce();
				
				claimLastFact(() -> {
					bind("definition_of_transposition", xy, m, n);
					autoApplyLastFact();
					bind(factName(-1), i, j);
					claimLastFact(() -> {
						bind("definition_of_matrix_" + operation, x, y, m, n);
						autoApplyLastFact();
						autoApplyLastFact();
						bind(factName(-1), j, i);
					});
					rewrite(factName(-2), factName(-1));
				});
				
				claimLastFact(() -> {
					bind("definition_of_matrix_" + operation, xt, yt, n, m);
					autoApplyLastFact();
					autoApplyLastFact();
					bind(factName(-1), i, j);
					claimLastFact(() -> {
						bind("definition_of_transposition", x, m, n);
						autoApplyLastFact();
						bind(factName(-1), i, j);
					});
					rewrite(factName(-2), factName(-1));
					claimLastFact(() -> {
						bind("definition_of_transposition", y, m, n);
						autoApplyLastFact();
						bind(factName(-1), i, j);
					});
					rewrite(factName(-2), factName(-1));
				});
				
				rewriteRight(factName(-2), factName(-1));
			}
			
			rewriteRight(factName(-1), factName(-2));
		}
	}
	
	public static final void claimTranspositionOfMultiplication() {
		claim("transposition_of_multiplication",
				$$("∀X,Y,m,n,o ((X∈≀M_(m,n)) → ((Y∈≀M_(n,o)) → ((XY)ᵀ=YᵀXᵀ)))"));
		{
			final Symbol x = introduce();
			final Symbol y = introduce();
			final Symbol m = introduce();
			final Symbol n = introduce();
			final Symbol o = introduce();
			final Expression xt = transpose(x);
			final Expression yt = transpose(y);
			final Expression xy = $(x, y);
			final Expression xyT = transpose(xy);
			final Expression ytxt = $(yt, xt);
			
			introduce();
			introduce();
			
			
			claim(realMatrix(xy, m, o));
			{
				bind("type_of_matrix_multiplication", x, y, m, n, o);
				autoApplyLastFact();
				autoApplyLastFact();
			}
			
			claim(realMatrix(xyT, o, m));
			{
				bind("type_of_transposition", xy, m, o);
				autoApplyLastFact();
			}
			
			claim(realMatrix(xt, n, m));
			{
				bind("type_of_transposition", x, m, n);
				autoApplyLastFact();
			}
			
			claim(realMatrix(yt, o, n));
			{
				bind("type_of_transposition", y, n, o);
				autoApplyLastFact();
			}
			
			claim(realMatrix(ytxt, o, m));
			{
				bind("type_of_matrix_multiplication", yt, xt, o, n, m);
				autoApplyLastFact();
				autoApplyLastFact();
			}
			
			bind("definition_of_matrix_equality", xyT, ytxt, o, m);
			autoApplyLastFact();
			autoApplyLastFact();
			
			claim(((Composite) fact(-1)).get(2));
			{
				final Symbol i = introduce();
				final Symbol j = introduce();
				final Symbol k = session().getCurrentModule().new Symbol("k");
				final Expression xjk = $(x, "_", $(j, ",", k));
				final Expression yki = $(y, "_", $(k, ",", i));
				
				bind("definition_of_transposition", xy, m, o);
				autoApplyLastFact();
				bind(factName(-1), i, j);
				String xyTFactName = factName(-1);
				bind("definition_of_matrix_multiplication", x, y, m, n, o);
				autoApplyLastFact();
				autoApplyLastFact();
				bind(factName(-1), j, i, k);
				rewrite(xyTFactName, factName(-1));
				xyTFactName = factName(-1);
				
				bind("definition_of_matrix_multiplication", yt, xt, o, n, m);
				autoApplyLastFact();
				autoApplyLastFact();
				bind(factName(-1), i, j, k);
				String ytxtFactName = factName(-1);
				bind("definition_of_transposition", x, m, n);
				autoApplyLastFact();
				bind(factName(-1), k, j);
				rewrite(ytxtFactName, factName(-1));
				ytxtFactName = factName(-1);
				bind("definition_of_transposition", y, n, o);
				autoApplyLastFact();
				bind(factName(-1), i, k);
				rewrite(ytxtFactName, factName(-1));
				ytxtFactName = factName(-1);
				bind("commutativity_of_multiplication", yki, xjk);
				applyLastFactOnMatrixElementRealness(y, n, o, k, i);
				applyLastFactOnMatrixElementRealness(x, m, n, j, k);
				rewrite(ytxtFactName, factName(-1));
				
				rewriteRight(xyTFactName, factName(-1));
			}
			
			rewriteRight(factName(-1), factName(-2));
		}
	}
	
	public static final void claimTypeOfMatrixScalarization() {
		claim("type_of_matrix_scalarization",
				$$("∀X ((X∈≀M_(1,1)) → (⟨X⟩∈ℝ))"));
		{
			final Symbol x = introduce();
			
			introduce();
			claimLastFact(() -> {
				bind("definition_of_matrix_scalarization", x);
				autoApplyLastFact();
			});
			
			claimLastFact(() -> {
				bind("type_of_matrix_element", x, ONE, ONE);
				autoApplyLastFact();
				autoApplyLastFact();
				autoApplyLastFact();
				bind(factName(-1), ZERO, ZERO);
			});
			
			rewriteRight(factName(-1), factName(-2));
		}
	}
	
}
