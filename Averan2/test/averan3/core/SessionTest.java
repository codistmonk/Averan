package averan3.core;

import static java.util.Collections.nCopies;
import static net.sourceforge.aprog.tools.Tools.join;
import static org.junit.Assert.*;

import java.io.PrintStream;
import java.util.Collections;

import averan3.core.Composite;
import averan3.core.Expression;
import averan3.core.Session;
import averan3.core.Variable;
import averan3.core.Session.Frame;
import averan3.core.Session.Exporter.Output;
import net.sourceforge.aprog.tools.Tools;

import org.junit.Test;

/**
 * @author codistmonk (creation 2014-12-21)
 */
public final class SessionTest {
	
	@Test
	public final void test1() {
		final Session session = new Session();
		
		session.prove("test", null);
		{
			final Variable x = new Variable("x");
			
			session.suppose("c1", x);
			session.suppose("c2", x);
		}
		session.accept();
		
		Session.Exporter.export(session, new ConsoleOutput());
	}
	
	/**
	 * @author codistmonk (creation 2014-12-21)
	 */
	public static final class ConsoleOutput implements Output {
		
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
		public final void processCondition(final Composite<?> condition) {
			this.out.println(this.indent + "	" + condition);
		}

		@Override
		public final void beginFacts() {
			this.out.println(this.indent + "((FACTS))");
		}
		
		@Override
		public final void beginFact(final Composite<?> fact) {
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
	
}
