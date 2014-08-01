package jrewrite3;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static jrewrite3.Module.EQUAL;
import static org.junit.Assert.*;

import java.util.function.Function;

import jrewrite3.Module.Variable;

import org.junit.Test;

/**
 * @author codistmonk (creation 2014)
 *
 */
public final class RewriterTest {
	
	@Test
	public final void test1() {
		final Variable variable = $("x");
		final Variable value = $("42");
		final Rewriter rewriter = new Rewriter().rewrite(variable, value);
		
		assertEquals(value, variable.accept(rewriter));
	}
	
	@Test
	public final void test2() {
		final Expression definitionOf2 = $("2", EQUAL, $("1", "+", "1"));
		final Rewriter rewriter = new Rewriter().rewrite($("1", "+", "1"), $("2"));
		
		assertEquals($("2", EQUAL, "2"), definitionOf2.accept(rewriter));
	}
	
	private static final Module EMPTY_MODULE = new Module(null);
	
	@SuppressWarnings("unchecked")
	private static final <E extends Expression> E $(final Object... objects) {
		final Function<Object, Expression> mapper = o -> o instanceof Expression
				? (Expression) o : EMPTY_MODULE.new Variable(o.toString());
		
		if (objects.length == 1) {
			return (E) mapper.apply(objects[0]);
		}
		
		return (E) new Composite(stream(objects).map(mapper).collect(toList()));
	}
	
}
