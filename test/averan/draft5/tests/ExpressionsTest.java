package averan.draft5.tests;

import static averan.draft5.expressions.Expressions.*;
import static averan.draft5.tests.ExpressionsTest.UnifierTester.u;
import static multij.tools.Tools.cast;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import averan.draft5.expressions.Unifier;

/**
 * @author codistmonk (creation 2015-04-19)
 */
public final class ExpressionsTest {
	
	@Test
	public final void testUnify1() {
		assertEquals("a", unify("a", "a"));
		assertEquals("a", unify("a", unifiable("a")));
		assertEquals("a", unify(unifiable("a"), "a"));
		assertEquals("a", unify(unifiable("a"), unifiable("a")));
		
		assertNull(unify("a", "b"));
		assertNull(unify(unifiable("a"), "b"));
		assertNull(unify("a", unifiable("b")));
		assertNull(unify(unifiable("a"), unifiable("b")));
		
		UnifierTester.reset();
		
		assertEquals($forall("a", "a"), fullLock(unify($forall("a", "a"), $forall("a", "a"))));
		assertEquals($forall("a", "a"), fullLock(unify(unifiable($forall("a", "a")), $forall("a", "a"))));
		assertEquals($forall("a", "a"), fullLock(unify($forall("a", "a"), unifiable($forall("a", "a")))));
		assertEquals($forall(u(0), u(0)), fullLock(unify(unifiable($forall("a", "a")), unifiable($forall("a", "a")))));
		
		assertNull(unify($forall("a", "a"), $forall("b", "b")));
		assertEquals($forall("b", "b"), fullLock(unify(unifiable($forall("a", "a")), $forall("b", "b"))));
		assertEquals($forall("a", "a"), fullLock(unify($forall("a", "a"), unifiable($forall("b", "b")))));
		assertEquals($forall(u(1), u(1)), fullLock(unify(unifiable($forall("a", "a")), unifiable($forall("b", "b")))));
		
		assertNull(unify($forall("a", "a"), $forall("b", "a")));
		assertNull(unify(unifiable($forall("a", "a")), $forall("b", "a")));
		assertEquals($forall("a", "a"), fullLock(unify($forall("a", "a"), unifiable($forall("b", "a")))));
		assertEquals($forall("a", "a"), fullLock(unify(unifiable($forall("a", "a")), unifiable($forall("b", "a")))));
		
		assertEquals($equality("a", "d"), fullLock(unify($equality("a", "d"), $equality("a", new Unifier("b")))));
		assertEquals($equality("a", "d"), fullLock(unify($equality("a", new Unifier("b")), $equality("a", "d"))));
	}
	
	@Test
	public final void testUnify2() {
		UnifierTester.reset();
		
		assertNull(unify($new("a"), $new("a")));
		assertNull(unify($new("a"), $new("b")));
		
		{
			final Object a = $new("a");
			final Object b = $new("b");
			
			assertNull(unify($forall(a, a), $forall(b, b)));
			assertEquals($forall(u(0), u(0)), fullLock(unify(unifiable($forall(a, a)), unifiable($forall(b, b)))));
		}
	}
	
	/**
	 * @author codistmonk (creation 2015-04-19)
	 */
	public static final class UnifierTester {
		
		private final int id;
		
		private Object unifier;
		
		public UnifierTester(final int id) {
			this.id = id;
		}
		
		@Override
		public final int hashCode() {
			return super.hashCode();
		}

		@Override
		public final boolean equals(final Object object) {
			final Unifier unifier = cast(Unifier.class, object);
			
			if (unifier != null) {
				if (this.unifier == null) {
					this.unifier = unifier.toString();
				}
				
				for (final UnifierTester tester : testers.values()) {
					if (tester != this && this.unifier.equals(tester.unifier)) {
						return false;
					}
				}
				
				return this.unifier.equals(unifier.toString());
			}
			
			return super.equals(object);
		}
		
		@Override
		public final String toString() {
			return this.unifier + "#" + this.id;
		}
		
		private static final Map<Integer, UnifierTester> testers = new HashMap<>();
		
		public static final Object u(final int id) {
			return testers.computeIfAbsent(id, i -> new UnifierTester(id));
		}
		
		public static final void reset() {
			testers.clear();
		}
		
	}
	
}
