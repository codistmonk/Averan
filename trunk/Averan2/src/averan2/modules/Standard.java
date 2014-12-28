package averan2.modules;

import static averan2.core.Composite.composite;
import static averan2.core.Equality.equality;
import static averan2.core.Expression.CollectParameters.collectParameters;
import static averan2.core.Session.Stack.*;

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
			
			deduce("bind", new Module().
					suppose($E).
					suppose(equality(composite($E, new Substitution().using(equality($X, $Y))), $F)).
					conclude($F));
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
			
			deduce("symmetry_of_identity", new Module().
					suppose(equality($X, $Y)).
					conclude(equality($Y, $X)));
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
		deduce();
		{
			final Expression<?> proposition = proposition(propositionName);
			final Variable parameter = proposition.accept(Variable.RESET).accept(collectParameters()).get(0);
			
			parameter.equals(value);
			substitute(proposition.accept(Variable.BIND), equality(value, value));
			apply("bind", propositionName);
			apply(name(-1), name(-2));
			conclude();
		}
	}
	
}
