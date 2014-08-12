package averan.io;

import java.io.Serializable;

import averan.core.Expression;
import averan.core.Module;
import averan.core.Module.Statement;

/**
 * @author codistmonk (creation 2014-08-08)
 */
public interface ExporterOutput extends Serializable {
	
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
