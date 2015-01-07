package averan4.core;

import static averan4.core.Composite.FORALL;
import static net.sourceforge.aprog.tools.Tools.append;
import static net.sourceforge.aprog.tools.Tools.array;
import static net.sourceforge.aprog.tools.Tools.ignore;
import static net.sourceforge.aprog.tools.Tools.last;
import static net.sourceforge.aprog.tools.Tools.lastIndex;
import averan4.core.Proof.Deduction;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

/**
 * @author codistmonk (creation 2015-01-06)
 */
public final class Session implements Serializable {
	
	private final List<Deduction> deductions = new ArrayList<>();
	
	public final List<Deduction> getDeductions() {
		return this.deductions;
	}
	
	public final Deduction removeCurrentDeduction() {
		return this.getDeductions().remove(lastIndex(this.getDeductions()));
	}
	
	public final Deduction getCurrentDeduction() {
		return this.deductions.isEmpty() ? null : last(this.getDeductions());
	}
	
	private static final long serialVersionUID = 2232568962812683141L;
	
	private static final Deque<Session> stack = new ArrayDeque<>();
	
	public static final Session begin() {
		final Session result = new Session();
		
		stack.addLast(result);
		
		return result;
	}
	
	public static final Session end() {
		return stack.removeLast();
	}
	
	public static final Session session() {
		return stack.getLast();
	}
	
	public static final Expression<?> $(final Object... objects) {
		if (objects.length == 1) {
			final Object object = objects[0];
			
			return object instanceof Expression<?> ? (Expression<?>) object : new Symbol<>(object);
		}
		
		return $$(objects);
	}
	
	public static final Composite<Expression<?>> $$(final Object... objects) {
		final Composite<Expression<?>> result = new Composite<>();
		
		for (final Object element : objects) {
			result.add(element instanceof Expression<?> ? (Expression<?>) element : $(element));
		}
		
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public static final Composite<Expression<?>> forall(final Variable... variables) {
		if (variables.length == 0) {
			throw new IllegalArgumentException();
		}
		
		return list(append(array((Expression<?>) FORALL), variables));
	}
	
	public static final Composite<Expression<?>> list(final Expression<?>... elements) {
		final Composite<Expression<?>> result = new Composite<>();
		
		for (final Expression<?> element : elements) {
			result.append(element);
		}
		
		return result;
	}
	
	public static final Deduction deduction() {
		return session().getCurrentDeduction();
	}
	
	public static final Deduction deduce() {
		return deduce(null, null);
	}
	
	public static final Deduction deduce(final String propositionName) {
		return deduce(propositionName, null);
	}
	
	public static final Deduction deduce(final Expression<?> goal) {
		return deduce(null, goal);
	}
	
	public static final Deduction deduce(final String propositionName, final Expression<?> goal) {
		final Deduction result = new Deduction(deduction(), propositionName, goal);
		
		session().getDeductions().add(result);
		
		return result;
	}
	
	public static final void include(final Deduction deduction, final Expression<?>... arguments) {
		deduction().include(deduction, arguments);
	}
	
	public static final Deduction cancel() {
		return session().removeCurrentDeduction();
	}
	
	public static final void abort(final String message) {
		throw new RuntimeException(message);
	}
	
	public static final String name(final int index) {
		return deduction().findPropositionName(index);
	}
	
	public static final <E extends Expression<?>> E proposition(final String name) {
		return deduction().findProposition(name);
	}
	
	public static final void intros() {
		try {
			while (true) {
				introduce();
			}
		} catch (final Exception exception) {
			ignore(exception);
		}
	}
	
	public static final <E extends Expression<?>> E introduce() {
		return introduce(null);
	}
	
	public static final <E extends Expression<?>> E introduce(final String parameterOrPropositionName) {
		return deduction().introduce(parameterOrPropositionName);
	}
	
	public static final void suppose(final Expression<?> proposition) {
		suppose(null, proposition);
	}
	
	public static final void suppose(final String propositionName, final Expression<?> proposition) {
		deduction().new Supposition(propositionName, proposition).conclude();
	}
	
	public static final void apply(final String ruleName, final String conditionName) {
		apply(null, ruleName, conditionName);
	}
	
	public static final void apply(final String propositionName, final String ruleName, final String conditionName) {
		deduction().new ModusPonens(propositionName, ruleName, conditionName).conclude();
	}
	
	public static final void substitute(final Composite<Expression<?>> substitutionExpression) {
		substitute(null, substitutionExpression);
	}
	
	public static final void substitute(final String propositionName, final Composite<Expression<?>> substitutionExpression) {
		deduction().new Substitution(propositionName, substitutionExpression).conclude();
	}
	
	public static final void rewrite(final String targetName, final String equalityName, final int... indices) {
		rewrite(null, targetName, equalityName, indices);
	}
	
	public static final void rewrite(final String propositionName, final String targetName, final String equalityName, final int... indices) {
		rewrite(propositionName, targetName, array(equalityName), indices);
	}
	
	public static final void rewrite(final String targetName, final String[] equalityNames, final int... indices) {
		rewrite(null, targetName, equalityNames, indices);
	}
	
	public static final void rewrite(final String propositionName, final String targetName, final String[] equalityNames, final int... indices) {
		deduction().new Rewrite(propositionName, targetName).using(equalityNames).at(indices).conclude();
	}
	
	public static final void bind(final String targetName, final Expression<?>... values) {
		bind(null, targetName, values);
	}
	
	public static final void bind(final String propositionName, final String targetName, final Expression<?>... values) {
		deduction().new Binding(propositionName, targetName, values).conclude();
	}
	
	public static final Deduction conclude() {
		return conclude(null);
	}
	
	public static final Deduction conclude(final String conclusionMessage) {
		final Deduction result = session().removeCurrentDeduction();
		
		result.conclude(conclusionMessage);
		
		return result;
	}
	
	public static final void export(final Session session, final Output output) {
		output.beginSession(session);
		
		export(session.getDeductions().iterator(), output);
		
		output.endSession(session);
	}
	
	public static final void export(final Iterator<Deduction> i, final Output output) {
		if (!i.hasNext()) {
			return;
		}
		
		final Deduction deduction = i.next();
		
		output.beginDeduction(deduction);
		
		deduction.getProofs().forEach(output::processProof);
		
		export(i, output);
		
		output.endDeduction(deduction);
	}
	
	public static final void export(final Deduction deduction, final Output output) {
		output.beginDeduction(deduction);
		
		deduction.getProofs().forEach(output::processProof);
		
		output.endDeduction(deduction);
	}
	
	/**
	 * @author codistmonk (creation 2015-01-06)
	 */
	public static abstract interface Output extends Serializable {
		
		public default void beginSession(final Session session) {
			ignore(session);
		}
		
		public default void beginDeduction(final Deduction deduction) {
			ignore(deduction);
		}
		
		public default void processProof(final Proof proof) {
			ignore(proof);
		}
		
		public default void endDeduction(final Deduction deduction) {
			ignore(deduction);
		}
		
		public default void endSession(final Session session) {
			ignore(session);
		}
		
	}
	
}
