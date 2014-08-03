package jrewrite3;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static jrewrite3.Module.EQUAL;
import static jrewrite3.Module.IDENTITY;
import static jrewrite3.Module.ROOT;
import static jrewrite3.Module.equality;
import static net.sourceforge.aprog.tools.Tools.array;
import static org.junit.Assert.*;

import java.util.function.Function;

import jrewrite3.Module.Symbol;

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
		
		module2.new Bind(ROOT, IDENTITY).bind(x).execute();
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
	public final void testBind() {
		final Module module1 = new Module(ROOT);
		
		module1.new Bind("2=2", module1, "identity").bind($("2")).execute();
		
		assertEquals($("2", EQUAL, "2"), module1.getProposition("2=2"));
	}
	
	@Test
	public final void testApply() {
		final Module module1 = new Module(null);
		
		module1.new Suppose("condition", $("A")).execute();
		module1.new Suppose("rule", rule("A", "B")).execute();
		module1.new Apply("fact", module1, "rule").apply(module1, "condition").execute();
		
		assertEquals($("B"), module1.getProposition("fact"));
	}
	
	private static final Module testModule = new Module(null);
	
	@SuppressWarnings("unchecked")
	static final <E extends Expression> E $(final Object... objects) {
		final Function<Object, Expression> mapper = object -> object instanceof Expression
				? (Expression) object : testModule.new Symbol(object.toString());
		
		if (objects.length == 1) {
			return (E) mapper.apply(objects[0]);
		}
		
		return (E) new Composite(stream(objects).map(mapper).collect(toList()));
	}
	
	private static final Module rule(final Object condition, final Object fact) {
		final Module result = new Module(null);
		
		result.new Suppose($(condition)).execute();
		result.new Admit($(fact)).execute();
		
		return result;
	}
	
}
