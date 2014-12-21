package averan5.core;

import averan5.core.Composite.Module;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author codistmonk (creation 2014-12-20)
 */
public final class Session implements Serializable {
	
	private final List<Frame> frames;
	
	public Session() {
		this.frames = new ArrayList<>();
	}
	
	public final Session prove(final String factName, final Expression<?> fact) {
		this.frames.add(this.new Frame(factName, fact));
		
		return this;
	}
	
	public final Session cancelFrame() {
		this.frames.remove(this.frames.size() - 1);
		
		return this;
	}
	
	public final void cancelSession() {
		throw new RuntimeException();
	}
	
	public final Session accept(final String... factNames) {
		final Frame frame = this.frames.get(this.frames.size() - 1);
		
		if (frame.getGoal() == null) {
			// TODO
		} else {
			// TODO
		}
		
		return this;
	}
	
	public final Session introduce() {
		// TODO
		return this.accept();
	}
	
	public final Session suppose(final String conditionName, final Expression<?> conditionProposition) {
		final Composite<Expression<?>> module = (Composite<Expression<?>>) this.getCurrentFrame().getModule();
		final Composite<Expression<?>> condition = new Composite<>(module);
		
		condition.getElements().add(new Symbol(conditionName));
		condition.getElements().add(condition.attach(conditionProposition));
		
		final Composite<Expression<?>> fact;
		if (module.getElementCount() == 0) {
			fact = module;
		} else {
			module.as(Module.class).getConclusion().getElements().add(fact = new Composite<>(module));
		}
		
		fact.getElements().add(condition);
		fact.getElements().add(Expression.IMPLIES);
		fact.getElements().add(new Composite<>(module));
		
		return this.accept();
	}
	
	public final Session apply(final String factName, final String moduleName, final Expression<?> value) {
		// TODO
		return this.accept();
	}
	
	public final Session apply(final String factName, final String moduleName, final String conditionName) {
		// TODO
		return this.accept();
	}
	
	public final Session rewrite(final String factName, final String propositionName, final String equalityName,
			final int... indices) {
		// TODO
		return this.accept();
	}
	
	public final Frame getCurrentFrame() {
		return this.frames.get(this.frames.size() - 1);
	}
	
	/**
	 * @author codistmonk (creation 2014-12-20)
	 */
	public final class Frame implements Serializable {
		
		private final String name;
		
		private final Composite<?> module;
		
		private Expression<?> goal;
		
		public Frame(final String name, final Expression<?> goal) {
			this.name = name;
			this.module = new Composite<>(getCurrentFrame().getModule());
			this.goal = goal;
		}
		
		public final String getName() {
			return this.name;
		}
		
		public final Composite<?> getModule() {
			return this.module;
		}
		
		public final Expression<?> getGoal() {
			return this.goal;
		}
		
		private static final long serialVersionUID = -5943416769824876039L;
		
	}
	
	private static final long serialVersionUID = 181621455530572267L;
	
	/**
	 * @author codistmonk (creation 2014-12-21)
	 */
	public static final class Exporter implements Serializable {
		
		public static final void export(final Session session, final Output output) {
			output.beginSession(session);
			
			exportFrame(session, 0, output);
			
			output.endSession();
		}
		
		public static final void exportFrame(final Session session, final int index, final Output output) {
			final Frame frame = session.frames.get(index);
			
			output.beginFrame(frame);
			
			output.beginModule(frame.getModule());
			
			// TODO
			
			if (index + 1 < session.frames.size()) {
				exportFrame(session, index + 1, output);
			}
			
			output.processGoal(frame.getGoal());
			
			output.endModule();
			
			output.endFrame();
		}
		
		private static final long serialVersionUID = 4419798598555424573L;
		
		/**
		 * @author codistmonk (creation 2014-12-21)
		 */
		public static abstract interface Output extends Serializable {
			
			public default void beginSession(final Session session) {
				// NOP
			}
			
			public default void beginFrame(final Frame frame) {
				// NOP
			}
			
			public default void beginModule(final Composite<?> module) {
				// NOP
			}
			
			public default void processCondition(final Composite<?> condition) {
				// NOP
			}
			
			public default void beginFact(final Composite<?> fact) {
				// NOP
			}
			
			public default void beginProof(final Composite<?> proof) {
				// NOP
			}
			
			public default void endProof() {
				// NOP
			}
			
			public default void endFact() {
				// NOP
			}
			
			public default void endModule() {
				// NOP
			}
			
			public default void processGoal(final Expression<?> goal) {
				// NOP
			}
			
			public default void endFrame() {
				// NOP
			}
			
			public default void endSession() {
				// NOP
			}
			
		}
		
	}
	
}
