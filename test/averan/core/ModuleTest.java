package averan.core;

import static averan.core.ExpressionTools.$;
import static averan.core.ExpressionTools.rule;
import static averan.core.Module.EQUAL;
import static averan.core.Module.ROOT;
import static averan.core.Module.equality;
import static averan.modules.Standard.IDENTITY;
import static multij.tools.Tools.array;
import static org.junit.Assert.*;
import averan.core.Composite;
import averan.core.Expression;
import averan.core.Module;
import averan.core.Module.Symbol;
import averan.modules.Standard;

import org.junit.Test;

/**
 * @author codistmonk (creation 2014-08-02)
 */
public final class ModuleTest {
	
	@Test
	public final void testParametrize() {
		final Module module1 = new Module(null);
		
		assertEquals(0, module1.getParameters().size());
		
		module1.new Parametrize("x").executeAndGet();
		
		assertEquals(1, module1.getParameters().size());
		
		module1.getParameter("x");
		
		assertEquals(1, module1.getParameters().size());
		
		module1.new Parametrize("y").executeAndGet();
		
		assertEquals(2, module1.getParameters().size());
	}
	
	@Test
	public final void testEquals() {
		final Module module1 = new Module(null);
		
		assertEquals(module1, module1);
		
		final Module module2 = new Module(null);
		
		assertEquals(module1, module2);
		
		{
			final Symbol x = module1.new Parametrize("x").executeAndGet();
			final Symbol y = module1.new Parametrize("y").executeAndGet();
			module1.new Admit(Module.equality(x, y)).execute();
		}
		
		assertNotEquals(module1, module2);
		
		{
			final Symbol y = module2.new Parametrize("y").executeAndGet();
			final Symbol x = module2.new Parametrize("x").executeAndGet();
			module2.new Admit(Module.equality(y, x)).execute();
		}
		
		assertEquals(module1, module2);
	}
	
	@Test
	public final void testImplies1() {
		final Module module1 = new Module(null);
		final Module module2 = new Module(null);
		
		assertTrue(module1.implies(module2));
		
		final Symbol x = $("x");
		
		module1.new Admit(x).execute();
		
		assertTrue(module1.implies(x));
		assertFalse(module2.implies(x));
		assertTrue(module1.implies(module2));
		assertFalse(module2.implies(module1));
		
		final Symbol y = $("y");
		
		module1.new Admit(y).execute();
		module2.new Admit(y).execute();
		
		assertTrue(module1.implies(y));
		assertTrue(module2.implies(y));
		assertTrue(module1.implies(module2));
		assertFalse(module2.implies(module1));
		
		final Symbol z = $("z");
		
		module1.new Suppose(z).execute();
		
		assertFalse(module1.implies(module2));
		assertFalse(module2.implies(module1));
		
		module2.new Suppose(z).execute();
		
		assertTrue(module1.implies(module2));
		assertFalse(module2.implies(module1));
	}
	
	@Test
	public final void testImplies2() {
		final Module module1 = new Module(null);
		final Module module2 = new Module(null);
		final Symbol z = $("z");
		
		module1.new Admit(module1.new Parametrize("x").executeAndGet()).execute();
		module1.new Admit(z).execute();
		module2.new Admit(module2.new Symbol("y")).execute();
		module2.new Admit(z).execute();
		
		assertFalse(module1.implies(module2));
		assertFalse(module2.implies(module1));
		
		module2.getFacts().clear();
		module2.getFactIndices().clear();
		module2.new Admit(module2.new Parametrize("y").executeAndGet()).execute();
		
		assertTrue(module1.implies(module2));
		assertFalse(module2.implies(module1));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public final void testImplies3() {
		final Module module = new Module(null);
		final Symbol x = module.new Symbol("x");
		
		module.new Admit(x).execute();
		module.new Parametrize(x.toString()).execute();
		
		fail();
	}
	
	@Test
	public final void testSuppose() {
		final Module module1 = new Module(null);
		final Expression condition = $();
		
		assertArrayEquals(array(), module1.getConditions().toArray());
		
		module1.new Suppose(condition).execute();
		
		assertArrayEquals(array(condition), module1.getConditions().toArray());
		
		module1.new Suppose("condition", condition).execute();
		
		assertArrayEquals(array(condition, condition), module1.getConditions().toArray());
		assertEquals(condition, module1.getProposition("condition"));
	}
	
	@Test
	public final void testAdmit() {
		final Module module1 = new Module(null);
		final Expression fact = $();
		
		assertArrayEquals(array(), module1.getConditions().toArray());
		
		module1.new Admit(fact).execute();
		
		assertArrayEquals(array(fact), module1.getFacts().toArray());
		
		module1.new Admit("fact", fact).execute();
		
		assertArrayEquals(array(fact, fact), module1.getFacts().toArray());
		assertEquals(fact, module1.getProposition("fact"));
	}
	
	@Test
	public final void testClaim() {
		final Module module1 = new Module(null);
		final Symbol x = module1.new Parametrize("x").executeAndGet();
		final Module module2 = new Module(ROOT);
		
		module2.new Bind(Standard.MODULE, IDENTITY).bind(x).execute();
		module1.new Claim("x=x", equality(x, x), module2).execute();
		
		assertEquals($(x, EQUAL, x), module1.getProposition("x=x"));
	}
	
	@Test
	public final void testRewrite() {
		final Module module1 = new Module(null);
		
		module1.new Suppose("x=y", $("x", EQUAL, "y")).execute();
		module1.new Suppose("y=z", $("y", EQUAL, "z")).execute();
		module1.new Rewrite("x=z", module1, "x=y", module1, "y=z").execute();
		
		assertEquals($("x", EQUAL, "z"), module1.getProposition("x=z"));
	}
	
	@Test
	public final void testSubstitute() {
		final Module module1 = new Module(null);
		final Composite subsitution = $($("x", "y"), $($("x", "=", "4"), $("y", "=", "2")));
		
		module1.new Substitute("x2=42", subsitution).execute();
		
		assertEquals($(subsitution, "=", $("4","2")), module1.getProposition("x2=42"));
	}
	
	@Test
	public final void testBind() {
		final Module module1 = new Module(Standard.MODULE);
		
		module1.new Bind("2=2", module1, IDENTITY).bind($("2")).execute();
		
		assertEquals($("2", EQUAL, "2"), module1.getProposition("2=2"));
	}
	
	@Test
	public final void testApply() {
		final Module module1 = new Module(null);
		
		module1.new Suppose("condition", $("A")).execute();
		module1.new Suppose("rule", rule("A", "B")).execute();
		module1.new Apply("fact", module1, "rule", module1, "condition").execute();
		
		assertEquals($("B"), module1.getProposition("fact"));
	}
	
}
