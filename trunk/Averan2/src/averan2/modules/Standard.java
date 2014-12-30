package averan2.modules;

import static averan2.core.Equality.equality;
//import static averan2.core.Expression.CollectParameters.collectParameters;
import static averan2.core.Session.*;
import static averan2.core.Session.Stack.*;
import static averan2.core.Symbol.symbol;
import static averan2.core.Variable.variable;
import averan2.core.*;
import averan2.io.ConsoleOutput;
import averan2.io.SessionExporter;
import net.sourceforge.aprog.tools.IllegalInstantiationException;
import net.sourceforge.aprog.tools.Tools;

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
			
			deduce("identity", $(forAll($X), $($X, "=", $X)));
			{
				final Expression<?> x = introduce();
				
				substitute(x);
				rewrite(name(-1), name(-1));
			}
		}
		
		{
			final Variable $X = variable("X");
			
			deduce("recall", $(forAll($X), $($X, "->", $X)));
			{
				intros();
			}
		}
		
		{
			final Variable $X = variable("X");
			final Variable $Y = variable("Y");
			
			deduce("symmetry_of_identity", $(forAll($X, $Y), $($($X, "=", $Y), "->", $($Y, "=", $X))));
			{
				final Symbol<String> x = introduce();
				
				intros();
				
				bind("identity", x);
				rewrite(name(-1), name(-2), 0);
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
