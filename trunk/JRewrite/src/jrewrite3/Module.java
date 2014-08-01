package jrewrite3;

import static net.sourceforge.aprog.tools.Tools.cast;

import java.util.ArrayList;
import java.util.List;

/**
 * @author codistmonk (creation 2014-08-01)
 */
final class Module implements Expression {
	
	private final Module parent;
	
	private final List<Variable> variables;
	
	private final List<Expression> conditions;
	
	private final List<Expression> facts;
	
	public Module(final Module parent) {
		this.parent = parent;
		this.variables = new ArrayList<>();
		this.conditions = new ArrayList<>();
		this.facts = new ArrayList<>();
	}
	
	public final Module getParent() {
		return this.parent;
	}
	
	public final List<Variable> getVariables() {
		return this.variables;
	}
	
	public final List<Expression> getConditions() {
		return this.conditions;
	}
	
	public final List<Expression> getFacts() {
		return this.facts;
	}
	
	@Override
	public final Object accept(final Visitor visitor) {
		final Object beforeVisit = visitor.visitBeforeVariables(this);
		final List<Object> variableVisits = Expression.listAccept(this.getVariables(), visitor);
		final List<Object> conditionVisits = Expression.listAccept(this.getConditions(), visitor);
		final List<Object> factVisits = Expression.listAccept(this.getFacts(), visitor);
		
		return visitor.visitAfterFacts(this, beforeVisit, variableVisits, conditionVisits, factVisits);
	}
	
	@Override
	public final int hashCode() {
		return this.getVariables().hashCode() + this.getConditions().hashCode() + this.getFacts().hashCode();
	}
	
	@Override
	public final boolean equals(final Object object) {
		final Module that = cast(this.getClass(), object);
		
		return that != null && this.getVariables().equals(that.getVariables())
				&& this.getConditions().equals(that.getConditions())
				&& this.getFacts().equals(that.getFacts());
	}
	
	@Override
	public final String toString() {
		return "?" + this.getVariables() + " " + this.getConditions() + "->" + this.getFacts();
	}
	
	/**
	 * @author codistmonk (creation 2014-08-01)
	 */
	public final class Variable implements Expression {
		
		private final String string;
		
		public Variable(final String string) {
			this.string = string;
		}
		
		public final Module getModule() {
			return Module.this;
		}
		
		@Override
		public final int hashCode() {
			return this.toString().hashCode();
		}
		
		@Override
		public final boolean equals(final Object object) {
			final Variable that = cast(this.getClass(), object);
			
			return that != null && this.toString().equals(that.toString())
					&& this.getModule() == that.getModule();
		}
		
		@Override
		public final String toString() {
			return this.string;
		}
		
		@Override
		public final Object accept(final Visitor visitor) {
			return visitor.visit(this);
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = 2038510531251596976L;
		
	}
	
	/**
	 * {@value}.
	 */
	private static final long serialVersionUID = -6696557631458945912L;
	
	public static final Module ROOT = new Module(null);
	
	public static final Variable EQUAL = ROOT.new Variable("=");
	
	public static final Composite equality(final Expression left, final Expression right) {
		final Composite result = new Composite(3);
		
		result.getChildren().add(left);
		result.getChildren().add(EQUAL);
		result.getChildren().add(right);
		
		return result;
	}
	
	static {
		ROOT.getVariables().add(EQUAL);
		
		final Module identity = new Module(ROOT);
		final Variable x = identity.new Variable("x");
		
		identity.getVariables().add(x);
		identity.getFacts().add(equality(x, x));
		
		ROOT.getFacts().add(identity);
	}
	
}