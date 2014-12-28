package averan2.modules;

import static averan2.core.Equality.equality;
import static averan2.core.Expression.CollectParameters.collectParameters;
import static averan2.core.Session.*;
import static averan2.core.Session.Stack.*;
import static averan2.core.Variable.variable;

import averan2.core.*;
import averan2.io.ConsoleOutput;
import averan2.io.SessionExporter;

import net.sourceforge.aprog.tools.IllegalInstantiationException;

/**
 * @author codistmonk (creation 2014-12-28)
 */
public final class Standard {
	
	private Standard() {
		throw new IllegalInstantiationException();
	}
	
	public static final Module MODULE = build(Standard.class.getName(), () -> {
		{
			final Variable $X = new Variable("X");
			
			deduce("identity", $($X, "=", $X));
			{
				final Expression<?> x = introduce();
				
				substitute(x);
				rewrite(name(-1), name(-1));
			}
		}
		
		{
			final Variable $E = variable("E");
			final Variable $F = variable("F");
			final Variable $X = variable("X");
			final Variable $Y = variable("Y");
			
			deduce("bind",
					$($E, "->", $($($E, new Substitution().using($($X, "=", $Y))), "=", $F), "->", $F));
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
			final Variable $X = variable("X");
			final Variable $Y = variable("Y");
			
			deduce("symmetry_of_identity", $($($X, "=", $Y), "->", $($Y, "=", $X)));
			{
				final Symbol<String> x = introduce();
				
				intros();
				
				bind("identity", x);
				
				rewrite(name(-1), name(-2), 0);
			}
		}
		
		{
			final Variable $X = variable("X");
			
			deduce("recall", $($X, "->", $X));
			{
				intros();
				
				rewrite(name(-1), "identity");
			}
		}
	});
	
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
	
	public static final void bind(final String propositionName, final Expression<?> value) {
		bind(null, propositionName, value);
	}
	
	public static final void bind(final String factName, final String propositionName, final Expression<?> value) {
		deduce(factName);
		{
			final Expression<?> proposition = proposition(propositionName);
			final Variable parameter = proposition.
					accept(Variable.RESET).accept(collectParameters()).get(0);
			
			parameter.equals(value);
			substitute(proposition.accept(Variable.BIND), equality(value, value));
			apply("bind", propositionName);
			apply(name(-1), name(-2));
			
			conclude();
		}
	}
	
	public static final void rewriteRight(final String propositionName,
			final String equalityName, final int... indices) {
		rewriteRight(null, propositionName, equalityName, indices);
	}
	
	public static final void rewriteRight(final String factName, final String propositionName,
			final String equalityName, final int... indices) {
		deduce(factName);
		{
			apply("symmetry_of_identity", equalityName);
			rewrite(propositionName, name(-1), indices);
			
			conclude();
		}
	}
	
}
