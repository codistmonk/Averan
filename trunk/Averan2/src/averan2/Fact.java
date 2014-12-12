package averan2;

/**
 * @author codistmonk (creation 2014-12-12)
 */
public final class Fact extends Proposition.Default {
	
	private final Proof proof;
	
	public Fact(final Symbol name, final Proof proof) {
		super(name, proof.getConclusion());
		this.proof = proof;
	}
	
	public final Proof getProof() {
		return this.proof;
	}
	
	@Override
	public final int getElementCount() {
		return 3;
	}
	
	@Override
	public final <E extends Expression> E getElement(final int index) {
		switch (index) {
		case 0:
			return (E) this.getName();
		case 1:
			return this.getExpression();
		case 2:
			return (E) this.getProof();
		}
		
		return null;
	}
	
	@Override
	public final <T> T accept(final Visitor<T> visitor) {
		return visitor.visit(this);
	}
	
	private static final long serialVersionUID = 2284891648674226439L;
	
}
