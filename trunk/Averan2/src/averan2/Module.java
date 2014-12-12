package averan2;


/**
 * @author codistmonk (creation 2014-12-11)
 */
public final class Module implements Expression {
	
	private final Module context;
	
	private final Symbol name;
	
	private final Composite parameters;
	
	private final Composite conditions;
	
	private final Composite facts;
	
	public Module(final Module context, final Symbol name) {
		this.context = context;
		this.name = name;
		this.parameters = new Composite();
		this.conditions = new Composite();
		this.facts = new Composite();
	}
	
	public final Module getContext() {
		return this.context;
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
		return ELEMENT_COUNT;
	}
	
	@Override
	public final <E extends Expression> E getElement(final int index) {
		switch (index) {
		case HELPER_1:
			return (E) FOR_ALL;
		case PARAMETERS:
			return (E) this.getParameters();
		case CONDITIONS:
			return (E) this.getConditions();
		case HELPER_2:
			return (E) IMPLIES;
		case FACTS:
			return (E) this.getFacts();
		}
		
		return null;
	}
	
	@Override
	public final <T> T accept(final Visitor<T> visitor) {
		return visitor.visit(this);
	}
	
	private static final long serialVersionUID = 5905556222810031902L;
	
	public static final Module ROOT = new Module(null, new Symbol(null, "Root"));
	
	public static final Symbol FOR_ALL = new Symbol(ROOT, "∀");
	
	public static final Symbol IMPLIES = new Symbol(ROOT, "→");
	
	public static final Symbol EQUALS = new Symbol(ROOT, "=");
	
	public static final int HELPER_1 = 0;
	
	public static final int PARAMETERS = 1;
	
	public static final int CONDITIONS = 2;
	
	public static final int HELPER_2 = 3;
	
	public static final int FACTS = 4;
	
	public static final int ELEMENT_COUNT = 5;
	
}