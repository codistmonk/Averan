package averan.common;

import static net.sourceforge.aprog.tools.Tools.list;

import java.io.Serializable;
import java.util.stream.Stream;

/**
 * @author codistmonk (creation 2014-12-20)
 */
public abstract interface Container<E> extends Serializable, Iterable<E> {
	
	public abstract int size();
	
	public abstract <F extends E> F get(int index);
	
	public default Stream<E> stream() {
		return list(this).stream();
	}
	
	public default Object[] toArray() {
		return list(this).toArray();
	}
	
	public default boolean isEmpty() {
		return this.size() == 0;
	}
	
	public default <F extends E> F last() {
		return this.get(this.size() - 1);
	}
	
	@Override
	public default java.util.Iterator<E> iterator() {
		return new Iterator<>(this);
	}
	
	/**
	 * @author codistmonk (creation 2014-12-12)
	 */
	public static final class Iterator<E> implements Serializable, java.util.Iterator<E> {
		
		private final Container<E> expression;
		
		private int index;
		
		public Iterator(final Container<E> expression) {
			this.expression = expression;
		}
		
		public final Container<E> getExpression() {
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
			return this.getIndex() < this.getExpression().size();
		}
		
		@Override
		public final E next() {
			final E result = this.getExpression().get(this.getIndex());
			
			this.setIndex(this.getIndex() + 1);
			
			return result;
		}
		
		private static final long serialVersionUID = 1395520944549107745L;
		
	}
	
}
