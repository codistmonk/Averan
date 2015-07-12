package averan.draft4.deductions;

import static averan.draft4.core.AveranTools.*;
import static java.util.Arrays.asList;
import static multij.tools.Tools.unchecked;

import java.util.Arrays;
import java.util.List;

import averan.draft4.core.Deduction;
import averan.draft4.io.Simple;
import multij.tools.IllegalInstantiationException;

/**
 * @author codistmonk (creation 2015-04-12)
 */
public final class Standard {
	
	private Standard() {
		throw new IllegalInstantiationException();
	}
	
	public static final void deduceIdentity() {
		subdeduction("identity");
		
		final List<Object> x = forall("X");
		
		substitute(x, map());
		rewrite(name(-1), name(-1));
		
		conclude();
	}
	
	public static final void supposeRewrite() {
		final List<Object> p = $new("P");
		final List<Object> q = $new("Q");
		final List<Object> x = $new("X");
		final List<Object> y = $new("Y");
		final List<Object> i = $new("I");
		
		// \/P P -> \/X,Y X=Y -> \/I,Q P|X=Y@[I] = Q -> Q 
		suppose("rewrite", $forall(p, $rule(p, $forall(x, $forall(y, $rule($equality(x, y), $forall(i, $forall(q, $rule($equality($(p, GIVEN, asList($equality(x, y)), AT, i), q), q)))))))));
	}
	
	public static final void rewrite(final String targetName, final String equalityName, final int... indices) {
		rewrite(newName(), targetName, equalityName, indices);
	}
	
	@SuppressWarnings("unchecked")
	public static final void rewrite(final String propositionName, final String targetName, final String equalityName, final int... indices) {
		subdeduction(propositionName);
		
		final List<Object> target = checkProposition(targetName);
		
		// rewrite: \/P P -> \/X,Y X=Y -> \/I,Q P|X=Y@[I] = Q -> Q 
		bind("rewrite", target);
		apply(name(-1), targetName);
		
		final List<Object> equality = checkEquality(equalityName);
		
		bind(name(-1), left(equality), right(equality));
		apply(name(-1), equalityName);
		substitute(target, map(left(equality), right(equality)), indices);
		bind(name(-2), indices(indices), right(proposition(-1)));
		apply(name(-1), name(-2));
		
		set(conclude().getMessage(), "By rewriting in", targetName, "using", equalityName, "at",
				Arrays.stream(indices).mapToObj(Integer::valueOf).collect(toTreeSet()));
	}
	
	public static final void deduceCommutativityOfEquality() {
		subdeduction("commutativity_of_equality");
		
		final List<Object> x = forall("X");
		final List<Object> y = forall("Y");
		
		suppose($equality(x, y));
		bind("identity", x);
		rewrite(name(-1), name(-2), 0);
		
		conclude();
	}
	
	public static final void rewriteRight(final String targetName, final String equalityName, final int... indices) {
		rewriteRight(newName(), targetName, equalityName, indices);
	}
	
	@SuppressWarnings("unchecked")
	public static final void rewriteRight(final String propositionName, final String targetName, final String equalityName, final int... indices) {
		subdeduction(propositionName);
		
		final List<Object> equality = checkEquality(equalityName);
		
		bind("commutativity_of_equality", left(equality), right(equality));
		apply(name(-1), equalityName);
		rewrite(targetName, name(-1));
		
		set(conclude().getMessage(), "By right rewriting in", targetName, "using", equalityName, "at",
				Arrays.stream(indices).mapToObj(Integer::valueOf).collect(toTreeSet()));
	}
	
	public static final void deduceRecall() {
		subdeduction("recall");
		
		final List<Object> x = forall("X");
		
		suppose(x);
		
		bind("identity", x);
		rewrite(name(-2), name(-1));
		
		conclude();
	}
	
	public static final Deduction subbuild(final String deductionName, final Runnable deductionBuilder) {
		return build(new Deduction(deduction(), deductionName), deductionBuilder);
	}
	
	public static final Deduction build(final String deductionName, final Runnable deductionBuilder) {
		return build(new Deduction(null, deductionName), deductionBuilder);
	}
	
	public static final Deduction build(final Deduction deduction, final Runnable deductionBuilder) {
		final Deduction result = push(deduction);
		
		try {
			deductionBuilder.run();
			
			return result.conclude();
		} catch (final Exception exception) {
			Simple.print(deduction(), 1);
			
			throw unchecked(exception);
		} finally {
			while (result != pop()) {
				// NOP
			}
		}
	}
	
}
