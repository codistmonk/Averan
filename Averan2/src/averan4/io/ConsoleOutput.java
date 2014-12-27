package averan4.io;

import static java.util.Collections.nCopies;
import static net.sourceforge.aprog.tools.Tools.join;

import averan4.core.Composite;
import averan4.core.Expression;
import averan4.core.Session.Frame;
import averan4.io.SessionExporter.Output;

import java.io.PrintStream;
import java.util.Collections;

import net.sourceforge.aprog.tools.Tools;

/**
 * @author codistmonk (creation 2014-12-21)
 */
public final class ConsoleOutput implements Output {
	
	private final PrintStream out;
	
	private int frameLevel;
	
	private String indent;
	
	public ConsoleOutput() {
		this(System.out);
	}
	
	public ConsoleOutput(final PrintStream out) {
		this.out = out;
		this.frameLevel = -1;
		this.indent = "";
	}
	
	@Override
	public final void beginFrame(final Frame frame) {
		++this.frameLevel;
		this.indent = join("", nCopies(this.frameLevel, '	').toArray());
		this.out.println(this.indent + "((MODULE " + frame.getName() + "))");
	}
	
	@Override
	public final void beginConditions() {
		this.out.println(this.indent + "((CONDITIONS))");
	}
	
	@Override
	public final void processCondition(final String name, final Expression<?> condition) {
		this.out.println(this.indent + "	" + condition);
	}

	@Override
	public final void beginFacts() {
		this.out.println(this.indent + "((FACTS))");
	}
	
	@Override
	public final void beginFact(final String name, final Expression<?> fact) {
		this.out.println(this.indent + "	" + fact);
	}
	
	@Override
	public final void processGoal(final Expression<?> goal) {
		this.out.println("((GOAL))");
		this.out.println("	" + goal);
	}
	
	@Override
	public final void endFrame() {
		--this.frameLevel;
	}
	
	private static final long serialVersionUID = 3659783931873586881L;
	
}
