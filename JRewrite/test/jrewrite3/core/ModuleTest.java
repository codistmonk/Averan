package jrewrite3.core;

import static jrewrite3.core.ExpressionTools.$;
import static jrewrite3.core.ExpressionTools.rule;
import static jrewrite3.core.Module.EQUAL;
import static jrewrite3.core.Module.ROOT;
import static jrewrite3.core.Module.equality;
import static jrewrite3.modules.Standard.IDENTITY;
import static net.sourceforge.aprog.tools.Tools.array;
import static org.junit.Assert.*;
import jrewrite3.core.Expression;
import jrewrite3.core.Module;
import jrewrite3.core.Module.Symbol;
import jrewrite3.modules.Standard;

import org.junit.Test;

/**
 * @author codistmonk (creation 2014-08-02)
 */
public final class ModuleTest {
	
	@Test
	public final void testParameters() {
		final Module module1 = new Module(null);
		
		assertEquals(0, module1.getParameters().size());
		
		module1.parameter("x");
		
		assertEquals(1, module1.getParameters().size());
		
		module1.parameter("x");
		
		assertEquals(1, module1.getParameters().size());
		
		module1.parameter("y");
		
		assertEquals(2, module1.getParameters().size());
	}
	
	@Test
	public final void testEquals() {
		final Module module1 = new Module(null);
		
		assertEquals(module1, module1);
		
		final Module module2 = new Module(null);
		
		assertEquals(module1, module2);
		
		{
			final Symbol x = module1.parameter("x");
			final Symbol y = module1.parameter("y");
			module1.new Admit(Module.equality(x, y)).execute();
		}
		
		assertNotEquals(module1, module2);
		
		{
			final Symbol y = module2.parameter("y");
			final Symbol x = module2.parameter("x");
			module2.new Admit(Module.equality(y, x)).execute();
		}
		
		assertEquals(module1, module2);
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
	public final void testRecall() {
		final Module module1 = new Module(null);
		
		module1.new Suppose("ifA", $("A")).execute();
		module1.new Recall("thenA", module1, "ifA").execute();
		
		assertEquals($("A"), module1.getProposition("thenA"));
	}
	
	@Test
	public final void testClaim() {
		final Module module1 = new Module(null);
		final Symbol x = module1.parameter("x");
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
