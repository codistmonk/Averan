package jrewrite2;

import static net.sourceforge.aprog.tools.Tools.cast;
import static net.sourceforge.aprog.tools.Tools.join;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author codistmonk (creation 2014-06-10)
 */
public final class Context implements Serializable {
	
	private final Context parent;
	
	private final int parentFactCount;
	
	private final List<Fact> facts;
	
	private final Map<String, Integer> factIndices;
	
	private Expression goal;
	
	private int goalReached;
	
	private final List<UndoableOperation> undoableOperations;
	
	public Context() {
		this(null, TRUE);
	}
	
	public Context(final Context parent, final Expression goal) {
		this.parent = parent;
		this.parentFactCount = parent == null ? 0 : parent.getFactCount();
		this.goal = goal;
		this.facts = new ArrayList<>();
		this.factIndices = new LinkedHashMap<>();
		this.goalReached = TRUE.equals(goal) || ASSUMED.equals(goal) ? 1 : 0;
		this.undoableOperations = new ArrayList<>();
		
		if (parent != null) {
			parent.new SubcontextCreatedEvent(this).fire();
		}
	}
	
	public final Context getParent() {
		return this.parent;
	}
	
	public final Expression getGoal() {
		return this.goal;
	}
	
	public final boolean isGoalReached() {
		return 0 < this.goalReached;
	}
	
	public final int getLocalFactCount() {
		return this.facts.size();
	}
	
	public final int getFactCount() {
		return this.parentFactCount + this.getLocalFactCount();
	}
	
	public final Fact getFact(final int index) {
		final int normalizedIndex = getNormalizedIndex(index);
		
		if (normalizedIndex < this.parentFactCount) {
			return this.getParent().getFact(normalizedIndex);
		}
		
		return this.facts.get(normalizedIndex - this.parentFactCount);
	}
	
	public final int getNormalizedIndex(final int index) {
		final int n = this.getFactCount();
		
		return (n + index) % n;
	}
	
	public final int getFactIndex(final String key) {
		Integer result = this.factIndices.get(key);
		
		if (result == null && this.getParent() != null) {
			result = this.getParent().getFactIndex(key);
		}
		
		return result;
	}
	
	public final void assume(final String key, final Expression proposition) {
		this.accept(key, proposition, ASSUMED);
	}
	
	public final Context prove(final String key, final Expression proposition) {
		return this.addFact(key, proposition, this, proposition).getProof();
	}
	
	public final void introduce(final String key) {
		final Rule rule = cast(Rule.class, this.getGoal());
		
		if (rule != null) {
			this.goal = rule.getExpression();
			
			this.accept(key, rule.getCondition(), TRUE);
			
			this.undoableOperations.add(UndoableOperation.Simple.INTRODUCE_CONDITION);
			
			return;
		}
		
		final Template template = cast(Template.class, this.getGoal());
		
		if (template != null) {
			this.goal = template.getProposition();
			final Symbol variable = new Symbol(template.getVariableName());
			
			this.accept(key, new Equality(variable, variable), TRUE);
			
			this.undoableOperations.add(new UndoableOperation.IntroduceVariable(template));
			
			return;
		}
		
		throw new IllegalStateException();
	}
	
	public final void bind(final String key, final int templateIndex, final Expression expression) {
		final Template template = (Template) this.getFact(templateIndex).getProposition();
		final Expression newProposition = (Expression) template.getProposition().accept(new Binder(template.getVariableName(), expression));
		
		this.accept(key, newProposition, TRUE);
	}
	
	public final void rewriteLeft(final String key, final int factIndex
			, final int equalityIndex, final Set<Integer> indices) {
		final Equality equality = (Equality) this.getFact(equalityIndex).getProposition();
		
		this.rewrite(key, factIndex, equality.getLeft(), equality.getRight(), indices);
	}
	
	private final void rewrite(final String key, final int factIndex
			, final Expression pattern, final Expression replacement, final Set<Integer> indices) {
		final Expression newExpression = (Expression) this.getFact(factIndex).getProposition()
				.accept(new Rewriter(pattern, replacement, indices));
		
		this.accept(key, newExpression, TRUE);
	}
	
	public final void apply(final String key, final int ruleIndex, final int conditionIndex) {
		final Rule rule = (Rule) this.getFact(ruleIndex).getProposition();
		
		if (!rule.getCondition().equals(this.getFact(conditionIndex).getProposition())) {
			throw new IllegalArgumentException();
		}
		
		this.accept(key, rule.getExpression(), TRUE);
	}
	
	public final int getDepth() {
		return this.getParent() == null ? 0 : 1 + this.getParent().getDepth();
	}
	
	public final void printTo(final PrintStream output) {
		output.println();
		
		final String indent = join("", Collections.nCopies(this.getDepth(), "\t").toArray());
		
		for (final Map.Entry<String, Integer> entry : this.factIndices.entrySet()) {
			final Fact fact = this.getFact(entry.getValue());
			
			output.println(indent + "(" + entry.getKey() + " : " + getJustification(fact.getProof()) + ")");
			output.println(indent + "\t" + fact.getProposition());
		}
		
		output.println();
		
		output.println(indent + "(goal : " + getJustification(this) + ")");
		output.println(indent + "\t" + this.getGoal());
	}
	
	public final void undo() {
		UndoableOperation operation = this.undoableOperations.remove(this.undoableOperations.size() - 1);
		final Fact lastFact = this.facts.remove(this.facts.size() - 1);
		this.factIndices.remove(lastFact.getKey());
		
		if (this.getGoal().equals(lastFact.getProposition())) {
			--this.goalReached;
		}
		
		if (operation == UndoableOperation.Simple.INTRODUCE_CONDITION) {
			this.goal = new Rule(lastFact.getProposition(), this.getGoal());
			operation = this.undoableOperations.remove(this.undoableOperations.size() - 1);
		} else if (operation instanceof UndoableOperation.IntroduceVariable) {
			this.goal = ((UndoableOperation.IntroduceVariable) operation).getOldGoal();
			operation = this.undoableOperations.remove(this.undoableOperations.size() - 1);
		}
		
		if (operation != UndoableOperation.Simple.ADD_FACT) {
			throw new IllegalStateException();
		}
		
		this.new FactRemovedEvent(this.getFactCount(), lastFact).fire();
	}
	
	private final Fact accept(final String key, final Expression proposition, final Symbol justification) {
		return this.addFact(key, proposition, null, justification);
	}
	
	private final Fact addFact(final String key, final Expression proposition
			, final Context proofParent, final Expression proofGoal) {
		final String actualKey = key != null ? key : this.newKey();
		final Fact result = new Fact(actualKey, proposition, new Context(proofParent, proofGoal));
		
		this.factIndices.put(actualKey, this.getFactCount());
		this.facts.add(result);
		
		if (this.getGoal().equals(result.getProposition())) {
			++this.goalReached;
		}
		
		this.undoableOperations.add(UndoableOperation.Simple.ADD_FACT);
		
		this.new FactAddedEvent(result).fire();
		
		return result;
	}
	
	private final String newKey() {
		return "#" + this.getFactCount();
	}
	
	/**
	 * @author codistmonk (creation 2014-06-11)
	 */
	public abstract class Event extends net.sourceforge.aprog.events.EventManager.AbstractEvent<Context> {
		
		protected Event() {
			super(Context.this);
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = 1040969981433873669L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-06-11)
	 */
	public final class SubcontextCreatedEvent extends Event {
		
		private final Context subcontext;
		
		public SubcontextCreatedEvent(final Context subcontext) {
			this.subcontext = subcontext;
		}
		
		public final Context getSubcontext() {
			return this.subcontext;
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = 1040969981433873669L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-06-11)
	 */
	public abstract class FactEvent extends Event {
		
		private final int factIndex;
		
		private final Fact fact;
		
		protected FactEvent(final int factIndex, final Fact fact) {
			this.factIndex = factIndex;
			this.fact = fact;
		}
		
		public final int getFactIndex() {
			return this.factIndex;
		}
		
		public final Fact getFact() {
			return this.fact;
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = 1040969981433873669L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-06-11)
	 */
	public final class FactAddedEvent extends FactEvent {
		
		public FactAddedEvent(final Fact fact) {
			super(Context.this.getFactIndex(fact.getKey()), fact);
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = -5833119728091250675L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-06-11)
	 */
	public final class FactRemovedEvent extends FactEvent {
		
		public FactRemovedEvent(final int oldIndex, final Fact fact) {
			super(oldIndex, fact);
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = 4515749350766209254L;
		
	}
	
	/**
	 * {@value}.
	 */
	private static final long serialVersionUID = 2082834263764356892L;
	
	public static final Symbol ASSUMED = new Symbol("Assumed");
	
	public static final Symbol TRUE = new Symbol("True");
	
	public static final Symbol FALSE = new Symbol("False");
	
	public static final String getJustification(final Context proof) {
		final String result;
		
		if (!proof.isGoalReached()) {
			result = "Unproven";
		} else if (Context.ASSUMED.equals(proof.getGoal())) {
			result = "Assumed";
		} else {
			result = "True";
		}
		
		return result;
	}
	
	/**
	 * @author codistmonk (creation 2014-06-10)
	 */
	private static abstract interface UndoableOperation extends Serializable {
		
		/**
		 * @author codistmonk (creation 2014-06-10)
		 */
		static enum Simple implements UndoableOperation {
			
			ADD_FACT, INTRODUCE_CONDITION;
			
		}
		
		/**
		 * @author codistmonk (creation 2014-06-10)
		 */
		static final class IntroduceVariable implements UndoableOperation {
			private final Template oldGoal;
			
			IntroduceVariable(final Template oldGoal) {
				this.oldGoal = oldGoal;
			}
			
			final Template getOldGoal() {
				return this.oldGoal;
			}
			
			/**
			 * {@value}.
			 */
			private static final long serialVersionUID = -6888538847397737959L;
			
		}
		
	}
	
	/**
	 * @author codistmonk (creation 2014-06-10)
	 */
	public static final class Fact implements Serializable {
		
		private final String key;
		
		private final Expression proposition;
		
		private final Context proof;
		
		public Fact(final String key, final Expression proposition, final Context proof) {
			this.key = key;
			this.proposition = proposition;
			this.proof = proof;
		}
		
		public final String getKey() {
			return this.key;
		}
		
		public final Expression getProposition() {
			return this.proposition;
		}
		
		public final Context getProof() {
			return this.proof;
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = 1869338402483804728L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-06-10)
	 */
	public static abstract class Visitor implements jrewrite2.Visitor {
		
		@Override
		public final Composite visitAfterChildren(final Composite composite,
				final Object[] childrenVisitationResults) {
			if (!newCompositeNeeded(composite, childrenVisitationResults)) {
				return composite;
			}
			
			return newComposite(childrenVisitationResults);
		}
		
		@Override
		public final Rule visitAfterChildren(final Rule rule,
				final Object[] childrenVisitationResults) {
			if (!newCompositeNeeded(rule.getComposite(), childrenVisitationResults)) {
				return rule;
			}
			
			return new Rule(newComposite(childrenVisitationResults));
		}
		
		@Override
		public final Equality visitAfterChildren(final Equality equality,
				final Object[] childrenVisitationResults) {
			if (!newCompositeNeeded(equality.getComposite(), childrenVisitationResults)) {
				return equality;
			}
			
			return new Equality(newComposite(childrenVisitationResults));
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = -2895672488681850761L;
		
		public static final boolean newCompositeNeeded(final Composite composite
				, final Object[] childrenVisitationResults) {
			final int n = composite.getChildCount();
			
			for (int i = 0; i < n; ++i) {
				if (composite.getChild(i) != childrenVisitationResults[i]) {
					return true;
				}
			}
			
			return false;
		}
		
		public static final Composite newComposite(final Object[] childrenVisitationResults) {
			final int n = childrenVisitationResults.length;
			final Expression[] expressions = new Expression[n];
			
			for (int i = 0; i < n; ++i) {
				expressions[i] = (Expression) childrenVisitationResults[i];
			}
			
			return new Composite(expressions);
		}
		
	}
	
	/**
	 * @author codistmonk (creation 2014-06-10)
	 */
	public static final class Binder extends Visitor {
		
		private final String variableName;
		
		private final Expression expression;
		
		public Binder(final String variableName, final Expression expression) {
			this.variableName = variableName;
			this.expression = expression;
		}
		
		@Override
		public final Expression visit(final Symbol symbol) {
			return this.variableName.equals(symbol.toString()) ? this.expression : symbol;
		}
		
		@Override
		public final Void visitBeforeChildren(final Composite composite) {
			return null;
		}
		
		@Override
		public final Void visitBeforeChildren(final Rule rule) {
			return null;
		}
		
		@Override
		public final Void visitBeforeChildren(final Equality equality) {
			return null;
		}
		
		@Override
		public final Template visitBeforeChildren(final Template template) {
			return this.variableName.equals(template.getVariableName()) ? template : null;
		}
		
		@Override
		public Expression visitAfterChildren(final Template template,
				final Object[] childrenVisitationResults) {
			if (childrenVisitationResults[0] == template.getProposition()) {
				return template;
			}
			
			return new Template(template.getVariableName(), (Expression) childrenVisitationResults[0]);
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = -4473669954982246122L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-06-10)
	 */
	public static final class Rewriter extends Visitor {
		
		private final Expression pattern;
		
		private final Expression replacement;
		
		private final Set<Integer> indices;
		
		private int index;
		
		public Rewriter(final Expression pattern, final Expression replacement,
				final Set<Integer> indices) {
			this.pattern = pattern;
			this.replacement = replacement;
			this.indices = indices;
		}
		
		@Override
		public final Expression visit(final Symbol symbol) {
			Expression result = this.visit((Expression) symbol);
			
			if (result == null) {
				result = symbol;
			}
			
			return result;
		}
		
		@Override
		public final Expression visitBeforeChildren(final Composite composite) {
			return this.visit(composite);
		}
		
		@Override
		public final Expression visitBeforeChildren(final Rule rule) {
			return this.visit(rule);
		}
		
		@Override
		public Expression visitBeforeChildren(final Equality equality) {
			return this.visit(equality);
		}
		
		@Override
		public final Expression visitBeforeChildren(final Template template) {
			return this.visit(template);
		}
		
		@Override
		public final Template visitAfterChildren(final Template template,
				final Object[] childrenVisitationResults) {
			final Expression newProposition = (Expression) childrenVisitationResults[0];
			
			return newProposition == template.getProposition() ? template
					: new Template(template.getVariableName(), newProposition);
		}
		
		private final Expression visit(final Expression object) {
			return this.pattern.equals(object) && this.indices.contains(this.index++) ? this.replacement : null;
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = 9150325302376037034L;
		
	}
	
}
