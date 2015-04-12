package averan4.core;

import static averan4.core.AveranTools.*;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.nCopies;
import static java.util.stream.Collectors.toCollection;
import static net.sourceforge.aprog.tools.Tools.*;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
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
			
			print(deduction, 1);
		}
		
		{
			debugPrint();
			
			push();
			
			supposeRewriteLeft();
			deduceIdentity();
			
			print(pop(), 3);
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
		
		set(conclude().getMessage(), "By left rewriting in", targetName, "using", equalityName, "at",
				Arrays.stream(indices).mapToObj(Integer::valueOf).collect(toTreeSet()));
	}
	
	public static int print(final Deduction deduction, final int proofDepth) {
		return print(deduction, proofDepth, System.out);
	}
	
	public static int print(final Deduction deduction, final int proofDepth, final PrintStream output) {
		if (deduction == null) {
			return -1;
		}
		
		final int result = 1 + print(deduction.getParent(), 0, output);
		
		print(deduction, proofDepth, Tools.join("", nCopies(result, "\t")), output);
		
		return result;
	}
	
	public static final void print(final Deduction deduction, final int proofDepth, final String prefix, final PrintStream output) {
		final String tab = "\t";
		final String prefix1 = prefix + tab;
		final String prefix2 = prefix1 + tab;
		
		output.println(prefix + "((Deduction of " + deduction.getProvedPropositionName() + "))");
		
		{
			final Collection<List<Object>> parameters = deduction.getParameters();
			
			if (!parameters.isEmpty()) {
				output.println(prefix1 + FORALL.get(0) + parameters);
			}
		}
		
		{
			final List<String> conditionNames = deduction.getConditionNames();
			
			if (!conditionNames.isEmpty()) {
				output.println(prefix + "((Conditions))");
				
				for (final String conditionName : conditionNames) {
					output.println(prefix1 + conditionName + ":");
					output.println(prefix1 + deduction.getProposition(conditionName));
				}
			}
		}
		
		{
			final List<String> conclusionNames = deduction.getConclusionNames();
			
			if (!conclusionNames.isEmpty()) {
				output.println(prefix + "((Conclusions))");
				
				for (final String conclusionName : conclusionNames) {
					output.println(prefix1 + conclusionName + ":");
					output.println(prefix1 + deduction.getProposition(conclusionName));
					
					if (1 <= proofDepth) {
						final Proof proof = deduction.getProofs().get(conclusionName);
						
						if (1 == proofDepth) {
							output.println(prefix2 + proof);
						} else if (proof instanceof Deduction) {
							print((Deduction) proof, proofDepth - 1, prefix2, output);
						}
					}
				}
			}
		}
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
