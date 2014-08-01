package jrewrite3;

import static jrewrite3.VisitorTest.Recorder.Event.*;
import static net.sourceforge.aprog.tools.Tools.array;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

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
	public static final class Recorder implements Visitor {
		
		private final List<Event> events = new ArrayList<>();
		
		public final List<Event> getEvents() {
			return this.events;
		}
		
		@Override
		public final Object visitBeforeChildren(final Composite composite) {
			this.getEvents().add(Event.COMPOSITE_BEFORE_CHILDREN);
			
			return composite;
		}
		
		@Override
		public final Object visitAfterChildren(final Composite composite,
				final Object beforeVisit, final List<Object> childVisits) {
			this.getEvents().add(Event.COMPOSITE_AFTER_CHILDREN);
			
			assertEquals(composite, beforeVisit);
			assertEquals(composite.getChildren(), childVisits);
			
			return Visitor.super.visitAfterChildren(composite, beforeVisit, childVisits);
		}
		
		@Override
		public final Object visit(final Module.Variable variable) {
			this.getEvents().add(Event.VARIABLE);
			
			return variable;
		}
		
		@Override
		public final Object visitBeforeVariables(final Module module) {
			this.getEvents().add(Event.MODULE_BEFORE_VARIABLES);
			
			return module;
		}
		
		@Override
		public final Object visitAfterFacts(final Module module, final Object beforeVisit,
				final List<Object> variableVisits, final List<Object> conditionVisits,
				final List<Object> factVisits) {
			this.getEvents().add(Event.MODULE_AFTER_FACTS);
			
			assertEquals(module, beforeVisit);
			assertEquals(module.getVariables(), variableVisits);
			assertEquals(module.getConditions(), conditionVisits);
			assertEquals(module.getFacts(), factVisits);
			
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
