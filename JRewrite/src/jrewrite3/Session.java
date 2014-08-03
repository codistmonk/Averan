package jrewrite3;

import java.io.Serializable;

/**
 * @author codistmonk (creation 2014-08-02)
 */
public final class Session implements Serializable {
	
	/**
	 * {@value}.
	 */
	private static final long serialVersionUID = -4545117345944633693L;
	
	/**
	 * @author codistmonk (creation 2014-08-02)
	 */
	public static final class ProofContext implements Serializable {
		
		private final Module module;
		
		private final Expression initialGoal;
		
		private Expression currentGoal;
		
		private boolean goalReached;
		
		public ProofContext(final Module parent, final Expression goal) {
			this.module = new Module(parent);
			this.initialGoal = goal;
			this.currentGoal = goal;
		}
		
		public final boolean isGoalReached() {
			return this.goalReached;
		}
		
		public final Expression getInitialGoal() {
			return this.initialGoal;
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = -6558665995824497727L;
		
	}
	
}
