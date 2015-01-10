package averan3.deductions;

import static averan3.core.Session.*;
import static averan3.deductions.Standard.*;
import static averan3.io.ConsoleOutput.group;

import averan3.core.Composite;
import averan3.core.Expression;
import averan3.core.Symbol;
import averan3.core.Proof.Deduction;
import averan3.core.Variable;
import averan3.io.HTMLOutput;

import net.sourceforge.aprog.tools.IllegalInstantiationException;

/**
 * @author codistmonk (creation 2015-01-08)
 */
public final class Reals {
	
	private Reals() {
		throw new IllegalInstantiationException();
	}
	
	public static final Symbol<String> ZERO = symbol("0");
	
	public static final Symbol<String> ONE = symbol("1");
	
	public static final Composite<?> BOOLEANS = $$("{", ZERO, ",", ONE, "}");
	
	public static final Symbol<String> NATURALS = symbol("ℕ");
	
	public static final Symbol<String> REALS = symbol("ℝ");
	
	public static final Symbol<String> ADDITION_OPERATOR = symbol("+");
	
	public static final Symbol<String> SUBTRACTION_OPERATOR = symbol("-");
	
	public static final Symbol<String> MULTIPLICATION_OPERATOR = symbol(" ");
	
	public static final Symbol<String> DIVISION_OPERATOR = symbol("/");
	
	public static final Deduction DEDUCTION = build(Reals.class.getName(), new Runnable() {
		
		@Override
		public final void run() {
			include(Standard.DEDUCTION);
			
			{
				final Variable $x = variable("x");
				
				suppose("naturals_are_reals",
						$(forall($x), rule(natural($x), real($x))));
			}
			
			{
				suppose("type_of_0",
						natural(ZERO));
				suppose("type_of_1",
						natural(ONE));
			}
			
			{
				final Variable $x = variable("x");
				
				suppose("nonzero_naturals_are_naturals",
						$(forall($x), rule(nonzeroNatural($x), natural($x))));
				suppose("nonzero_reals_are_reals",
						$(forall($x), rule(nonzeroReal($x), real($x))));
				suppose("nonzero_naturals_are_nonzero_reals",
						$(forall($x), rule(nonzeroNatural($x), nonzeroReal($x))));
			}
			
			{
				suppose("nonzero_naturalness_of_1",
						nonzeroNatural(ONE));
			}
			
			{
				final Variable $x = variable("x");
				final Variable $n = variable("n");
				
				suppose("definition_of_natural_range",
						$(forall($x, $n), rule(natural($x, $n), conjunction(natural($x), $($x, "<", $n)))));
			}
			
			{
				final Variable $x = variable("x");
				final Variable $y = variable("y");
				final Variable $z = variable("z");
				
				suppose("type_of_real_addition",
						$(forall($x, $y), rule(real($x), real($y), real(addition($x, $y)))));
				suppose("commutativity_of_real_addition",
						$(forall($x, $y), rule(real($x), real($y), $(addition($x, $y), "=", addition($y, $x)))));
				suppose("associativity_of_real_addition",
						$(forall($x, $y, $z), rule(real($x), real($y), real($z),
								$(addition($x, group(addition($y, $z))), "=", addition(group(addition($x, $y)), $z)))));
			}
			
			{
				final Variable $x = variable("x");
				final Variable $y = variable("y");
				final Variable $z = variable("z");
				
				suppose("type_of_real_multiplication",
						$(forall($x, $y), rule(real($x), real($y), real(multiplication($x, $y)))));
				suppose("associativity_of_real_multiplication",
						$(forall($x, $y, $z), rule(real($x), real($y), real($z),
								equality(multiplication($x, group(multiplication($y, $z))),
										multiplication(group(multiplication($x, $y)), $z)))));
				suppose("commutativity_of_real_multiplication",
						$(forall($x, $y), rule(real($x), real($y), equality(multiplication($x, $y), multiplication($y, $x)))));
			}
			
			{
				final Variable $X = variable("X");
				final Variable $n = variable("n");
				final Variable $i = variable("i");
				
				suppose("definition_of_sum_0",
						$(forall($X, $i), equality(sum($i, ZERO, $X),
								$($X, list(equality($i, ZERO)), list()))));
				
				suppose("definition_of_sum_n",
						$(forall($X, $n, $i), equality(sum($i, $n, $X),
								addition(sum($i, subtraction($n, ONE), $X), $($X, list(equality($i, $n)), list())))));
			}
			
			{
				final Variable $X = variable("X");
				final Variable $m = variable("m");
				final Variable $n = variable("n");
				final Variable $i = variable("i");
				final Variable $j = variable("j");
				
				suppose("definition_of_matrices",
						$(forall($X, $m, $n), $(realMatrix($X, $m, $n), "=", conjunction(
								nonzeroNatural($m),
								nonzeroNatural($n),
								$(forall($i, $j), rule(natural($i, $m), natural($j, $n),
										real(matrixElement($X, $i, $j))))))));
				
				check(autoDeduce("type_of_matrix_rows",
						$(forall($X, $m, $n), rule(realMatrix($X, $m, $n), nonzeroNatural($m))), 5));
				check(autoDeduce("type_of_matrix_columns",
						$(forall($X, $m, $n), rule(realMatrix($X, $m, $n), nonzeroNatural($n))), 5));
				check(autoDeduce("type_of_matrix_element",
						$(forall($X, $m, $n), rule(realMatrix($X, $m, $n), $(forall($i, $j),
								rule(natural($i, $m), natural($j, $n), real(matrixElement($X, $i, $j)))))), 5));
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
		
	}, new HTMLOutput());
	
	public static final Composite<Expression<?>> matrixElement(final Object matrix, final Object row, final Object column) {
		return $$(matrix, "_", $(row, ",", column));
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
		return $$(expression, "*");
	}
	
	public static final Composite<?> nonzeroNatural(final Object expression) {
		return membership(expression, nonzero(NATURALS));
	}
	
	public static final Composite<?> nonzeroReal(final Object expression) {
		return membership(expression, nonzero(REALS));
	}
	
	public static final Composite<?> addition(final Object left, final Object right) {
		return $$(left, "+", right);
	}
	
	public static final Composite<?> subtraction(final Object left, final Object right) {
		return $$(left, "-", right);
	}
	
	public static final Composite<?> multiplication(final Object left, final Object right) {
		return $$(left, " ", right);
	}
	
	public static final Composite<?> inverse(final Object expression) {
		return $$("1", "/", expression);
	}
	
	public static final Composite<?> sum(final Object i, final Object n, final Object x) {
		final Expression<?> nAsExpression = $(n);
		final boolean nIsSingle = nAsExpression instanceof Symbol<?> || nAsExpression instanceof Variable;
		
		return $$($($("Σ", "_", group($(i, "=", ZERO))), "^", nIsSingle ? nAsExpression : group(nAsExpression)), " ", x);
	}
	
	public static final Composite<?> realMatrix(final Object matrix, final Object rows, final Object columns) {
		return membership(matrix, $("ℳ", "_", $(rows, ",", columns)));
	}
	
}
