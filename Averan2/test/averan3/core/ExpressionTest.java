package averan3.core;

import static averan3.core.Composite.FORALL;
import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author codistmonk (creation 2015-01-05)
 */
public final class ExpressionTest {
	
	@Test
	public final void testBind1() {
		final Expression<?> expression = new Symbol<>("test");
		
		assertSame(expression, expression.accept(Variable.BIND));
	}
	
	@Test
	public final void testBind2() {
		{
			final Expression<?> expression = new Variable("test");
			
			assertSame(expression, expression.accept(Variable.BIND));
		}
		{
			final Expression<?> expression = new Variable("test");
			
			expression.equals(new Symbol<>("test"));
			
			final Expression<?> actual = expression.accept(Variable.BIND);
			
			assertNotSame(expression, actual);
			assertEquals(new Symbol<>("test"), actual);
		}
	}
	
	@Test
	public final void testBind3() {
		{
			final Expression<?> expression = new Composite<>();
			
			assertSame(expression, expression.accept(Variable.BIND));
		}
		{
			final Expression<?> expression = new Composite<>().add(new Symbol<>("test")).add(new Variable("test"));
			
			assertSame(expression, expression.accept(Variable.BIND));
		}
		{
			final Variable variable = new Variable("test");
			final Expression<?> expression = new Composite<>().add(new Symbol<>("test")).add(variable);
			
			variable.equals(new Symbol<>("test"));
			
			final Expression<?> actual = expression.accept(Variable.BIND);
			
			assertNotSame(expression, actual);
			assertEquals(new Composite<>().add(new Symbol<>("test")).add(new Symbol<>("test")), actual);
		}
		{
			final Variable variable = new Variable("test");
			final Expression<?> expression = new Composite<>().add(new Composite<>().add(FORALL).add(variable)).add(variable);
			
			variable.equals(new Symbol<>("test"));
			
			final Expression<?> actual = expression.accept(Variable.BIND);
			
			assertNotSame(expression, actual);
			assertEquals(new Symbol<>("test"), actual);
		}
	}
	
}
