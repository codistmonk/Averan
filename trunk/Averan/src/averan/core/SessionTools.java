package averan.core;

import averan.core.Module.Bind;
import averan.core.Module.Symbol;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.aprog.tools.IllegalInstantiationException;

/**
 * @author codistmonk (creation 2014-08-11)
 */
public final class SessionTools {
	
	private SessionTools() {
		throw new IllegalInstantiationException();
	}
	
	private static final List<Session> sessions = new ArrayList<>();
	
	public static final Session session() {
		return sessions.get(0);
	}
	
	public static final Session pushNewSession(final Module module) {
		return pushSession(new Session(module));
	}
	
	public static final Session pushSession(final Session session) {
		sessions.add(0, session);
		
		return session;
	}
	
	public static final Session popSession() {
		return sessions.remove(0);
	}
	
	public static final <E extends Expression> E getCurrentGoal() {
		return session().getCurrentContext().getCurrentGoal();
	}
	
	public static final Symbol parameter(final String name) {
		return session().getCurrentContext().parameter(name);
	}
	
	public static final <E extends Expression> E condition(final int index) {
		return session().getCondition(index);
	}
	
	public static final String conditionName(final int index) {
		return session().getConditionName(index);
	}
	
	public static final <E extends Expression> E fact(final int index) {
		return session().getFact(index);
	}
	
	public static final String factName(final int index) {
		return session().getFactName(index);
	}
	
	public static final <E extends Expression> E proposition(final String name) {
		return session().getProposition(name);
	}
	
	public static final <E extends Expression> E introduce() {
		return session().introduceAndGet();
	}
	
	public static final void introduce(final String conditionName) {
		session().introduce(conditionName);
	}
	
	public static final void suppose(final Expression condition) {
		session().suppose(condition);
	}
	
	public static final void suppose(final String conditionName, final Expression proposition) {
		session().suppose(conditionName, proposition);
	}
	
	public static final void admit(final Expression fact) {
		session().admit(fact);
	}
	
	public static final void admit(final String factName, final Expression fact) {
		session().admit(factName, fact);
	}
	
	public static final void abort() {
		session().abort();
	}
	
	public static final void claim(final Expression fact) {
		session().claim(fact);
	}
	
	public static final void claim(final String factName, final Expression fact) {
		session().claim(factName, fact);
	}
	
	public static final void bind(final String moduleName, final Expression... expressions) {
		session().bind(moduleName, expressions);
	}
	
	public static final void bind(final String factName, final String moduleName, final Expression... expressions) {
		session().bind(factName, moduleName, expressions);
	}
	
	public static final void apply(final String moduleName, final String conditionName) {
		session().apply(moduleName, conditionName);
	}
	
	public static final void apply(final String factName, final String moduleName, final String conditionName) {
		session().apply(factName, moduleName, conditionName);
	}
	
	public static final void rewrite(final String sourceName, final String equalityName, final Integer... indices) {
		session().rewrite(sourceName, equalityName, indices);
	}
	
	public static final void rewrite(final String factName, final String sourceName, final String equalityName, final Integer... indices) {
		session().rewrite(factName, sourceName, equalityName, indices);
	}
	
	public static final void substitute(final Composite substitution) {
		session().substitute(substitution);
	}
	
	public static final void substitute(final String factName, final Composite substitution) {
		session().substitute(factName, substitution);
	}
	
	@SuppressWarnings("unchecked")
	public static final <E extends Expression> E goal() {
		return (E) session().getCurrentGoal();
	}
	
	public static final Module module() {
		return session().getCurrentModule();
	}
	
	public static final Session trust(final Module module) {
		return session().trust(module);
	}
	
	public static final void unifyAndApply(final String moduleName, final String conditionName) {
		unifyAndApply(session(), moduleName, conditionName);
	}
	
	public static final void unifyAndApply(final Session session, final String moduleName, final String conditionName) {
		final Module module = session.getProposition(moduleName);
		final Pattern anyfiedCondition = Pattern.anyfy(module.getConditions().get(0));
		
		if (!anyfiedCondition.equals(session.getProposition(conditionName))) {
			throw new IllegalArgumentException("Failed to unify (" + conditionName + ") with " + anyfiedCondition.getTemplate());
		}
		
		computeBind(session.getCurrentModule(), module, moduleName, anyfiedCondition).execute();
		
		session.apply(session.getFactName(-1), conditionName);
	}
	
	private static final Bind computeBind(final Module context, final Module module, final String moduleName, final Pattern anyfiedCondition) {
		final Bind result = context.new Bind(context, moduleName);
		
		for (final Symbol parameter : module.getParameters()) {
			final String parameterName = parameter.toString();
			
			result.bind(parameterName, anyfiedCondition.get(parameterName));
		}
		
		return result;
	}
	
}
