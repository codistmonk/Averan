package averan.draft4.io;

import static averan.draft4.core.AveranTools.*;
import static java.util.Collections.nCopies;
import static java.util.stream.Collectors.toList;
import static multij.tools.Tools.cast;

import java.io.PrintStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import averan.draft4.core.Deduction;
import averan.draft4.core.Proof;
import multij.tools.IllegalInstantiationException;
import multij.tools.Tools;

/**
 * @author codistmonk (creation 2015-04-12)
 */
public final class Simple {
	
	private Simple() {
		throw new IllegalInstantiationException();
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
					output.println(prefix1 + collapse(deduction.getProposition(conditionName)));
				}
			}
		}
		
		{
			final List<String> conclusionNames = deduction.getConclusionNames();
			
			if (!conclusionNames.isEmpty()) {
				output.println(prefix + "((Conclusions))");
				
				for (final String conclusionName : conclusionNames) {
					output.println(prefix1 + conclusionName + ":");
					output.println(prefix1 + collapse(deduction.getProposition(conclusionName)));
					
					if (1 <= proofDepth) {
						final Proof proof = deduction.getProofs().get(conclusionName);
						
						if (1 == proofDepth || !(proof instanceof Deduction)) {
							output.println(prefix2 + collapse(proof.getMessage().stream().map(e -> e instanceof String ? " " + e + " " : collapse(e)).collect(toList())));
						} else {
							print((Deduction) proof, proofDepth - 1, prefix2, output);
						}
					}
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public static final String collapse(final Object object) {
		final Map<?, ?> map = cast(Map.class, object);
		
		if (map != null) {
			return Tools.join(",", iterable(
					map.entrySet().stream().map(e -> collapse(e.getKey()) + "=" + collapse(e.getValue()))));
		}
		
		final List<Object> expression = cast(List.class, object);
		
		if (expression == null) {
			return "" + object;
		}
		
		if (expression.isEmpty()) {
			return expression.toString();
		}
		
		if (isBlock(expression)) {
			return group(collapse(quantification(expression)) + " " + collapse(scope(expression)));
		}
		
		final String protoresult = Tools.join("", iterable(expression.stream().map(Simple::collapse)));
		
		if (isSubstitution(expression) || isEquality(expression)) {
			return group(protoresult);
		}
		
		return protoresult;
	}
	
	public static final String group(final Object object) {
		return "(" + object + ")";
	}
	
}
