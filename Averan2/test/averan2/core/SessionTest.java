package averan2.core;

import static averan2.core.Session.*;
import static averan2.core.Session.Stack.*;
import static averan2.core.Symbol.symbol;
import averan2.core.Expression;
import averan2.core.Module;
import averan2.core.Session;
import averan2.core.Variable;
import averan2.io.ConsoleOutput;
import averan2.io.SessionExporter;
import averan2.modules.Standard;

import java.util.List;

import net.sourceforge.aprog.tools.Pair;

import org.junit.Test;

/**
 * @author codistmonk (creation 2014-12-27)
 */
public final class SessionTest {
	
	@Test
	public final void test1() {
		pushSession(new Session());
		
		try {
			deduce("test");
			{
				final Variable $X = new Variable("X");
				
				deduce("recall", new Module().suppose($X).conclude($X));
				{
					final Expression<?> x = introduce();
					
					introduce();
					
					substitute(x);
					rewrite(name(-1), name(-1));
					rewrite(name(-3), name(-1));
				}
				
				deduce();
				{
					suppose(symbol("Y"));
					apply("recall", name(-1));
					conclude();
				}
			}
		} finally {
			SessionExporter.export(popSession(), new ConsoleOutput());
		}
	}
	
	@Test
	public final void test2() {
		pushSession(new Session());
		
		try {
			include(Standard.MODULE);
			
			deduce("test");
			{
				suppose($("A", "->", "B"));
				suppose($("B", "->", "C"));
				
				deduce((Expression<?>) $("A", "->", "C"));
				{
					introduce();
					
					final List<Pair<String, Expression<?>>> justification1 = justificationsFor(goal());
					final Module justification1Module = (Module) justification1.get(0).getSecond();
					
					deduce((Expression<?>) justification1Module.getConditions().get(0));
					{
						final List<Pair<String, Expression<?>>> justification2 = justificationsFor(goal());
						final Module justification2Module = (Module) justification2.get(0).getSecond();
						
						deduce((Expression<?>) justification2Module.getConditions().get(0));
						{
							check(autoDeduce());
						}
						
						apply(justification2.get(0).getFirst(), name(-1));
					}
					
					apply(justification1.get(0).getFirst(), name(-1));
				}
			}
		} finally {
			SessionExporter.export(popSession(), new ConsoleOutput());
		}
	}
	
	public static final void check(final boolean ok) {
		if (!ok) {
			throw new RuntimeException();
		}
	}
	
	public static final boolean autoDeduce() {
		return autoDeduce(goal());
	}
	
	public static final boolean autoDeduce(final Expression<?> expression) {
		deduce(expression);
		{
			final Module unfinishedProof = module();
			
			intros();
			
			for (final Pair<String, Expression<?>> justification : justificationsFor(goal())) {
				if (justification.getSecond().equals(goal())) {
					apply("recall", justification.getFirst());
					break;
				}
			}
			
			if (module() == unfinishedProof) {
				cancel();
				
				return false;
			}
		}
		
		return true;
	}
	
}
