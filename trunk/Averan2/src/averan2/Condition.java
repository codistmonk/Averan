package averan2;

/**
 * @author codistmonk (creation 2014-12-12)
 */
public final class Condition extends Proposition.Default {
	
	public Condition(final Symbol name, final Expression expression) {
		super(name, expression);
	}
	
	@Override
	public final int getElementCount() {
		return ELEMENT_COUNT;
	}
	
	@Override
	public final <E extends Expression> E getElement(final int index) {
		switch (index) {
		case NAME:
			return (E) this.getName();
		case EXPRESSION:
			return this.getExpression();
		}
		
		return null;
	}
	
	@Override
	public final <T> T accept(final Visitor<T> visitor) {
		return visitor.visit(this);
	}
	
	private static final long serialVersionUID = 2284891648674226439L;
	
	public static final int ELEMENT_COUNT = 2;
	
}
