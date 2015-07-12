package averan.common;

import static multij.tools.Tools.cast;

import java.io.Serializable;

/**
 * @author codistmonk (creation 2014-12-29)
 *
 * @param <T>
 */
public final class IdentityKey<T> implements Serializable {
	
	private final T object;
	
	public IdentityKey(final T object) {
		this.object = object;
	}
	
	public final T getObject() {
		return this.object;
	}
	
	@Override
	public final int hashCode() {
		return this.getObject().hashCode();
	}
	
	@Override
	public final boolean equals(final Object object) {
		final IdentityKey<?> that = cast(this.getClass(), object);
		
		return that != null && this.getObject() == that.getObject();
	}
	
	@Override
	public final String toString() {
		return this.getObject().toString();
	}
	
	private static final long serialVersionUID = 1282044383571704275L;
	
}
