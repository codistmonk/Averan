package averan3.core;

import static org.junit.Assert.*;

import java.util.Arrays;

import averan3.core.Expression.Module;
import net.sourceforge.aprog.tools.Tools;

import org.junit.Test;

/**
 * @author codistmonk (creation 2014-12-14)
 */
public final class ExpressionTest {
	
	@Test
	public final void test1() {
		final Module module1 = new Module();
		
		module1.setCondition(module1.new Variable("X"));
		module1.getFacts().add(new Module(module1, module1.getCondition(), Arrays.asList(module1.getCondition())));
		
		Tools.debugPrint(module1);
		
		fail("TODO");
	}
	
}
