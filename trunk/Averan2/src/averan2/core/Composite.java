package averan2.core;

import java.util.ArrayList;
import java.util.List;

/**
 * @author codistmonk (creation 2014-12-11)
 */
public final class Composite<E extends Expression> implements Expression {
	
	private final List<E> elements;
	
	public Composite() {
		this.elements = new ArrayList<>();
	}
	
	public final List<E> getElements() {
		return this.elements;
	}
	
	@Override
	public final int getElementCount() {
		return this.getElements().size();
	}
	
	@Override
	public final E getElement(final int index) {
		if (0 <= index && index < this.getElementCount()) {
			return this.getElements().get(index);
		}
		
		return null;
	}
	
	@Override
	public final <T> T accept(final Visitor<T> visitor) {
		return visitor.visit(this);
	}
	
	private static final long serialVersionUID = 1243299481185522051L;
	
}