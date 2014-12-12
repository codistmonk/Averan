package averan.modules;

import static averan.core.ExpressionTools.*;
import static averan.core.SessionTools.*;
import static averan.io.ExpressionParser.$$;
import static averan.modules.Standard.*;
import static org.junit.Assert.*;

import averan.core.Module;

import org.junit.Test;

/**
 * @author codistmonk (creation 2014-12-11)
 */
public class RealMatricesTest {
	
	@Test
	public final void test1() {
		pushNewSession(new Module(RealMatrices.MODULE));
		
		try {
			
		} finally {
			popSession();
		}
	}
	
}
