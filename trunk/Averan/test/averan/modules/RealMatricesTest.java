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
//				suppose("type_of_ones",
//						$$(""));
			}
			
			private static final long serialVersionUID = 2969099922483811015L;
			
		};
	}
	
}
