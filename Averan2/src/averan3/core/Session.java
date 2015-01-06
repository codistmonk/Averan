package averan3.core;

import static net.sourceforge.aprog.tools.Tools.last;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import averan3.core.Proof.Deduction;

/**
 * @author codistmonk (creation 2015-01-06)
 */
public final class Session implements Serializable {
	
	private final List<Deduction> deductions = new ArrayList<>();
	
	public final List<Deduction> getDeductions() {
		return this.deductions;
	}
	
	public final Deduction getCurrentDeduction() {
		return this.deductions.isEmpty() ? null : last(this.getDeductions());
	}
	
	private static final long serialVersionUID = 2232568962812683141L;
	
	private static final Deque<Session> stack = new ArrayDeque<>();
	
	public static final Session start() {
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
	
}
