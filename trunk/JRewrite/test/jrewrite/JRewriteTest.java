package jrewrite;

import static net.sourceforge.aprog.tools.Tools.cast;
import static org.junit.Assert.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jrewrite.JRewriteTest.Pattern;

import net.sourceforge.aprog.tools.Tools;

import org.junit.Test;

/**
 * @author codistmonk (creation 2013-10-25)
 */
public final class JRewriteTest {
	
	@Test
	public final void test1() {
		final Rule rule = new Rule(new LeafPattern(null, "A"), new LeafPattern(null, "B"));
		
		assertEquals(new LeafPattern(null, "B"), rule.apply(new LeafPattern(null, "A")));
	}
	
	/**
	 * @author codistmonk (creation 2013-10-25)
	 */
	public static abstract class Pattern implements Serializable {
		
		private final Pattern parent;
		
		protected Pattern(final Pattern parent) {
			this.parent = parent;
		}
		
		public final Pattern getParent() {
			return this.parent;
		}
		
//		public abstract boolean verifies(Pattern condition, Map<BindingPattern, Pattern> context);
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = -6898079507688381833L;
		
	}
	
	/**
	 * @author codistmonk (creation 2013-10-25)
	 */
	public static abstract class BindingPattern extends Pattern {
		
		private final String symbol;
		
		private final Pattern scope;
		
		protected BindingPattern(final Pattern parent, final String symbol, final Pattern scope) {
			super(parent);
			this.symbol = symbol;
			this.scope = scope;
		}
		
		public final String getSymbol() {
			return this.symbol;
		}
		
		public final Pattern getScope() {
			return this.scope;
		}
		
		@Override
		public final int hashCode() {
			return this.getSymbol().hashCode() + this.getScope().hashCode();
		}
		
		@Override
		public final boolean equals(final Object object) {
			final BindingPattern that = cast(this.getClass(), object);
			
			return that != null && this.getSymbol().equals(that.getSymbol()) && this.getScope().equals(that.getScope());
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = -8666688675483706696L;
		
	}
	
	/**
	 * @author codistmonk (creation 2013-10-25)
	 */
	public static final class UniversalQuantification extends BindingPattern {
		
		public UniversalQuantification(final Pattern parent, final String symbol, final Pattern scope) {
			super(parent, symbol, scope);
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = -5488810820673090247L;
		
	}
	
	/**
	 * @author codistmonk (creation 2013-10-25)
	 */
	public static final class ExistentialQuantification extends BindingPattern {
		
		public ExistentialQuantification(final Pattern parent, final String symbol, final Pattern scope) {
			super(parent, symbol, scope);
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = -991908107257362159L;
		
	}
	
	/**
	 * @author codistmonk (creation 2013-10-25)
	 */
	public static final class SequencePattern extends Pattern {
		
		private final List<Pattern> subpatterns;
		
		public SequencePattern(final Pattern parent) {
			super(parent);
			this.subpatterns = new ArrayList<Pattern>();
		}
		
		public final List<Pattern> getSubpatterns() {
			return this.subpatterns;
		}
		
		@Override
		public final int hashCode() {
			return this.getSubpatterns().hashCode();
		}
		
		@Override
		public final boolean equals(final Object object) {
			final SequencePattern that = cast(this.getClass(), object);
			
			return that != null && this.getSubpatterns().equals(that.getSubpatterns());
		}
		
		@Override
		public final String toString() {
			return this.getSubpatterns().toString();
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = -3961672988993972580L;
		
	}
	
	/**
	 * @author codistmonk (creation 2013-10-25)
	 */
	public static final class LeafPattern extends Pattern {
		
		private final String symbol;
		
		public LeafPattern(final Pattern parent, final String symbol) {
			super(parent);
			this.symbol = symbol;
		}
		
		public final String getSymbol() {
			return this.symbol;
		}
		
		public final BindingPattern getBinder() {
			Pattern ancestor = this.getParent();
			
			while (ancestor != null) {
				final BindingPattern binder = cast(BindingPattern.class, ancestor);
				
				if (binder != null && this.getSymbol().equals(binder.getSymbol())) {
					return binder;
				}
				
				ancestor = ancestor.getParent();
			}
			
			return null;
		}
		
		@Override
		public final String toString() {
			return this.getSymbol();
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = -80343323477979009L;
		
	}
	
	/**
	 * @author codistmonk (creation 2013-10-25)
	 */
	public static final class Rule implements Serializable {
		
		private final Pattern condition;
		
		private final Pattern consequence;
		
		public Rule(final Pattern condition, final Pattern consequence) {
			this.condition = condition;
			this.consequence = consequence;
		}
		
		public final Pattern getCondition() {
			return this.condition;
		}
		
		public final Pattern getConsequence() {
			return this.consequence;
		}
		
		public final Pattern apply(final Pattern fact) {
			
			
			return null;
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = -149040987214775071L;
		
	}
	
}
