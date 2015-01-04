package averan3.core;

import static averan3.core.Composite.IMPLIES;
import static net.sourceforge.aprog.tools.Tools.cast;
import static org.junit.Assert.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import org.junit.Test;

/**
 * @author codistmonk (creation 2015-01-04)
 */
public final class ExpressionTest {
	
	@Test
	public final void test() {
		fail("Not yet implemented");
	}
	
	/**
	 * @author codistmonk (creation 2015-01-04)
	 */
	public static abstract interface Proof extends Serializable {
		
		public abstract Deduction getParent();
		
		public abstract void apply();
		
	}
	
	/**
	 * @author codistmonk (creation 2015-01-04)
	 */
	public static final class Deduction implements Proof {
		
		private final Deduction parent;
		
		private final String name;
		
		private final Composite<Expression<?>> module;
		
		private final List<Proof> proofs;
		
		private final Collection<Symbol<String>> protoparameters;
		
		private Expression<?> goal;
		
		private int depth;
		
		public Deduction(final Deduction parent, final String name, final Composite<Expression<?>> module, final Expression<?> goal) {
			this.parent = parent;
			this.name = name;
			this.module = module;
			this.proofs = new ArrayList<>();
			this.protoparameters = new LinkedHashSet<>();
			this.goal = goal;
			// primitive module operations: parametrize, suppose, apply, substitute
			// primitive deduction operations: introduce, deduce, conclude
			// standard tactics: recall, bind, rewrite, rewriteRight, autoDeduce
		}
		
		public final Expression<?> introduce(final String parameterOrPropositionName) {
			final Composite<?> goal = cast(Composite.class, this.getGoal());
			
			if (goal != null) {
				if (goal.getParameters() != null) {
					final Variable parameter = (Variable) goal.getParameters().get(1);
					final Symbol<String> result = new Symbol<String>(parameterOrPropositionName != null ? parameterOrPropositionName : parameter.getName());
					
					if (!this.protoparameters.add(result)) {
						throw new IllegalArgumentException();
					}
					
					this.goal = this.goal.accept(new Expression.Substitution().bind(new Variable(parameter.getName(), parameter), result));
					
					return result;
				}
				
				if (goal.getCondition() != null) {
					this.new Supposition(parameterOrPropositionName, goal.getCondition()).apply();
					this.goal = goal.getConclusion();
					
					return null;
				}
			}
			
			throw new IllegalStateException();
		}
		
		@Override
		public final Deduction getParent() {
			return this.parent;
		}
		
		@Override
		public final void apply() {
			// TODO Auto-generated method stub
		}
		
		public final String getName() {
			return this.name;
		}
		
		public final Composite<Expression<?>> getModule() {
			return this.module;
		}
		
		public final Expression<?> getGoal() {
			return this.goal;
		}
		
		@SuppressWarnings("unchecked")
		final void add(final Expression<?> proposition, final Proof proof) {
			final Composite<Expression<?>> module = this.getModule();
			
			switch (module.size()) {
			case 0:
				module.add(proposition);
				break;
			case 1:
				module.add(IMPLIES).add(proposition);
				break;
			case 3:
				if (IMPLIES.equals(module.get(1))) {
					Composite<Expression<?>> currentConclusion = module;
					
					for (int i = 2; i < this.depth; ++i) {
						currentConclusion = (Composite<Expression<?>>) currentConclusion.get(2);
						
						if (currentConclusion.size() != 3 || !IMPLIES.equals(currentConclusion.get(1))) {
							throw new IllegalStateException();
						}
					}
					
					currentConclusion.add(new Composite<>()
							.add(currentConclusion.removeLast()).add(IMPLIES).add(proposition));
					
					break;
				}
			default:
				throw new IllegalStateException();
			}
			
			++this.depth;
			
			this.proofs.add(proof);
		}
		
		/**
		 * @author codistmonk (creation 2015-01-04)
		 */
		public final class Supposition implements Proof {
			
			private final String propositionName;
			
			private final Expression<?> proposition;
			
			public Supposition(final String propositionName, final Expression<?> proposition) {
				this.propositionName = propositionName;
				this.proposition = proposition;
			}
			
			public final String getPropositionName() {
				return this.propositionName;
			}
			
			public final Expression<?> getProposition() {
				return this.proposition;
			}
			
			@Override
			public final Deduction getParent() {
				return Deduction.this;
			}
			
			@Override
			public final void apply() {
				this.getParent().add(this.getProposition(), this);
			}
			
			private static final long serialVersionUID = -2449310594857640213L;
			
		}
		
		private static final long serialVersionUID = -4622604986554143041L;
		
	}
	
}
