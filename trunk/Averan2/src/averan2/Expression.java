package averan2;

import java.io.Serializable;
import java.util.Iterator;

/**
 * @author codistmonk (creation 2014-12-11)
 */
public abstract interface Expression extends Serializable, Iterable<Expression> {
	
	public abstract int getElementCount();
	
	public abstract <E extends Expression> E getElement(int index);
	
	public abstract <T> T accept(Visitor<T> visitor);
	
	@Override
	public default Iterator<Expression> iterator() {
		return new ExpressionIterator(this);
	}
	
	/**
	 * @author codistmonk (creation 2014-12-12)
	 */
	public static final class ExpressionIterator implements Serializable, Iterator<Expression> {
		
		private final Expression expression;
		
		private int index;
		
		public ExpressionIterator(final Expression expression) {
			this.expression = expression;
		}
		
		public final Expression getExpression() {
			return this.expression;
		}
		
		public final int getIndex() {
			return this.index;
		}
		
		public final void setIndex(final int index) {
			this.index = index;
		}
		
		@Override
		public final boolean hasNext() {
			return this.getIndex() < this.getExpression().getElementCount();
		}
		
		@Override
		public final Expression next() {
			final Expression result = this.getExpression().getElement(this.getIndex());
			
			this.setIndex(this.getIndex() + 1);
			
			return result;
		}
		
		private static final long serialVersionUID = 1395520944549107745L;
		
	}
	
}