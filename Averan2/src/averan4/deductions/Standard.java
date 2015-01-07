package averan4.deductions;

import static averan4.core.Composite.*;
import static averan4.core.Session.*;

import averan4.core.Composite;
import averan4.core.Expression;
import averan4.core.Variable;
import averan4.core.Proof.Deduction;
import averan4.io.ConsoleOutput;

import java.util.Arrays;

import net.sourceforge.aprog.tools.IllegalInstantiationException;

/**
 * @author codistmonk (creation 2015-01-07)
 */
public final class Standard {
	
	private Standard() {
		throw new IllegalInstantiationException();
	}
	
	public static final Deduction DEDUCTION = build(Standard.class.getName(), new Runnable() {
		
		@Override
		public final void run() {
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
				final Variable $T = new Variable("‥");
				
				suppose("bind1",
						$(forall($E, $X, $T, $Y, $F),
								$($($$().add(FORALL).add($($X, $T)), $E),
										IMPLIES, $($($($E, list($($X, EQUALS, $Y)), list()), EQUALS, $F),
												IMPLIES, $($$().add(FORALL).add($T), $F)))));
			}
		}
		
	}, new ConsoleOutput());
	
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
