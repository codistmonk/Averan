package averan4.core;

import static averan4.core.AveranTools.*;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static net.sourceforge.aprog.tools.Tools.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import net.sourceforge.aprog.tools.IllegalInstantiationException;
import net.sourceforge.aprog.tools.Tools;

/**
 * @author codistmonk (creation 2015-04-11)
 */
public final class Demo {
	
	private Demo() {
		throw new IllegalInstantiationException();
	}
	
	/**
	 * @param commandLineArguments
	 * <br>Unused
	 */
	public static final void main(final String[] commandLineArguments) {
		{
			debugPrint();
			
			final Deduction deduction = new Deduction(null);
			
			deduction.conclude(new Substitution("p", $("x"), map($("x"), $("y")), emptyList()));
			
			print(deduction);
		}
		
		{
			debugPrint();
			
			push();
			
			{
				final List<Object> p = $new("P");
				final List<Object> q = $new("Q");
				final List<Object> x_ = $new("X");
				final List<Object> y = $new("Y");
				final List<Object> i = $new("I");
				
				// \/P P -> \/X,Y X=Y -> \/I,Q P|X=Y@[I] = Q -> Q 
				suppose("rewrite", $forall(p, $rule(p, $forall(x_, $forall(y, $rule($equality(x_, y), $forall(i, $forall(q, $rule($equality($(p, GIVEN, asList($equality(x_, y)), AT, i), q), q)))))))));
			}
			
			{
				subdeduction();
				
				final List<Object> x = forall("X");
				
				substitute(x, map(), indices());
				final List<Object> equality = proposition(-1);
				bind("rewrite", equality);
				apply(name(-1), name(-2));
				bind(name(-1), left(equality));
				bind(name(-1), right(equality));
				apply(name(-1), name(-5));
				bind(name(-1), $()/*TODO*/);
				bind(name(-1), $equality(x, x));
				substitute(equality, map(left(equality), right(equality)), indices());
				apply(name(-2), name(-1));
				
				set(conclude().getMessage(), "Done");
			}
			
			print(pop());
		}
	}
	
	public static final <T> void set(final Collection<T> collection, @SuppressWarnings("unchecked") final T... elements) {
		collection.clear();
		collection.addAll(Arrays.asList(elements));
	}
	
	public static void print(final Deduction deduction) {
		debugPrint(FORALL, deduction.getParameters(),
				Tools.join(IMPLIES.get(0).toString(), iterable(deduction.getConditionNames().stream().map(n -> n + ":" + deduction.getProposition(n)))),
				IMPLIES, Tools.join(AND.get(0).toString(), iterable(deduction.getConclusionNames().stream().map(n -> n + ":" + deduction.getProposition(n)))));
		debugPrint(deduction);
	}
	
	@SuppressWarnings("unchecked")
	public static final List<Object> left(final List<Object> binaryOperation) {
		return (List<Object>) binaryOperation.get(0);
	}
	
	@SuppressWarnings("unchecked")
	public static final List<Object> right(final List<Object> binaryOperation) {
		return (List<Object>) binaryOperation.get(2);
	}
	
}
