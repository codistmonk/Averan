package averan3.core;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.aprog.tools.Tools;

/**
 * @author codistmonk (creation 2014-12-14)
 */
public final class Session implements Serializable {
	
	private Module context;
	
	private final Map<Module, Expression> goals;
	
	public Session() {
		this.context = new Module();
		this.goals = new IdentityHashMap<>();
	}
	
	public final Module getContext() {
		return this.context;
	}
	
	public final Expression getGoal(final Module context) {
		return this.goals.get(context);
	}
	
	public final Expression getGoal() {
		return this.getGoal(this.getContext());
	}
	
	public final Session push(final Expression goal) {
		this.context = new Module(this.getContext());
		this.goals.put(this.getContext(), goal);
		
		return this;
	}
	
	public final Session pop() {
		final Module toRemove = this.getContext();
		this.context = toRemove.getContext();
		this.goals.remove(toRemove);
		
		return this;
	}
	
	private static final long serialVersionUID = 2232568962812683141L;
	
	/**
	 * @author codistmonk (creation 2014-12-14)
	 */
	public static abstract interface Exporter<Value> extends Serializable {
		
		public abstract Value export(Session session);
		
	}
	
	/**
	 * @author codistmonk (creation 2014-12-14)
	 */
	public static final class ConsoleExporter implements Exporter<Void> {
		
		private final PrintStream out = System.out;
		
		@Override
		public final Void export(final Session session) {
			final List<Module> modules = new ArrayList<>();
			
			{
				Module context = session.getContext();
				
				while (context != null) {
					modules.add(0, context);
					context = context.getContext();
				}
			}
			
			this.export(session, modules, 0);
			
			return null;
		}
		
		private final void export(final Session session, final List<Module> modules, final int i) {
			if (modules.size() <= i) {
				return;
			}
			
			final String indent = Tools.join("", Collections.nCopies(i, '\t').toArray());
			final Module context = modules.get(i);
			
			this.out.println(indent + "((MODULE))");
			this.out.println(indent + "\t" + context);
			
			this.export(session, modules, i + 1);
			
			this.out.println(indent + "((GOAL))");
			this.out.println(indent + "\t" + session.getGoal(context));
		}
		
		private static final long serialVersionUID = -5360958720713515781L;
		
		public static final int getLevel(final Module module) {
			return module == null ? -1 : 1 + getLevel(module.getContext());
		}
		
	}
	
}
