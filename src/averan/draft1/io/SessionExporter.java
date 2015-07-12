package averan.draft1.io;

import static multij.tools.Tools.cast;
import averan.draft1.core.Expression;
import averan.draft1.core.Module;
import averan.draft1.core.Session;
import averan.draft1.core.Module.Claim;
import averan.draft1.core.Module.Statement;
import averan.draft1.core.Session.ProofContext;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author codistmonk (creation 2014-08-08)
 */
public final class SessionExporter implements Serializable {
	
	private final Session session;
	
	private final Output output;
	
	private final int maximumProofDepth;
	
	public SessionExporter(final Session session) {
		this(session, 0);
	}
	
	public SessionExporter(final Session session, final int maximumProofDepth) {
		this(session, new SimplePrinter(), maximumProofDepth);
	}
	
	public SessionExporter(final Session session, final Output output, final int maximumProofDepth) {
		this.session = session;
		this.output = output;
		this.maximumProofDepth = maximumProofDepth;
	}
	
	public final void exportSession() {
		this.output.beginSession();
		
		this.exportContext(this.session.getStack().size() - 1);
		
		this.output.endSession();
	}
	
	private final void exportContext(final int i) {
		final ProofContext context = this.session.getStack().get(i);
		final Module module = context.getModule();
		
		this.output.subcontext(context.getName());
		
		this.exportModule(module, 0);
		
		if (0 < i) {
			this.exportContext(i - 1);
		}
		
		final Expression currentGoal = context.getCurrentGoal();
		
		if (currentGoal != null) {
			this.output.processCurrentGoal(currentGoal);
		}
	}
	
	private final void exportModule(final Module module, final int currentProofDepth) {
		if (!module.getParameters().isEmpty()) {
			this.output.processModuleParameters(module);
		}
		
		final List<Expression> conditions = module.getConditions();
		
		if (!conditions.isEmpty()) {
			this.output.beginModuleConditions(module);
			
			for (final Map.Entry<String, Integer> entry : module.getConditionIndices().entrySet()) {
				this.output.processModuleCondition(entry.getKey(), conditions.get(entry.getValue()));
			}
			
			this.output.endModuleConditions(module);
		}
		
		{
			this.output.beginModuleFacts(module);
			
			final List<Expression> facts = module.getFacts();
			final List<Statement> statements = module.getStatements();
			
			for (final Map.Entry<String, Integer> entry : module.getFactIndices().entrySet()) {
				this.output.processModuleFact(entry.getKey(), facts.get(entry.getValue()));
				
				if (currentProofDepth < this.maximumProofDepth) {
					this.output.beginModuleFactProof();
					
					final Statement command = statements.get(entry.getValue());
					final Claim claim = cast(Claim.class, command);
					
					if (claim == null || currentProofDepth + 1 == this.maximumProofDepth) {
						this.output.processModuleFactProof(command);
					} else  {
						this.exportModule(claim.getProofContext(), currentProofDepth + 2);
					}
					
					this.output.endModuleFactProof();
				}
			}
			
			this.output.endModuleFacts(module);
		}
	}
	
	/**
	 * {@value}.
	 */
	private static final long serialVersionUID = 2272468614566549833L;
	
	/**
	 * @author codistmonk (creation 2014-08-08)
	 */
	public interface Output extends Serializable {
		
		public abstract void beginSession();
		
		public abstract void subcontext(String name);
		
		public abstract void processModuleParameters(Module module);
		
		public abstract void beginModuleConditions(Module module);
		
		public abstract void processModuleCondition(String conditionName, Expression condition);
		
		public abstract void endModuleConditions(Module module);
		
		public abstract void beginModuleFacts(Module module);
		
		public abstract void processModuleFact(String factName, Expression fact);
		
		public abstract void beginModuleFactProof();
		
		public abstract void processModuleFactProof(Statement command);
		
		public abstract void endModuleFactProof();
		
		public abstract void endModuleFacts(Module module);
		
		public abstract void processCurrentGoal(Expression currentGoal);
		
		public abstract void endSession();
		
	}
	
}
