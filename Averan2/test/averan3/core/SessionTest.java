package averan3.core;

import static averan3.core.Composite.*;
import static averan3.core.Session.*;

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
				deduce("recall");
				{
					final Variable p = introduce("P");
					
					suppose(p);
					substitute($$(p, $(), $()));
					rewrite(name(-1), name(-1));
					rewrite(name(-3), name(-1));
					
					conclude();
				}
				
				{
					final Variable $E = new Variable("E");
					final Variable $F = new Variable("F");
					final Variable $X = new Variable("X");
					final Variable $Y = new Variable("Y");
					
					suppose("bind1",
							$($(FORALL, $E, $X, $Y, $F),
									$($($(FORALL, $X), $E),
											IMPLIES, $($($($E, $$($($X, EQUALS, $Y)), $()), EQUALS, $F),
													IMPLIES, $F))));
				}
				
				bind1("test", "recall", $("toto"));
			}
		} finally {
			export(end(), new ConsoleOutput());
		}
	}
	
	public static final void bind1(final String targetName, final Expression<?> value) {
		bind1(null, targetName, value);
	}
	
	public static final void bind1(final String propositionName, final String targetName, final Expression<?> value) {
		deduce(propositionName);
		{
			final Composite<?> target = proposition(targetName);
			final Variable parameter = (Variable) target.getParameters().get(1);
			
			apply(propositionName, "bind1", targetName);
			substitute($$(target.get(1), $$($(parameter , EQUALS, value)), $()));
			apply(name(-2), name(-1));
			conclude("By binding " + parameter.getName() + " with " + value + " in " + targetName);
		}
	}
	
}
