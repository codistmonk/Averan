package averan.draft2.modules;

import static averan.draft2.core.Equality.equality;
import static averan.draft2.core.Session.$;
import static averan.draft2.core.Session.forAll;
import static averan.draft2.core.Session.Stack.*;
import static averan.draft2.core.Symbol.symbol;
import static averan.draft2.core.Variable.variable;
import static averan.draft2.io.ConsoleOutput.group;
import static averan.draft2.modules.Standard.build;
import static java.util.Arrays.copyOfRange;
import averan.draft2.core.Composite;
import averan.draft2.core.Expression;
import averan.draft2.core.Module;
import averan.draft2.core.Substitution;
import averan.draft2.core.Symbol;
import averan.draft2.core.Variable;
import multij.tools.IllegalInstantiationException;

/**
 * @author codistmonk (creation 2014-12-31)
 */
public final class Reals {

	private Reals() {
		throw new IllegalInstantiationException();
	}
	
	public static final Symbol<String> ZERO = symbol("0");
	
	public static final Symbol<String> ONE = symbol("1");
	
	public static final Composite<?> BOOLEANS = $("{", ZERO, ",", ONE, "}");
	
	public static final Symbol<String> NATURALS = symbol("ℕ");
	
	public static final Symbol<String> REALS = symbol("ℝ");
	
	public static final Symbol<String> ADDITION_OPERATOR = symbol("+");
	
	public static final Symbol<String> SUBTRACTION_OPERATOR = symbol("-");
	
	public static final Symbol<String> MULTIPLICATION_OPERATOR = symbol(" ");
	
	public static final Symbol<String> DIVISION_OPERATOR = symbol("/");
	
	public static final Module MODULE = build(Reals.class.getName(), new Runnable() {
		
		@Override
		public final void run() {
			include(Standard.MODULE);
			
			// TODO move logic rules to new module
			{
				final Variable $X = variable("X");
				final Variable $Y = variable("Y");
				
				suppose("left_introduction_of_conjunction",
						$(forAll($X, $Y), $($X, "->", conjunction($X, $Y))));
			}
			
			{
				final Variable $X = variable("X");
				final Variable $Y = variable("Y");
				
				suppose("right_introduction_of_conjunction",
						$(forAll($X, $Y), $($Y, "->", conjunction($X, $Y))));
			}
			
			{
				final Variable $X = variable("X");
				final Variable $Y = variable("Y");
				
				suppose("left_elimination_of_conjunction",
						$(forAll($X, $Y), $(conjunction($X, $Y), "->", $X)));
			}
			
			{
				final Variable $X = variable("X");
				final Variable $Y = variable("Y");
				
				suppose("right_elimination_of_conjunction",
						$(forAll($X, $Y), $(conjunction($X, $Y), "->", $Y)));
			}
			
			{
				final Variable $X = variable("X");
				final Variable $Y = variable("Y");
				
				deduce("commutativity_of_conjunction",
						$(forAll($X, $Y), $(conjunction($X, $Y), "->", conjunction($Y, $X))));
				{
					final Symbol<String> x = introduce();
					final Symbol<String> y = introduce();
					
					intros();
					
					apply("left_elimination_of_conjunction", name(-1));
					bind("right_introduction_of_conjunction", y, x);
					apply(name(-1), name(-2));
				}
			}
			
			{
				final Variable $X = variable("X");
				final Variable $Y = variable("Y");
				
				suppose("left_introduction_of_disjunction",
						$(forAll($X, $Y), $($X, "->", disjunction($X, $Y))));
			}
			
			{
				final Variable $X = variable("X");
				final Variable $Y = variable("Y");
				
				suppose("right_introduction_of_disjunction",
						$(forAll($X, $Y), $($Y, "->", disjunction($X, $Y))));
			}
			
			{
				final Variable $X = variable("X");
				final Variable $Y = variable("Y");
				final Variable $Z = variable("Z");
				
				suppose("elimination_of_disjunction",
						$(forAll($X, $Y, $Z), $($($X, "->", $Z), "->", $($Y, "->", $Z), "->", $(disjunction($X, $Y), "->", $Z))));
			}
			
			{
				final Variable $X = variable("X");
				final Variable $Y = variable("Y");
				
				deduce("commutativity_of_disjunction",
						$(forAll($X, $Y), $(disjunction($X, $Y), "->", disjunction($Y, $X))));
				{
					final Symbol<String> x = introduce();
					final Symbol<String> y = introduce();
					
					intros();
					
					bind("elimination_of_disjunction", x, y, disjunction(y, x));
					apply(name(-1), "right_introduction_of_disjunction");
					apply(name(-1), "left_introduction_of_disjunction");
					autoDeduce();
				}
			}
			
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
				final Variable $n = variable("n");
				
				suppose("definition_of_natural_range",
						$(forAll($x, $n), $(natural($x, $n), "->", conjunction(natural($x), $($x, "<", $n)))));
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
				final Variable $X = variable("X");
				final Variable $m = variable("m");
				final Variable $n = variable("n");
				final Variable $i = variable("i");
				final Variable $j = variable("j");
				
				suppose("definition_of_matrices",
						$(forAll($X, $m, $n), equality(realMatrix($X, $m, $n), conjunction(
								nonzeroNatural($m),
								nonzeroNatural($n),
								$(forAll($i, $j), $(natural($i, $m), "->", natural($j, $n), "->",
										real($("X", "_", $($i, ",", $j)))))))));
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
	
	public static final Composite<?> conjunction(final Object... expressions) {
		return binaryOperation("/\\", expressions);
	}
	
	public static final Composite<?> disjunction(final Object... expressions) {
		return binaryOperation("\\/", expressions);
	}
	
	public static final Composite<?> binaryOperation(final String operator, final Object... expressions) {
		final int n = expressions.length;
		
		if (n < 2) {
			throw new IllegalArgumentException();
		}
		
		if (n == 2) {
			return $(expressions[0], operator, expressions[1]);
		}
		
		return $(expressions[0], operator, binaryOperation(operator, copyOfRange(expressions, 1, n)));
	}
	
	public static final Composite<?> realMatrix(final Object matrix, final Object rows, final Object columns) {
		return membership(matrix, $("ℳ", "_", $(rows, ",", columns)));
	}
	
	public static final Composite<?> membership(final Object element, final Object set) {
		return $(element, "∈", set);
	}
	
	public static final Composite<?> natural(final Object expression) {
		return membership(expression, NATURALS);
	}
	
	public static final Composite<?> natural(final Object expression, final Object end) {
		return membership(expression, $(NATURALS, "_", end));
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
