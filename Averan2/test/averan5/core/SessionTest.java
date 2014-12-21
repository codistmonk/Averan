package averan5.core;

import static org.junit.Assert.*;

import java.io.PrintStream;

import averan5.core.Session.Exporter.Output;
import averan5.core.Session.Frame;

import org.junit.Test;

/**
 * @author codistmonk (creation 2014-12-21)
 */
public final class SessionTest {
	
	@Test
	public final void test1() {
		final Session session = new Session();
		
		session.prove("test", null);
		session.accept();
		
		Session.Exporter.export(session, new ConsoleOutput());
	}
	
	/**
	 * @author codistmonk (creation 2014-12-21)
	 */
	public static final class ConsoleOutput implements Output {
		
		private final PrintStream out;
		
		private int frameLevel;
		
		public ConsoleOutput() {
			this(System.out);
		}
		
		public ConsoleOutput(final PrintStream out) {
			this.out = out;
			this.frameLevel = -1;
		}
		
		@Override
		public final void beginFrame(final Frame frame) {
			++this.frameLevel;
			this.out.println("((MODULE " + frame.getName() + "))");
		}
		
		@Override
		public final void beginModule(final Composite<?> module) {
			// TODO
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
