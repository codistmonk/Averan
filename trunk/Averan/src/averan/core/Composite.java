package averan.core;

import static net.sourceforge.aprog.tools.Tools.cast;
import static net.sourceforge.aprog.tools.Tools.join;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

import averan.core.Module.Symbol;

/**
 * @author codistmonk (creation 2014-08-01)
 */
public final class Composite implements Expression, Iterable<Expression> {
	
	private final List<Expression> children;
	
	public Composite() {
		this(8);
	}
	
	public Composite(final int initialCapacity) {
		this(new ArrayList<>(initialCapacity));
	}
	
	public Composite(final List<Expression> children) {
		this.children = children;
	}
	
	public final List<Expression> getChildren() {
		return this.children;
	}
	
	@SuppressWarnings("unchecked")
	public final <E extends Expression> E get(final int index) {
		return (E) this.getChildren().get(index);
	}
	
	@Override
	public final Iterator<Expression> iterator() {
		return this.getChildren().iterator();
	}
	
	@Override
	public final <R> R accept(final Visitor<R> visitor) {
		return visitor.visit(this);
	}
	
	public final <R> Supplier<List<R>> childrenAcceptor(final Visitor<R> visitor) {
		return Expression.listAcceptor(this.getChildren(), visitor);
	}
	
	@Override
	public final int hashCode() {
		return this.getChildren().hashCode();
	}
	
	@Override
	public final boolean equals(final Object object) {
		final Composite that = cast(this.getClass(), object);
		
		return that != null && this.getChildren().equals(that.getChildren());
	}
	
	@Override
	public final String toString() {
		final List<Expression> children = this.getChildren();
		final StringBuilder resultBuilder = new StringBuilder();
		final boolean thisIsBraced = isBracedComposite(this);
		
		if (!thisIsBraced && Module.isSubstitution(this)) {
			final Composite equalities = (Composite) children.get(1);
					
			resultBuilder.append(children.get(0)).append('{').append(join(",", equalities.getChildren().toArray())).append('}');
			
			if (children.size() == 3) {
				final Composite indices = (Composite) children.get(2);
				resultBuilder.append(children.get(0)).append('[').append(join(",", indices.getChildren().toArray())).append(']');
			}
			
			return resultBuilder.toString();
		}
		
		for (final Expression child : children) {
			if (thisIsBraced || child instanceof Symbol || isBracedComposite(child)) {
				resultBuilder.append(child);
			} else {
				resultBuilder.append('(').append(child).append(')');
			}
		}
		
		return resultBuilder.toString();
	}
	
	/**
	 * {@value}.
	 */
	private static final long serialVersionUID = -280050093929737583L;
	
	public static final boolean isBracedComposite(final Object object) {
		final Composite composite = cast(Composite.class, object);
		
		if (composite == null || composite.getChildren().size() < 2) {
			return false;
		}
		
		final List<Expression> children = composite.getChildren();
		
		final Symbol left = cast(Symbol.class, children.get(0));
		final Symbol right = cast(Symbol.class, children.get(children.size() - 1));
		
		return left != null && right != null
				&& ("(".equals(left.toString()) && ")".equals(right.toString())
				|| "[".equals(left.toString()) && "]".equals(right.toString())
				|| "{".equals(left.toString()) && "}".equals(right.toString())
				|| "⟨".equals(left.toString()) && "⟩".equals(right.toString()));
	}
	
}