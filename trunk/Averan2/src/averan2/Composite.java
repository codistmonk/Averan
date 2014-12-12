package averan2;

import java.util.ArrayList;
import java.util.List;

/**
 * @author codistmonk (creation 2014-12-11)
 */
public final class Composite implements Expression {
	
	private final List<Expression> expressions;
	
	public Composite() {
		this.expressions = new ArrayList<>();
	}
	
	public final List<Expression> getExpressions() {
		return this.expressions;
	}
	
	@Override
	public final int getElementCount() {
		return this.getExpressions().size();
	}
	
	@Override
	public <E extends Expression> E getElement(final int index) {
		if (0 <= index && index < this.getElementCount()) {
			return (E) this.getExpressions().get(index);
		}
		
		return null;
	}
	
	@Override
	public final <T> T accept(final Visitor<T> visitor) {
		return visitor.visit(this);
	}
	
	private static final long serialVersionUID = 1243299481185522051L;
	
}