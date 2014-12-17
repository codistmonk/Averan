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
						$$("∀X,Y,m,n,o ((X∈≀M_(m,n)) → ((Y∈≀M_(m,o)) → (Var_(X,Y)=(X-M_X)ᵀ(Y-M_Y))))"));
				claimTypeOfCovariance();
				suppose("definition_of_variance",
						$$("∀X,m,n ((X∈≀M_(m,n)) → (Var_X=Var_(X,X)))"));
				suppose("definition_of_class_means",
						$("TODO"));
				suppose("definition_of_separability",
						$$("∀w,X,m,n,c,j,k ((w∈≀M_(m,1)) → ((∀i (X_i∈≀M_(m,n_i))) → (S_(wᵀX)=(⟨(Σ_(j=0)^(c-1)) ((Σ_(k=(j+1))^(c-1)) (Var_(wᵀX_j,wᵀX_k)))⟩/⟨(Σ_(j=0)^(c-1)) (Var_(wᵀX_j))⟩))))"));
			}
			
			private static final long serialVersionUID = 2969099922483811015L;
			
		};
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
				$$("∀X,Y,m,n,o ((n∈ℕ) → ((o∈ℕ) → ((X∈≀M_(m,n)) → ((Y∈≀M_(m,o)) → (Var_(X,Y)∈≀M_(n,o))))))"));
		{
			final Symbol x = introduce();
			final Symbol y = introduce();
			final Symbol m = introduce();
			final Symbol n = introduce();
			final Symbol o = introduce();
			
			introduce();
			introduce();
			introduce();
			introduce();
			
			bind("definition_of_covariance", x, y, m, n, o);
			autoApplyLastFact();
			autoApplyLastFact();
			
			final Expression xmx = $(x, "-", $("M", "_", x));
			final Composite xmxt = transpose(xmx);
			final Expression ymy = $(y, "-", $("M", "_", y));
			
			claim(realMatrix($(xmxt, ymy), n, o));
			{
				claim(realMatrix(xmxt, n, m));
				{
					proveUsingBindAndApply(realMatrix(xmx, m, n));
					bind("type_of_transposition", xmx, m, n);
					autoApplyLastFact();
				}
				
				proveUsingBindAndApply(realMatrix(ymy, m, o));
				
				bind("type_of_matrix_multiplication", xmxt, ymy, n, m, o);
				autoApplyLastFact();
				autoApplyLastFact();
			}
			
			rewriteRight(factName(-1), factName(-2));
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
