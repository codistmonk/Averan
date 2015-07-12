package averan.draft1.modules;

import static averan.draft1.core.ExpressionTools.*;
import static averan.draft1.core.Session.breakSession;
import static averan.draft1.core.SessionTools.*;
import static averan.draft1.io.ExpressionParser.$$;
import static averan.draft1.modules.RealMatrices.*;
import static averan.draft1.modules.Reals.*;
import static averan.draft1.modules.Standard.*;
import static org.junit.Assert.*;
import averan.draft1.core.Composite;
import averan.draft1.core.Expression;
import averan.draft1.core.Module;
import averan.draft1.core.Session;
import averan.draft1.core.Module.Symbol;
import averan.draft1.io.SessionExporter;
import averan.draft1.io.SessionScaffold;
import averan.draft1.modules.RealMatrices;
import multij.tools.Tools;

import org.junit.Test;

/**
 * @author codistmonk (creation 2014-12-11)
 */
public final class RealMatricesTest {
	
	@Test
	public final void test1() {
//		SessionScaffold.exportLatexPNG(new Session(Reals.MODULE), 0, "reals.png");
//		Tools.debugPrint(RealMatrices.MODULE != null);
//		SessionScaffold.exportLatexPNG(session() == null ? new Session(RealMatrices.MODULE) : session(), 0, "view.png");
//		
//		if (true) return;
		
		new SessionScaffold(RealMatrices.MODULE) {
			
			@Override
			public final void buildSession() {
				suppose("definition_of_mean",
						$$("∀X,m,n ((n∈ℕ) → ((X∈≀M_(m,n)) → (μ_X=(1/n)X(1_(n,1)))))"));
				claimTypeOfMean();
				suppose("definition_of_replicated_mean",
						$$("∀X,m,n ((n∈ℕ) → ((X∈≀M_(m,n)) → (M_X=(μ_X)(1_(1,n)))))"));
				claimTypeOfReplicatedMean();
				suppose("definition_of_covariance",
						$$("∀X,Y,m,n ((X∈≀M_(m,n)) → ((Y∈≀M_(m,n)) → ('Var'_(X,Y)=(X-M_X)(Y-M_Y)ᵀ)))"));
				claimTypeOfCovariance();
				suppose("definition_of_variance",
						$$("∀X,m,n ((X∈≀M_(m,n)) → ('Var'_X='Var'_(X,X)))"));
				claimTypeOfVariance();
				claimSimplifiedDefinitionOfVariance();
				suppose("definition_of_class_means",
						$$("∀X,m,n,c ((c∈ℕ) → ((∀i ((i∈ℕ_c) → ((n_i∈ℕ) ∧ (X_i∈≀M_(m,n_i))))) → (∀i,j ((j∈ℕ_c) → ((U_(X,c))_(i,j)=(μ_(X_j))_(i,1))))))"));
				// TODO claim (?)
				suppose("type_of_class_means",
						$$("∀X,m,n,c ((c∈ℕ) → ((∀i ((i∈ℕ_c) → ((n_i∈ℕ) ∧ (X_i∈≀M_(m,n_i))))) → ((U_(X,c))∈≀M_(m,c))))"));
				
				suppose("definition_of_fisher_linear_discriminant",
						$$("∀w,X,m,n,c,j " + conditionFisherLinearDiscriminant("S_(wᵀX,c)=(⟨'Var'_(wᵀU_(X,c))⟩/((Σ_(j=0)^(c-1)) ⟨'Var'_(wᵀX_j)⟩))")));
				claimTypeOfFisherLinearDiscriminant();
			}
			
			private static final long serialVersionUID = 2969099922483811015L;
			
		};
	}
	
	public static final void claimSimplifiedDefinitionOfVariance() {
		claim("simplified_definition_of_variance",
				$$("∀X,m,n ((m∈ℕ) → ((n∈ℕ) → ((X∈≀M_(m,n)) → ('Var'_X=X(Xᵀ)-(1/n)((X1_(n,1))(X1_(n,1))ᵀ)))))"));
		{
			final Expression x = introduce();
			final Expression m = introduce();
			final Expression n = introduce();
			final Expression xt = transpose(x);
			final Expression invn = $(ONE, "/", n);
			final Expression onen1 = ones(n, ONE);
			final Expression one1n = ones(ONE, n);
			final Expression mux = $("μ", "_", x);
			final Expression muxt = transpose(mux);
			final Expression mx = $("M", "_", x);
			
			introduce();
			introduce();
			introduce();
			
			claimLastFact(() -> {
				bind("naturals_are_reals", m);
				autoApplyLastFact();
			});
			
			claimLastFact(() -> {
				bind("naturals_are_reals", n);
				autoApplyLastFact();
			});
			
			claimLastFact(() -> {
				bind("type_of_matrix_multiplication", x, onen1, m, n, ONE);
				autoApplyLastFact();
				autoApplyLastFact();
			});
			
			claimLastFact(() -> {
				bind("type_of_mean", x, m, n);
				autoApplyLastFact();
				autoApplyLastFact();
			});
			
			claimLastFact(() -> {
				claimLastFact(() -> {
					bind("definition_of_mean", x, m, n);
					autoApplyLastFact();
					autoApplyLastFact();
				});
				rewrite(factName(-2), factName(-1));
			});
			
			claimLastFact(() -> {
				bind("type_of_matrix_multiplication", (Expression) $(x, onen1), muxt, m, ONE, m);
				autoApplyLastFact();
				autoApplyLastFact();
			});
			
			claimLastFact(() -> {
				bind("type_of_matrix_multiplication", x, xt, m, n, m);
				autoApplyLastFact();
				autoApplyLastFact();
			});
			
			claimLastFact(() -> {
				bind("type_of_replicated_mean", x, m, n);
				autoApplyLastFact();
				autoApplyLastFact();
			});
			
			claimLastFact(() -> {
				bind("type_of_matrix_multiplication", mx, xt, m, n, m);
				autoApplyLastFact();
				autoApplyLastFact();
			});
			
			claimLastFact(() -> {
				bind("definition_of_replicated_mean", x, m, n);
				autoApplyLastFact();
				autoApplyLastFact();
			});
			
			final String definitionOfReplicatedMeans = factName(-1);
			
			claimLastFact(() -> {
				claimLastFact(() -> {
					bind("definition_of_mean", x, m, n);
					autoApplyLastFact();
					autoApplyLastFact();
				});
				rewrite(definitionOfReplicatedMeans, factName(-1));
			});
			
			final String expandedDefinitionOfReplicatedMeans = factName(-1);
			
			claimLastFact(() -> {
				bind("type_of_replicated_mean", x, m, n);
				autoApplyLastFact();
				autoApplyLastFact();
				rewrite(factName(-1), expandedDefinitionOfReplicatedMeans);
			});
			
			claimLastFact(() -> {
				claimLastFact(() -> {
					bind("definition_of_variance", x, m, n);
					autoApplyLastFact();
				});
				claimLastFact(() -> {
					new SessionExporter(session()).exportSession();
					bind("definition_of_covariance", x, x, m, n);
					autoApplyLastFact();
					autoApplyLastFact();
				});
				rewrite(factName(-2), factName(-1));
			});
			
			claimLastFact(() -> {
				claimLastFact(() -> {
					claimLastFact(() -> {
						bind("type_of_replicated_mean", x, m, n);
						autoApplyLastFact();
						autoApplyLastFact();
					});
					
					bind("transposition_of_subtraction", x, mx, m, n);
					autoApplyLastFact();
					autoApplyLastFact();
					autoApplyLastFact();
					autoApplyLastFact();
				});
				rewrite(factName(-2), factName(-1));
			});
			
			final Expression xmx = $(x, "-", mx);
			final Expression mxt = transpose(mx);
			
			claimLastFact(() -> {
				claimLastFact(() -> {
					claimLastFact(() -> {
						bind("left_distributivity_of_matrix_multiplication_over_subtraction", xmx, xt, mxt, m, n, m);
						autoApplyLastFact();
						autoApplyLastFact();
						autoApplyLastFact();
						autoApplyLastFact();
						autoApplyLastFact();
						autoApplyLastFact();
					});
					rewrite(factName(-2), factName(-1));
				});
				claimLastFact(() -> {
					claimLastFact(() -> {
						bind("right_distributivity_of_matrix_multiplication_over_subtraction", x, mx, xt, m, n, m);
						autoApplyLastFact();
						autoApplyLastFact();
						autoApplyLastFact();
						autoApplyLastFact();
						autoApplyLastFact();
						autoApplyLastFact();
					});
					rewrite(factName(-2), factName(-1));
				});
				claimLastFact(() -> {
					claimLastFact(() -> {
						bind("right_distributivity_of_matrix_multiplication_over_subtraction", x, mx, mxt, m, n, m);
						autoApplyLastFact();
						autoApplyLastFact();
						autoApplyLastFact();
						autoApplyLastFact();
						autoApplyLastFact();
						autoApplyLastFact();
					});
					rewrite(factName(-2), factName(-1));
				});
			});
			
			claimLastFact(() -> {
				rewrite(factName(-1), definitionOfReplicatedMeans, 1, 2, 3);
				claimLastFact(() -> {
					bind("definition_of_mean", x, m, n);
					autoApplyLastFact();
					autoApplyLastFact();
				});
				rewrite(factName(-2), factName(-1), 1);
			});
			
			claimLastFact(() -> {
				claimLastFact(() -> {
					bind("transposition_of_multiplication", mux, one1n, m, ONE, n);
					autoApplyLastFact();
					autoApplyLastFact();
					autoApplyLastFact();
					autoApplyLastFact();
					autoApplyLastFact();
				});
				rewrite(factName(-2), factName(-1));
				claimLastFact(() -> {
					bind("transposition_of_ones", ONE, n);
					autoApplyLastFact();
					autoApplyLastFact();
				});
				rewrite(factName(-2), factName(-1));
			});
			
			final Expression invnx = $(invn, x);
			final Expression invnx1n1 = $(invnx, onen1);
			final Expression invnx1n111n = $(invnx1n1, one1n);
			final Expression one1nn1 = $(one1n, onen1);
			
			claimLastFact(() -> {
				claimLastFact(() -> {
					bind("associativity_of_matrix_multiplication", invnx1n111n, onen1, muxt, m, n, ONE, m);
					autoApplyLastFact();
					autoApplyLastFact();
					autoApplyLastFact();
					autoApplyLastFact();
					autoApplyLastFact();
					autoApplyLastFact();
					autoApplyLastFact();
				});
				rewrite(factName(-2), factName(-1));
			});
			
			claimLastFact(() -> {
				claimLastFact(() -> {
					bind("associativity_of_matrix_multiplication", invnx1n1, one1n, onen1, m, ONE, n, ONE);
					autoApplyLastFact();
					autoApplyLastFact();
					autoApplyLastFact();
					autoApplyLastFact();
					autoApplyLastFact();
					autoApplyLastFact();
					autoApplyLastFact();
				});
				rewriteRight(factName(-2), factName(-1));
			});
			claimLastFact(() -> {
				bind("right_scalarization_in_multiplication", invnx1n1, one1nn1, m);
				autoApplyLastFact();
				autoApplyLastFact();
				claimLastFact(() -> {
					bind("type_of_matrix_multiplication", one1n, onen1, ONE, n, ONE);
					autoApplyLastFact();
					autoApplyLastFact();
				});
				apply(factName(-2), factName(-1));
				claimLastFact(() -> {
					bind("multiplication_of_ones", n);
					autoApplyLastFact();
				});
				rewrite(factName(-2), factName(-1));
				claimLastFact(() -> {
					bind("commutativity_of_matrix_scalar_multiplication", n, invnx1n1, m, ONE);
					autoApplyLastFact();
					autoApplyLastFact();
				});
				rewriteRight(factName(-2), factName(-1));
				claimLastFact(() -> {
					bind("associativity_of_matrix_scalar_multiplication", n, invnx, onen1, m, n, ONE);
					autoApplyLastFact();
					autoApplyLastFact();
					autoApplyLastFact();
				});
				rewrite(factName(-2), factName(-1));
				claimLastFact(() -> {
					bind("associativity_of_matrix_scalar_multiplication_2", n, invn, x, m, n);
					autoApplyLastFact();
					autoApplyLastFact();
					autoApplyLastFact();
				});
				rewrite(factName(-2), factName(-1));
				claimLastFact(() -> {
					bind("definition_of_inverse", n);
					autoApplyLastFact();
				});
				rewrite(factName(-2), factName(-1));
				claimLastFact(() -> {
					bind("matrix_scalar_multiplication_1", (Expression) $(x, onen1), m, ONE);
					claimLastFact(() -> {
						bind("type_of_matrix_multiplication", x, onen1, m, n, ONE);
						autoApplyLastFact();
						autoApplyLastFact();
					});
					apply(factName(-2), factName(-1));
					claimLastFact(() -> {
						bind("associativity_of_matrix_scalar_multiplication", ONE, x, onen1, m, n, ONE);
						autoApplyLastFact();
						autoApplyLastFact();
						autoApplyLastFact();
					});
					rewrite(factName(-2), factName(-1));
				});
				rewrite(factName(-2), factName(-1));
			});
			rewrite(factName(-2), factName(-1));
			
			claimLastFact(() -> {
				bind("associativity_of_matrix_multiplication", x, onen1, muxt, m, n, ONE, m);
				autoApplyLastFact();
				autoApplyLastFact();
				autoApplyLastFact();
				autoApplyLastFact();
				autoApplyLastFact();
				autoApplyLastFact();
				autoApplyLastFact();
			});
			rewrite(factName(-2), factName(-1));
			
			claimLastFact(() -> {
				bind("matrix_self_subtraction_is_0", (Expression) $($(x, onen1), muxt), m, m);
				autoApplyLastFact();
				autoApplyLastFact();
				autoApplyLastFact();
			});
			rewrite(factName(-2), factName(-1));
			
			final Expression xxtmxxt = ((Composite) lastEqualityRight()).get(0);
			
			claimLastFact(() -> {
				bind("matrix_minus_0", xxtmxxt, m, m);
				autoApplyLastFact();
				autoApplyLastFact();
				autoApplyLastFact();
			});
			rewrite(factName(-2), factName(-1));
			
			rewrite(factName(-1), expandedDefinitionOfReplicatedMeans);
			
			claimLastFact(() -> {
				claimLastFact(() -> {
					bind("associativity_of_matrix_multiplication", invnx1n1, one1n, xt, m, ONE, n, m);
					autoApplyLastFact();
					autoApplyLastFact();
					autoApplyLastFact();
					autoApplyLastFact();
					autoApplyLastFact();
					autoApplyLastFact();
					autoApplyLastFact();
				});
				rewriteRight(factName(-2), factName(-1));
				claimLastFact(() -> {
					bind("transposition_of_ones", n, ONE);
					autoApplyLastFact();
					autoApplyLastFact();
				});
				rewriteRight(factName(-2), factName(-1));
				claimLastFact(() -> {
					bind("transposition_of_multiplication", x, onen1, m, n, ONE);
					autoApplyLastFact();
					autoApplyLastFact();
					autoApplyLastFact();
					autoApplyLastFact();
					autoApplyLastFact();
				});
				rewriteRight(factName(-2), factName(-1));
				claimLastFact(() -> {
					bind("associativity_of_matrix_scalar_multiplication", invn, x, onen1, m, n, ONE);
					autoApplyLastFact();
					autoApplyLastFact();
					autoApplyLastFact();
				});
				rewriteRight(factName(-2), factName(-1));
			});
			
			claimLastFact(() -> {
				bind("associativity_of_matrix_scalar_multiplication", invn, $(x, onen1), transpose($(x, onen1)), m, ONE, m);
				autoApplyLastFact();
				autoApplyLastFact();
				autoApplyLastFact();
			});
			rewriteRight(factName(-2), factName(-1));
		}
	}
	
	public static final String conditionFisherLinearDiscriminant(final String expression) {
		return "((c∈ℕ) → ((w∈≀M_(m,1)) → ((∀i ((i∈ℕ_c) → ((n_i∈ℕ) ∧ (X_i∈≀M_(m,n_i))))) → (" + expression + "))))";
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
			
			claim(realMatrix(lastEqualityRight(), m, $("1")));
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
				$$("∀X,Y,m,n ((n∈ℕ) → ((X∈≀M_(m,n)) → ((Y∈≀M_(m,n)) → ('Var'_(X,Y)∈≀M_(m,m)))))"));
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
				$$("∀X,m,n ((n∈ℕ) → ((X∈≀M_(m,n)) → ('Var'_X∈≀M_(m,m))))"));
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
			
			claim(realMatrix(lastEqualityRight(), m, n));
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
	
	public static final void claimTypeOfFisherLinearDiscriminant() {
		claim("type_of_fisher_linear_discriminant",
				$$("∀w,X,m,n,c " + conditionFisherLinearDiscriminant("S_(wᵀX,c)∈ℝ")));
		{
			final Symbol w = introduce();
			final Symbol x = introduce();
			final Symbol m = introduce();
			final Symbol n = introduce();
			final Symbol c = introduce();
			final Symbol j = session().getCurrentModule().new Symbol("j");
			
			final Expression wt = transpose(w);
			final Expression uxc = $("U", "_", $(x, ",", c));
			final Expression wtuxc = $(wt, uxc);
			final Expression varwtuxc = $("Var", "_", wtuxc);
			final Expression xj = $(x, "_", j);
			final Expression wtxj = $(wt, xj);
			final Expression varwtxj = $("Var", "_", wtxj);
			
			introduce();
			introduce();
			introduce();
			
			final String complexConditionName = conditionName(-1);
			
			proveWithBindAndApply(real(c));
			
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
			
			claim(realMatrix(varwtuxc, $("1"), $("1")));
			{
				bind("type_of_variance", wtuxc, $("1"), c);
				autoApplyLastFact();
				autoApplyLastFact();
			}
			
			claim(real(scalarize(varwtuxc)));
			{
				bind("definition_of_matrix_scalarization", varwtuxc);
				autoApplyLastFact();
				bind("type_of_matrix_element", varwtuxc, ONE, ONE);
				autoApplyLastFact();
				bind(factName(-1), ZERO, ZERO);
				rewriteRight(factName(-1), factName(-4));
			}
			
			claimLastFact(() -> {
				bind("definition_of_fisher_linear_discriminant", w, x, m, n, c, j);
				autoApplyLastFact();
				autoApplyLastFact();
				apply(factName(-1), conditionName(-1));
			});
			
			claim(real(lastEqualityRight()));
			{
				final Composite goal = goal();
				final Composite fraction = goal.get(0);
				
				bind("type_of_division", (Expression) fraction.get(0), fraction.get(2));
				autoApplyLastFact();
				
				{
					final String moduleName = factName(-1);
					
					claimAppliedAndCondition(fact(-1));
					{
						{
							bind("type_of_sum", scalarize(varwtxj), $(c, "-", "1"), j);
							
							final String moduleName2 = factName(-1);
							
							claimAppliedAndCondition(fact(-1));
							{
								{
									introduce();
									
									bind("subtract_1_add_1", c);
									autoApplyLastFact();
									rewrite(conditionName(-1), factName(-1));
									
									bind(complexConditionName, j);
									autoApplyLastFact();
									bind(factName(-1));
									
									final Expression nj = $(n, "_", j);
									
									claim(realMatrix(wtxj, ONE, nj));
									{
										bind("type_of_matrix_multiplication", wt, xj, ONE, m, nj);
										autoApplyLastFact();
										autoApplyLastFact();
									}
									
									claim(realMatrix(varwtxj, ONE, ONE));
									{
										bind("type_of_variance", wtxj, ONE, nj);
										autoApplyLastFact();
										autoApplyLastFact();
									}
									
									bind("definition_of_matrix_scalarization", varwtxj);
									
									final String moduleName3 = factName(-1);
									
									claimAppliedAndCondition(fact(-1));
									{
										{
											bind("type_of_variance", wtxj, ONE, nj);
											autoApplyLastFact();
											autoApplyLastFact();
										}
										
										apply(moduleName3, factName(-1));
									}
									
									claim(real($(varwtxj, "_", $(ZERO, ",", ZERO))));
									{
										bind("type_of_matrix_element", varwtxj, ONE, ONE);
										autoApplyLastFact();
										bind(factName(-1), ZERO, ZERO);
									}
									
									rewriteRight(factName(-1), factName(-2));
								}
								
								apply(moduleName2, factName(-1));
							}
						}
						
						apply(moduleName, factName(-1));
					}
					
					rewriteRight(factName(-1), factName(-2));
				}
			}
		}
	}
	
	public static final Expression mean(final Object expression) {
		return $("μ", "_", expression);
	}
	
}
