package averan4.core;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author codistmonk (creation 2014-12-27)
 */
public final class ModuleTest {
	
	@Test
	public final void testCanonicalize1() {
		final Module module = new Module(null);
		final Module canonical = new Module(null);
		
		module.addCondition("p1", new Symbol<>("A"));
		module.addCondition("p2", new Symbol<>("B"));
		module.addFact("p3", new Symbol<>("C"), null);
		
		canonical.addCondition("c1", new Symbol<>("A"));
		canonical.addCondition("c2", new Symbol<>("B"));
		canonical.addFact("f1", new Symbol<>("C"), null);
		
		assertEquals(canonical, module);
		assertEquals(module, canonical);
	}
	
	@Test
	public final void testCanonicalize2() {
		final Module module = new Module(null);
		final Module canonical = new Module(null);
		
		module.addCondition("p1", new Symbol<>("A"));
		module.addFact("p2", new Module(null).addCondition("p3", new Symbol<>("B")).addFact("p4", new Symbol<>("C"), null), null);
		
		canonical.addCondition("c1", new Symbol<>("A"));
		canonical.addCondition("c2", new Symbol<>("B"));
		canonical.addFact("f1", new Symbol<>("C"), null);
		
		assertEquals(canonical, module);
		assertEquals(module, canonical);
	}
	
}
