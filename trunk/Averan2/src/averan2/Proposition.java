package averan2;

/**
 * @author codistmonk (creation 2014-12-12)
 */
public abstract interface Proposition extends Expression {
	
	public abstract Symbol getName();
	
	public abstract <E extends Expression> E getExpression();
	
	/**
	 * @author codistmonk (creation 2014-12-12)
	 */
	public abstract class Default implements Proposition {
		
		private final Symbol name;
		
		private final Expression expression;
		
		protected Default(final Symbol name, final Expression expression) {
			this.name = name;
			this.expression = expression;
		}
		
		@Override
		public final Symbol getName() {
			return this.name;
		}
		
		@Override
		public final <E extends Expression> E getExpression() {
			return (E) this.expression;
		}
		
		@Override
		public final <T> T accept(final Visitor<T> visitor) {
			return visitor.visit(this);
		}
		
		private static final long serialVersionUID = 8044074110246201631L;
		
	}
	
}
