package averan2.core;


/**
 * @author codistmonk (creation 2014-12-11)
 */
public final class Module implements Expression {
	
	private final Module context;
	
	private final Symbol name;
	
	private final Composite<Symbol> parameters;
	
	private final Composite<Condition> conditions;
	
	private final Composite<Fact> facts;
	
	public Module(final Module context, final Symbol name) {
		this(context, name, new Composite<>(), new Composite<>(), new Composite<>());
	}
	
	public Module(final Module context, final Symbol name, final Composite<Symbol> parameters,
			final Composite<Condition> conditions, final Composite<Fact> facts) {
		this.context = context;
		this.name = name;
		this.parameters = parameters;
		this.conditions = conditions;
		this.facts = facts;
	}
	
	public final Symbol parameter(final Object name) {
		Symbol result = this.findParameter(name);
		
		if (result == null) {
			result = new Symbol(this, name.toString());
			this.getParameters().getElements().add(result);
		}
		
		return result;
	}
	
	public final Symbol findParameter(final Object name) {
		for (final Symbol parameter : this.getParameters().getElements()) {
			if (name.toString().equals(parameter.toString())) {
				return parameter;
			}
		}
		
		return null;
	}
	
	public final Condition findCondition(final Symbol name) {
		final Condition result = find(name, this.getConditions().getElements());
		
		return result == null && this.getContext() != null ? this.getContext().findCondition(name) : result;
	}
	
	public final Fact findFact(final Symbol name) {
		final Fact result = find(name, this.getFacts().getElements());
		
		return result == null && this.getContext() != null ? this.getContext().findFact(name) : result;
	}
	
	public final Proposition findProposition(final Symbol name) {
		Proposition result = this.findFact(name);
		
		if (result == null) {
			result = this.findCondition(name);
		}
		
		return result;
	}
	
	public final Module getContext() {
		return this.context;
	}
	
	public final Symbol getName() {
		return this.name;
	}
	
	public final Composite<Symbol> getParameters() {
		return this.parameters;
	}
	
	public final Composite<Condition> getConditions() {
		return this.conditions;
	}
	
	public final Composite<Fact> getFacts() {
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
	
	@Override
	public final String toString() {
		return FOR_ALL + "" + this.getParameters() + " " + this.getConditions() + IMPLIES + this.getFacts();
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
	
	public static final <E extends Proposition> E find(final Symbol name, final Iterable<E> propositions) {
		for (final E proposition : propositions) {
			if (name.equals(proposition.getName())) {
				return proposition;
			}
		}
		
		return null;
	}
	
}