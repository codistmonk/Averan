package averan4.core;

import java.io.Serializable;
import java.util.List;

/**
 * @author codistmonk (creation 2015-04-11)
 */
public abstract interface Proof extends Serializable {
	
	public abstract List<Object> propositionFor(Deduction context);
	
}