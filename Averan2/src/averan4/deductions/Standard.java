package averan4.deductions;

import static averan4.core.AveranTools.*;
import static java.util.Arrays.asList;

import java.util.Arrays;
import java.util.List;

import net.sourceforge.aprog.tools.IllegalInstantiationException;

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
	
	@SuppressWarnings("unchecked")
	public static final void rewriteLeft(final String targetName, final String equalityName, final int... indices) {
		subdeduction();
		
		final List<Object> target = checkProposition(targetName);
		
		// rewrite: \/P P -> \/X,Y X=Y -> \/I,Q P|X=Y@[I] = Q -> Q 
		bind("rewriteLeft", target);
		apply(name(-1), targetName);
		
		final List<Object> equality = checkEquality(equalityName);
		
		bind(name(-1), left(equality), right(equality));
		apply(name(-1), equalityName);
		substitute(target, map(left(equality), right(equality)), indices);
		bind(name(-2), indices(indices), right(proposition(-1)));
		apply(name(-1), name(-2));
		
		set(conclude().getMessage(), "By left rewriting in", targetName, "using", equalityName, "at",
				Arrays.stream(indices).mapToObj(Integer::valueOf).collect(toTreeSet()));
	}
	
	public static final void deduceRecall() {
		subdeduction("recall");
		
		final List<Object> x = forall("X");
		
		suppose(x);
		
		bind("identity", x);
		rewriteLeft(name(-2), name(-1));
		
		conclude();
	}
	
}
