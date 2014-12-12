package averan2;


/**
 * @author codistmonk (creation 2014-12-11)
 */
public final class Module implements Expression {
	
	private final Symbol name;
	
	private final Composite parameters;
	
	private final Composite conditions;
	
	private final Composite facts;
	
	public Module(final Symbol name) {
		this.name = name;
		this.parameters = new Composite();
		this.conditions = new Composite();
		this.facts = new Composite();
	}
	
	public final Symbol getName() {
		return this.name;
	}
	
	public final Composite getParameters() {
		return this.parameters;
	}
	
	public final Composite getConditions() {
		return this.conditions;
	}
	
	public final Composite getFacts() {
		return this.facts;
	}
	
	@Override
	public final int getElementCount() {
		return 5;
	}
	
	@Override
	public final <E extends Expression> E getElement(final int index) {
		switch (index) {
		case 0:
			return (E) FOR_ALL;
		case 1:
			return (E) this.getParameters();
		case 2:
			return (E) this.getConditions();
		case 3:
			return (E) IMPLIES;
		case 4:
			return (E) this.getFacts();
		}
		
		return null;
	}
	
	@Override
	public final <T> T accept(final Visitor<T> visitor) {
		return visitor.visit(this);
	}
	
	private static final long serialVersionUID = 5905556222810031902L;
	
	public static final Module ROOT = new Module(new Symbol(null, "Root"));
	
	public static final Symbol FOR_ALL = new Symbol(ROOT, "∀");
	
	public static final Symbol IMPLIES = new Symbol(ROOT, "→");
	
	public static final Symbol EQUALS = new Symbol(ROOT, "=");
	
}