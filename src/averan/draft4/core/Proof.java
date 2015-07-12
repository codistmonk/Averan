package averan.draft4.core;

import java.io.Serializable;
import java.util.List;

/**
 * @author codistmonk (creation 2015-04-11)
 */
public abstract interface Proof extends Serializable {
	
	public abstract String getProvedPropositionName();
	
	public abstract List<Object> getProvedPropositionFor(Deduction context);
	
	public abstract List<Object> getMessage();
	
	/**
	 * @author codistmonk (creation 2015-04-11)
	 */
	public static abstract class Abstract implements Proof {
		
		private final String provedPropositionName;
		
		private final List<Object> message;
		
		protected Abstract(final String provedPropositionName, final List<Object> message) {
			this.provedPropositionName = provedPropositionName;
			this.message = message;
		}
		
		@Override
		public final String getProvedPropositionName() {
			return this.provedPropositionName;
		}
		
		@Override
		public final List<Object> getMessage() {
			return this.message;
		}
		
		@Override
		public final String toString() {
			return this.getMessage().toString();
		}
		
		private static final long serialVersionUID = -2226060514599512812L;
		
	}
	
}
