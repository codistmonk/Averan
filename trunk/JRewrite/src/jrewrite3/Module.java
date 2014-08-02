package jrewrite3;

import static java.util.Collections.emptySet;
import static net.sourceforge.aprog.tools.Tools.cast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author codistmonk (creation 2014-08-01)
 */
public final class Module implements Expression {
	
	private final Module parent;
	
	private final List<Symbol> parameters;
	
	private final List<Expression> conditions;
	
	private final Map<String, Integer> conditionIndices;
	
	private final List<Expression> facts;
	
	private final Map<String, Integer> factIndices;
	
	private final List<Command> proofs;
	
	public Module(final Module parent) {
		this(parent, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
	}
	
	Module(final Module parent, final List<Symbol> parameters,
			final List<Expression> conditions, final List<Expression> facts) {
		this.parent = parent;
		this.parameters = parameters;
		this.conditions = conditions;
		this.conditionIndices = new LinkedHashMap<>(conditions.size());
		this.facts = facts;
		this.factIndices = new LinkedHashMap<>(facts.size());
		this.proofs = new ArrayList<>(facts.size());
	}
	
	public final Symbol parameter(final String parameter) {
		for (final Symbol p : this.getParameters()) {
			if (p.toString().equals(parameter)) {
				return p;
			}
		}
		
		final Symbol result = this.new Symbol(parameter);
		
		this.getParameters().add(result);
		
		return result;
	}
	
	public final Module execute(final Suppose suppose) {
		this.newProposition(this.getConditionIndices(), suppose.getConditionName());
		this.getConditions().add(suppose.getCondition());
		
		return this;
	}
	
	public final Module execute(final Admit admit) {
		this.newProposition(this.getFactIndices(), admit.getFactName());
		this.getFacts().add(admit.getFact());
		this.getProofs().add(admit);
		
		return this;
	}
	
	public final Module execute(final Rewrite rewrite) {
		final Expression source = rewrite.getSource();
		final Composite equality = rewrite.getEquality();
		final Expression pattern = equality.getChildren().get(0);
		final Expression replacement = equality.getChildren().get(2);
		final Expression newFact = source.accept(
				new Rewriter(rewrite).rewrite(pattern, replacement));
		
		this.newProposition(this.getFactIndices(), rewrite.getFactName());
		this.getFacts().add(newFact);
		this.getProofs().add(rewrite);
		
		return this;
	}
	
	public final Expression getProposition(final String name) {
		Integer resultIndex = this.getConditionIndices().get(name);
		
		if (resultIndex != null) {
			return this.getConditions().get(resultIndex);
		}
		
		resultIndex = this.getFactIndices().get(name);
		
		if (resultIndex != null) {
			return this.getFacts().get(resultIndex);
		}
		
		if (this.getParent() != null) {
			return this.getParent().getProposition(name);
		}
		
		throw new IllegalArgumentException("Proposition not found: " + name);
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
	
	public final Map<String, Integer> getConditionIndices() {
		return this.conditionIndices;
	}
	
	public final List<Expression> getFacts() {
		return this.facts;
	}
	
	public final Map<String, Integer> getFactIndices() {
		return this.factIndices;
	}
	
	public final List<Command> getProofs() {
		return this.proofs;
	}
	
	public final int getPropositionCount() {
		return this.getConditions().size() + this.getFacts().size();
	}
	
	public final String newPropositionName() {
		return "#" + this.getPropositionCount();
	}
	
	@Override
	public final <R> R accept(final Visitor<R> visitor) {
		return visitor.endVisit(this, visitor.beginVisit(this),
				Expression.listAcceptor(this.getParameters(), visitor),
				Expression.listAcceptor(this.getConditions(), visitor),
				Expression.listAcceptor(this.getFacts(), visitor));
	}
	
	@Override
	public final int hashCode() {
		return this.getParameters().hashCode() + this.getConditions().hashCode() + this.getFacts().hashCode();
	}
	
	@Override
	public final boolean equals(final Object object) {
		final int n = this.getParameters().size();
		Module that = cast(this.getClass(), object);
		
		if (that == null || n != that.getParameters().size()
				|| this.getConditions().size() != that.getConditions().size()
				|| this.getFacts().size() != that.getFacts().size()) {
			return false;
		}
		
		final Rewriter rewriter = new Rewriter();
		
		for (int i = 0; i < n; ++i) {
			rewriter.rewrite(that.getParameters().get(i), this.getParameters().get(i));
		}
		
		that = (Module) that.accept(rewriter);
		
		return this.getConditions().equals(that.getConditions())
				&& this.getFacts().equals(that.getFacts());
	}
	
	@Override
	public final String toString() {
		return (this.getParameters().isEmpty() ? "" : "?" + this.getParameters() + " ")
				+ (this.getConditions().isEmpty() ? "" : this.getConditions() + " -> ")
				+ this.getFacts();
	}
	
	private final void newProposition(final Map<String, Integer> indices, final String propositionName) {
		indices.put(propositionName == null ? this.newPropositionName() : propositionName, indices.size());
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
	
	public static final String IDENTITY = "identity";
	
	public static final Composite equality(final Expression left, final Expression right) {
		return new Composite(Arrays.asList(left, EQUAL, right));
	}
	
	public static final boolean isEquality(final Object object) {
		final Composite composite = cast(Composite.class, object);
		
		return composite != null
				&& composite.getChildren().size() == 3
				&& EQUAL.equals(composite.getChildren().get(1));
	}
	
	static {
		final Module identity = new Module(ROOT);
		final Symbol x = identity.parameter("x");
		
		identity.execute(new Admit(equality(x, x)));
		
		ROOT.execute(new Admit(IDENTITY, identity));
	}
	
	/**
	 * @author codistmonk (creation 2014-08-02)
	 */
	public static abstract interface Command extends Serializable {
		//  Empty
	}
	
	/**
	 * @author codistmonk (creation 2014-08-02)
	 */
	public static final class Suppose implements Command {
		
		private final String conditionName;
		
		private final Expression condition;
		
		public Suppose(final Expression condition) {
			this(null, condition);
		}
		
		public Suppose(final String conditionName, final Expression condition) {
			this.conditionName = conditionName;
			this.condition = condition;
		}
		
		public final String getConditionName() {
			return this.conditionName;
		}
		
		public final Expression getCondition() {
			return this.condition;
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = -3935414790571741334L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-08-02)
	 */
	public static final class Admit implements Command {
		
		private final String factName;
		
		private final Expression fact;
		
		public Admit(final Expression fact) {
			this(null, fact);
		}
		
		public Admit(final String factName, final Expression fact) {
			this.factName = factName;
			this.fact = fact;
		}
		
		public final String getFactName() {
			return this.factName;
		}
		
		public final Expression getFact() {
			return this.fact;
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = -6762359358588862640L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-08-02)
	 */
	public static final class Rewrite implements Command {
		
		private final String factName;
		
		private final Module sourceModule;
		
		private final String sourceName;
		
		private final Module equalityModule;
		
		private final String equalityName;
		
		private final Set<Integer> indices;
		
		public Rewrite(final Module sourceModule, final String sourceName,
				final Module equalityModule, final String equalityName) {
			this(null, sourceModule, sourceName, equalityModule, equalityName);
		}
		
		public Rewrite(final Module sourceModule, final String sourceName,
				final Module equalityModule, final String equalityName,
				final Set<Integer> indices) {
			this(null, sourceModule, sourceName, equalityModule, equalityName, indices);
		}
		
		public Rewrite(final String factName, final Module sourceModule, final String sourceName,
				final Module equalityModule, final String equalityName) {
			this(factName, sourceModule, sourceName, equalityModule, equalityName, emptySet());
		}
		
		public Rewrite(final String factName, final Module sourceModule,
				final String sourceName, final Module equalityModule, final String equalityName,
				final Set<Integer> indices) {
			this.factName = factName;
			this.sourceModule = sourceModule;
			this.sourceName = sourceName;
			this.equalityModule = equalityModule;
			this.equalityName = equalityName;
			this.indices = indices;
		}
		
		public final String getFactName() {
			return this.factName;
		}
		
		public final Module getSourceModule() {
			return this.sourceModule;
		}
		
		public final String getSourceName() {
			return this.sourceName;
		}
		
		public final Module getEqualityModule() {
			return this.equalityModule;
		}
		
		public final String getEqualityName() {
			return this.equalityName;
		}
		
		public final Set<Integer> getIndices() {
			return this.indices;
		}
		
		public final Expression getSource() {
			return this.getSourceModule().getProposition(this.getSourceName());
		}
		
		public final Composite getEquality() {
			final Composite result = cast(Composite.class, this.getEqualityModule().getProposition(this.getEqualityName()));
			
			if (!isEquality(result)) {
				throw new IllegalArgumentException("Not an equality: " + this.getEqualityName());
			}
			
			return result;
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = -1742061211293593816L;
		
	}
	
}
