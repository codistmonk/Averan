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
	
	private final List<Symbol> parameters;
	
	private final List<Expression> conditions;
	
	private final List<Expression> facts;
	
	public Module(final Module parent) {
		this(parent, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
	}
	
	Module(final Module parent, final List<Symbol> parameters,
			final List<Expression> conditions, final List<Expression> facts) {
		this.parent = parent;
		this.parameters = parameters;
		this.conditions = conditions;
		this.facts = facts;
	}
	
	public final Symbol parameter(final String parameter) {
		final Symbol result = this.new Symbol(parameter);
		
		this.getParameters().add(result);
		
		return result;
	}
	
	public final Module assume(final Expression fact) {
		this.getFacts().add(fact);
		
		return this;
	}
	
	public final Module getParent() {
		return this.parent;
	}
	
	public final List<Symbol> getParameters() {
		return this.parameters;
	}
	
	public final List<Expression> getConditions() {
		return this.conditions;
	}
	
	public final List<Expression> getFacts() {
		return this.facts;
	}
	
	@Override
	public final <R> R accept(final Visitor<R> visitor) {
		return visitor.endVisit(this, visitor.beginVisit(this),
				Expression.listAccept(this.getParameters(), visitor),
				Expression.listAccept(this.getConditions(), visitor),
				Expression.listAccept(this.getFacts(), visitor));
	}
	
	@Override
	public final int hashCode() {
		return this.getParameters().hashCode() + this.getConditions().hashCode() + this.getFacts().hashCode();
	}
	
	@Override
	public final boolean equals(final Object object) {
		final Module that = cast(this.getClass(), object);
		
		return that != null && this.getParameters().equals(that.getParameters())
				&& this.getConditions().equals(that.getConditions())
				&& this.getFacts().equals(that.getFacts());
	}
	
	@Override
	public final String toString() {
		return "?" + this.getParameters() + " " + this.getConditions() + "->" + this.getFacts();
	}
	
	/**
	 * @author codistmonk (creation 2014-08-01)
	 */
	public final class Symbol implements Expression {
		
		private final String string;
		
		public Symbol(final String string) {
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
			final Symbol that = cast(this.getClass(), object);
			
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
	
	public static final Symbol EQUAL = ROOT.parameter("=");
	
	public static final Composite equality(final Expression left, final Expression right) {
		return new Composite(Arrays.asList(left, EQUAL, right));
	}
	
	static {
		final Module identity = new Module(ROOT);
		final Symbol x = identity.parameter("x");
		
		identity.assume(equality(x, x));
		
		ROOT.assume(identity);
	}
	
}
