package averan.draft2.io;

import averan.draft2.core.Expression;
import averan.draft2.core.Module;
import averan.draft2.core.Session;
import averan.draft2.core.Module.Proof;
import averan.draft2.core.Session.Frame;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
	
	public static final List<String> getConditionNames(final Module module) {
		final List<String> result = new ArrayList<>();
		
		for (final String name : module.getPropositionIds().keySet()) {
			if (module.getProof(name) == null) {
				result.add(name);
			}
		}
		
		return result;
	}
	
	public static final List<String> getFactNames(final Module module) {
		final List<String> result = new ArrayList<>();
		
		for (final String name : module.getPropositionIds().keySet()) {
			if (module.getProof(name) != null) {
				result.add(name);
			}
		}
		
		return result;
	}
	
	public static final void exportFrame(final Session session, final int index, final SessionExporter.Output output) {
		final Frame frame = session.getFrames().get(index);
		
		output.beginFrame(frame);
		
		output.beginModule(frame.getModule());
		
		{
			final Module module = frame.getModule();
			
			{
				final List<String> conditionNames = getConditionNames(module);
				
				output.beginConditions(conditionNames);
				
				for (final String name : conditionNames) {
					output.processCondition(name, module.findProposition(name));
				}
				
				output.endConditions();
			}
			
			{
				final List<String> factNames = getFactNames(module);
				
				output.beginFacts(factNames);
				
				for (final String name : factNames) {
					output.beginFact(name, module.findProposition(name));
					
					output.beginProof(module.getProof(name));
					// TODO
					output.endProof();
					
					output.endFact();
				}
				
				output.endFacts();
			}
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
		
		public default void beginConditions(final List<String> conditionNames) {
			// NOP
		}
		
		public default void processCondition(final String conditionName, final Expression<?> conditionProposition) {
			// NOP
		}
		
		public default void endConditions() {
			// NOP
		}
		
		public default void beginFacts(final List<String> factNames) {
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