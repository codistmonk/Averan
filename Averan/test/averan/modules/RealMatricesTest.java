package averan.modules;

import static averan.core.ExpressionTools.*;
import static averan.core.SessionTools.*;
import static averan.io.ExpressionParser.$$;
import static averan.modules.Reals.*;
import static averan.modules.RealMatrices.*;
import static averan.modules.Standard.*;
import static org.junit.Assert.*;

import averan.core.Composite;
import averan.core.Expression;
import averan.core.Module;
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
				suppose("definition_of_replicated_mean",
						$$("∀X,m,n ((n∈ℕ) → ((X∈≀M_(m,n)) → (M_X=(1/n)X(1_(n,1))(1_(1,n)))))"));
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
						bind("type_of_ones", (Expression) $("1"), n);
						bind("type_of_ones", n, $("1"));
						final Expression invN = inverse(n);
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
						
						final Expression invNx = $(invN, x);
						final Expression invNx1n1 = $(invNx, ones(n, "1"));
						
						claim(realMatrix(invNx1n1, m, $("1")));
						{
							bind("type_of_matrix_multiplication", (Expression) invNx, ones(n, "1"), m, n, $("1"));
							autoApplyLastFact();
							autoApplyLastFact();
						}
						
						bind("type_of_matrix_multiplication", invNx1n1, ones("1", n), m, $("1"), n);
						autoApplyLastFact();
						autoApplyLastFact();
					}
					
					rewriteRight(factName(-1), factName(-2));
				}
				suppose("definition_of_covariance",
						$$("∀X,Y,m,n,o ((X∈≀M_(m,n)) → ((Y∈≀M_(m,o)) → (Var_(X,Y)=(X-M_X)ᵀ(Y-M_Y))))"));
				// TODO type of covariance
				suppose("definition_of_variance",
						$$("∀X,m,n ((X∈≀M_(m,n)) → (Var_X=Var_(X,X)))"));
			}
			
			private static final long serialVersionUID = 2969099922483811015L;
			
		};
	}
	
	public static final Expression ones(final Object m, final Object n) {
		return $("1", "_", $(m, ",", n));
	}
	
}
