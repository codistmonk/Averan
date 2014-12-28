package averan2.core;

import static averan2.core.Composite.composite;
import static averan2.core.Equality.equality;
import static averan2.core.Session.Stack.*;
import static averan2.core.Symbol.symbol;
import averan2.core.Expression;
import averan2.core.Expression.CollectParameters;
import averan2.core.Module;
import averan2.core.Session;
import averan2.core.Variable;
import averan2.core.Expression.Visitor;
import averan2.io.ConsoleOutput;
import averan2.io.SessionExporter;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.aprog.tools.Pair;
import net.sourceforge.aprog.tools.Tools;

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
			include(STANDARD);
			
			deduce("test");
			{
				suppose(new Module().suppose(symbol("A")).conclude(symbol("B")));
				suppose(new Module().suppose(symbol("B")).conclude(symbol("C")));
				
				deduce(new Module().suppose(symbol("A")).conclude(symbol("C")));
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
							final List<Pair<String, Expression<?>>> justification3 = justificationsFor(goal());
							
							apply("recall", justification3.get(0).getFirst());
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
	
	public static final Module STANDARD = build("averan.modules.Standard", () -> {
		{
			final Variable $X = new Variable("X");
			
			deduce("identity", new Module().conclude(equality($X, $X)));
			{
				final Expression<?> x = introduce();
				
				substitute(x);
				rewrite(name(-1), name(-1));
			}
		}
		
		{
			final Variable $E = new Variable("E");
			final Variable $F = new Variable("F");
			final Variable $X = new Variable("X");
			final Variable $Y = new Variable("Y");
			
			deduce("bind", new Module().suppose($E).suppose(equality(composite($E, new Substitution().bind(equality($X, $Y))), $F)).conclude($F));
			{
				final Symbol<String> e = introduce();
				
				introduce();
				
				final String truthnessOfE = name(-1);
				final Symbol<String> x = introduce();
				final Symbol<String> y = introduce();
				
				intros();
				
				substitute(e, equality(x, y));
				rewrite(name(-2), name(-1));
				rewrite(truthnessOfE, name(-1));
			}
		}
		
		{
			final Variable $X = new Variable("X");
			final Variable $Y = new Variable("Y");
			
			deduce("symmetry_of_identity", new Module().suppose(equality($X, $Y)).conclude(equality($Y, $X)));
			{
				final Symbol<String> x = introduce();
				
				intros();
				
				bind("identity", x);
				
				rewrite(name(-1), name(-2), 0);
			}
		}
		
		{
			final Variable $X = new Variable("X");
			
			deduce("recall", new Module().suppose($X).conclude($X));
			{
				intros();
				
				rewrite(name(-1), "identity");
			}
		}
	});
	
	public static final void bind(final String propositionName, final Expression<?> value) {
		deduce();
		{
			final Expression<?> proposition = proposition(propositionName);
			final Variable parameter = proposition.accept(Variable.RESET).accept(CollectParameters.collectParameters()).get(0);
			
			parameter.equals(value);
			substitute(proposition.accept(Variable.BIND), equality(value, value));
			apply("bind", propositionName);
			apply(name(-1), name(-2));
			conclude();
		}
	}
	
	public static final Module build(final String moduleName, final Runnable moduleDefinition) {
		pushSession(new Session());
		
		final Module result;
		
		try {
			deduce(moduleName);
			{
				result = module();
				
				moduleDefinition.run();
			}
		} finally {
			SessionExporter.export(popSession(), new ConsoleOutput());
		}
		
		return result;
	}
	
}
