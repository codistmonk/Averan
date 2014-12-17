package averan.modules;

import static averan.core.ExpressionTools.*;
import static averan.core.SessionTools.*;
import static averan.io.ExpressionParser.$$;
import static averan.modules.Standard.*;
import static org.junit.Assert.*;
import averan.core.Module;
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
						$$("∀X,m,n ((X∈≀M_(m,n)) → (M_X=(1/n)X(1_(n,1))(1_(1,n))))"));
			}
			
			private static final long serialVersionUID = 2969099922483811015L;
			
		};
	}
	
}
