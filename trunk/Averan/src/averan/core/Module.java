package averan.core;

import static java.lang.Integer.parseInt;
import static java.lang.Math.min;
import static java.util.stream.Collectors.toCollection;
import static net.sourceforge.aprog.tools.Tools.array;
import static net.sourceforge.aprog.tools.Tools.cast;
import static net.sourceforge.aprog.tools.Tools.ignore;
import static net.sourceforge.aprog.tools.Tools.list;

import averan.core.Pattern.Any;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import net.sourceforge.aprog.tools.Pair;
import net.sourceforge.aprog.tools.Tools;

/**
 * @author codistmonk (creation 2014-08-01)
 */
public final class Module implements Expression {
	
	private final Module parent;
	
	private final String name;
	
	private final List<Module> trustedModules;
	
	private final List<Symbol> parameters;
	
	private final List<Expression> conditions;
	
	private final Map<String, Integer> conditionIndices;
	
	private final List<Expression> facts;
	
	private final Map<String, Integer> factIndices;
	
	private final List<Statement> statements;
	
	public Module(final Module parent) {
		this(parent, "");
	}
	
	public Module(final Module parent, final String name) {
		this(parent, name, new ArrayList<>());
	}
	
	public Module(final Module parent, final String name, final List<Module> trustedModules) {
		this(parent, name, trustedModules,
				new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
	}
	
	public Module(final Module parent, final String name, final List<Module> trustedModules,
			final List<Symbol> parameters, final List<Expression> conditions, final List<Expression> facts) {
		this.parent = parent;
		this.name = name;
		this.trustedModules = trustedModules;
		
		{
			final Rewriter rewriter = new Rewriter();
			
			this.parameters = parameters.stream()
					.map(s -> this.localParameter(rewriter, s))
					.collect(toCollection(ArrayList::new));
			
			if (!rewriter.getRewrites().isEmpty()) {
				this.conditions = Expression.listAcceptor(conditions, rewriter).get();
				this.facts = Expression.listAcceptor(facts, rewriter).get();
			} else {
				this.conditions = conditions;
				this.facts = facts;
			}
			
			// XXX the proofs may also need to be rewritten
		}
		
		this.conditionIndices = new LinkedHashMap<>(conditions.size());
		this.factIndices = new LinkedHashMap<>(facts.size());
		this.statements = new ArrayList<>(facts.size());
	}
	
	public final Symbol findParameter(final String name) {
		Symbol result = this.getParameter(name);
		
		if (result != null || this.getParent() == null) {
			return result;
		}
		
		return this.getParent().findParameter(name);
	}
	
	public final Symbol getParameter(final String name) {
		for (final Symbol parameter : this.getParameters()) {
			if (parameter.toString().equals(name)) {
				return parameter;
			}
		}
		
		return null;
	}
	
	public final Expression getPropositionOrNull(final String name) {
		Integer resultIndex = this.getConditionIndices().get(name);
		
		if (resultIndex != null) {
			return this.getConditions().get(resultIndex);
		}
		
		resultIndex = this.getFactIndices().get(name);
		
		if (resultIndex != null) {
			return this.getFacts().get(resultIndex);
		}
		
		for (final Module trustedModule : this.getTrustedModules()) {
			Expression result = trustedModule.getPropositionOrNull(name);
			
			if (result != null) {
				return result;
			}
		}
		
		if (this.getParent() != null) {
			return this.getParent().getPropositionOrNull(name);
		}
		
		return null;
	}
	
	public final Expression getProposition(final String name) {
		final Expression result = this.getPropositionOrNull(name);
		
		if (result == null) {
			throw new IllegalArgumentException("Proposition not found: " + name);
		}
		
		return result;
	}
	
	public final Module getParent() {
		return this.parent;
	}
	
	public final String getName() {
		return this.name;
	}
	
	public final List<Module> getTrustedModules() {
		return this.trustedModules;
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
	
	public final List<Statement> getStatements() {
		return this.statements;
	}
	
	public final int getPropositionCount() {
		return this.getConditions().size() + this.getFacts().size();
	}
	
	public final String newPropositionName() {
		return (this.getName().isEmpty() ? "" : (this.getName()) + ".") + (this.getPropositionCount() + 1);
	}
	
	@Override
	public final <R> R accept(final Visitor<R> visitor) {
		return visitor.visit(this);
	}
	
	public final <R> Supplier<List<R>> parametersAcceptor(final Visitor<R> visitor) {
		return Expression.listAcceptor(this.getParameters(), visitor);
	}
	
	public final <R> Supplier<List<R>> conditionsAcceptor(final Visitor<R> visitor) {
		return Expression.listAcceptor(this.getConditions(), visitor);
	}
	
	public final <R> Supplier<List<R>> factsAcceptor(final Visitor<R> visitor) {
		return Expression.listAcceptor(this.getFacts(), visitor);
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
		
		that = this.bind(that);
		
		return this.getConditions().equals(that.getConditions())
				&& this.getFacts().equals(that.getFacts());
	}
	
	public final Module bind(final Module that) {
		if (that == null) {
			return that;
		}
		
		final int n = min(this.getParameters().size(), that.getParameters().size());
		final Rewriter rewriter = new Rewriter();
		
		for (int i = 0; i < n; ++i) {
			rewriter.rewrite(that.getParameters().get(i), this.getParameters().get(i));
		}
		
		@SuppressWarnings("unchecked")
		final List<Symbol> newParameters = (List<Symbol>) (Object) Expression.listAcceptor(
				that.getParameters(), rewriter).get();
		
		newParameters.removeAll(this.getParameters());
		
		return new Module(that.getParent(), that.getName(), this.getTrustedModules(), newParameters,
			Expression.listAcceptor(that.getConditions(), rewriter).get(),
			Expression.listAcceptor(that.getFacts(), rewriter).get());
	}
	
	@Override
	public final String toString() {
		return (this.getParameters().isEmpty() ? "" : "∀" + join(",", this.getParameters()) + " ")
				+ (this.getConditions().isEmpty() ? "" : formatConjunction(this.getConditions()) + " → ")
				+ formatConjunction(this.getFacts());
	}
	
	public static final String formatConjunction(final List<Expression> propositions) {
		if (propositions.size() == 1) {
			final Expression proposition = propositions.get(0);
			
			if (proposition instanceof Symbol || Composite.isBracedComposite(proposition)) {
				return proposition.toString();
			}
		}
		
		return "(" + join(" ∧ ", propositions) + ")";
	}
	
	public static final String join(final String separator, final Iterable<?> elements) {
		return Tools.join(separator, list(elements).toArray());
	}
	
	public final boolean isFree() {
		return this.getParameters().isEmpty() && this.getConditions().isEmpty();
	}
	
	public final boolean canAccess(final Module context) {
		if (this.isInside(context)) {
			return true;
		}
		
		Module freeContextParent = context;
		
		while (freeContextParent != null && freeContextParent.isFree()) {
			freeContextParent = freeContextParent.getParent();
			
			if (this.isInside(freeContextParent)) {
				return true;
			}
		}
		
		return false;
	}
	
	public final boolean isInside(final Module module) {
		Module parent = this;
		
		if (parent == module) {
			return true;
		}
		
		do {
			parent = parent.getParent();
			
			if (parent == module) {
				return true;
			}
		} while (parent != null);
		
		return false;
	}
	
	public final Module canonical() {
		return new Module(this.getParent(), this.getName(), this.getTrustedModules(),
				this.collectParameters(), this.collectConditions(), this.collectTerminalFacts());
	}
	
	public final List<Symbol> collectParameters() {
		final List<Symbol> result = new ArrayList<>(this.getParameters());
		final List<Expression> facts = this.getFacts();
		
		if (facts.size() == 1) {
			final Module fact = cast(Module.class, facts.get(0));
			
			if (fact != null) {
				result.addAll(fact.collectParameters());
			}
		}
		
		return result;
	}
	
	public final List<Expression> collectConditions() {
		final List<Expression> result = flattenFreeFacts(this.getConditions());
		final List<Expression> facts = this.getFacts();
		
		if (facts.size() == 1) {
			final Module fact = cast(Module.class, facts.get(0));
			
			if (fact != null) {
				result.addAll(fact.collectConditions());
			}
		}
		
		return result;
	}
	
	public final List<Expression> collectTerminalFacts() {
		final List<Expression> result = new ArrayList<>();
		final List<Expression> facts = this.getFacts();
		
		if (facts.size() == 1) {
			final Module fact = cast(Module.class, facts.get(0));
			
			if (fact != null) {
				result.addAll(fact.collectTerminalFacts());
			} else {
				result.addAll(flattenFreeFacts(facts));
			}
		} else {
			result.addAll(flattenFreeFacts(facts));
		}
		
		return result;
	}
	
	public final boolean implies(final Expression proposition) {
		if (this.isFree() && this.getFacts().contains(proposition)) {
			return true;
		}
		
		{
			Module that = cast(Module.class, proposition);
			
			if (that == null) {
				return false;
			}
			
			final Module thisCanonical = this.canonical();
			final Module thatCanonical = thisCanonical.bind(that.canonical());
			
			return thatCanonical.getConditions().containsAll(thisCanonical.getConditions()) && thisCanonical.getFacts().containsAll(thatCanonical.getFacts());
		}
	}
	
	public static final List<Expression> collectConditions(final Module module) {
		final List<Expression> result = flattenFreeFacts(module.getConditions());
		
		if (module.getFacts().size() == 1) {
			final Module fact = cast(Module.class, module.getFacts().get(0));
			
			if (fact != null) {
				result.addAll(collectConditions(fact));
			}
		}
		
		return result;
	}
	
	public static final List<Expression> flattenFreeFacts(final List<Expression> facts) {
		final List<Expression> result = new ArrayList<>(facts);
		int i = 0;
		int n = result.size();
		
		while (i < n) {
			while (i < n) {
				final Module module = cast(Module.class, result.get(i));
				
				if (module != null && module.isFree()) {
					result.addAll(module.getFacts());
				}
				
				++i;
			}
			
			n = result.size();
		}
		
		return result;
	}
	
	public static final List<Expression> flattenTerminalFacts(final List<Expression> facts) {
		final List<Expression> result = new ArrayList<>();
		
		for (final Expression fact : facts) {
			final Module module = cast(Module.class, fact);
			
			if (module != null) {
				result.addAll(flattenTerminalFacts(module.getFacts()));
			} else {
				result.add(fact);
			}
		}
		
		return result;
	}
	
	final void newProposition(final Map<String, Integer> indices, final String propositionName) {
		if (this.getPropositionOrNull(propositionName) != null) {
			throw new IllegalArgumentException("Name already in use: " + propositionName);
		}
		
		indices.put(propositionName == null ? this.newPropositionName() : propositionName, indices.size());
	}
	
	private final Symbol localParameter(final Rewriter rewriter, final Symbol parameter) {
		if (parameter.getContext() == this) {
			return parameter;
		}
		
		final Symbol result = this.new Symbol(parameter.toString());
		
		rewriter.rewrite(parameter, result);
		
		return result;
	}
	
	/**
	 * @author codistmonk (creation 2014-08-01)
	 */
	public final class Symbol implements Expression {
		
		private final String string;
		
		public Symbol(final String string) {
			this.string = string;
		}
		
		public final Module getContext() {
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
					&& this.getContext() == that.getContext();
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
	 * @author codistmonk (creation 2014-08-02)
	 */
	public abstract class AddProposition implements Statement {
		
		private final String propositionName;
		
		public AddProposition(final String propositionName) {
			this.propositionName = propositionName;
		}
		
		public final String getPropositionName() {
			return this.propositionName;
		}
		
		protected final Module addProposition(final Expression proposition, final String propositionName,
				final List<Expression> propositions, final Map<String, Integer> propositionIndices) {
			final Module result = Module.this;
			
			result.newProposition(propositionIndices, propositionName);
			propositions.add(proposition);
			
			return result;
		}
		
		protected final Module addFact(final Expression fact, final String factName) {
			final Module result = Module.this;
			
			this.addProposition(fact, factName, result.getFacts(), result.getFactIndices());
			result.getStatements().add(this);
			
			return result;
		}
		
		protected final Module addFact(final Expression fact) {
			return this.addFact(fact, this.getPropositionName());
		}
		
		protected final Module addFacts(final Module protofact) {
			if (1 == protofact.getFacts().size()) {
				return this.addFact(protofact.getFacts().get(0));
			}
			
			for (final Map.Entry<String, Integer> entry : protofact.getFactIndices().entrySet()) {
				this.addFact(protofact.getFacts().get(entry.getValue()),
						this.getPropositionName() + "/" + entry.getKey());
			}
			
			return Module.this;
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = 5756077527983505640L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-08-12)
	 */
	public final class Parametrize implements Statement {
		
		private final String name;
		
		public Parametrize(final String name) {
			this.name = name;
			
			if (Module.this.getParameter(name) != null) {
				throw new IllegalArgumentException("Duplicate parameter: " + name);
			}
			
			final Symbol s = Module.this.new Symbol(name);
			final AtomicBoolean alreadyInUse = new AtomicBoolean();
			
			Module.this.accept(new Visitor<Void>() {
				
				@Override
				public final Void visit(final Any any) {
					throw new IllegalArgumentException();
				}
				
				@Override
				public final Void visit(final Composite composite) {
					if (!alreadyInUse.get()) {
						composite.childrenAcceptor(this).get();
					}
					
					return null;
				}
				
				@Override
				public final Void visit(final Symbol symbol) {
					if (!alreadyInUse.get() && s.equals(symbol)) {
						alreadyInUse.set(true);
					}
					
					return null;
				}
				
				@Override
				public final Void visit(final Module module) {
					if (!alreadyInUse.get()) {
						module.parametersAcceptor(this).get();
					}
					
					if (!alreadyInUse.get()) {
						module.conditionsAcceptor(this).get();
					}
					
					if (!alreadyInUse.get()) {
						module.factsAcceptor(this).get();
					}
					
					return null;
				}
				
				/**
				 * {@value}.
				 */
				private static final long serialVersionUID = 4148000735632446839L;
				
			});
			
			if (alreadyInUse.get()) {
				throw new IllegalArgumentException("Requested parameter already in use: " + name);
			}
		}
		
		public final String getName() {
			return this.name;
		}
		
		public final Symbol executeAndGet() {
			return this.execute().getParameter(this.getName());
		}
		
		@Override
		public final Parametrize copyFor(final Module otherModule) {
			return otherModule.new Parametrize(this.getName());
		}
		
		@Override
		public final Module execute() {
			final Module result = Module.this;
			
			result.getParameters().add(result.new Symbol(this.getName()));
			
			return result;
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = 1492936934188936877L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-08-02)
	 */
	public final class Suppose extends AddProposition {
		
		private final Expression condition;
		
		public Suppose(final Expression condition) {
			this(null, condition);
		}
		
		public Suppose(final String conditionName, final Expression condition) {
			super(conditionName);
			this.condition = condition;
		}
		
		public final Expression getCondition() {
			return this.condition;
		}
		
		@Override
		public final Suppose copyFor(final Module otherModule) {
			return otherModule.new Suppose(this.getPropositionName(), this.getCondition());
		}
		
		@Override
		public final Module execute() {
			final Module module = Module.this;
			
			return this.addProposition(this.getCondition(), this.getPropositionName(),
					module.getConditions(), module.getConditionIndices());
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = -3935414790571741334L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-08-02)
	 */
	public final class Admit extends AddProposition {
		
		private final Expression fact;
		
		public Admit(final Expression fact) {
			this(null, fact);
		}
		
		public Admit(final String factName, final Expression fact) {
			super(factName);
			this.fact = fact;
		}
		
		public final Expression getFact() {
			return this.fact;
		}
		
		@Override
		public final Statement copyFor(final Module otherModule) {
			return otherModule.new Admit(this.getPropositionName(), this.getFact());
		}
		
		@Override
		public final Module execute() {
			return this.addFact(this.getFact());
		}
		
		@Override
		public final String toString() {
			return "Admit";
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = -6762359358588862640L;
		
	}
		
	/**
	 * @author codistmonk (creation 2014-08-02)
	 */
	public final class Claim extends AddProposition {
		
		private final Expression fact;
		
		private final Module proofContext;
		
		public Claim(final Module module) {
			this((String) null, module);
		}
		
		public Claim(final String factName, final Module module) {
			this(factName, module, module);
		}
		
		public Claim(final Expression fact, final Module proofContext) {
			this(null, fact, proofContext);
		}
		
		public Claim(final String factName, final Expression fact, final Module proofContext) {
			super(factName);
			
			this.fact = fact;
			this.proofContext = proofContext;
			
			if (fact instanceof Module) {
				if (!proofContext.implies(fact)) {
					Tools.debugError(proofContext);
					Tools.debugError(fact);
					throw new IllegalArgumentException("Invalid proof");
				}
			} else if (!Module.this.canAccess(proofContext)
					|| !proofContext.getParameters().isEmpty()
					|| !proofContext.getConditions().isEmpty()
					|| !fact.equals(proofContext.getFacts().get(proofContext.getFacts().size() - 1))) {
				throw new IllegalArgumentException("Invalid proof");
			}
		}
		
		public final Expression getFact() {
			return this.fact;
		}
		
		public final Module getProofContext() {
			return this.proofContext;
		}
		
		@Override
		public final Claim copyFor(final Module otherModule) {
			return otherModule.new Claim(this.getPropositionName(), this.getFact(), this.getProofContext());
		}
		
		@Override
		public final Module execute() {
			return this.addFact(this.getFact());
		}
		
		@Override
		public final String toString() {
			final int n = this.getProofContext().getFacts().size();
			return "Claim in " + n + " step" + (n == 1 ? "" : "s");
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = 8939922924220505450L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-08-02)
	 */
	public final class Substitute extends AddProposition {
		
		private final Composite subsitution;
		
		public Substitute(final Composite expression) {
			this(null, expression);
		}
		
		public Substitute(final String propositionName, final Composite expression) {
			super(propositionName);
			
			if (!isSubstitution(expression)) {
				throw new IllegalArgumentException("Not a substitution: " + expression);
			}
			
			this.subsitution = expression;
		}
		
		public final Composite getExpression() {
			return this.subsitution;
		}
		
		@Override
		public final Substitute copyFor(final Module otherModule) {
			return otherModule.new Substitute(this.getPropositionName(), this.getExpression());
		}
		
		@Override
		public final Module execute() {
			final Rewriter rewriter = new Rewriter(this);
			final List<Expression> subsitution = this.getExpression().getChildren();
			
			for (final Expression e : (Composite) subsitution.get(1)) {
				final Composite equality = (Composite) e;
				
				rewriter.rewrite(equality.getChildren().get(0), equality.getChildren().get(2));
			}
			
			if (subsitution.size() == 3) {
				for (final Expression e : (Composite) subsitution.get(2)) {
					rewriter.getIndices().add(parseInt(e.toString()));
				}
			}
			
			return this.addFact(equality(this.getExpression(), subsitution.get(0).accept(rewriter)));
		}
		
		@Override
		public final String toString() {
			final Pair<String, Object[]> format = this.getFormatString(e -> e.toString());
			
			return String.format(format.getFirst(), format.getSecond());
		}
		
		@Override
		public final Pair<String, Object[]> getFormatString(final Function<? super Expression, String> f) {
			return new Pair<>("Substitute %s", array(f.apply(this.getExpression())));
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = -6314585645769164671L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-08-02)
	 */
	public final class Rewrite extends AddProposition {
		
		private final PropositionReference<Expression> source;
		
		private final PropositionReference<Composite> equality;
		
		private final Set<Integer> indices;
		
		public Rewrite(final Module sourceContext, final String sourceName,
				final Module equalityContext, final String equalityName) {
			this(null, sourceContext, sourceName, equalityContext, equalityName);
		}
		
		public Rewrite(final Module sourceContext, final String sourceName,
				final Module equalityContext, final String equalityName,
				final Set<Integer> indices) {
			this(null, sourceContext, sourceName, equalityContext, equalityName, indices);
		}
		
		public Rewrite(final String factName, final Module sourceContext, final String sourceName,
				final Module equalityContext, final String equalityName) {
			this(factName, sourceContext, sourceName, equalityContext, equalityName, new TreeSet<>());
		}
		
		public Rewrite(final String factName, final Module sourceContext,
				final String sourceName, final Module equalityContext, final String equalityName,
				final Set<Integer> indices) {
			super(factName);
			
			if (!Module.this.canAccess(sourceContext)) {
				throw new IllegalArgumentException("Inaccessible source context");
			}
			
			if (!Module.this.canAccess(equalityContext)) {
				throw new IllegalArgumentException("Inaccessible equality context");
			}
			
			this.source = new PropositionReference<>(sourceContext, sourceName);
			this.equality = new PropositionReference<>(equalityContext, equalityName);
			this.indices = indices;
			
			if (!isEquality(this.getEquality().getProposition())) {
				throw new IllegalArgumentException();
			}
		}
		
		public final Rewrite atIndices(final Integer... indices) {
			for (final Integer index : indices) {
				this.getIndices().add(index);
			}
			
			return this;
		}
		
		public final Set<Integer> getIndices() {
			return this.indices;
		}
		
		public final PropositionReference<Expression> getSource() {
			return this.source;
		}
		
		public final PropositionReference<Composite> getEquality() {
			return this.equality;
		}
		
		@Override
		public final Rewrite copyFor(final Module otherModule) {
			return otherModule.new Rewrite(this.getPropositionName(),
					this.getSource().getContext(), this.getSource().getPropositionName(),
					this.getEquality().getContext(), this.getEquality().getPropositionName(),
					this.getIndices());
		}
		
		@Override
		public final Module execute() {
			final Module result = Module.this;
			final Expression source = this.getSource().getProposition();
			final Composite equality = this.getEquality().getProposition();
			final Expression pattern = equality.getChildren().get(0);
			final Expression replacement = equality.getChildren().get(2);
			final Expression newFact = source.accept(
					new Rewriter(this).rewrite(pattern, replacement).atIndices(this.getIndices()));
			
			this.addFact(newFact);
			
			return result;
		}
		
		@Override
		public final String toString() {
			return "Rewrite (" + this.getSource().getPropositionName()
					+ ") using (" + this.getEquality().getPropositionName() + ")"
					+ (this.getIndices().isEmpty() ? "" : " at indices " + this.getIndices());
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = -1742061211293593816L;
		
	}
	
	public static final <T> Map<T, T> mapAccept(final Map<Expression, Expression> map, final Visitor<T> visitor) {
		final Map<T, T> result = new LinkedHashMap<>();
		
		for (final Map.Entry<Expression, Expression> entry : map.entrySet()) {
			result.put(entry.getKey().accept(visitor), entry.getValue().accept(visitor));
		}
		
		return result;
	}
	
	/**
	 * @author codistmonk (creation 2014-08-02)
	 */
	public final class Bind extends AddProposition {
		
		private final PropositionReference<Module> module;
		
		private final Rewriter binder;
		
		private int bound;
		
		public Bind(final Module context, final String moduleName) {
			this(null, context, moduleName);
		}
		
		public Bind(final String factName, final Module context, final String moduleName) {
			super(factName);
			
			if (!Module.this.canAccess(context)) {
				throw new IllegalArgumentException("Inaccessible module context");
			}
			
			this.module = new PropositionReference<>(context, moduleName);
			this.binder = new Rewriter(this);
		}
		
		public final PropositionReference<Module> getModule() {
			return this.module;
		}
		
		public final Rewriter getBinder() {
			return this.binder;
		}
		
		public final Bind bind(final Expression... expressions) {
			final List<Symbol> parameters = this.getModule().getProposition().getParameters();
			
			for (final Expression expression : expressions) {
				if (this.bound < 0 || parameters.size() <= this.bound) {
					Tools.debugError(this.bound);
					Tools.debugError(parameters.size());
					throw new IllegalStateException("Inconsistent binding");
				}
				
				this.getBinder().rewrite(parameters.get(this.bound++), expression);
			}
			
			return this;
		}
		
		public final Bind bind(final String parameterName, final Expression expression) {
			if (0 < this.bound) {
				throw new IllegalStateException("Inconsistent binding");
			}
			
			final Symbol parameter = this.getModule().getProposition().getParameter(parameterName);
			
			if (parameter == null) {
				throw new IllegalArgumentException("Parameter not found: " + parameterName);
			}
			
			this.getBinder().rewrite(parameter, expression);
			
			this.bound = -1;
			
			return this;
		}
		
		@Override
		public final Bind copyFor(final Module otherModule) {
			final Bind result = otherModule.new Bind(this.getPropositionName(), this.getModule().getContext(), this.getModule().getPropositionName());
			
			result.getBinder().getRewrites().putAll(this.getBinder().getRewrites());
			result.getBinder().getIndices().addAll(this.getBinder().getIndices());
			
			return result;
		}
		
		@Override
		public final Module execute() {
			final Module protofact = (Module) this.getModule().getProposition().accept(this.getBinder());
			
			if (protofact.isFree()) {
				return this.addFacts(protofact);
			}
			
			return this.addFact(protofact);
		}
		
		@Override
		public final String toString() {
			final Pair<String, Object[]> format = this.getFormatString(e -> e.toString());
			
			return String.format(format.getFirst(), format.getSecond());
		}
		
		@Override
		public final Pair<String, Object[]> getFormatString(final Function<? super Expression, String> f) {
			final Map<String, String> map = new LinkedHashMap<>();
			
			for (final Map.Entry<Expression, Expression> entry : this.getBinder().getRewrites().entrySet()) {
				map.put(f.apply(entry.getKey()), f.apply(entry.getValue()));
			}
			
			return new Pair<>("Bind (" + this.getModule().getPropositionName() + ")"
					+ (this.getBinder().getRewrites().isEmpty() ? "" : " using %s"), array(map.toString()));
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = 32338105611173978L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-08-02)
	 */
	public final class Apply extends AddProposition {
		
		private final PropositionReference<Module> module;
		
		private final PropositionReference<Module> proposition;
		
		private int removedConditions;
		
		public Apply(final Module moduleContext, final String moduleName,
				final Module propositionContext, final String propositionName) {
			this(null, moduleContext, moduleName, propositionContext, propositionName);
		}
		
		public Apply(final String factName, final Module moduleContext, final String moduleName,
				final Module propositionContext, final String propositionName) {
			super(factName);
			
			if (!Module.this.canAccess(moduleContext)) {
				throw new IllegalArgumentException("Inaccessible module context");
			}
			
			if (!Module.this.canAccess(propositionContext)) {
				throw new IllegalArgumentException("Inaccessible proposition context");
			}
			
			this.module = new PropositionReference<>(moduleContext, moduleName);
			this.proposition = new PropositionReference<>(propositionContext, propositionName);
			
			{
				final Expression condition = this.getProposition().getProposition();
				
				if (this.getModule().getProposition().getConditions()
						.get(this.removedConditions).equals(condition)) {
					++this.removedConditions;
				} else {
					throw new IllegalArgumentException("Condition " + this.getRemovedConditions()
							+ " does not match " + this.getProposition().getPropositionName());
				}
			}
		}
		
		public final PropositionReference<Module> getModule() {
			return this.module;
		}
		
		public final PropositionReference<Module> getProposition() {
			return this.proposition;
		}
		
		public final int getRemovedConditions() {
			return this.removedConditions;
		}
		
		@Override
		public final Apply copyFor(final Module otherModule) {
			return otherModule.new Apply(this.getPropositionName(),
					this.getModule().getContext(), this.getModule().getPropositionName(),
					this.getProposition().getContext(), this.getProposition().getPropositionName());
		}
		
		@Override
		public final Module execute() {
			final Module protofact = this.getModule().getProposition();
			final List<Expression> allConditions = protofact.getConditions();
			final int removedConditions = this.getRemovedConditions();
			final List<Expression> remainingConditions = allConditions.subList(
					removedConditions, allConditions.size());
			
			if (protofact.getParameters().isEmpty() && remainingConditions.isEmpty()) {
				return this.addFacts(protofact);
			}
			
			if (remainingConditions.size() != allConditions.size()) {
				final Module newFact = new Module(protofact.getParent(), protofact.getName(), protofact.getTrustedModules(),
						protofact.getParameters(), remainingConditions, protofact.getFacts());
				
				for (final Map.Entry<String, Integer> entry : protofact.getConditionIndices().entrySet()) {
					if (removedConditions <= entry.getValue()) {
						newFact.getConditionIndices().put(entry.getKey(), entry.getValue() - removedConditions);
					}
				}
				
				newFact.getFactIndices().putAll(protofact.getFactIndices());
				newFact.getStatements().addAll(Collections.nCopies(protofact.getFacts().size(), this));
				
				return this.addFact(newFact);
			}
			
			return this.addFact(protofact);
		}
		
		@Override
		public final String toString() {
			return "Apply (" + this.getModule().getPropositionName()
					+ ") using (" + this.getProposition().getPropositionName() + ")";
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = 1301730993080837859L;
		
	}
	
	/**
	 * {@value}.
	 */
	private static final long serialVersionUID = -6696557631458945912L;
	
	public static final Module ROOT = new Module(null);
	
	public static final Symbol EQUAL = ROOT.new Symbol("=");
	
	public static final Pattern NATURAL = Pattern.compile("\\d+");
	
	public static final Composite equality(final Expression left, final Expression right) {
		return new Composite(Arrays.asList(left, EQUAL, right));
	}
	
	public static final boolean isEquality(final Object object) {
		final Composite composite = cast(Composite.class, object);
		
		return composite != null
				&& composite.getChildren().size() == 3
				&& EQUAL.equals(composite.getChildren().get(1));
	}
	
	public static final boolean isSubstitution(final Object object) {
		final Composite composite = cast(Composite.class, object);
		
		if (composite == null) {
			return false;
		}
		
		final List<Expression> children = composite.getChildren();
		final int n = children.size();
		
		return 2 <= n && n <= 3
				&& isSequenceOfEqualities(children.get(1))
				&& (n != 3 || isSequenceOfIndices(children.get(2)));
	}
	
	public static final boolean isNatural(final Object object) {
		return object instanceof Symbol && NATURAL.matcher(object.toString()).matches();
	}
	
	public static final boolean isSequenceOfIndices(final Object object) {
		final Composite composite = cast(Composite.class, object);
		
		if (composite == null) {
			return false;
		}
		
		for (final Expression child : composite) {
			if (!isNatural(child)) {
				return false;
			}
		}
		
		return true;
	}
	
	public static final boolean isSequenceOfEqualities(final Object object) {
		final Composite composite = cast(Composite.class, object);
		
		if (composite == null) {
			return false;
		}
		
		for (final Expression child : composite) {
			if (!isEquality(child)) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * @author codistmonk (creation 2014-08-02)
	 */
	public static abstract interface Statement extends Serializable {
		
		public abstract Statement copyFor(Module otherModule);
		
		public abstract Module execute();
		
		public default Pair<String, Object[]> getFormatString(final Function<? super Expression, String> f) {
			ignore(f);
			
			return new Pair<>(this.toString(), array());
		}
		
	}
	
	/**
	 * @author codistmonk (creation 2014-08-02)
	 * @param <P> 
	 */
	public static final class PropositionReference<P extends Expression> implements Serializable {
		
		private final Module context;
		
		private final String propositionName;
		
		private final P proposition;
		
		public PropositionReference(final Module context, final String propositionName) {
			this.context = context;
			this.propositionName = propositionName;
			this.proposition = this.getProposition();
		}
		
		public final Module getContext() {
			return this.context;
		}
		
		public final String getPropositionName() {
			return this.propositionName;
		}
		
		@SuppressWarnings("unchecked")
		public final P getProposition() {
			if (this.proposition != null) {
				return this.proposition;
			}
			
			return (P) this.getContext().getProposition(this.getPropositionName());
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = 8821738607644174239L;
		
	}
	
}
