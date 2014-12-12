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
			return 0;
		}
		
		@Override
		public final <E extends Expression> E getElement(final int index) {
			switch (index) {
			case 0:
				return (E) ADMIT;
			case 1:
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
			return 6;
		}
		
		@Override
		public final <E extends Expression> E getElement(final int index) {
			switch (index) {
			case 0:
				return (E) BIND;
			case 1:
				return (E) this.getModuleName();
			case 2:
				return (E) DOT;
			case 3:
				return (E) this.getParameter();
			case 4:
				return (E) USING;
			case 5:
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
			// TODO Auto-generated method stub
			return null;
		}
		
		private static final long serialVersionUID = 4047331031987916386L;
		
		public static final Symbol BIND = new Symbol(Module.ROOT, " Bind: ");
		
		public static final Symbol DOT = new Symbol(Module.ROOT, ".");
		
		public static final Symbol USING = new Symbol(Module.ROOT, " Using: ");
		
	}
	
}
