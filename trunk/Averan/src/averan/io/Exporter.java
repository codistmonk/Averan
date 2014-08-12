package averan.io;

import static net.sourceforge.aprog.tools.Tools.cast;

import averan.core.Expression;
import averan.core.Module;
import averan.core.Module.Claim;
import averan.core.Module.Statement;
import averan.tactics.Session;
import averan.tactics.Session.ProofContext;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author codistmonk (creation 2014-08-08)
 */
public final class Exporter implements Serializable {
	
	private final Session session;
	
	private final ExporterOutput output;
	
	private final int maximumProofDepth;
	
	public Exporter(final Session session) {
		this(session, 0);
	}
	
	public Exporter(final Session session, final int maximumProofDepth) {
		this(session, new Printer(), maximumProofDepth);
	}
	
	public Exporter(final Session session, final ExporterOutput output, final int maximumProofDepth) {
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
				
				if (currentProofDepth <= this.maximumProofDepth) {
					this.output.beginModuleFactProof();
					
					final Statement command = statements.get(entry.getValue());
					final Claim claim = cast(Claim.class, command);
					
					if (claim == null || currentProofDepth == this.maximumProofDepth) {
						this.output.processModuleFactProof(command);
					} else  {
						this.exportModule(claim.getProofContext(), currentProofDepth + 1);
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
	
}

