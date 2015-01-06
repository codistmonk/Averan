package averan3.core;

import static averan3.core.Composite.*;
import static averan3.core.Session.*;
import static org.junit.Assert.*;

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
					
					suppose("bind",
							$($(FORALL, $E, $X, $Y, $F),
									$($($(FORALL, $X), $E),
											IMPLIES, $($($($E, $$($($X, EQUALS, $Y)), $()), EQUALS, $F),
													IMPLIES, $F))));
				}
				
				deduce("test");
				{
					bind("recall", $(0));
					conclude();
				}
			}
		} finally {
			export(end(), new ConsoleOutput());
		}
	}
	
	public static final void bind(final String targetName, final Expression<?> value) {
		bind(null, targetName, value);
		final Composite<?> target = proposition(targetName);
		substitute($$(target.get(1), $$($(target.getParameters().get(1) , EQUALS, value)), $()));
		apply(name(-2), name(-1));
	}
	
	public static final void bind(final String propositionName, final String targetName, final Expression<?> value) {
		apply(propositionName, "bind", targetName);
	}
	
}
