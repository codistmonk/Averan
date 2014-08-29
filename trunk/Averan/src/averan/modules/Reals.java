package averan.modules;

import static averan.core.ExpressionTools.$;
import static averan.core.SessionTools.*;
import static averan.core.StructureMatcher.listsMatch;
import static averan.io.ExpressionParser.$$;
import static averan.modules.Standard.IDENTITY;
import static averan.modules.Standard.proveUsingBindAndApply;
import static averan.modules.Standard.recall;
import static averan.modules.Standard.rewriteRight;
import static java.util.stream.Collectors.toList;
import static net.sourceforge.aprog.tools.Tools.cast;
import averan.core.Composite;
import averan.core.Expression;
import averan.core.Module;
import averan.core.Pattern;
import averan.core.Rewriter;
import averan.core.Session;
import averan.core.Visitor;
import averan.core.Module.Bind;
import averan.core.Module.Symbol;
import averan.core.Pattern.Any;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.aprog.tools.IllegalInstantiationException;
import net.sourceforge.aprog.tools.Pair;
import net.sourceforge.aprog.tools.Tools;

/**
 * @author codistmonk (creation 2014-08-27)
 */
public final class Reals {
	
	private Reals() {
		throw new IllegalInstantiationException();
	}
	
	public static final Module MODULE = new Module(Standard.MODULE);
	
	public static final AlgebraicProperty[] REAL_ALGEBRA_RULES = {
			new Noninversion("definition_of_subtraction"),
			new Noninversion("opposite_of_multiplication"),
			new Noninversion("right_distributivity_of_multiplication_over_addition"),
			new Noninversion("associativity_of_addition"),
			new Inversion("ordering_of_terms"),
			new Inversion("commutativity_of_addition"),
			new Noninversion("associativity_of_multiplication"),
			new Inversion("commutativity_of_multiplication"),
			new Inversion("ordering_of_factors"),
			new Noninversion("left_distributivity_of_multiplication_over_addition")
	};
	
	static {
		pushNewSession(MODULE);
		
		try {
			suppose("definition_of_subtraction",
					$$("∀x,y ((x∈ℝ) → ((y∈ℝ) → ((x-y)=(x+(-y)))))"));
			suppose("type_of_opposite",
					$$("∀x ((x∈ℝ) → ((-x)∈ℝ))"));
			suppose("type_of_addition",
					$$("∀x,y ((x∈ℝ) → ((y∈ℝ) → ((x+y)∈ℝ)))"));
			suppose("type_of_subtraction",
					$$("∀x,y ((x∈ℝ) → ((y∈ℝ) → ((x-y)∈ℝ)))"));
			suppose("type_of_multiplication",
					$$("∀x,y ((x∈ℝ) → ((y∈ℝ) → ((xy)∈ℝ)))"));
			admit("right_distributivity_of_multiplication_over_addition",
					$$("∀a,b,c ((a∈ℝ) → ((b∈ℝ) → ((c∈ℝ) → (((a+b)c)=((ac)+(bc))))))"));
			admit("associativity_of_addition",
					$$("∀x,y,z ((x∈ℝ) → ((y∈ℝ) → ((z∈ℝ) → ((x+(y+z))=((x+y)+z)))))"));
			admit("associativity_of_multiplication",
					$$("∀x,y,z ((x∈ℝ) → ((y∈ℝ) → ((z∈ℝ) → ((x(yz))=((xy)z)))))"));
			admit("left_distributivity_of_multiplication_over_addition",
					$$("∀a,b,c ((a∈ℝ) → ((b∈ℝ) → ((c∈ℝ) → ((a(b+c))=((ab)+(ac))))))"));
			admit("right_distributivity_of_multiplication_over_subtraction",
					$$("∀a,b,c ((a∈ℝ) → ((b∈ℝ) → ((c∈ℝ) → (((a-b)c)=((ac)-(bc))))))"));
			admit("left_distributivity_of_multiplication_over_subtraction",
					$$("∀a,b,c ((a∈ℝ) → ((b∈ℝ) → ((c∈ℝ) → ((a(b-c))=((ab)-(ac))))))"));
			admit("commutativity_of_multiplication",
					$$("∀x,y ((x∈ℝ) → ((y∈ℝ) → ((xy)=(yx))))"));
			admit("commutativity_of_addition",
					$$("∀x,y ((x∈ℝ) → ((y∈ℝ) → ((x+y)=(y+x))))"));
			admit("ordering_of_terms",
					$$("∀x,y,z ((x∈ℝ) → ((y∈ℝ) → ((z∈ℝ) → (((x+z)+y)=((x+y)+z)))))"));
			admit("ordering_of_factors",
					$$("∀x,y,z ((x∈ℝ) → ((y∈ℝ) → ((z∈ℝ) → (((xz)y)=((xy)z)))))"));
			admit("opposite_of_multiplication",
					$$("∀x,y ((x∈ℝ) → ((y∈ℝ) → (((-x)y)=(-(xy)))))"));
		} finally {
			popSession();
		}
	}
	
	public static final Expression real(final Expression expression) {
		return $(expression, "∈", "ℝ");
	}
	
	public static final void claimRealEquality(final String factName, final Expression goal) {
		claimRealEquality(session(), factName, goal);
	}
	
	public static final void claimRealEquality(final Session session, final String factName, final Expression goal) {
		session.claim(factName == null ? session.newPropositionName() : factName, goal);
		{
			{
				Expression g = session.getCurrentGoal();
				
				while (g instanceof Module) {
					session.introduce();
					g = session.getCurrentGoal();
				}
			}
			
			final Composite equality = (Composite) session.getCurrentGoal();
			
			canonicalize(equality.get(0), REAL_ALGEBRA_RULES);
			canonicalize(equality.get(2), REAL_ALGEBRA_RULES);
			rewriteRight(factName(-2), factName(-1));
		}
	}
	
	public static final void canonicalize(final Expression expression, final AlgebraicProperty... transformationRules) {
		canonicalize(session(), expression, transformationRules);
	}
	
	public static final void canonicalize(final Session session, final Expression expression, final AlgebraicProperty... transformationRules) {
		boolean keepGoing = true;
		final Module module = new Module(session.getCurrentModule());
		final Session s = new Session(module);
		
//		s.getTrustedModules().addAll(session.getTrustedModules());
		
		s.bind(IDENTITY, expression);
		
		while (keepGoing) {
			keepGoing = false;
			
			for (final AlgebraicProperty transformationRule : transformationRules) {
				final Composite proposition = s.getFact(-1);
				final String propositionName = s.getFactName(-1);
				final List<Pair<Integer, Pattern>> indices = proposition.accept(new IndexFinder(true, transformationRule.newLeftPattern(session)));
				
				for (final Pair<Integer, Pattern> pair : indices) {
					final Integer index = pair.getFirst();
					final Pattern pattern = pair.getSecond();
					
					if (!transformationRule.bindAndApply(s, pattern)) {
						continue;
					}
					
					s.rewrite(propositionName, s.getFactName(-1), index);
					
					final Expression last = s.getFact(-1);
					
					if (transformationRule instanceof Inversion && 0 <= last.toString().compareTo(proposition.toString())) {
						recall(s, propositionName);
					} else {
						keepGoing = true;
						break;
					}
				}
			}
		}
		
		session.getCurrentModule().new Claim(module.getFacts().get(module.getFacts().size() - 1), module).execute();
	}
	
	/**
	 * @author codistmonk (creation 2014-08-22)
	 */
	public static final class IndexFinder implements Visitor<List<Pair<Integer, Pattern>>> {
		
		private final Pattern pattern;
		
		private final boolean waitForTopLevelRHS;
		
		private int subindex;
		
		private int level;
		
		private boolean active;
		
		private final List<Pair<Integer, Pattern>> result;
		
		private final Map<Object, Integer> indices;
		
		public IndexFinder(final Pattern pattern) {
			this(false, pattern);
		}
		
		public IndexFinder(final boolean waitForTopLevelRHS, final Pattern pattern) {
			this.pattern = pattern;
			this.waitForTopLevelRHS = waitForTopLevelRHS;
			this.subindex = -1;
			this.level = -1;
			this.active = !this.waitForTopLevelRHS;
			this.result = new ArrayList<>();
			this.indices = new HashMap<>();
		}
		
		public final Pattern getPattern() {
			return this.pattern;
		}
		
		@Override
		public final List<Pair<Integer, Pattern>> visit(final Any any) {
			return this.beginVisit(any).endVisit(any);
		}
		
		@Override
		public final List<Pair<Integer, Pattern>> visit(final Composite composite) {
			return this.beginVisit(composite)
					.findIndicesIn(composite.getChildren())
					.endVisit(composite);
		}
		
		@Override
		public final List<Pair<Integer, Pattern>> visit(final Symbol symbol) {
			return this.beginVisit(symbol).endVisit(symbol);
		}
		
		@Override
		public final List<Pair<Integer, Pattern>> visit(final Module module) {
			return this.beginVisit(module)
					.findIndicesIn(module.getParameters())
					.findIndicesIn(module.getConditions())
					.findIndicesIn(module.getFacts())
					.endVisit(module);
		}
		
		private final IndexFinder beginVisit(final Expression expression) {
			++this.level;
			
			this.computeResult(this.getPattern().equals(expression));
			
			return this;
		}
		
		private final List<Pair<Integer, Pattern>> endVisit(final Expression expression) {
			if (this.level == 1 && this.subindex == 1 && Module.EQUAL.equals(expression)) {
				this.active = true;
			}
			
			--this.level;
			
			return this.result;
		}
		
		private final boolean isActive() {
			return this.active;
		}
		
		private final List<Pair<Integer, Pattern>> computeResult(final boolean match) {
			if (match) {
				final Pattern patternCopy = this.getPattern().copy();
				final Integer patternIndex = this.indices.compute(patternCopy.getBindings(),
						(k, v) -> v == null ? 0 : v + 1);
				
				if (this.isActive()) {
					this.result.add(new Pair<>(patternIndex, patternCopy));
				}
			}
			
			return this.result;
		}
		
		private final IndexFinder findIndicesIn(final List<? extends Expression> list) {
			final int n = list.size();
			
			for (int i = 0; i < n; ++i) {
				this.subindex = i;
				list.get(i).accept(this);
			}
			
			this.subindex = -1;
			
			return this;
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = -6666401837567106389L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-08-22)
	 */
	public static final class AlgebraicCanonicalizer implements Visitor<Expression> {
		
		private final Session session;
		
		private final Collection<AlgebraicProperty> transformationRules;
		
		public AlgebraicCanonicalizer(Session session) {
			this.session = session;
			this.transformationRules = new ArrayList<>();
		}

		public final AlgebraicCanonicalizer addRules(final AlgebraicProperty... transformationRules) {
			for (final AlgebraicProperty transformationRule : transformationRules) {
				this.transformationRules.add(transformationRule);
			}
			
			return this;
		}
		
		@Override
		public final Expression visit(final Any any) {
			throw new IllegalArgumentException();
		}
		
		@Override
		public final Expression visit(final Composite composite) {
			final Expression compositeVisit = this.tryToReplace(composite);
			
			if (compositeVisit != composite) {
				return compositeVisit;
			}
			
			final List<Expression> childVisits = composite.childrenAcceptor(this).get();
			
			if (listsMatch(composite.getChildren(), childVisits)) {
				return composite;
			}
			
			return new Composite(childVisits);
		}
		
		@Override
		public final Expression visit(final Symbol symbol) {
			return this.tryToReplace(symbol);
		}
		
		@Override
		public final Expression visit(final Module module) {
			final Expression moduleVisit = this.tryToReplace(module);
			
			if (moduleVisit != module) {
				return moduleVisit;
			}
			
			@SuppressWarnings("unchecked")
			final List<Symbol> parameterVisits = (List) module.parametersAcceptor(this).get()
					.stream().filter(e -> module.getParameters().contains(e)).collect(toList());
			final List<Expression> conditionVisits = module.conditionsAcceptor(this).get();
			final List<Expression> factVisits = module.factsAcceptor(this).get();
			
			if (listsMatch(module.getParameters(), parameterVisits)
					&& listsMatch(module.getConditions(), conditionVisits)
					&& listsMatch(module.getFacts(), factVisits)) {
				return module;
			}
			
			return new Module(module.getParent(), module.getName(),
					parameterVisits, conditionVisits, factVisits);
		}
		
		private final Expression tryToReplace(final Expression expression) {
			for (final AlgebraicProperty transformationRule : this.transformationRules) {
				final Pattern pattern = transformationRule.newPattern(this.session);
				final Composite equality = (Composite) pattern.getTemplate();
				
				if (equality.get(0).equals(expression)) {
					final Expression result = pattern.express(equality.get(2));
					
					if (!(transformationRule instanceof Inversion)
							|| result.toString().compareTo(expression.toString()) < 0) {
						return result;
					}
				}
			}
			
			return expression;
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = -4871598360704641832L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-08-22)
	 */
	public static abstract class AlgebraicProperty implements Serializable {
		
		private final String justification;
		
		protected AlgebraicProperty(final String justification) {
			this.justification = justification;
		}
		
		public final String getJustification() {
			return this.justification;
		}
		
		public final boolean bindAndApply(final Session session, final Pattern pattern) {
			final Session s = new Session(new Module(session.getCurrentModule(), session.newPropositionName()));
			
//			s.getTrustedModules().addAll(session.getTrustedModules());
			
			Module module = cast(Module.class, s.getProposition(this.getJustification()));
			
			if (module != null) {
				final Bind bind = s.getCurrentModule().new Bind(s.getCurrentModule(), this.getJustification());
				
				for (Map.Entry<Any.Key, Expression> binding : pattern.getBindings().entrySet()) {
					bind.bind(binding.getKey().getName().toString(), binding.getValue());
				}
				
				bind.execute();
				
				module = cast(Module.class, s.getFact(-1));
				
				while (module != null && !module.getConditions().isEmpty()) {
					final Expression condition = module.getConditions().get(0);
					final int oldFactCount = s.getCurrentModule().getFacts().size();
					proveUsingBindAndApply(s, condition);
					final int newFactCount = s.getCurrentModule().getFacts().size();
					
					if (oldFactCount == newFactCount) {
						return false;
					}
					
					s.apply(s.getFactName(-2), s.getFactName(-1));
					module = cast(Module.class, s.getFact(-1));
				}
			}
			
			session.getCurrentModule().new Claim(session.newPropositionName(), s.getFact(-1), s.getCurrentModule()).execute();
			
			return true;
		}
		
		public final Pattern newPattern(final Session session) {
			final Expression proposition = session.getProposition(this.getJustification());
			
			if (Module.isEquality(proposition)) {
				return new Pattern(proposition);
			}
			
			if (proposition instanceof Module) {
				final Module canonicalModule = ((Module) proposition).canonical();
				final List<Expression> facts = canonicalModule.getFacts();
				
				if (facts.isEmpty()) {
					throw new IllegalArgumentException();
				}
				
				final Expression lastFact = facts.get(facts.size() - 1);
				
				if (!Module.isEquality(lastFact)) {
					throw new IllegalArgumentException();
				}
				
				return this.anyfy(canonicalModule, lastFact);
			}
			
			throw new IllegalArgumentException();
		}
		
		private final Pattern anyfy(final Module context, final Expression expression) {
			final Rewriter rewriter = new Rewriter();
			
			for (final Symbol parameter : context.getParameters()) {
				rewriter.rewrite(parameter, Pattern.any(parameter.toString()));
			}
			
			return new Pattern(expression.accept(rewriter));
		}
		
		public final Pattern newLeftPattern(final Session session) {
			final Expression proposition = session.getProposition(this.getJustification());
			
			if (Module.isEquality(proposition)) {
				return new Pattern(((Composite) proposition).getChildren().get(0));
			}
			
			if (proposition instanceof Module) {
				final Module canonicalModule = ((Module) proposition).canonical();
				final List<Expression> facts = canonicalModule.getFacts();
				
				if (facts.isEmpty()) {
					throw new IllegalArgumentException();
				}
				
				final Composite lastFact = (Composite) facts.get(facts.size() - 1);
				
				if (!Module.isEquality(lastFact)) {
					throw new IllegalArgumentException();
				}
				
				final Rewriter rewriter = new Rewriter();
				
				for (final Symbol parameter : canonicalModule.getParameters()) {
					rewriter.rewrite(parameter, Pattern.any(parameter.toString()));
				}
				
				return new Pattern(lastFact.getChildren().get(0).accept(rewriter));
			}
			
			throw new IllegalArgumentException();
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = 1878172301362441979L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-08-22)
	 */
	public static final class Inversion extends AlgebraicProperty {
		
		public Inversion(final String justification) {
			super(justification);
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = -3885973523639244860L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-08-22)
	 */
	public static final class Noninversion extends AlgebraicProperty {
		
		public Noninversion(final String justification) {
			super(justification);
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = 3917509705666449092L;
		
	}
	
}
