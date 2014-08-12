package averan.io;

import static java.lang.Math.max;
import static java.util.Collections.nCopies;
import static net.sourceforge.aprog.tools.Tools.join;

import averan.core.Expression;
import averan.core.Module;
import averan.core.Module.Statement;

import java.io.PrintStream;

/**
 * @author codistmonk (creation 2014-08-08)
 */
public final class Printer implements SessionExporter.Output {
	
	private final PrintStream output;
	
	private int indentLevel;
	
	private String indent;
	
	public Printer() {
		this(System.out);
	}
	
	public Printer(final PrintStream output) {
		this.output = output;
		this.indentLevel = -1;
		this.indent = "";
	}
	
	@Override
	public final void beginSession() {
		// NOP
	}
	
	@Override
	public final void subcontext(final String name) {
		this.indent = join("", nCopies(++this.indentLevel, ATOMIC_INDENT).toArray());
		
		this.output.println(this.indent + "((MODULE " + name + "))");
	}
	
	@Override
	public final void processModuleParameters(final Module module) {
		this.output.println(this.indent + "∀" + module.getParameters());
	}
	
	@Override
	public final void beginModuleConditions(final Module module) {
		this.output.println(this.indent + "((CONDITIONS))");
	}
	
	@Override
	public final void processModuleCondition(final String conditionName, final Expression condition) {
		this.output.println(this.indent + "(" + conditionName + ")");
		this.output.println(this.indent + ATOMIC_INDENT + condition);
	}
	
	@Override
	public final void endModuleConditions(final Module module) {
		// NOP
	}
	
	@Override
	public final void beginModuleFacts(final Module module) {
		if (module.getFacts().isEmpty()) {
			this.output.println(this.indent + "()");
		} else {
			this.output.println(this.indent + "((FACTS))");
		}
	}
	
	@Override
	public final void processModuleFact(final String factName, final Expression fact) {
		this.output.println(this.indent + "(" + factName + ")");
		this.output.println(this.indent + ATOMIC_INDENT + fact);
	}
	
	@Override
	public final void beginModuleFactProof() {
		this.output.println(this.indent + ATOMIC_INDENT + "((PROOF))");
		this.indent = join("", nCopies(++this.indentLevel, ATOMIC_INDENT).toArray());
	}
	
	@Override
	public final void processModuleFactProof(final Statement command) {
		this.output.println(this.indent + ATOMIC_INDENT + command);
	}
	
	@Override
	public final void endModuleFactProof() {
		this.indent = join("", nCopies(max(0, --this.indentLevel), ATOMIC_INDENT).toArray());
	}
	
	@Override
	public final void endModuleFacts(final Module module) {
		// NOP
	}
	
	@Override
	public final void processCurrentGoal(final Expression currentGoal) {
		this.output.println(this.indent + "((GOAL))");
		this.output.println(this.indent + ATOMIC_INDENT + currentGoal);
	}
	
	@Override
	public final void endSession() {
		this.output.flush();
	}
	
	/**
	 * {@value}.
	 */
	private static final long serialVersionUID = 5124521844835011803L;
	
	public static final String ATOMIC_INDENT = "\t";
	
}
