package averan.demo;

import static net.sourceforge.aprog.tools.Tools.ignore;
import static net.sourceforge.aprog.tools.Tools.unchecked;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import averan.core.Expression;
import averan.core.Module;
import averan.core.Session;
import averan.core.Module.Symbol;
import net.sourceforge.aprog.tools.IllegalInstantiationException;

/**
 * @author codistmonk (creation 2014-08-11)
 */
public abstract class SessionTools {
	
	protected SessionTools() {
		throw new IllegalInstantiationException();
	}
	
	private static final Map<Class<?>, Session> sessions = new HashMap<>();
	
	protected static final <E extends Expression> E getCurrentGoal() {
		return getOrCreateSession().getCurrentContext().getCurrentGoal();
	}
	
	protected static final Symbol parameter(final String name) {
		return getOrCreateSession().getCurrentModule().parameter(name);
	}
	
	protected static final <E extends Expression> E getCondition(final int index) {
		return getOrCreateSession().getCondition(index);
	}
	
	protected static final String getConditionName(final int index) {
		return getOrCreateSession().getConditionName(index);
	}
	
	protected static final <E extends Expression> E getFact(final int index) {
		return getOrCreateSession().getFact(index);
	}
	
	protected static final String getFactName(final int index) {
		return getOrCreateSession().getFactName(index);
	}
	
	@SuppressWarnings("unchecked")
	protected static final <E extends Expression> E introduce() {
		final Session session = getOrCreateSession();
		final List<Symbol> parameters = session.getCurrentModule().getParameters();
		final List<Expression> conditions = session.getCurrentModule().getConditions();
		final int oldParameterCount = parameters.size();
		final int oldConditionCount = conditions.size();
		
		session.introduce();
		
		if (oldParameterCount < parameters.size()) {
			return (E) session.getParameter(-1);
		}
		
		if (oldConditionCount < conditions.size()) {
			return session.getCondition(-1);
		}
		
		throw new IllegalStateException();
	}
	
	protected static final void suppose(final Expression condition) {
		getOrCreateSession().suppose(condition);
	}
	
	protected static final void suppose(final String conditionName, final Expression proposition) {
		getOrCreateSession().suppose(conditionName, proposition);
	}
	
	protected static final void admit(final Expression fact) {
		getOrCreateSession().admit(fact);
	}
	
	protected static final void admit(final String factName, final Expression fact) {
		getOrCreateSession().admit(factName, fact);
	}
	
	protected static final void recall(final String propositionName) {
		getOrCreateSession().recall(propositionName);
	}
	
	protected static final void recall(final String factName, final String propositionName) {
		getOrCreateSession().recall(factName, propositionName);
	}
	
	protected static final void claim(final Expression fact) {
		getOrCreateSession().claim(fact);
	}
	
	protected static final void claim(final String factName, final Expression fact) {
		getOrCreateSession().claim(factName, fact);
	}
	
	protected static final void bind(final String moduleName, final Expression... expressions) {
		getOrCreateSession().bind(moduleName, expressions);
	}
	
	protected static final void bind(final String factName, final String moduleName, final Expression... expressions) {
		getOrCreateSession().bind(factName, moduleName, expressions);
	}
	
	protected static final void apply(final String moduleName, final String conditionName) {
		getOrCreateSession().apply(moduleName, conditionName);
	}
	
	protected static final void apply(final String factName, final String moduleName, final String conditionName) {
		getOrCreateSession().apply(factName, moduleName, conditionName);
	}
	
	protected static final void rewrite(final String sourceName, final String equalityName, final Integer... indices) {
		getOrCreateSession().rewrite(sourceName, equalityName, indices);
	}
	
	protected static final void rewrite(final String factName, final String sourceName, final String equalityName, final Integer... indices) {
		getOrCreateSession().rewrite(factName, sourceName, equalityName, indices);
	}
	
	protected static final Session getOrCreateSession() {
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
