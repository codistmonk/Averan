package averan4.core;

import static averan4.core.AveranTools.*;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toCollection;
import static net.sourceforge.aprog.tools.Tools.*;

import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collector;

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
			
			supposeRewriteLeft();
			deduceIdentity();
			
			print(pop());
		}
	}
	
	public static final void checkRequiredRule(final String ruleName) {
		if (proposition(ruleName) == null) {
			throw new IllegalStateException("Missing required rule: " + ruleName);
		}
	}
	
	public static final void deduceIdentity() {
		subdeduction("identity");
		
		final List<Object> x = forall("X");
		
		substitute(x, map());
		rewriteLeft(name(-1), name(-1));
		
		conclude();
	}
	
	public static final void supposeRewriteLeft() {
		final List<Object> p = $new("P");
		final List<Object> q = $new("Q");
		final List<Object> x = $new("X");
		final List<Object> y = $new("Y");
		final List<Object> i = $new("I");
		
		// \/P P -> \/X,Y X=Y -> \/I,Q P|X=Y@[I] = Q -> Q 
		suppose("rewriteLeft", $forall(p, $rule(p, $forall(x, $forall(y, $rule($equality(x, y), $forall(i, $forall(q, $rule($equality($(p, GIVEN, asList($equality(x, y)), AT, i), q), q)))))))));
	}
	
    public static final <T> Collector<T, ?, TreeSet<T>> toTreeSet() {
        return toCollection(TreeSet::new);
    }
	
	@SuppressWarnings("unchecked")
	public static final void rewriteLeft(final String targetName, final String equalityName, final int... indices) {
		checkRequiredRule("rewriteLeft");
		
		subdeduction();
		
		final List<Object> target = proposition(targetName);
		
		// rewrite: \/P P -> \/X,Y X=Y -> \/I,Q P|X=Y@[I] = Q -> Q 
		bind("rewriteLeft", target);
		apply(name(-1), targetName);
		
		final List<Object> equality = proposition(equalityName);
		
		bind(name(-1), left(equality), right(equality));
		apply(name(-1), equalityName);
		substitute(target, map(left(equality), right(equality)), indices);
		bind(name(-2), indices(indices), right(proposition(-1)));
		apply(name(-1), name(-2));
		
		set(conclude().getMessage(), "Rewrite left in", targetName, "using", equalityName, "at",
				Arrays.stream(indices).mapToObj(Integer::valueOf).collect(toTreeSet()));
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
