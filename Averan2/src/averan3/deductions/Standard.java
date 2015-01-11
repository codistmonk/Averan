package averan3.deductions;

import static averan3.core.Composite.*;
import static averan3.core.Session.*;
import static averan3.deductions.AutoDeduce.autoDeduce;

import averan3.core.Composite;
import averan3.core.Expression;
import averan3.core.Variable;
import averan3.core.Proof.Deduction;
import averan3.io.HTMLOutput;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import net.sourceforge.aprog.tools.IllegalInstantiationException;

/**
 * @author codistmonk (creation 2015-01-07)
 */
public final class Standard {
	
	private Standard() {
		throw new IllegalInstantiationException();
	}
	
	public static final AtomicInteger autoDeduceDepth = new AtomicInteger(4); 
	
	public static final Deduction DEDUCTION = build(Standard.class.getName(), new Runnable() {
		
		@Override
		public final void run() {
			AutoDeduce3.deduceFundamentalPropositions();
			
			{
				final Variable $E = new Variable("E");
				final Variable $F = new Variable("F");
				final Variable $X = new Variable("X");
				final Variable $Y = new Variable("Y");
				final Variable $T = new Variable("..");
				
				suppose("bind1",
						$(forall($E, $X, $T, $Y, $F),
								rule($($$(FORALL, $($X, $T)), $E),
										equality($($E, list(equality($X, $Y)), list()), $F),
										$($$(FORALL, $T), $F))));
			}
			
			{
				final Variable $E = new Variable("E");
				final Variable $F = new Variable("F");
				final Variable $X = new Variable("X");
				final Variable $Y = new Variable("Y");
				final Variable $I = new Variable("I");
				final Variable $T = new Variable("..");
				
				suppose("rewrite1",
						$(forall($E, $X, $Y, $T, $I, $F),
								rule($E,
										equality($X, $Y),
										equality($($E, $$(equality($X, $Y), $T), $I), $F),
										$F)));
			}
			
			{
				final Variable $X = variable("X");
				final Variable $Y = variable("Y");
				
				deduce("left_elimination_of_equality",
						$(forall($X, $Y), rule($X, equality($X, $Y), $Y)));
				{
					intros();
					rewrite(name(-2), name(-1));
					conclude();
				}
			}
			
			{
				final Variable $X = variable("X");
				final Variable $Y = variable("Y");
				
				deduce("right_elimination_of_equality",
						$(forall($X, $Y), rule($Y, equality($X, $Y), $X)));
				{
					intros();
					rewriteRight(name(-2), name(-1));
					conclude();
				}
			}
			
			{
				final Variable $X = variable("X");
				final Variable $Y = variable("Y");
				
				suppose("introduction_of_conjunction",
						$(forall($X, $Y), rule($X, $Y, conjunction($X, $Y))));
			}
			
			{
				final Variable $X = variable("X");
				final Variable $Y = variable("Y");
				
				suppose("left_elimination_of_conjunction",
						$(forall($X, $Y), rule(conjunction($X, $Y), $X)));
			}
			
			{
				final Variable $X = variable("X");
				final Variable $Y = variable("Y");
				
				suppose("right_elimination_of_conjunction",
						$(forall($X, $Y), rule(conjunction($X, $Y), $Y)));
			}
			
			{
				final Variable $X = variable("X");
				final Variable $Y = variable("Y");
				
				check(autoDeduce("commutativity_of_conjunction",
						$(forall($X, $Y), rule(conjunction($X, $Y), conjunction($Y, $X))), 3));
			}
			
			{
				final Variable $X = variable("X");
				final Variable $Y = variable("Y");
				
				suppose("left_introduction_of_disjunction",
						$(forall($X, $Y), rule($X, disjunction($X, $Y))));
			}
			
			{
				final Variable $X = variable("X");
				final Variable $Y = variable("Y");
				
				suppose("right_introduction_of_disjunction",
						$(forall($X, $Y), rule($Y, disjunction($X, $Y))));
			}
			
			{
				final Variable $X = variable("X");
				final Variable $Y = variable("Y");
				final Variable $Z = variable("Z");
				
				suppose("elimination_of_disjunction",
						$(forall($X, $Y, $Z), rule(rule($X, $Z), rule($Y, $Z), rule(disjunction($X, $Y), $Z))));
			}
			
			{
				final Variable $X = variable("X");
				final Variable $Y = variable("Y");
				
				
				deduce("commutativity_of_disjunction",
						$(forall($X, $Y), rule(disjunction($X, $Y), disjunction($Y, $X))));
				{
					final Variable x = introduce();
					final Variable y = introduce();
					intros();
					bind("right_introduction_of_disjunction", y, x);
					apply("elimination_of_disjunction", name(-1));
					bind("left_introduction_of_disjunction", y, x);
					apply(name(-2), name(-1));
					apply(name(-1), name(-5));
					conclude();
				}
//				check(autoDeduce("commutativity_of_disjunction",
//						$(forall($X, $Y), rule(disjunction($X, $Y), disjunction($Y, $X))), 3));
			}
		}
		
	}, new HTMLOutput());
	
	public static final Composite<?> conjunction(final Object... expressions) {
		return binaryOperation("⋀", expressions);
	}
	
	public static final Composite<?> disjunction(final Object... expressions) {
		return binaryOperation("⋁", expressions);
	}
	
	public static final Composite<?> membership(final Object element, final Object set) {
		return binaryOperation("∈", element, set);
	}
	
	public static final void rewriteRight(final String targetName, final String equalityName, final int... indices) {
		rewriteRight(null, targetName, equalityName, indices);
	}
	
	public static final void rewriteRight(final String propositionName, final String targetName, final String equalityName, final int... indices) {
		deduce(propositionName);
		{
			apply("symmetry_of_equality", equalityName);
			rewrite(targetName, name(-1), indices);
			conclude("By right-rewriting (" + equalityName + ") in (" + targetName + ") at indices " + Arrays.toString(indices));
		}
	}
	
	public static final void bind1(final String targetName, final Expression<?> value) {
		bind1(null, targetName, value);
	}
	
	public static final void bind1(final String propositionName, final String targetName, final Expression<?> value) {
		deduce(propositionName);
		{
			final Composite<?> target = proposition(targetName);
			final Variable parameter = (Variable) target.getParameters().getListElement(1);
			
			apply("bind1", targetName);
			substitute($$(target.get(1), $$().append($(parameter , EQUALS, value)), $()));
			apply(name(-2), name(-1));
			conclude("By binding " + parameter.getName() + " with " + value + " in (" + targetName + ")");
		}
	}
	
	public static final void rewrite1(final String targetName, final String equalityName, final int... indices) {
		rewrite1(null, targetName, equalityName, indices);
	}
	
	@SuppressWarnings("unchecked")
	public static final void rewrite1(final String propositionName, final String targetName, final String equalityName, final int... indices) {
		deduce(propositionName);
		{
			apply("rewrite1", targetName);
			apply(name(-1), equalityName);
			
			final Composite<Expression<?>> block = proposition(name(-1));
			
			block.getParameters().getListElement(1).equals(list());
			block.getParameters().getListElement(2).equals(indices(indices));
			
			substitute((Composite<Expression<?>>) block.getContents().get(0).get(0).accept(Variable.BIND));
			apply(name(-2), name(-1));
			conclude("By rewriting (" + targetName + ") using (" + equalityName + ") at indices " + Arrays.toString(indices));
		}
	}
	
	public static final Composite<Expression<?>> indices(final int... indices) {
		final Composite<Expression<?>> result = list();
		
		for (final int index : indices) {
			result.append($(index));
		}
		
		return result;
	}
	
}
