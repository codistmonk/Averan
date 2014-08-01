package jrewrite3;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author codistmonk (creation 2014-08-01)
 */
public abstract interface Expression extends Serializable {
	
	public abstract <R> R accept(Visitor<R> visitor);
	
	public static <R> List<R> listAccept(final Collection<? extends Expression> expressions, final Visitor<R> visitor) {
		return expressions.stream().map(e -> e.accept(visitor)).collect(Collectors.toList());
	}
	
}
