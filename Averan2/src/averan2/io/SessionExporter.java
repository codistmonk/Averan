package averan2.io;

import java.io.Serializable;
import java.util.Map;

import averan2.core.Composite;
import averan2.core.Expression;
import averan2.core.Module;
import averan2.core.Session;
import averan2.core.Module.Proof;
import averan2.core.Session.Frame;

/**
 * @author codistmonk (creation 2014-12-21)
 */
public final class SessionExporter implements Serializable {
	
	public static final void export(final Session session, final SessionExporter.Output output) {
		output.beginSession(session);
		
		if (!session.getFrames().isEmpty()) {
			exportFrame(session, 0, output);
		}
		
		output.endSession();
	}
	
	public static final void exportFrame(final Session session, final int index, final SessionExporter.Output output) {
		final Frame frame = session.getFrames().get(index);
		
		output.beginFrame(frame);
		
		output.beginModule(frame.getModule());
		
		{
			final Module module = frame.getModule();
			
			output.beginConditions(module.getConditions());
			
			for (final Map.Entry<String, Integer> entry : module.getConditionIds().entrySet()) {
				final Expression<?> condition = module.getConditions().get(entry.getValue());
				output.processCondition(entry.getKey(), condition);
			}
			
			output.endConditions();
			
			output.beginFacts(module.getFacts());
			
			for (final Map.Entry<String, Integer> entry : module.getFactIds().entrySet()) {
				final Expression<?> fact = module.getFacts().get(entry.getValue());
				output.beginFact(entry.getKey(), fact);
				
				output.beginProof(module.getProof(entry.getKey()));
				// TODO
				output.endProof();
			}
			
			output.endFacts();
		}
		
		if (index + 1 < session.getFrames().size()) {
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
		
		public default void beginConditions(final Composite<Expression<?>> conditions) {
			// NOP
		}
		
		public default void processCondition(final String conditionName, final Expression<?> conditionProposition) {
			// NOP
		}
		
		public default void endConditions() {
			// NOP
		}
		
		public default void beginFacts(final Composite<Expression<?>> facts) {
			// NOP
		}
		
		public default void beginFact(final String factName, final Expression<?> factProposition) {
			// NOP
		}
		
		public default void beginProof(final Proof factProof) {
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