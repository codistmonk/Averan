package jrewrite;

import static java.lang.Math.max;
import static java.util.Collections.nCopies;
import static net.sourceforge.aprog.tools.Tools.cast;
import static net.sourceforge.aprog.tools.Tools.debugPrint;
import static org.junit.Assert.*;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

/**
 * @author codistmonk (creation 2013-10-25)
 */
public final class JRewrite2Test {
	
	@Test
	public final void test1() {
		debugPrint(new Date());
		
		final ProofContext context = new ProofContext(null);
		final LeafPattern a = new LeafPattern("A");
		final LeafPattern b = new LeafPattern("B");
		
		context.getFacts().add(a);
		context.getFacts().add(new Rule(a, b));
		context.getGoals().add(b);
		
		context.printTo(System.out);
		
		context.applyRule(2);
		
		context.printTo(System.out);
	}
	
	@Test
	public final void test2() {
		debugPrint(new Date());
		
		final ProofContext context = new ProofContext(null);
		final LeafPattern a = new LeafPattern("A");
		final LeafPattern b = new LeafPattern("B");
		final LeafPattern plus = new LeafPattern("+");
		
		context.getFacts().add(sequence(a, plus, b));
		context.getFacts().add(rule(sequence(a, plus, b), sequence(b, plus, a)));
		context.getGoals().add(sequence(b, plus, a));
		
		context.printTo(System.out);
		
		context.applyRule(2);
		
		context.printTo(System.out);
	}
	
	
	@Test
	public final void test3() {
		debugPrint(new Date());
		
		final ProofContext context = new ProofContext(null);
		final LeafPattern a = new LeafPattern("A");
		final LeafPattern b = new LeafPattern("B");
		final LeafPattern plus = new LeafPattern("+");
		
		context.getFacts().add(sequence(a, plus, b));
		context.getGoals().add(sequence(b, plus, a));
		
		context.printTo(System.out);
		
		context.substitueInFact(0, substitute("A", b, "B", a));
		
		context.printTo(System.out);
		
		context.substitueInGoal(0, substitute("A", b, "B", a));
		
		context.printTo(System.out);
	}
	
	public static final Map<String, Proposition> substitute(final Object... keyValues) {
		final Map<String, Proposition> result = new LinkedHashMap<String, Proposition>();
		final int n = keyValues.length;
		
		for (int i = 0; i < n; i += 2) {
			result.put((String) keyValues[i], (Proposition) keyValues[i + 1]);
		}
		
		return result;
	}
	
	public static final String join(final List<?> list, final String separator) {
		final StringBuilder resultBuilder = new StringBuilder();
		
		if (!list.isEmpty()) {
			resultBuilder.append(list.get(0));
			final int n = list.size();
			
			for (int i = 1; i < n; ++i) {
				resultBuilder.append(separator).append(list.get(i));
			}
		}
		
		return resultBuilder.toString();
	}
	
	public static final SequencePattern sequence(final Proposition... propositions) {
		return new SequencePattern(Arrays.asList(propositions));
	}
	
	public static final Rule rule(final Proposition condition, final Proposition consequence) {
		return new Rule(condition, consequence);
	}
	
	/**
	 * @author codistmonk (creation 2013-10-25)
	 */
	public static final class ProofContext implements Serializable {
		
		private final ProofContext parent;
		
		private final List<Proposition> facts;
		
		private final List<Proposition> goals;
		
		public ProofContext(final ProofContext parent) {
			this.parent = parent;
			this.facts = new ArrayList<Proposition>();
			this.goals = new ArrayList<Proposition>();
		}
		
		public final ProofContext getParent() {
			return this.parent;
		}
		
		public final List<Proposition> getFacts() {
			return this.facts;
		}
		
		public final List<Proposition> getGoals() {
			return this.goals;
		}
		
		public final void applyRule(final int factIndex) {
			((Rule) this.getFacts().get(factIndex - 1)).apply(this);
		}
		
		public final void printTo(final PrintStream out) {
			printFactsAnsGoals(this.getFacts(), this.getGoals(), out);
		}
		
		public final void substitueInFact(final int factIndex, final Map<String, Proposition> substitutions) {
			substituteInPropotition(this.getFacts(), factIndex, substitutions);
		}
		
		public final void substitueInGoal(final int factIndex, final Map<String, Proposition> substitutions) {
			substituteInPropotition(this.getGoals(), factIndex, substitutions);
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = -1527162683657631334L;

		public static final void printFactsAnsGoals(final List<?> facts, final List<?> goals, final PrintStream out) {
			out.println();
			out.println(join(nCopies(printPropositions(facts, out), "="), ""));
			printPropositions(goals, out);
			out.println();
		}
		
		public static final int printPropositions(final List<?> propositions, final PrintStream out) {
			int result = 0;
			final int propositionCount = propositions.size();
			
			for (int i = 0; i < propositionCount; ++i) {
				final String line = i + ": " + propositions.get(i);
				
				out.println(line);
				result = max(result, line.length());
			}
			
			return result;
		}
		
		public static final void substituteInPropotition(final List<Proposition> propositions,
				final int propositionIndex, final Map<String, Proposition> substitutions) {
			if (0 <= propositionIndex) {
				propositions.set(propositionIndex, propositions.get(propositionIndex).substitute(substitutions));
			} else {
				final int n = propositions.size();
				
				for (int i = 0; i < n; ++i) {
					substituteInPropotition(propositions, i, substitutions);
				}
			}
		}
		
	}
	
	/**
	 * @author codistmonk (creation 2013-10-25)
	 */
	public static abstract class Proposition implements Serializable {
		
		private final String representation;
		
		protected Proposition(final String representation) {
			this.representation = representation;
		}
		
		@Override
		public final int hashCode() {
			return this.toString().hashCode();
		}
		
		@Override
		public final boolean equals(final Object object) {
			final Proposition that = cast(Proposition.class, object);
			
			return that != null && this.toString().equals(that.toString());
		}
		
		@Override
		public final String toString() {
			return this.representation;
		}
		
		public abstract Proposition substitute(Map<String, Proposition> substitutions);
		
	}
	
	/**
	 * @author codistmonk (creation 2013-10-25)
	 */
	public static final class SequencePattern extends Proposition {
		
		private final List<Proposition> sequence;
		
		public SequencePattern(final List<Proposition> sequence) {
			super(join(sequence, " "));
			this.sequence = sequence;
		}
		
		public final List<Proposition> getSequence() {
			return this.sequence;
		}
		
		@Override
		public final Proposition substitute(final Map<String, Proposition> substitutions) {
			final List<Proposition> newSequence = new ArrayList<Proposition>(this.getSequence().size());
			
			for (final Proposition sub : this.getSequence()) {
				newSequence.add(sub.substitute(substitutions));
			}
			
			return new SequencePattern(newSequence);
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = 3139628136685201590L;
		
	}
	
	/**
	 * @author codistmonk (creation 2013-10-25)
	 */
	public static final class LeafPattern extends Proposition {
		
		public LeafPattern(final String representation) {
			super(representation);
		}
		
		@Override
		public final Proposition substitute(final Map<String, Proposition> substitutions) {
			final Proposition value = substitutions.get(this.toString());
			
			return value != null ? value : new LeafPattern(this.toString());
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = 3139628136685201590L;
		
	}
	
	/**
	 * @author codistmonk (creation 2013-10-25)
	 */
	public static final class Rule extends Proposition {
		
		private final Proposition condition;
		
		private final Proposition consequence;
		
		public Rule(final Proposition condition, final Proposition consequence) {
			super("(" + condition + ") -> (" + consequence + ")");
			this.condition = condition;
			this.consequence = consequence;
		}
		
		public final Proposition getCondition() {
			return this.condition;
		}
		
		public final Proposition getConsequence() {
			return this.consequence;
		}
		
		public final void apply(final ProofContext context) {
			boolean ok = false;
			
			for (final Proposition fact : context.getFacts()) {
				if (this.getCondition().equals(fact)) {
					ok = true;
					break;
				}
			}
			
			if (ok) {
				for (int i = context.getGoals().size() - 1; 0 <= i; --i) {
					if (this.getConsequence().equals(context.getGoals().get(i))) {
						context.getGoals().remove(i);
					}
				}
				
				context.getFacts().add(this.getConsequence());
			}
		}
		
		@Override
		public final Proposition substitute(final Map<String, Proposition> substitutions) {
			return new Rule(this.getCondition().substitute(substitutions), this.getConsequence().substitute(substitutions));
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = 6525443711413877051L;
		
	}
	
}
