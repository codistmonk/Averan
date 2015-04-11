package averan4.core;

import static averan4.core.AveranTools.*;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static net.sourceforge.aprog.tools.Tools.*;

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
	@SuppressWarnings("unchecked")
	public static final void main(final String[] commandLineArguments) {
		{
			debugPrint();
			
			final Deduction deduction = new Deduction(null);
			
			deduction.conclude("p", new Substitution($("x"), map($("x"), $("y")), emptyList()));
			
			print(deduction);
		}
		
		{
			debugPrint();
			
			final Deduction deduction = new Deduction(null);
			
			{
				final List<Object> p = $new("P");
				final List<Object> q = $new("Q");
				final List<Object> x_ = $new("X");
				final List<Object> y = $new("Y");
				final List<Object> i = $new("I");
				
				// \/P P -> \/X,Y X=Y -> \/I,Q P|X=Y@[I] = Q -> Q 
				deduction.suppose("rewrite", $forall(p, $rule(p, $forall(x_, $forall(y, $rule($equality(x_, y), $forall(i, $forall(q, $rule($equality($(p, GIVEN, asList($equality(x_, y)), AT, i), q), q)))))))));
			}
			
			{
				push(new Deduction(deduction));
				
				final List<Object> x = forall("X");
				
				conclude("p0", new Substitution(x, map(), indices()));
				final List<Object> equality = proposition(-1);
				conclude("p1", new Binding("rewrite", equality));
				conclude("p2", new ModusPonens(name(-1), name(-2)));
				conclude("p3", new Binding(name(-1), (List<Object>) equality.get(0)));
				conclude("p4", new Binding(name(-1), (List<Object>) equality.get(2)));
				conclude("p5", new ModusPonens(name(-1), name(-5)));
				conclude("p6", new Binding(name(-1), $()/*TODO*/));
				conclude("p7", new Binding(name(-1), $equality(x, x)));
				conclude("p8", new Substitution(equality, map(equality.get(0), equality.get(2)), indices()));
				conclude("p9", new ModusPonens(name(-2), name(-1)));
				
				deduction.conclude("p0", pop());
			}
			
			print(deduction);
		}
	}

	public static void print(final Deduction deduction) {
		debugPrint(FORALL, deduction.getParameters(),
				Tools.join(IMPLIES.get(0).toString(), iterable(deduction.getConditionNames().stream().map(n -> n + ":" + deduction.getProposition(n)))),
				IMPLIES, Tools.join(AND.get(0).toString(), iterable(deduction.getConclusionNames().stream().map(n -> n + ":" + deduction.getProposition(n)))));
		debugPrint(deduction);
	}
	
}
