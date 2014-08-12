package averan.core;

import static averan.core.VisitorTest.Recorder.Event.*;
import static net.sourceforge.aprog.tools.Tools.array;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import averan.core.Composite;
import averan.core.Expression;
import averan.core.Module;
import averan.core.Visitor;
import averan.core.Module.Symbol;

/**
 * @author codistmonk (creation 2014-08-01)
 */
public final class VisitorTest {
	
	@Test
	public final void test1() {
		final Recorder recorder = new Recorder();
		
		final Module module = new Module(null);
		final Symbol x = module.parameter("x");
		
		module.new Suppose(x).execute();
		module.new Admit(Module.equality(x, x)).execute();
		
		assertEquals(module, module.accept(recorder));
		assertArrayEquals(array(
				MODULE_BEFORE_PARAMETERS, // begin module
				SYMBOL, // parameter "x"
				SYMBOL, // condition "x"
				COMPOSITE_BEFORE_CHILDREN, // begin [x=x]
				SYMBOL, // symbol "x"
				SYMBOL, // symbol "="
				SYMBOL, // symbol "x"
				COMPOSITE_AFTER_CHILDREN, // end [x=x]
				MODULE_AFTER_FACTS // end module
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
		public final Expression visit(final Composite composite) {
			this.getEvents().add(Event.COMPOSITE_BEFORE_CHILDREN);
			
			assertEquals(composite.getChildren(), composite.childrenAcceptor(this).get());
			
			this.getEvents().add(Event.COMPOSITE_AFTER_CHILDREN);
			
			return composite;
		}
		
		@Override
		public final Expression visit(final Module.Symbol symbol) {
			this.getEvents().add(Event.SYMBOL);
			
			return symbol;
		}
		
		@Override
		public final Expression beginVisit(final Module module) {
			this.getEvents().add(Event.MODULE_BEFORE_PARAMETERS);
			
			return module;
		}
		
		@Override
		public final Expression endVisit(final Module module, final Expression moduleVisit) {
			assertEquals(module, moduleVisit);
			assertEquals(module.getParameters(), module.parametersAcceptor(this).get());
			assertEquals(module.getConditions(), module.conditionsAcceptor(this).get());
			assertEquals(module.getFacts(), module.factsAcceptor(this).get());
			
			this.getEvents().add(Event.MODULE_AFTER_FACTS);
			
			return module;
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
			MODULE_BEFORE_PARAMETERS, MODULE_AFTER_FACTS,
			SYMBOL;
			
		}
		
	}
	
}
