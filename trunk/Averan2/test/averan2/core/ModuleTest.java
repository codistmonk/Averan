package averan2.core;

import static averan2.core.Composite.composite;
import static averan2.core.Equality.equality;
import static averan2.core.Symbol.symbol;
import static org.junit.Assert.*;
import averan2.core.Composite;
import averan2.core.Expression;
import averan2.core.Module;
import averan2.core.Substitution;
import averan2.core.Symbol;
import averan2.core.Variable;
import net.sourceforge.aprog.tools.Tools;

import org.junit.Test;

/**
 * @author codistmonk (creation 2014-12-27)
 */
public final class ModuleTest {
	
	@Test
	public final void testCanonicalize1() {
		final Module module = new Module();
		final Module canonical = new Module();
		
		module.addCondition("p1", A);
		module.addCondition("p2", B);
		module.addFact("p3", C, null);
		
		canonical.addCondition("c1", A);
		canonical.addCondition("c2", B);
		canonical.addFact("f1", C, null);
		
		assertEquals(canonical, module);
		assertEquals(module, canonical);
	}
	
	@Test
	public final void testCanonicalize2() {
		final Module module = new Module();
		final Module canonical = new Module();
		
		module.addCondition("p1", A);
		module.addFact("p2", new Module(null).addCondition("p3", B).addFact("p4", C, null), null);
		
		canonical.addCondition("c1", A);
		canonical.addCondition("c2", B);
		canonical.addFact("f1", C, null);
		
		assertEquals(canonical, module);
		assertEquals(module, canonical);
	}
	
	@Test
	public final void testProofByApply1() {
		final Module context = new Module();
		
		context.addCondition("p1", A);
		context.addCondition("p2", new Module(context).addCondition("p3", A).addFact("p4", B, null));
		
//		assertEquals(2L, context.getConditions().size());
//		assertEquals(0L, context.getFacts().size());
		assertEquals(2L, context.getFacts().size());
		
		context.new ProofByApply("p5", "p2", "p1").apply();
		
//		assertEquals(2L, context.getConditions().size());
//		assertEquals(1L, context.getFacts().size());
		assertEquals(3L, context.getFacts().size());
//		assertEquals(B, context.getFacts().get(0));
		assertEquals(B, context.getFacts().get(context.getFacts().size() - 1));
	}
	
	@Test
	public final void testProofByApply2() {
		final Module context = new Module();
		
		context.addCondition("p1_1", symbol("A1"));
		context.addCondition("p1_2", symbol("A2"));
		context.addCondition("p2", new Module(context).addCondition("p3", new Variable("X")).addFact("p4", B, null));
		
//		assertEquals(3L, context.getConditions().size());
//		assertEquals(0L, context.getFacts().size());
		assertEquals(3L, context.getFacts().size());
		
		context.new ProofByApply("p5", "p2", "p1_1").apply();
		context.new ProofByApply("p6", "p2", "p1_2").apply();
		
//		assertEquals(3L, context.getConditions().size());
//		assertEquals(2L, context.getFacts().size());
		assertEquals(5L, context.getFacts().size());
//		assertEquals(B, context.getFacts().get(0));
//		assertEquals(B, context.getFacts().get(1));
		assertEquals(B, context.getFacts().get(3));
		assertEquals(B, context.getFacts().get(4));
	}
	
	@Test
	public final void testProofBySubstitute1() {
		final Module context = new Module();
		final Composite<Expression<?>> expression = composite(A, B, B);
		final Substitution substitution = new Substitution().using(equality(B, C)).at(1);
		
		context.new ProofBySubstitute("p1", expression, substitution).apply();
		
//		assertEquals(0L, context.getConditions().size());
//		assertEquals(1L, context.getFacts().size());
		assertEquals(1L, context.getFacts().size());
		assertEquals(equality(composite(expression, substitution), composite(A, B, C)), context.getFacts().get(0));
	}
	
	@Test
	public final void testProofByRewrite1() {
		final Module context = new Module();
		
		context.addCondition("p1", equality(A, B));
		context.addCondition("p2", A);
		context.new ProofByRewrite("p3", "p2").using("p1").apply();
		
//		assertEquals(2L, context.getConditions().size());
//		assertEquals(1L, context.getFacts().size());
		assertEquals(3L, context.getFacts().size());
//		assertEquals(B, context.getFacts().get(0));
		assertEquals(B, context.getFacts().get(context.getFacts().size() - 1));
	}
	
	@Test
	public final void testProofByDeduce1() {
		final Module context = new Module();
		
		context.addCondition("p1", implication(A, B));
		context.addCondition("p2", implication(B, C));
		
		final Module subcontext = new Module(context);
		{
			subcontext.addCondition("p3", A);
			
			subcontext.new ProofByApply("p4", "p1", "p3").apply();
			subcontext.new ProofByApply("p5", "p2", "p4").apply();
		}
		
		context.new ProofByDeduce("p3", subcontext).apply();
		
//		assertEquals(2L, context.getConditions().size());
//		assertEquals(1L, context.getFacts().size());
		assertEquals(3L, context.getFacts().size());
//		assertEquals(implication(A, C), context.getFacts().get(0));
		Tools.debugPrint(context);
		assertEquals(implication(A, C), context.getFacts().get(2));
	}
	
	public static final Symbol<String> A = symbol("A");
	
	public static final Symbol<String> B = symbol("B");
	
	public static final Symbol<String> C = symbol("C");
	
	public static final Module implication(final Expression<?> condition, final Expression<?> fact) {
		return new Module().addCondition(null, condition).addFact(null, fact, null);
	}
	
}
