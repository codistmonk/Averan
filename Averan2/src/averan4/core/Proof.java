package averan4.core;

import java.io.Serializable;
import java.util.List;

/**
 * @author codistmonk (creation 2015-04-11)
 */
public abstract interface Proof extends Serializable {
	
	public abstract String getProvedPropositionName();
	
	public abstract List<Object> getProvedPropositionFor(Deduction context);
	
	/**
	 * @author codistmonk (creation 2015-04-11)
	 */
	public static abstract class Abstract implements Proof {
		
		private final String provedPropositionName;
		
		protected Abstract(final String provedPropositionName) {
			this.provedPropositionName = provedPropositionName;
		}
		
		@Override
		public final String getProvedPropositionName() {
			return this.provedPropositionName;
		}
		
		private static final long serialVersionUID = -2226060514599512812L;
		
	}
	
}
