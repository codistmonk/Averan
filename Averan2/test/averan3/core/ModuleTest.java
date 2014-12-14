package averan3.core;

import static org.junit.Assert.*;

import java.util.Arrays;

import net.sourceforge.aprog.tools.Tools;

import org.junit.Test;

/**
 * @author codistmonk (creation 2014-12-14)
 */
public final class ModuleTest {
	
	@Test
	public final void test1() {
		final Module module1 = newTautology();
		
		Tools.debugPrint(module1);
		
		fail("TODO");
	}
	
	public static final Module newTautology() {
		final Module result = new Module();
		
		result.setCondition(result.new Variable("P"));
		result.getFacts().add(new Module(result, result.getCondition(), Arrays.asList(result.getCondition())));
		
		return result;
	}
	
}
