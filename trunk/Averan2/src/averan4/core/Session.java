package averan4.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
		// TODO
		return this.accept();
	}
	
	public final Session apply(final String factName, final String moduleName, final String conditionName) {
		// TODO
		return this.accept();
	}
	
	public final Session substitute(final String factName, final Expression<?> expression, final Composite<?>... equalities) {
		// TODO
		return this.accept();
	}
	
	public final Session rewrite(final String factName, final String propositionName, final String equalityName,
			final int... indices) {
		// TODO
		return this.accept();
	}
	
	public final Module getCurrentModule() {
		final Frame currentFrame = this.getCurrentFrame();
		
		return currentFrame == null ? null : currentFrame.getModule();
	}
	
	public final Frame getCurrentFrame() {
		return this.frames.isEmpty() ? null : this.frames.get(this.frames.size() - 1);
	}
	
	/**
	 * @author codistmonk (creation 2014-12-20)
	 */
	public final class Frame implements Serializable {
		
		private final String name;
		
		private final Module module;
		
		private Expression<?> goal;
		
		public Frame(final String name, final Expression<?> goal) {
			this.name = name;
			this.module = new Module(getCurrentModule());
			this.goal = goal;
		}
		
		public final String getName() {
			return this.name;
		}
		
		public final Module getModule() {
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
			
			if (!session.frames.isEmpty()) {
				exportFrame(session, 0, output);
			}
			
			output.endSession();
		}
		
		public static final void exportFrame(final Session session, final int index, final Output output) {
			final Frame frame = session.frames.get(index);
			
			output.beginFrame(frame);
			
			output.beginModule(frame.getModule());
			
			{
				final Module module = frame.getModule();
				
				output.beginConditions();
				
				for (final Map.Entry<String, Integer> entry : module.getConditionIndices().entrySet()) {
					final Expression<?> condition = module.getConditions().get(entry.getValue());
					output.processCondition(entry.getKey(), condition);
				}
				
				output.endConditions();
				
				output.beginFacts();
				
				final Composite<Expression<?>> factList = module.get(1);
				
				for (final Map.Entry<String, Integer> entry : module.getFactIndices().entrySet()) {
					final Expression<?> fact = module.getConditions().get(entry.getValue());
					output.beginFact(entry.getKey(), fact);
					
					output.beginProof(module.getProof(entry.getKey()));
					// TODO
					output.endProof();
				}
				
				output.endFacts();
			}
			
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
			
			public default void beginModule(final Module module) {
				// NOP
			}
			
			public default void beginConditions() {
				// NOP
			}
			
			public default void processCondition(final String conditionName, final Expression<?> conditionProposition) {
				// NOP
			}
			
			public default void endConditions() {
				// NOP
			}
			
			public default void beginFacts() {
				// NOP
			}
			
			public default void beginFact(final String factName, final Expression<?> factProposition) {
				// NOP
			}
			
			public default void beginProof(final Module.Proof factProof) {
				// NOP
			}
			
			public default void endProof() {
				// NOP
			}
			
			public default void endFact() {
				// NOP
			}
			
			public default void endFacts() {
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
