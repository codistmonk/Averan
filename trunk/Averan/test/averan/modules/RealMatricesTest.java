package averan.modules;

import static averan.core.ExpressionTools.*;
import static averan.core.Session.breakSession;
import static averan.core.SessionTools.*;
import static averan.io.ExpressionParser.$$;
import static averan.modules.Reals.*;
import static averan.modules.RealMatrices.*;
import static averan.modules.Standard.*;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import averan.core.Composite;
import averan.core.Expression;
import averan.core.Module;
import averan.core.Pattern.Any;
import averan.core.Visitor;
import averan.core.Module.Symbol;
import averan.core.Session;
import averan.io.SessionExporter;
import averan.io.SessionScaffold;
import net.sourceforge.aprog.tools.Pair;
import net.sourceforge.aprog.tools.Tools;

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
				suppose("definition_of_ones",
						$$("∀m,n,i,j ((1_(m,n))_(i,j)=1)"));
				suppose("type_of_ones",
						$$("∀m,n (1_(m,n)∈≀M_(m,n))"));
				suppose("definition_of_mean",
						$$("∀X,m,n ((n∈ℕ) → ((X∈≀M_(m,n)) → (μ_X=(1/n)X(1_(n,1)))))"));
				breakSession();
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
				suppose("definition_of_class_means",
						$$("∀X,m,n,c ((c∈ℕ) → ((∀i ((i∈ℕ_c) → ((n_i∈ℕ) ∧ (X_i∈≀M_(m,n_i))))) → (∀i,j ((j∈ℕ_c) → ((U_(X,c))_(i,j)=(μ_(X_j))_(i,1))))))"));
				// TODO claim (?)
				suppose("type_of_class_means",
						$$("∀X,m,n,c ((c∈ℕ) → ((∀i ((i∈ℕ_c) → ((n_i∈ℕ) ∧ (X_i∈≀M_(m,n_i))))) → ((U_(X,c))∈≀M_(m,c))))"));
				
				suppose("definition_of_fisher_linear_discriminant",
						$$("∀w,X,m,n,c,j " + conditionFisherLinearDiscriminant("S_(wᵀX,c)=(⟨'Var'_(wᵀU_(X,c))⟩/((Σ_(j=0)^(c-1)) ⟨'Var'_(wᵀX_j)⟩))")));
				claimTypeOfFisherLinearDiscriminant();
				
				
				claimTranspositionOfOnes();
				claimMultiplicationOfOnes();
				claim("scalarization_in_multiplication",
						$$("∀X,Y,n ((X∈≀M_(1,1)) → ((Y∈≀M_(1,n)) → (XY=⟨X⟩Y)))"));
				{
					final Symbol x = introduce();
					final Symbol y = introduce();
					final Symbol n = introduce();
					
					introduce();
					introduce();
					
					final Expression xy = $(x, y);
					final Expression sxy = $(scalarize(x), y);
					
					claimLastFact(() -> {
						claimLastFact(() -> {
							bind("definition_of_matrix_equality", xy, sxy, ONE, n);
							claimLastFact(() -> {
								bind("type_of_matrix_multiplication", x, y, ONE, ONE, n);
								autoApplyLastFact();
								autoApplyLastFact();
							});
							apply(factName(-2), factName(-1));
						});
						autoApplyLastFact();
					});
					claim(lastEqualityRight());
					{
						final Symbol i = introduce();
						final Symbol j = introduce();
						final Symbol k = session().getCurrentModule().new Symbol("k");
						
						claimLastFact(() -> {
							bind("definition_of_matrix_multiplication", x, y, ONE, ONE, n);
							autoApplyLastFact();
							autoApplyLastFact();
							bind(factName(-1), i, j, k);
							claimLastFact(() -> {
								bind("definition_of_opposite", ONE);
								autoApplyLastFact();
								claimLastFact(() -> {
									bind("definition_of_subtraction", ONE, ONE);
									autoApplyLastFact();
									autoApplyLastFact();
								});
								rewrite(factName(-1), factName(-2));
							});
							rewrite(factName(-2), factName(-1));
							
							bind("definition_of_sum_0", (Expression) $($(x, "_", $(i, ",", k)), $(y, "_", $(k, ",", j))), k);
							rewrite(factName(-2), factName(-1));
							substitute(lastEqualityRight());
							rewrite(factName(-2), factName(-1));
						});
						
						claimLastFact(() -> {
							bind("definition_of_matrix_scalar_multiplication", scalarize(x), y, ONE, n);
							autoApplyLastFact();
							autoApplyLastFact();
							bind(factName(-1), i, j);
						});
					}
					
					breakSession();
				}
				
				{
					final Expression m = $("m");
					final Expression n = $("n");
					final Expression x = $("X");
					
					admit(natural(n));
					admit(realMatrix(x, m, n));
					
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
					
					final Expression mx = $("M", "_", x);
					
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
						});
						rewrite(factName(-2), factName(-1));
					});
					
					final Expression xt = transpose(x);
					final Expression xmx = $(x, "-", mx);
					final Expression mxt = transpose(mx);
					
					claimLastFact(() -> {
						claimLastFact(() -> {
							claimLastFact(() -> {
								bind("left_distributivity_of_matrix_multiplication_over_subtraction", xmx, xt, mxt, m, n, m);
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
							});
							rewrite(factName(-2), factName(-1));
						});
						claimLastFact(() -> {
							claimLastFact(() -> {
								bind("right_distributivity_of_matrix_multiplication_over_subtraction", x, mx, mxt, m, n, m);
								autoApplyLastFact();
								autoApplyLastFact();
								autoApplyLastFact();
							});
							rewrite(factName(-2), factName(-1));
						});
					});
					
					claimLastFact(() -> {
						claimLastFact(() -> {
							claimLastFact(() -> {
								bind("definition_of_replicated_mean", x, m, n);
								autoApplyLastFact();
								autoApplyLastFact();
							});
							claimLastFact(() -> {
								bind("definition_of_mean", x, m, n);
								autoApplyLastFact();
								autoApplyLastFact();
							});
							rewrite(factName(-2), factName(-1));
						});
						rewrite(factName(-2), factName(-1), 1, 2, 3);
					});
					
					final Expression onen1 = ones(n, ONE);
					final Expression one1n = ones(ONE, n);
					
					claimLastFact(() -> {
						claimLastFact(() -> {
							final Expression invn = $(ONE, "/", n);
							final Expression invnx = $(invn, x);
							final Expression invnx1n1 = $(invnx, onen1);
							
							bind("transposition_of_multiplication", invnx1n1, one1n, m, ONE, n);
							{
								final String moduleName = factName(-1);
								
								claimAppliedAndCondition(fact(-1));
								{
									{
										bind("type_of_matrix_multiplication", invnx, onen1, m, n, ONE);
										autoApplyLastFact();
										autoApplyLastFact();
									}
									
									apply(moduleName, factName(-1));
								}
							}
							autoApplyLastFact();
						});
						rewrite(factName(-2), factName(-1));
					});
					
					claimLastFact(() -> {
						bind("transposition_of_ones", ONE, n);
						rewrite(factName(-2), factName(-1));
					});
					
					// TODO
				}
			}
			
			private static final long serialVersionUID = 2969099922483811015L;
			
		};
	}
	
	public static final void claimMultiplicationOfOnes() {
		claim("multiplication_of_ones",
				$$("∀n (n∈ℕ → (⟨(1_(1,n))(1_(n,1))⟩=n))"));
		{
			final Symbol n = introduce();
			final Expression one1n = ones(ONE, n);
			final Expression onen1 = ones(n, ONE);
			final Expression one1nonen1 = $(one1n, onen1);
			final Symbol k = session().getCurrentModule().new Symbol("k");
			
			introduce();
			
			claimLastFact(() -> {
				bind("definition_of_matrix_scalarization", one1nonen1);
				
				{
					final String moduleName = factName(-1);
					
					claimAppliedAndCondition(fact(-1));
					{
						{
							bind("type_of_matrix_multiplication", one1n, onen1, ONE, n, ONE);
							autoApplyLastFact();
							autoApplyLastFact();
						}
						
						apply(moduleName, factName(-1));
					}
				}
			});
			
			claimLastFact(() -> {
				claimLastFact(() -> {
					bind("definition_of_matrix_multiplication", one1n, onen1, ONE, n, ONE);
					autoApplyLastFact();
					autoApplyLastFact();
					bind(factName(-1), ZERO, ZERO, k);
				});
				
				rewrite(factName(-2), factName(-1));
			});
			
			claimLastFact(() -> {
				bind("definition_of_ones", ONE, n, ZERO, k);
				rewrite(factName(-2), factName(-1));
				bind("definition_of_ones", n, ONE, k, ZERO);
				rewrite(factName(-2), factName(-1));
				claimLastFact(() -> {
					bind("definition_of_1", ONE);
					autoApplyLastFact();
				});
				rewrite(factName(-2), factName(-1));
			});
			
			claimLastFact(() -> {
				final Expression p = equality(sum(k, n, ONE), $(n, "+", ONE));
				bind("mathematical_induction", p, n);
				{
					final String moduleName = factName(-1);
					
					claimAppliedAndCondition(fact(-1));
					{
						{
							substitute(goal());
							claim(lastEqualityRight());
							{
								bind("definition_of_sum_0", ONE, k);
								substitute(lastEqualityRight());
								rewrite(factName(-2), factName(-1));
								
								claimLastFact(() -> {
									claimLastFact(() -> {
										bind("definition_of_0", ONE);
										autoApplyLastFact();
									});
									claimLastFact(() -> {
										bind("commutativity_of_addition", ONE, ZERO);
										autoApplyLastFact();
										autoApplyLastFact();
									});
									rewrite(factName(-2), factName(-1));
								});
								
								rewriteRight(factName(-2), factName(-1), 1);
							}
							rewriteRight(factName(-1), factName(-2));
						}
						apply(moduleName, factName(-1));
					}
				}
				{
					final String moduleName = factName(-1);
					
					claimAppliedAndCondition(fact(-1));
					{
						{
							introduce();
							introduce();
							
							final String inductionHypothesis = conditionName(-1);
							
							substitute(goal());
							claim(lastEqualityRight());
							{
								bind("definition_of_sum_n", ONE, $(n, "+", ONE), k);
								claimLastFact(() -> {
									bind("add_1_subtract_1", n);
									autoApplyLastFact();
								});
								rewrite(factName(-2), factName(-1));
							}
							rewrite(factName(-1), inductionHypothesis);
							substitute(substitution(ONE, equality(k, $(n, "+", ONE))));
							rewrite(factName(-2), factName(-1));
						}
						rewriteRight(factName(-1), factName(-2));
					}
					apply(moduleName, factName(-1));
				}
			});
			
			claimLastFact(() -> {
				bind(factName(-1), (Expression) $(n, "-", ONE));
				substitute(fact(-1));
				rewrite(factName(-2), factName(-1));
				claimLastFact(() -> {
					bind("subtract_1_add_1", n);
					autoApplyLastFact();
				});
				rewrite(factName(-2), factName(-1));
			});
			
			rewrite(factName(-3), factName(-1));
		}
	}

	public static final void claimTranspositionOfOnes() {
		claim("transposition_of_ones",
				$$("∀m,n ((1_(m,n))ᵀ=(1_(n,m)))"));
		{
			final Symbol m = introduce();
			final Symbol n = introduce();
			final Expression onemn = ones(m, n);
			final Expression onemnt = transpose(onemn);
			final Expression onenm = ones(n, m);
			
			claimLastFact(() -> {
				bind("definition_of_matrix_equality", onemnt, onenm, n, m);
				autoApplyLastFact();
				autoApplyLastFact();
			});
			
			claim(lastEqualityRight());
			{
				final Symbol i = introduce();
				final Symbol j = introduce();
				
				claimLastFact(() -> {
					claimLastFact(() -> {
						bind("definition_of_transposition", onemn, m, n);
						autoApplyLastFact();
						bind(factName(-1), i, j);
					});
					
					bind("definition_of_ones", m, n, j, i);
					rewrite(factName(-2), factName(-1));
				});
				
				bind("definition_of_ones", n, m, i, j);
				
				rewriteRight(factName(-2), factName(-1), 1);
			}
			
			rewriteRight(factName(-1), factName(-2));
		}
	}
	
	public static final String conditionFisherLinearDiscriminant(final String expression) {
		return "((c∈ℕ) → ((w∈≀M_(m,1)) → ((∀i ((i∈ℕ_c) → ((n_i∈ℕ) ∧ (X_i∈≀M_(m,n_i))))) → (" + expression + "))))";
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
			
			bind("definition_of_fisher_linear_discriminant", w, x, m, n, c, j);
			autoApplyLastFact();
			autoApplyLastFact();
			apply(factName(-1), conditionName(-1));
			
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
	
	public static final Expression ones(final Object m, final Object n) {
		return $("1", "_", $(m, ",", n));
	}
	
	public static final Expression mean(final Object expression) {
		return $("μ", "_", expression);
	}
	
	public static final Expression scalarize(final Object expression) {
		return $("⟨", expression, "⟩");
	}
	
	public static final Composite sum(final Object i, final Object n, final Object x) {
		return $($($("Σ", "_", $(i, "=", ZERO)), "^", n), x);
	}
	
}
