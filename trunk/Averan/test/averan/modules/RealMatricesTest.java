package averan.modules;

import static averan.core.ExpressionTools.*;
import static averan.core.Session.breakSession;
import static averan.core.SessionTools.*;
import static averan.io.ExpressionParser.$$;
import static averan.modules.Reals.*;
import static averan.modules.RealMatrices.*;
import static averan.modules.Standard.*;
import static org.junit.Assert.*;
import averan.core.Composite;
import averan.core.Expression;
import averan.core.Module;
import averan.core.Session;
import averan.core.Module.Symbol;
import averan.io.SessionScaffold;

import org.junit.Test;

/**
 * @author codistmonk (creation 2014-12-11)
 */
public final class RealMatricesTest {
	
	@Test
	public final void test1() {
		new SessionScaffold(RealMatrices.MODULE) {
			
			@Override
			public final void buildSession() {
				suppose("definition_of_ones",
						$$("∀m,n,i,j ((1_(m,n))_(i,j)=1)"));
				suppose("type_of_ones",
						$$("∀m,n (1_(m,n)∈≀M_(m,n))"));
				suppose("definition_of_mean",
						$$("∀X,m,n ((n∈ℕ) → ((X∈≀M_(m,n)) → (μ_X=(1/n)X(1_(n,1)))))"));
				claimTypeOfMean();
				suppose("definition_of_replicated_mean",
						$$("∀X,m,n ((n∈ℕ) → ((X∈≀M_(m,n)) → (M_X=(μ_X)(1_(1,n)))))"));
				claimTypeOfReplicatedMean();
				suppose("definition_of_covariance",
						$$("∀X,Y,m,n ((X∈≀M_(m,n)) → ((Y∈≀M_(m,n)) → (Var_(X,Y)=(X-M_X)(Y-M_Y)ᵀ)))"));
				claimTypeOfCovariance();
				suppose("definition_of_variance",
						$$("∀X,m,n ((X∈≀M_(m,n)) → (Var_X=Var_(X,X)))"));
				claimTypeOfVariance();
				suppose("definition_of_class_means",
						$$("∀X,m,n,c ((c∈ℕ) → ((∀i ((i∈ℕ_c) → ((n_i∈ℕ) ∧ (X_i∈≀M_(m,n_i))))) → (∀i,j ((j∈ℕ_c) → ((U_(X,c))_(i,j)=(μ_(X_j))_(i,1))))))"));
				// TODO claim (?)
				suppose("type_of_class_means",
						$$("∀X,m,n,c ((c∈ℕ) → ((∀i ((i∈ℕ_c) → ((n_i∈ℕ) ∧ (X_i∈≀M_(m,n_i))))) → ((U_(X,c))∈≀M_(m,c))))"));
				
				suppose("definition_of_fisher_linear_separability",
						$$("∀w,X,m,n,c,j,k ((w∈≀M_(m,1)) → ((∀i ((i∈ℕ_c) → ((n_i∈ℕ) ∧(X_i∈≀M_(m,n_i))))) → (S_(wᵀX,c)=(⟨Var_(wᵀU_(X,c))⟩/⟨(Σ_(j=0)^(c-1)) (Var_(wᵀX_j))⟩))))"));
				claim("type_of_fisher_linear_separability",
						$$("∀w,X,m,n,c,j,k ((c∈ℕ) → ((w∈≀M_(m,1)) → ((∀i ((i∈ℕ_c) → ((n_i∈ℕ) ∧(X_i∈≀M_(m,n_i))))) → (S_(wᵀX,c)∈ℝ))))"));
				{
					final Symbol w = introduce();
					final Symbol x = introduce();
					final Symbol m = introduce();
					final Symbol n = introduce();
					final Symbol c = introduce();
					final Symbol j = introduce();
					final Symbol k = introduce();
					
					final Expression wt = transpose(w);
					final Expression uxc = $("U", "_", $(x, ",", c));
					final Expression wtuxc = $(wt, uxc);
					
					introduce();
					introduce();
					introduce();
					
					proveWithBindAndApply(realMatrix(wt, $("1"), m));
					
					claim(realMatrix(uxc, m, c));
					{
						bind("type_of_class_means", x, m, n, c);
						autoApplyLastFact();
						apply(factName(-1), conditionName(-1));
					}
					
					claim(realMatrix(wtuxc, $("1"), c));
					{
						bind("type_of_matrix_multiplication", wt, uxc, $("1"), m, c);
						autoApplyLastFact();
						autoApplyLastFact();
					}
					
					bind("definition_of_fisher_linear_separability", w, x, m, n, c, j, k);
					autoApplyLastFact();
					apply(factName(-1), conditionName(-1));
					
					claim(real(((Composite) fact(-1)).get(2)));
					{
						final Composite goal = goal();
						final Composite fraction = goal.get(0);
						
						bind("type_of_division", (Expression) fraction.get(0), fraction.get(2));
						
						claimAppliedAndCondition(fact(-1));
						{
							{
								final Expression expression = ((Composite) ((Composite) goal()).get(0)).get(1);
								
								bind("definition_of_matrix_scalarization", expression);
								
								final String moduleName = factName(-1);
								
								claimAppliedAndCondition(fact(-1));
								{
									{
										bind("type_of_variance", wtuxc, $("1"), c);
										autoApplyLastFact();
										autoApplyLastFact();
									}
									
									apply(moduleName, factName(-1));
								}
								
								breakSession();
							}
						}
					}
				}
			}
			
			private static final long serialVersionUID = 2969099922483811015L;
			
		};
	}
	
	public static final void claimAppliedAndCondition(final Module module) {
		claimApplied(module);
		claim(module.getConditions().get(0));
	}
	
	public static final void claimTypeOfMean() {
		claim("type_of_mean",
				$$("∀X,m,n ((n∈ℕ) → ((X∈≀M_(m,n)) → (μ_X∈≀M_(m,1))))"));
		{
			final Symbol x = introduce();
			final Symbol m = introduce();
			final Symbol n = introduce();
			
			introduce();
			introduce();
			
			bind("definition_of_mean", x, m, n);
			autoApplyLastFact();
			autoApplyLastFact();
			
			claim(realMatrix(((Composite) fact(-1)).get(2), m, $("1")));
			{
				final Expression invN = inverse(n);
				final Expression invNx = $(invN, x);
				
				claim(real(invN));
				{
					bind("naturals_are_reals", n);
					autoApplyLastFact();
					bind("type_of_inverse", n);
					autoApplyLastFact();
				}
				
				bind("type_of_matrix_scalar_multiplication", invN, x, m, n);
				autoApplyLastFact();
				autoApplyLastFact();
				
				bind("type_of_ones", n, $("1"));
				
				bind("type_of_matrix_multiplication", invNx, ones(n, $("1")), m, n, $("1"));
				autoApplyLastFact();
				autoApplyLastFact();
			}
			
			rewriteRight(factName(-1), factName(-2));
		}
	}
	
	public static final void claimTypeOfCovariance() {
		claim("type_of_covariance",
				$$("∀X,Y,m,n ((n∈ℕ) → ((X∈≀M_(m,n)) → ((Y∈≀M_(m,n)) → (Var_(X,Y)∈≀M_(m,m)))))"));
		{
			final Symbol x = introduce();
			final Symbol y = introduce();
			final Symbol m = introduce();
			final Symbol n = introduce();
			
			introduce();
			introduce();
			introduce();
			
			bind("definition_of_covariance", x, y, m, n);
			autoApplyLastFact();
			autoApplyLastFact();
			
			final Expression xmx = $(x, "-", $("M", "_", x));
			final Expression ymy = $(y, "-", $("M", "_", y));
			final Composite ymyt = transpose(ymy);
			
			claim(realMatrix($(xmx, ymyt), m, m));
			{
				proveUsingBindAndApply(realMatrix(xmx, m, n));
				claim(realMatrix(ymyt, n, m));
				{
					proveUsingBindAndApply(realMatrix(ymy, m, n));
					bind("type_of_transposition", ymy, m, n);
					autoApplyLastFact();
				}
				
				bind("type_of_matrix_multiplication", xmx, ymyt, m, n, m);
				autoApplyLastFact();
				autoApplyLastFact();
			}
			
			rewriteRight(factName(-1), factName(-2));
		}
	}
	
	public static final void claimTypeOfVariance() {
		claim("type_of_variance",
				$$("∀X,m,n ((n∈ℕ) → ((X∈≀M_(m,n)) → (Var_X∈≀M_(m,m))))"));
		{
			final Symbol x = introduce();
			final Symbol m = introduce();
			final Symbol n = introduce();
			
			introduce();
			introduce();
			
			bind("type_of_covariance", x, x, m, n);
			autoApplyLastFact();
			autoApplyLastFact();
			autoApplyLastFact();
			
			bind("definition_of_variance", x, m, n);
			autoApplyLastFact();
			
			rewriteRight(factName(-3), factName(-1));
		}
	}
	
	public static final void claimTypeOfReplicatedMean() {
		claim("type_of_replicated_mean",
				$$("∀X,m,n ((n∈ℕ) → ((X∈≀M_(m,n)) → (M_X∈≀M_(m,n))))"));
		{
			final Symbol x = introduce();
			final Symbol m = introduce();
			final Symbol n = introduce();
			
			introduce();
			introduce();
			
			bind("definition_of_replicated_mean", x, m, n);
			autoApplyLastFact();
			autoApplyLastFact();
			
			claim(realMatrix(((Composite) fact(-1)).get(2), m, n));
			{
				bind("type_of_mean", x, m, n);
				autoApplyLastFact();
				autoApplyLastFact();
				
				bind("type_of_ones", (Expression) $("1"), n);
				
				bind("type_of_matrix_multiplication", mean(x), ones("1", n), m, $("1"), n);
				autoApplyLastFact();
				autoApplyLastFact();
			}
			
			rewriteRight(factName(-1), factName(-2));
		}
	}
	
	public static final Expression ones(final Object m, final Object n) {
		return $("1", "_", $(m, ",", n));
	}
	
	public static final Expression mean(final Object expression) {
		return $("μ", "_", expression);
	}
	
}
