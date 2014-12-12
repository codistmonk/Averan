package averan2;


/**
 * @author codistmonk (creation 2014-12-11)
 */
public final class Symbol implements Expression {
	
	private final Module context;
	
	private final String string;
	
	public Symbol(final Module context, final String string) {
		this.context = context;
		this.string = string;
	}
	
	public final Module getContext() {
		return this.context;
	}
	
	@Override
	public final int getElementCount() {
		return 0;
	}
	
	@Override
	public <E extends Expression> E getElement(final int index) {
		return null;
	}
	
	@Override
	public final <T> T accept(final Visitor<T> visitor) {
		return visitor.visit(this);
	}
	
	@Override
	public final String toString() {
		return this.string;
	}
	
	private static final long serialVersionUID = 2799712160346889585L;
	
}