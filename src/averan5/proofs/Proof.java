package averan5.proofs;

import static multij.tools.Tools.ignore;

import java.io.Serializable;
import java.util.List;

import averan5.expressions.Expressions;

/**
 * @author codistmonk (creation 2015-04-11)
 */
public abstract interface Proof extends Serializable {
	
	public default String getProvedPropositionName() {
		throw new UnsupportedOperationException();
	}
	
	public default Object getProvedPropositionFor(final Deduction context) {
		ignore(context);
		
		throw new UnsupportedOperationException();
	}
	
	public default List<Object> getMessage() {
		throw new UnsupportedOperationException();
	}
	
	public default Deduction concludeIn(final Deduction context) {
		return context.conclude(this);
	}
	
	public default Proof lock() {
		final int n = this.getMessage().size();
		
		for (int i = 0; i < n; ++i) {
			this.getMessage().set(i, Expressions.lock(this.getMessage().get(i)));
		}
		
		return this;
	}
	
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
