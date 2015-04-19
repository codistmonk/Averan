package averan5.deductions;

import static averan5.core.AveranTools.*;
import static averan5.deductions.Standard.*;
import static java.util.stream.Collectors.toList;
import static net.sourceforge.aprog.tools.Tools.*;

import averan5.core.Deduction;
import averan5.core.Goal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import net.sourceforge.aprog.tools.Tools;

import org.junit.Test;

/**
 * @author codistmonk (creation 2015-04-13)
 */
public final class StandardTest {
	
//	@Test
//	public final void areEqualTest() {
//		{
//			assertTrue(areEqual("a", "a"));
//			assertFalse(areEqual("a", "b"));
//			assertTrue(areEqual($forall("a", "a"), $forall("b", "b")));
//			assertTrue(areEqual($equality($forall("a", "a"), $forall("a", "a")), $equality($forall("b", "b"), $forall("b", "b"))));
//			assertTrue(areEqual($forall("a", "a"), $forall("b", "a")));
//		}
//		
//		{
//			assertFalse(areEqual($new("a"), $new("a")));
//			assertFalse(areEqual($new("a"), $new("b")));
//			
//			{
//				final Object a = $new("a");
//				final Object b = $new("b");
//				
//				assertTrue(areEqual($forall(a, a), $forall(b, b)));
//			}
//		}
//	}
	
	@Test
	public final void testRewrite() {
		build(() -> {
			supposeRewrite();
			
			suppose($equality("a", "b"));
			
			final Goal goal = Goal.deduce($equality("b", "b"));
			
			rewrite(name(-1), name(-1));
			
			goal.conclude();
		});
	}
	
	@Test
	public final void testRewriteRight() {
		build(() -> {
			supposeRewrite();
			deduceIdentity();
			deduceCommutativityOfEquality();
			
			suppose($equality("a", "b"));
			
			final Goal goal = Goal.deduce($equality("a", "a"));
			
			rewriteRight(name(-1), name(-1));
			
			goal.conclude();
		});
	}
	
	@Test
	public final void testDeduceIdentity() {
		build(() -> {
			supposeRewrite();
			deduceIdentity();
			
			final Goal goal = Goal.deduce($equality("a", "a"));
			
			bind("identity", $("a"));
			
			goal.conclude();
		});
	}
	
	@Test
	public final void testDeduceRecall() {
		build(() -> {
			supposeRewrite();
			deduceIdentity();
			deduceRecall();
			
			suppose($("a"));
			
			final Goal goal = Goal.deduce($rule("a", "a"));
			
			bind("recall", $("a"));
			
			goal.conclude();
		});
	}
	
	public static final Deduction build(final Runnable deductionBuilder) {
		return build(getCallerMethodName(), deductionBuilder, 2);
	}
	
	public static final Deduction build(final String deductionName, final Runnable deductionBuilder, final int debugDepth) {
		return Standard.build(deductionName, deductionBuilder, debugDepth);
	}
	
	/**
	 * @author codistmonk (creation 2015-04-17)
	 * 
	 * @param <V>
	 */
	public static abstract interface ExpressionVisitor<V> extends Serializable, Function<Object, V>, Consumer<Object> {
		
		@Override
		public default void accept(final Object expression) {
			this.apply(expression);
		}
		
		@Override
		public default V apply(final Object expression) {
			if (expression instanceof List) {
				return this.visit((List<?>) expression);
			}
			
			return this.visit(expression);
		}
		
		public abstract V visit(Object expression);
		
		public default V visit(final List<?> expression) {
			expression.forEach(this);
			
			return this.visit((Object) expression);
		}
		
	}
	
	/**
	 * @author codistmonk (creation 2015-04-17)
	 */
	public static abstract interface ExpressionRewriter extends ExpressionVisitor<Object> {
		
		@Override
		public default Object visit(final Object expression) {
			return expression;
		}
		
		@Override
		public default Object visit(final List<?> expression) {
			return expression.stream().map(this).collect(toList());
		}
		
	}
	
	/**
	 * @author codistmonk (creation 2015-04-19)
	 * 
	 * @param <V>
	 */
	public static abstract interface ExpressionZipper<V> extends Serializable, BiFunction<Object, Object, V>, BiConsumer<Object, Object> {
		
		@Override
		public default void accept(final Object expression1, final Object expression2) {
			this.apply(expression1, expression2);
		}
		
		@Override
		public default V apply(final Object expression1, final Object expression2) {
			if (expression1 instanceof List && expression2 instanceof List) {
				return this.visit((List<?>) expression1, (List<?>) expression2);
			}
			
			return this.visit(expression1, expression2);
		}
		
		public abstract V visit(Object expression1, Object expression2);
		
		public default V visit(final List<?> expression1, final List<?> expression2) {
			final int n = expression1.size();
			
			if (n != expression2.size()) {
				return null;
			}
			
			for (int i = 0; i < n; ++i) {
				this.accept(expression1.get(i), expression2.get(i));
			}
			
			return this.visit((Object) expression1, (Object) expression2);
		}
		
	}
	
	/**
	 * @author codistmonk (creation 2015-04-19)
	 */
	public static abstract interface ExpressionCombiner extends ExpressionZipper<Object> {
		
		@Override
		public default Object visit(final List<?> expression1, final List<?> expression2) {
			final int n = expression1.size();
			
			if (n != expression2.size()) {
				return null;
			}
			
			final List<Object> result = new ArrayList<>(n);
			
			for (int i = 0; i < n; ++i) {
				final Object element = this.apply(expression1.get(i), expression2.get(i));
				
				if (element == null) {
					return null;
				}
				
				result.add(element);
			}
			
			return result;
		}
		
	}
	
	/**
	 * @author codistmonk (creation 2015-04-17)
	 */
	public static final class Wildcard implements Serializable {
		
		private final Object variable;
		
		private Object object;
		
		public Wildcard(final Object variable) {
			this.variable = variable;
		}
		
		public final Object getVariable() {
			return this.variable;
		}
		
		public final Object getObject() {
			return this.object;
		}
		
		public final Object getObjectOrVariable() {
			final Object object = this.getObject();
			
			return object != null ? object : this.getVariable();
		}
		
		public final Wildcard setObject(final Object object) {
			this.object = object;
			
			return this;
		}
		
		@Override
		public final int hashCode() {
			return Tools.hashCode(this.getObject());
		}
		
		@Override
		public final boolean equals(final Object object) {
			if (object instanceof Wildcard) {
				return this == object;
			}
			
			if (this.getObject() == null) {
				this.object = object;
				
				return true;
			}
			
			return Tools.equals(this.getObject(), object);
		}
		
		private static final long serialVersionUID = -7784368590863733909L;
		
		public static final Object addTo(final Object expression) {
			return new Add().apply(expression);
		}
		
		public static final Object removeFrom(final Object expression) {
			return new Remove().apply(expression);
		}
		
		public static final void restore(final Map<Wildcard, Object> snapshot) {
			snapshot.forEach(Wildcard::setObject);
		}
		
		public static final Map<Wildcard, Object> snapshot(final Object expression) {
			return new ExpressionVisitor<Map<Wildcard, Object>>() {
				
				private final Map<Wildcard, Object> result = new HashMap<>();
				
				@Override
				public final Map<Wildcard, Object> visit(final Object expression) {
					final Wildcard wildcard = cast(Wildcard.class, expression);
					
					if (wildcard != null) {
						this.result.putIfAbsent(wildcard, wildcard.getObject());
					}
					
					return this.result;
				}
				
				private static final long serialVersionUID = 416410928718755243L;
				
			}.apply(expression);
		}
		
		/**
		 * @author codistmonk (creation 2015-04-17)
		 */
		public static final class Add implements ExpressionRewriter {
			
			private final Map<Object, Object> wildcards = new HashMap<>();
			
			@Override
			public final Object visit(final Object expression) {
				return this.wildcards.getOrDefault(expression, expression);
			}
			
			@Override
			public final Object visit(final List<?> expression) {
				if (isBlock(expression)) {
					final Object variable = variable(expression);
					final boolean remove = !this.wildcards.containsKey(variable);
					final Object old = this.wildcards.put(variable, new Wildcard(variable));
					
					try {
						return ExpressionRewriter.super.visit(expression);
					} finally {
						if (remove) {
							this.wildcards.remove(variable);
						} else {
							this.wildcards.put(variable, old);
						}
					}
				}
				
				return ExpressionRewriter.super.visit(expression);
			}
			
			private static final long serialVersionUID = 8944845022767742777L;
			
		}
		
		/**
		 * @author codistmonk (creation 2015-04-17)
		 */
		public static final class Remove implements ExpressionRewriter {
			
			@Override
			public final Object visit(final Object expression) {
				final Wildcard wildcard = cast(Wildcard.class, expression);
				
				return wildcard != null ? wildcard.getObjectOrVariable() : expression;
			}
			
			@Override
			public final Object visit(final List<?> expression) {
				if (isBlock(expression)) {
					final Wildcard wildcard = cast(Wildcard.class, variable(expression));
					
					if (wildcard != null && wildcard.getObject() != null) {
						return this.apply(scope(expression));
					}
				}
				
				return ExpressionRewriter.super.visit(expression);
			}
			
			private static final long serialVersionUID = 2963254845866681747L;
			
		}
		
	}
	
}
