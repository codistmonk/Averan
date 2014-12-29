package averan2.core;

import static net.sourceforge.aprog.tools.Tools.cast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.aprog.tools.Tools;

/**
 * @author codistmonk (creation 2014-12-20)
 */
public abstract interface Expression<E extends Expression<?>> extends Container<E> {
	
	public abstract <V> V accept(Visitor<V> visitor);
	
	/**
	 * @author codistmonk (creation 2014-12-20)
	 */
	public static abstract interface Visitor<V> extends Serializable {
		
		public abstract V visit(Symbol<?> symbol);
		
		public abstract V visit(Variable variable);
		
		public abstract V visit(Composite<Expression<?>> composite);
		
		public abstract V visit(Module module);
		
		public abstract V visit(Substitution substitution);
		
		public abstract V visit(Equality equality);
		
		public static <E extends Expression<?>> E visitElementsOf(final E expression, final Visitor<?> visitor) {
			for (final Expression<?> element : expression) {
				element.accept(visitor);
			}
			
			return expression;
		}
		
	}
	
	/**
	 * @author codistmonk (creation 2014-12-28)
	 */
	public static final class CollectParameters implements Visitor<List<Variable>> {
		
		private final Map<Variable, Variable> done = new IdentityHashMap<>();
		
		private final List<Variable> result = new ArrayList<>();
		
		@Override
		public final List<Variable> visit(final Symbol<?> symbol) {
			return this.result;
		}
		
		@Override
		public final List<Variable> visit(final Variable variable) {
			if (this.done.putIfAbsent(variable, variable) == null) {
				this.result.add(variable);
			}
			
			return this.result;
		}
		
		@Override
		public final List<Variable> visit(final Composite<Expression<?>> composite) {
			Visitor.visitElementsOf(composite, this);
			
			return this.result;
		}
		
		@Override
		public final List<Variable> visit(final Module module) {
//			if (0 < module.getConditions().size()) {
//				return module.getConditions().get(0).accept(this);
//			}
			
			return module.getFacts().accept(this);
		}
		
		@Override
		public final List<Variable> visit(final Substitution substitution) {
			Visitor.visitElementsOf(substitution, this);
			
			return this.result;
		}
		
		@Override
		public final List<Variable> visit(final Equality equality) {
			Visitor.visitElementsOf(equality, this);
			
			return this.result;
		}
		
		private static final long serialVersionUID = -936926873552336509L;
		
		public static final CollectParameters collectParameters() {
			return new CollectParameters();
		}
		
	}
	
	/**
	 * @author codistmonk (creation 2014-12-29)
	 */
	public static final class GatherParameters implements Visitor<Void> {
		
		private final Map<Module, Module> moduleContexts = new IdentityHashMap<>();
		
//		private final Map<Variable, Module> variableContexts = new IdentityHashMap<>();
		private final Map<Key<Variable>, Module> variableContexts = new HashMap<>();
		
		private final List<Module> stack = new ArrayList<>();
		
		public final Map<Module, Module> getModuleContexts() {
			return this.moduleContexts;
		}
		
//		public final Map<Variable, Module> getVariableContexts() {
		public final Map<Key<Variable>, Module> getVariableContexts() {
			return this.variableContexts;
		}
		
		@Override
		public final Void visit(final Symbol<?> symbol) {
			return null;
		}
		
		@Override
		public final Void visit(final Variable variable) {
			this.getVariableContexts().put(new Key<>(variable),
					this.getCommonAncestor(this.peek(), this.getVariableContexts().get(new Key<>(variable))));
			
			return null;
		}
		
		@Override
		public final Void visit(final Composite<Expression<?>> composite) {
			Visitor.visitElementsOf(composite, this);
			
			return null;
		}
		
		@Override
		public final Void visit(final Module module) {
			final Module context = this.peek();
			final Module declaredContext = this.getModuleContexts().get(module);
			final Module actualContext = module.getContext();
			
			if (!(nullOrEqual(context, declaredContext) && nullOrEqual(context, actualContext) && nullOrEqual(declaredContext, actualContext))) {
				throw new IllegalStateException();
			}
			
			this.getModuleContexts().put(module, select(context, declaredContext, actualContext));
			
			this.push(module);
			
			Visitor.visitElementsOf(module, this);
			
			this.pop();
			
			return null;
		}
		
		@Override
		public final Void visit(final Substitution substitution) {
			Visitor.visitElementsOf(substitution, this);
			
			return null;
		}
		
		@Override
		public final Void visit(final Equality equality) {
			Visitor.visitElementsOf(equality, this);
			
			return null;
		}
		
		private final void push(final Module module) {
			this.stack.add(module);
		}
		
		private final Module peek() {
			return this.stack.isEmpty() ? null : this.stack.get(this.stack.size() - 1);
		}
		
		private final void pop() {
			this.stack.remove(this.stack.size() - 1);
		}
		
		private final Module getCommonAncestor(final Module module1, final Module module2) {
			if (nullOrEqual(module1, module2)) {
				return select(module1, module2);
			}
			
			Module m1 = module1;
			Module m2 = module2;
			int depth1 = this.getDepth(module1);
			int depth2 = this.getDepth(module2);
			
			while (depth1 < depth2) {
				m2 = this.getModuleContexts().get(m2);
				--depth2;
			}
			
			while (depth2 < depth1) {
				m1 = this.getModuleContexts().get(m1);
				--depth1;
			}
			
			if (depth1 != depth2) {
				throw new IllegalStateException();
			}
			
			while (!nullOrEqual(m1, m2)) {
				m1 = this.getModuleContexts().get(m1);
				m2 = this.getModuleContexts().get(m2);
			}
			
			if (m1 != m2) {
				Tools.debugError(module1, module2);
				Tools.debugError(m1, m2);
				throw new IllegalStateException();
			}
			
			return m1;
		}
		
		private final int getDepth(final Module module) {
			final Module context = this.getModuleContexts().get(module);
			
			return 1 + (context == null ? 0 : this.getDepth(context));
		}
		
		private static final long serialVersionUID = -1826106345804095658L;
		
		public static final boolean nullOrEqual(final Object object1, final Object object2) {
			return object1 == null || object2 == null || object1 == object2;
		}
		
		public static final <T> T select(final T... objects) {
			for (final T object : objects) {
				if (object != null) {
					return object;
				}
			}
			
			throw new IllegalStateException();
		}
		/**
		 * @author codistmonk (creation 2014-12-29)
		 *
		 * @param <T>
		 */
		public static final class Key<T> implements Serializable {
			
			private final T object;
			
			public Key(final T object) {
				this.object = object;
			}
			
			public final T getObject() {
				return this.object;
			}
			
			@Override
			public final int hashCode() {
				return this.getObject().hashCode();
			}
			
			@Override
			public final boolean equals(final Object object) {
				final Key<?> that = cast(this.getClass(), object);
				
				return that != null && this.getObject() == that.getObject();
			}
			
			@Override
			public final String toString() {
				return this.getObject().toString();
			}
			
			private static final long serialVersionUID = 1282044383571704275L;
			
		}
		
	}
	
}
