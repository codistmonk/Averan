package averan.core;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author codistmonk (creation 2014-08-01)
 */
public abstract interface Expression extends Serializable {
	
	public abstract <R> R accept(Visitor<R> visitor);
	
	public static <R> Supplier<List<R>> listAcceptor(final Collection<? extends Expression> expressions, final Visitor<R> visitor) {
		return new Supplier<List<R>>() {
			
			private List<R> result;
			
			@Override
			public final synchronized List<R> get() {
				if (this.result == null) {
					this.result = expressions.stream().map(e -> e.accept(visitor)).collect(Collectors.toList());
				}
				
				return this.result;
			}
			
		}; 
	}
	
}
