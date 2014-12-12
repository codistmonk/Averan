package averan2;

/**
 * @author codistmonk (creation 2014-12-12)
 */
public final class Rewriter implements Visitor<Expression> {
	
	private final Expression pattern;
	
	private final Expression replacement;
	
	public Rewriter(final Expression pattern, final Expression replacement) {
		this.pattern = pattern;
		this.replacement = replacement;
	}
	
	public final Expression getPattern() {
		return this.pattern;
	}
	
	public final Expression getReplacement() {
		return this.replacement;
	}
	
	@Override
	public final Expression visit(final Symbol symbol) {
		if (this.getPattern().equals(symbol)) {
			return this.getReplacement();
		}
		
		return symbol;
	}
	
	@Override
	public final Expression visit(final Module module) {
		if (this.getPattern().equals(module)) {
			return this.getReplacement();
		}
		
		
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public final Expression visit(final Composite composite) {
		if (this.getPattern().equals(composite)) {
			return this.getReplacement();
		}
		
		final Composite newComposite = new Composite();
		boolean returnNewComposite = false;
		
		for (final Expression expression : composite) {
			final Expression newExpression = expression.accept(this);
			
			newComposite.getExpressions().add(newExpression);
			
			returnNewComposite |= expression != newExpression;
		}
		
		return returnNewComposite ? newComposite : composite;
	}
	
	@Override
	public final Expression visit(final Condition condition) {
		if (this.getPattern().equals(condition)) {
			return this.getReplacement();
		}
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public final Expression visit(final Fact fact) {
		if (this.getPattern().equals(fact)) {
			return this.getReplacement();
		}
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public final Expression visit(final Proof.Admit admit) {
		if (this.getPattern().equals(admit)) {
			return this.getReplacement();
		}
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public final Expression visit(final Proof.Bind bind) {
		if (this.getPattern().equals(bind)) {
			return this.getReplacement();
		}
		// TODO Auto-generated method stub
		return null;
	}
	
	private static final long serialVersionUID = -4150340599391663242L;
	
}
