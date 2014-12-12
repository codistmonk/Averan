package averan2;

/**
 * @author codistmonk (creation 2014-12-12)
 */
public abstract class Proof implements Expression {
	
	private final Expression conclusion;
	
	protected Proof(final Expression conclusion) {
		this.conclusion = conclusion;
	}
	
	public final <E extends Expression> E getConclusion() {
		return (E) this.conclusion;
	}
	
	@Override
	public final <T> T accept(final Visitor<T> visitor) {
		return visitor.visit(this);
	}
	
	private static final long serialVersionUID = -8318551923110061376L;
	
	/**
	 * @author codistmonk (creation 2014-12-12)
	 */
	public static final class Admit extends Proof {
		
		public Admit(final Expression conclusion) {
			super(conclusion);
		}
		
		@Override
		public final int getElementCount() {
			return 0;
		}
		
		@Override
		public final <E extends Expression> E getElement(final int index) {
			switch (index) {
			case 0:
				return (E) ADMIT;
			case 1:
				return this.getConclusion();
			}
			
			return null;
		}
		
		private static final long serialVersionUID = 2744181863226646658L;
		
		public static final Symbol ADMIT = new Symbol(Module.ROOT, " Admit: ");
		
	}
	
}
