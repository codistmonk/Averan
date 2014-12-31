package averan2.modules;

import static averan2.core.Equality.equality;
import static averan2.core.Session.$;
import static averan2.core.Session.forAll;
import static averan2.core.Session.Stack.autoDeduce;
import static averan2.core.Session.Stack.include;
import static averan2.core.Session.Stack.suppose;
import static averan2.core.Symbol.symbol;
import static averan2.core.Variable.variable;
import static averan2.io.ConsoleOutput.group;
import static averan2.modules.Standard.build;
import averan2.core.Composite;
import averan2.core.Expression;
import averan2.core.Module;
import averan2.core.Substitution;
import averan2.core.Symbol;
import averan2.core.Variable;
import net.sourceforge.aprog.tools.IllegalInstantiationException;

/**
 * @author codistmonk (creation 2014-12-31)
 */
public final class Reals {

	private Reals() {
		throw new IllegalInstantiationException();
	}
	
	public static final String NATURALS = "ℕ";
	
	public static final String REALS = "ℝ";
	
	public static final Symbol<String> ZERO = symbol("0");
	
	public static final Symbol<String> ONE = symbol("1");
	
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
				
				suppose("nonzero_naturals_are_naturals",
						$(forAll($x), $(nonzeroNatural($x), "->", natural($x))));
			}
			
			{
				final Variable $x = variable("x");
				
				suppose("nonzero_reals_are_reals",
						$(forAll($x), $(nonzeroReal($x), "->", real($x))));
			}
			
			{
				final Variable $x = variable("x");
				
				suppose("nonzero_naturals_are_nonzero_reals",
						$(forAll($x), $(nonzeroNatural($x), "->", nonzeroReal($x))));
			}
			
			{
				suppose("nonzero_naturalness_of_1",
						nonzeroNatural(ONE));
			}
			
			{
				final Variable $x = variable("x");
				final Variable $y = variable("y");
				
				suppose("type_of_real_addition",
						$(forAll($x, $y), $(real($x), "->", real($y), "->", real(addition($x, $y)))));
			}
			
			{
				final Variable $x = variable("x");
				final Variable $y = variable("y");
				
				suppose("commutativity_of_real_addition",
						$(forAll($x, $y), $(real($x), "->", real($y), "->", equality(addition($x, $y), addition($y, $x)))));
			}
			
			{
				final Variable $x = variable("x");
				final Variable $y = variable("y");
				final Variable $z = variable("z");
				
				suppose("associativity_of_real_addition",
						$(forAll($x, $y, $z), $(real($x), "->", real($y), "->", real($z), "->", equality(addition($x, group(addition($y, $z))), addition(group(addition($x, $y)), $z)))));
			}
			
			{
				final Variable $x = variable("x");
				final Variable $y = variable("y");
				
				suppose("type_of_real_multiplication",
						$(forAll($x, $y), $(real($x), "->", real($y), "->", real(multiplication($x, $y)))));
			}
			
			{
				final Variable $x = variable("x");
				final Variable $y = variable("y");
				final Variable $z = variable("z");
				
				suppose("associativity_of_real_multiplication",
						$(forAll($x, $y, $z), $(real($x), "->", real($y), "->", real($z), "->", equality(multiplication($x, group(multiplication($y, $z))), multiplication(group(multiplication($x, $y)), $z)))));
			}
			
			{
				final Variable $x = variable("x");
				final Variable $y = variable("y");
				
				suppose("commutativity_of_real_multiplication",
						$(forAll($x, $y), $(real($x), "->", real($y), "->", equality(multiplication($x, $y), multiplication($y, $x)))));
			}
			
			{
				final Variable $X = variable("X");
				final Variable $i = variable("i");
				
				suppose("definition_of_sum_0",
						$(forAll($X, $i), equality(sum($i, ZERO, $X), $($X, new Substitution().using(equality($i, ZERO))))));
			}
			
			{
				final Variable $X = variable("X");
				final Variable $n = variable("n");
				final Variable $i = variable("i");
				
				suppose("definition_of_sum_n",
						$(forAll($X, $n, $i), equality(sum($i, $n, $X), addition(sum($i, subtraction($n, ONE), $X), $($X, new Substitution().using(equality($i, $n)))))));
			}
			
			{
				autoDeduce("realness_of_0",
						real(ZERO));
				autoDeduce("realness_of_1",
						real(ONE));
				autoDeduce(
						real(addition(ZERO, ONE)));
				autoDeduce(
						real(multiplication(ZERO, ONE)));
			}
		}
		
	});
	
	public static final Composite<?> membership(final Object element, final Object set) {
		return $(element, "∈", set);
	}
	
	public static final Composite<?> natural(final Object expression) {
		return membership(expression, NATURALS);
	}
	
	public static final Composite<?> real(final Object expression) {
		return membership(expression, REALS);
	}
	
	public static final Composite<?> nonzero(final Object expression) {
		return $(expression, "*");
	}
	
	public static final Composite<?> nonzeroNatural(final Object expression) {
		return membership(expression, nonzero(NATURALS));
	}
	
	public static final Composite<?> nonzeroReal(final Object expression) {
		return membership(expression, nonzero(REALS));
	}
	
	public static final Composite<?> addition(final Object left, final Object right) {
		return $(left, "+", right);
	}
	
	public static final Composite<?> subtraction(final Object left, final Object right) {
		return $(left, "-", right);
	}
	
	public static final Composite<?> multiplication(final Object left, final Object right) {
		return $(left, " ", right);
	}
	
	public static final Composite<?> inverse(final Object expression) {
		return $("1", "/", expression);
	}
	
	public static final Composite<?> sum(final Object i, final Object n, final Object x) {
		final Expression<?> nAsExpression = $(n);
		final boolean nIsSingle = nAsExpression instanceof Symbol<?> || nAsExpression instanceof Variable;
		
		return $($($("Σ", "_", group($(i, "=", ZERO))), "^", nIsSingle ? nAsExpression : group(nAsExpression)), " ", x);
	}
	
}
