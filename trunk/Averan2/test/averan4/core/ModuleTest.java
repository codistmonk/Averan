package averan4.core;

import static org.junit.Assert.*;
import net.sourceforge.aprog.tools.Tools;

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
	
	@Test
	public final void testProofByApply1() {
		final Module context = new Module(null);
		
		context.addCondition("p1", new Symbol<>("A"));
		context.addCondition("p2", new Module(context).addCondition("p3", new Symbol<>("A")).addFact("p4", new Symbol<>("B"), null));
		
		assertEquals(2L, context.getConditions().size());
		assertEquals(0L, context.getFacts().size());
		
		context.new ProofByApply("p5", "p2", "p1").apply();
		
		assertEquals(2L, context.getConditions().size());
		assertEquals(1L, context.getFacts().size());
		assertEquals(new Symbol<>("B"), context.getFacts().get(0));
	}
	
	@Test
	public final void testProofByApply2() {
		final Module context = new Module(null);
		
		context.addCondition("p1_1", new Symbol<>("A1"));
		context.addCondition("p1_2", new Symbol<>("A2"));
		context.addCondition("p2", new Module(context).addCondition("p3", new Variable("X")).addFact("p4", new Symbol<>("B"), null));
		
		assertEquals(3L, context.getConditions().size());
		assertEquals(0L, context.getFacts().size());
		
		context.new ProofByApply("p5", "p2", "p1_1").apply();
		context.new ProofByApply("p6", "p2", "p1_2").apply();
		
		assertEquals(3L, context.getConditions().size());
		assertEquals(2L, context.getFacts().size());
		assertEquals(new Symbol<>("B"), context.getFacts().get(0));
		assertEquals(new Symbol<>("B"), context.getFacts().get(1));
	}
	
}
