package averan2;

import static net.sourceforge.aprog.tools.Tools.cast;
import net.sourceforge.aprog.tools.Tools;


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
		return ELEMENT_COUNT;
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
	public final int hashCode() {
		return this.getContext().hashCode() + this.toString().hashCode();
	}
	
	@Override
	public final boolean equals(final Object object) {
		final Symbol that = cast(this.getClass(), object);
		
		return that != null && this.getContext() == that.getContext() && this.toString().equals(that.toString());
	}
	
	@Override
	public final String toString() {
		return this.string;
	}
	
	private static final long serialVersionUID = 2799712160346889585L;
	
	public static final int ELEMENT_COUNT = 0;
	
}