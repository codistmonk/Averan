package averan2.modules;

import static averan2.core.Session.$;
import static averan2.core.Session.forAll;
import static averan2.core.Session.Stack.autoDeduce;
import static averan2.core.Session.Stack.include;
import static averan2.core.Session.Stack.suppose;
import static averan2.core.Symbol.symbol;
import static averan2.core.Variable.variable;
import static averan2.modules.Standard.build;
import net.sourceforge.aprog.tools.IllegalInstantiationException;
import averan2.core.Composite;
import averan2.core.Module;
import averan2.core.Symbol;
import averan2.core.Variable;

/**
 * @author codistmonk (creation 2014-12-31)
 */
public final class Reals {
	
	private Reals() {
		throw new IllegalInstantiationException();
	}
	
	public static final Symbol<String> ZERO = symbol("0");
	
	public static final Symbol<String> ONE = symbol("1");
	
	public static final Composite<?> natural(final Object expression) {
		return $(expression, "∈", "ℕ");
	}
	
	public static final Composite<?> real(final Object expression) {
		return $(expression, "∈", "ℝ");
	}
	
	public static final Module MODULE = build(Reals.class.getName(), new Runnable() {
		
		@Override
		public final void run() {
			include(Standard.MODULE);
			
			{
				final Variable $x = variable("x");
				
				suppose("naturals_are_reals",
						$(forAll($x), $(natural($x), "->", real($x))));
			}
			
			{
				suppose("type_of_0",
						natural(ZERO));
			}
			
			{
				suppose("type_of_1",
						natural(ONE));
			}
			
			{
				final Variable $x = variable("x");
				final Variable $y = variable("y");
				
				suppose("type_of_real_addition",
						$(forAll($x, $y), $(real($x), "->", real($y), "->", real(addition($x, $y)))));
			}
			
			{
				autoDeduce("realness_of_0",
						real(ZERO));
				autoDeduce("realness_of_1",
						real(ONE));
				autoDeduce(real(addition(ZERO, ONE)));
			}
		}
		
	});
	
	public static final Composite<?> addition(final Object left, final Object right) {
		return $(left, "+", right);
	}
	
}