package averan2.core;

import static org.junit.Assert.*;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

/**
 * @author codistmonk (creation 2014-12-13)
 */
public final class SessionTest {
	
	@Test
	public void test() {
		fail("Not yet implemented");
	}
	
	/**
	 * @author codistmonk (creation 2014-12-13)
	 */
	public static final class Session implements Serializable {
		
		private final List<Context> stack;
		
		public Session(final Module root) {
			this.stack = new LinkedList<>();
			this.stack.add(new Context(root, null));
		}
		
		public final Session push(final String goalName, final Expression goal) {
			final Module currentModule = this.getCurrentModule();
			
			this.stack.add(0, new Context(new Module(currentModule, new Symbol(currentModule, goalName)), goal));
			
			return this;
		}
		
		public final Context getCurrentContext() {
			return this.stack.isEmpty() ? null : this.stack.get(0);
		}
		
		public final Module getCurrentModule() {
			final Context context = this.getCurrentContext();
			
			return context == null ? null : context.getModule();
		}
		
		public final <E extends Expression> E getCurrentGoal() {
			final Context context = this.getCurrentContext();
			
			return context == null ? null : context.getGoal();
		}
		
		private static final long serialVersionUID = -4301114061803192020L;
		
		/**
		 * @author codistmonk (creation 2014-12-13)
		 */
		public static final class Context implements Serializable {
			
			private final Module module;
			
			private final Expression goal;
			
			public Context(final Module module, final Expression goal) {
				this.module = module;
				this.goal = goal;
			}
			
			public final Module getModule() {
				return this.module;
			}
			
			public final <E extends Expression> E getGoal() {
				return (E) this.goal;
			}
			
			private static final long serialVersionUID = -3183421913917977496L;
			
		}
		
	}
	
}
