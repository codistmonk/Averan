package jrewrite3;

import static jrewrite3.VisitorTest.Recorder.Event.*;
import static net.sourceforge.aprog.tools.Tools.array;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.junit.Test;

/**
 * @author codistmonk (creation 2014-08-01)
 */
public final class VisitorTest {
	
	@Test
	public final void test1() {
		final Recorder recorder = new Recorder();
		
		assertEquals(Module.ROOT, Module.ROOT.accept(recorder));
		assertArrayEquals(array(
				MODULE_BEFORE_VARIABLES, // begin ROOT
				VARIABLE, // variable "="
				MODULE_BEFORE_VARIABLES, // begin equality
				VARIABLE, // variable "x"
				COMPOSITE_BEFORE_CHILDREN, // begin [x=x]
				VARIABLE, // variable "x"
				VARIABLE, // variable "="
				VARIABLE, // variable "x"
				COMPOSITE_AFTER_CHILDREN, // end [x=x]
				MODULE_AFTER_FACTS, // end equality
				MODULE_AFTER_FACTS // end ROOT
				), recorder.getEvents().toArray());
	}
	
	/**
	 * @author codistmonk (creation 2014-08-01)
	 */
	public static final class Recorder implements Visitor<Expression> {
		
		private final List<Event> events = new ArrayList<>();
		
		public final List<Event> getEvents() {
			return this.events;
		}
		
		@Override
		public final Expression visitBeforeChildren(final Composite composite) {
			this.getEvents().add(Event.COMPOSITE_BEFORE_CHILDREN);
			
			return composite;
		}
		
		@Override
		public final Expression visitAfterChildren(final Composite composite,
				final Expression beforeVisit, final Supplier<List<Expression>> childVisits) {
			assertEquals(composite, beforeVisit);
			assertEquals(composite.getChildren(), childVisits.get());
			
			this.getEvents().add(Event.COMPOSITE_AFTER_CHILDREN);
			
			return Visitor.super.visitAfterChildren(composite, beforeVisit, childVisits);
		}
		
		@Override
		public final Expression visit(final Module.Variable variable) {
			this.getEvents().add(Event.VARIABLE);
			
			return variable;
		}
		
		@Override
		public final Expression visitBeforeVariables(final Module module) {
			this.getEvents().add(Event.MODULE_BEFORE_VARIABLES);
			
			return module;
		}
		
		@Override
		public final Expression visitAfterFacts(final Module module, final Expression beforeVisit,
				final Supplier<List<Expression>> variableVisits, final Supplier<List<Expression>> conditionVisits,
				final Supplier<List<Expression>> factVisits) {
			assertEquals(module, beforeVisit);
			assertEquals(module.getVariables(), variableVisits.get());
			assertEquals(module.getConditions(), conditionVisits.get());
			assertEquals(module.getFacts(), factVisits.get());
			
			this.getEvents().add(Event.MODULE_AFTER_FACTS);
			
			return Visitor.super.visitAfterFacts(module, beforeVisit, variableVisits,
					conditionVisits, factVisits);
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = -7418401972134012397L;
		
		/**
		 * @author codistmonk (creation 2014-08-01)
		 */
		public static enum Event {
			
			COMPOSITE_BEFORE_CHILDREN, COMPOSITE_AFTER_CHILDREN,
			MODULE_BEFORE_VARIABLES, MODULE_AFTER_FACTS,
			VARIABLE;
			
		}
		
	}
	
}
