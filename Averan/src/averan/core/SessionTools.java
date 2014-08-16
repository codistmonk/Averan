package averan.core;

import static net.sourceforge.aprog.tools.Tools.ignore;
import static net.sourceforge.aprog.tools.Tools.unchecked;

import averan.core.Module.Symbol;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.aprog.tools.IllegalInstantiationException;

/**
 * @author codistmonk (creation 2014-08-11)
 */
public final class SessionTools {
	
	private SessionTools() {
		throw new IllegalInstantiationException();
	}
	
	private static final Map<Class<?>, Session> sessions = new HashMap<>();
	
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
	
	public static final Session session() {
		final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		
		for (int i = stackTrace.length - 1; 0 <= i; --i) {
			try {
				final Class<?> cls = Class.forName(stackTrace[i].getClassName());
				final Field moduleField = cls.getDeclaredField("MODULE");
				
				return sessions.compute(cls, (k, v) -> v == null ?
						new Session((Module) getStaticFieldValue(moduleField)) : v);
			} catch (final ClassNotFoundException exception) {
				exception.printStackTrace();
			} catch (final NoSuchFieldException exception) {
				ignore(exception);
			}
		}
		
		throw new IllegalStateException();
	}
	
	@SuppressWarnings("unchecked")
	public static final <T> T getStaticFieldValue(final Field field) {
		try {
			field.setAccessible(true);
			
			return (T) field.get(null);
		} catch (final Exception exception) {
			throw unchecked(exception);
		}
	}
	
}
