package averan4.core;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static net.sourceforge.aprog.tools.Tools.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.sourceforge.aprog.tools.IllegalInstantiationException;
import net.sourceforge.aprog.tools.Tools;

/**
 * @author codistmonk (creation 2015-04-11)
 */
public final class Demo {
	
	private Demo() {
		throw new IllegalInstantiationException();
	}
	
	public static final List<Object> FORALL = expression("\\/");
	
	public static final List<Object> IMPLIES = expression("->");
	
	public static final List<Object> EQUALS = expression("=");
	
	public static final List<Object> GIVEN = expression("|");
	
	public static final List<Object> AND = expression(",");
	
	public static final List<Object> AT = expression("@");
	
	/**
	 * @param commandLineArguments
	 * <br>Unused
	 */
	@SuppressWarnings("unchecked")
	public static final void main(final String[] commandLineArguments) {
		{
			debugPrint();
			
			final Deduction deduction = new Deduction(null);
			
			deduction.conclude("p", new Substitution(expression("x"), map(expression("x"), expression("y")), emptyList()));
			
			debugPrint(FORALL, deduction.getParameters(),
					Tools.join(IMPLIES.get(0).toString(), iterable(deduction.getConditionNames().stream().map(n -> n + ":" + deduction.getProposition(n)))),
					IMPLIES, Tools.join(AND.get(0).toString(), iterable(deduction.getConclusionNames().stream().map(n -> n + ":" + deduction.getProposition(n)))));
			debugPrint(deduction);
		}
		
		{
			debugPrint();
			
			final Deduction deduction = new Deduction(null);
			
			{
				final List<Object> p = newId("P");
				final List<Object> q = newId("Q");
				final List<Object> x_ = newId("X");
				final List<Object> y = newId("Y");
				final List<Object> i = newId("I");
				
				// \/P P -> \/X,Y X=Y -> \/I,Q P|X=Y@[I] = Q -> Q 
				deduction.suppose("rewrite", forall(p, rule(p, forall(x_, forall(y, rule(equality(x_, y), forall(i, forall(q, rule(equality(expression(p, GIVEN, asList(equality(x_, y)), AT, i), q), q)))))))));
			}
			
			{
				final Deduction subDeduction = new Deduction(deduction);
				final List<Object> x = newId("X");
				
				subDeduction.forall(x);
				
				subDeduction.conclude("p0", new Substitution(x, map(), indices()));
				final List<Object> equality = subDeduction.getPropositions().get(last(subDeduction.getPropositionNames()));
				subDeduction.conclude("p1", new Binding("rewrite", equality));
				subDeduction.conclude("p2", new ModusPonens(subDeduction.getPropositionName(-1), subDeduction.getPropositionName(-2)));
				subDeduction.conclude("p3", new Binding(subDeduction.getPropositionName(-1), (List<Object>) equality.get(0)));
				subDeduction.conclude("p4", new Binding(subDeduction.getPropositionName(-1), (List<Object>) equality.get(2)));
				subDeduction.conclude("p5", new ModusPonens(subDeduction.getPropositionName(-1), subDeduction.getPropositionName(-5)));
				subDeduction.conclude("p6", new Binding(subDeduction.getPropositionName(-1), expression()/*TODO*/));
				subDeduction.conclude("p7", new Binding(subDeduction.getPropositionName(-1), equality(x, x)));
				subDeduction.conclude("p8", new Substitution(equality, map(equality.get(0), equality.get(2)), indices()));
				subDeduction.conclude("p9", new ModusPonens(subDeduction.getPropositionName(-2), subDeduction.getPropositionName(-1)));
				
				deduction.conclude("p0", subDeduction);
			}
			
			debugPrint(FORALL, deduction.getParameters(),
					Tools.join(IMPLIES.get(0).toString(), iterable(deduction.getConditionNames().stream().map(n -> n + ":" + deduction.getProposition(n)))),
					IMPLIES, Tools.join(AND.get(0).toString(), iterable(deduction.getConclusionNames().stream().map(n -> n + ":" + deduction.getProposition(n)))));
			debugPrint(deduction);
		}
	}
	
	public static final List<Object> newId(final String name) {
		return expression(new Id(name));
	}
	
	public static final List<Object> forall(final Object variableOrName) {
		return expression(FORALL, expression(variableOrName));
	}
	
	public static final List<Object> forall(final Object variableOrName, final Object... scoped) {
		return expression(forall(variableOrName), expression(scoped));
	}
	
	@SuppressWarnings("unchecked")
	public static final <K, V> Map<K, V> map(final Object... keyAndValues) {
		final Map<K, V> result = new LinkedHashMap<>();
		final int n = keyAndValues.length;
		
		for (int i = 0; i < n; i += 2) {
			result.put((K) keyAndValues[i + 0], (V) keyAndValues[i + 1]);
		}
		
		
		return result;
	}
	
	public static final Collection<Integer> indices(final int... indices) {
		return Arrays.stream(indices).mapToObj(Integer::valueOf).collect(Collectors.toCollection(TreeSet::new));
	}
	
	public static final List<Object> rule(final Object... propositions) {
		return join(IMPLIES, propositions);
	}
	
	public static final List<Object> join(final Object separator, final Object... objects) {
		return join(separator, Arrays.asList(objects));
	}
	
	public static final List<Object> join(final Object separator, final Iterable<Object> objects) {
		final Collection<Object> collection = cast(Collection.class, objects);
		final List<Object> result = collection == null ? new ArrayList<>() : new ArrayList<>(2 * collection.size() + 1);
		final Iterator<Object> i = objects.iterator();
		
		if (i.hasNext()) {
			result.add(i.next());
			
			while (i.hasNext()) {
				result.add(separator);
				result.add(i.next());
			}
		}
		
		return result;
	}
	
	public static final <T> Iterable<T> iterable(final Stream<T> stream) {
		return () -> stream.iterator();
	}
	
	public static final List<Object> equality(final Object left, final Object right) {
		return expression(left, EQUALS, right);
	}
	
	@SuppressWarnings("unchecked")
	public static final List<Object> expression(final Object... objects) {
		if (objects.length == 1 && objects[0] instanceof List) {
			return (List<Object>) objects[0];
		}
		
		if (objects.length <= 1) {
			return Arrays.asList(objects);
		}
		
		return Arrays.stream(objects).map(Demo::expression).collect(toList());
	}
	
	public static final void checkArgument(final boolean check) {
		if (!check) {
			throw new IllegalArgumentException();
		}
	}
	
}
