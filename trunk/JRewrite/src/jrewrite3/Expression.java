package jrewrite3;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author codistmonk (creation 2014-08-01)
 */
public abstract interface Expression extends Serializable {
	
	public abstract Object accept(Visitor visitor);
	
	public static List<Object> listAccept(final Collection<? extends Expression> expressions, final Visitor visitor) {
		return expressions.stream().map(e -> e.accept(visitor)).collect(Collectors.toList());
	}
	
}

/**
 * @author codistmonk (creation 2014-08-01)
 */
final class Composite implements Expression {
	
	private final List<Expression> children = new ArrayList<>();
	
	public final List<Expression> getChildren() {
		return this.children;
	}
	
	@Override
	public final Object accept(final Visitor visitor) {
		final Object beforeVisit = visitor.visitBeforeChildren(this);
		final List<Object> childVisits = Expression.listAccept(this.getChildren(), visitor);
		
		return visitor.visitAfterChildren(this, beforeVisit, childVisits);
	}
	
	/**
	 * {@value}.
	 */
	private static final long serialVersionUID = -280050093929737583L;
	
}

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
	
	/**
	 * @author codistmonk (creation 2014-08-01)
	 */
	public final class Variable implements Expression {
		
		private final String string;
		
		public Variable(final String string) {
			this.string = string;
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
	
}
