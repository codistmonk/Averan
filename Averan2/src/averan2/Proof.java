package averan2;

/**
 * @author codistmonk (creation 2014-12-12)
 */
public abstract class Proof implements Expression {
	
	private final Module context;
	
	private Expression conclusion;
	
	protected Proof(final Module context) {
		this.context = context;
	}
	
	public final Module getContext() {
		return this.context;
	}
	
	public final <E extends Expression> E getConclusion() {
		if (this.conclusion == null) {
			this.conclusion = this.computeConclusion();
		}
		
		return (E) this.conclusion;
	}
	
	protected abstract Expression computeConclusion();
	
	private static final long serialVersionUID = -8318551923110061376L;
	
	public static final int TYPE = 0;
	
	/**
	 * @author codistmonk (creation 2014-12-12)
	 */
	public static final class Admit extends Proof {
		
		private final Expression conclusion;
		
		public Admit(final Module context, final Expression conclusion) {
			super(context);
			this.conclusion = conclusion;
		}
		
		@Override
		public final int getElementCount() {
			return ELEMENT_COUNT;
		}
		
		@Override
		public final <E extends Expression> E getElement(final int index) {
			switch (index) {
			case TYPE:
				return (E) ADMIT;
			case CONCLUSION:
				return this.getConclusion();
			}
			
			return null;
		}
		
		@Override
		public final <T> T accept(final Visitor<T> visitor) {
			return visitor.visit(this);
		}
		
		@Override
		protected final Expression computeConclusion() {
			return this.conclusion;
		}
		
		private static final long serialVersionUID = 2744181863226646658L;
		
		public static final Symbol ADMIT = new Symbol(Module.ROOT, " Admit: ");
		
		public static final int CONCLUSION = 1;
		
		public static final int ELEMENT_COUNT = 2;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-12-12)
	 */
	public static final class Bind extends Proof {
		
		private final Symbol moduleName;
		
		private final Symbol parameter;
		
		private final Expression expression;
		
		public Bind(final Module context, final Symbol moduleName,
				final Symbol parameter, final Expression expression) {
			super(context);
			this.moduleName = moduleName;
			this.parameter = parameter;
			this.expression = expression;
		}
		
		public final Symbol getModuleName() {
			return this.moduleName;
		}
		
		public final Symbol getParameter() {
			return this.parameter;
		}
		
		public final Expression getExpression() {
			return this.expression;
		}
		
		@Override
		public final int getElementCount() {
			return ELEMENT_COUNT;
		}
		
		@Override
		public final <E extends Expression> E getElement(final int index) {
			switch (index) {
			case TYPE:
				return (E) BIND;
			case MODULE_NAME:
				return (E) this.getModuleName();
			case HELPER_1:
				return (E) DOT;
			case PARAMETER:
				return (E) this.getParameter();
			case HELPER_2:
				return (E) USING;
			case EXPRESSION:
				return (E) this.getExpression();
			}
			
			return null;
		}
		
		@Override
		public final <T> T accept(final Visitor<T> visitor) {
			return visitor.visit(this);
		}
		
		@Override
		protected final Expression computeConclusion() {
			final Proposition proposition = this.getContext().findProposition(this.getModuleName());
			final Module module = proposition.getExpression();
			final Symbol parameter = module.findParameter(this.getParameter());
			final Symbol newName = new Symbol(this.getContext(), module.getName().toString() + ".bound");
			final Composite<Symbol> newParameters = new Composite<>();
			
			for (final Symbol p : module.getParameters().getElements()) {
				if (p != parameter) {
					newParameters.getElements().add(p);
				}
			}
			
			final Rewriter rewriter = new Rewriter(parameter, this.getExpression());
			final Composite<Condition> newConditions = (Composite<Condition>) module.getConditions().accept(rewriter);
			final Composite<Fact> newFacts = (Composite<Fact>) module.getFacts().accept(rewriter);
			
			return new Module(this.getContext(), newName, newParameters, newConditions, newFacts);
		}
		
		private static final long serialVersionUID = 4047331031987916386L;
		
		public static final Symbol BIND = new Symbol(Module.ROOT, " Bind: ");
		
		public static final Symbol DOT = new Symbol(Module.ROOT, ".");
		
		public static final Symbol USING = new Symbol(Module.ROOT, " Using: ");
		
		public static final int MODULE_NAME = 1;
		
		public static final int HELPER_1 = 2;
		
		public static final int PARAMETER = 3;
		
		public static final int HELPER_2 = 4;
		
		public static final int EXPRESSION = 5;
		
		public static final int ELEMENT_COUNT = 6;
		
	}
	
}
