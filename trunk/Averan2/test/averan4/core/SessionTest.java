package averan4.core;

import static averan4.core.Composite.*;
import static averan4.core.Session.*;

import java.util.Arrays;

import org.junit.Test;

/**
 * @author codistmonk (creation 2015-01-06)
 */
public final class SessionTest {
	
	@Test
	public final void test1() {
		begin();
		
		try {
			deduce("averan.deductions.Standard");
			{
				deduce("identity");
				{
					final Variable x = introduce("x");
					
					substitute($$(x, $(), $()));
					rewrite(name(-1), name(-1));
					conclude();
				}
				
				deduce("symmetry_of_equality");
				{
					final Variable x = introduce("x");
					final Variable y = introduce("y");
					
					suppose($(x, EQUALS, y));
					bind("identity", x);
					rewrite(name(-1), name(-2), 0);
					conclude();
				}
				
				deduce("recall");
				{
					final Variable p = introduce("P");
					
					suppose(p);
					bind("identity", p);
					rewrite(name(-2), name(-1));
					conclude();
				}
				
				{
					final Variable $E = new Variable("E");
					final Variable $F = new Variable("F");
					final Variable $X = new Variable("X");
					final Variable $Y = new Variable("Y");
					
					suppose("bind1",
							$(forall($E, $X, $Y, $F),
									$($(forall($X), $E),
											IMPLIES, $($($($E, list($($X, EQUALS, $Y)), list()), EQUALS, $F),
													IMPLIES, $F))));
				}
				
				bind1("test1", "recall", $("toto"));
				
				deduce("test2");
				{
					suppose($("a", EQUALS, "b"));
					suppose($("b"));
					rewriteRight(name(-1), name(-2));
					conclude();
				}
			}
		} finally {
			export(end(), new ConsoleOutput());
		}
	}
	
	public static final void rewriteRight(final String targetName, final String equalityName, final int... indices) {
		rewriteRight(null, targetName, equalityName, indices);
	}
	
	public static final void rewriteRight(final String propositionName, final String targetName, final String equalityName, final int... indices) {
		deduce(propositionName);
		{
			apply("symmetry_of_equality", equalityName);
			rewrite(targetName, name(-1), indices);
			conclude("By right-rewriting (" + equalityName + ") in (" + targetName + ") at indices " + Arrays.toString(indices));
		}
	}
	
	public static final void bind1(final String targetName, final Expression<?> value) {
		bind1(null, targetName, value);
	}
	
	public static final void bind1(final String propositionName, final String targetName, final Expression<?> value) {
		deduce(propositionName);
		{
			final Composite<?> target = proposition(targetName);
			final Variable parameter = (Variable) target.getParameters().getListElement(1);
			
			apply(propositionName, "bind1", targetName);
			substitute($$(target.get(1), $$().append($(parameter , EQUALS, value)), $()));
			apply(name(-2), name(-1));
			conclude("By binding " + parameter.getName() + " with " + value + " in (" + targetName + ")");
		}
	}
	
}
