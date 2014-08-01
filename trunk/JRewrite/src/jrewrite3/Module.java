package jrewrite3;

import static net.sourceforge.aprog.tools.Tools.cast;

import java.util.ArrayList;
import java.util.Arrays;
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
		this(parent, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
	}
	
	Module(final Module parent, final List<Variable> variables,
			final List<Expression> conditions, final List<Expression> facts) {
		this.parent = parent;
		this.variables = variables;
		this.conditions = conditions;
		this.facts = facts;
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
	public final <R> R accept(final Visitor<R> visitor) {
		return visitor.visitAfterFacts(this, visitor.visitBeforeVariables(this),
				Expression.listAccept(this.getVariables(), visitor),
				Expression.listAccept(this.getConditions(), visitor),
				Expression.listAccept(this.getFacts(), visitor));
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
		public final <R> R accept(final Visitor<R> visitor) {
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
		return new Composite(Arrays.asList(left, EQUAL, right));
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