package averan2;

import averan2.Proof.Admit;

import java.util.ArrayList;
import java.util.List;

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
		return this.tryReplace(symbol);
	}
	
	@Override
	public final Expression visit(final Module module) {
		Expression result = this.tryReplace(module);
		
		if (result == module) {
			// TODO
		}
		
		return result;
	}
	
	@Override
	public final Expression visit(final Composite composite) {
		Expression result = this.tryReplace(composite);
		
		if (result == null) {
			final Composite newComposite = new Composite();
			
			if (this.visitElements(composite, newComposite.getExpressions())) {
				result = newComposite;
			}
		}
		
		return result;
	}
	
	@Override
	public final Expression visit(final Condition condition) {
		Expression result = this.tryReplace(condition);
		
		if (result == condition) {
			final List<Expression> newElements = new ArrayList<>();
			
			if (this.visitElements(condition, newElements)) {
				result = new Condition((Symbol) newElements.get(Condition.NAME), newElements.get(Condition.EXPRESSION));
			}
		}
		
		return result;
	}
	
	@Override
	public final Expression visit(final Fact fact) {
		Expression result = this.tryReplace(fact);
		
		if (result == fact) {
			final List<Expression> newElements = new ArrayList<>();
			
			if (this.visitElements(fact, newElements)) {
				result = new Fact((Symbol) newElements.get(Fact.NAME), (Proof) newElements.get(Fact.PROOF));
			}
		}
		
		return result;
	}
	
	@Override
	public final Expression visit(final Proof.Admit admit) {
		Expression result = this.tryReplace(admit);
		
		if (result == admit) {
			final List<Expression> newElements = new ArrayList<>();
			
			if (this.visitElements(admit, newElements)) {
				result = new Proof.Admit(admit.getContext(), newElements.get(Admit.CONCLUSION));
			}
		}
		
		return result;
	}
	
	@Override
	public final Expression visit(final Proof.Bind bind) {
		Expression result = this.tryReplace(bind);
		
		if (result == bind) {
			final List<Expression> newElements = new ArrayList<>();
			
			if (this.visitElements(bind, newElements)) {
				result = new Proof.Bind(bind.getContext(),
						(Symbol) newElements.get(Proof.Bind.MODULE_NAME),
						(Symbol) newElements.get(Proof.Bind.PARAMETER),
						newElements.get(Proof.Bind.EXPRESSION));
			}
		}
		
		return result;
	}
	
	private final boolean visitElements(final Expression expression, final List<Expression> newElements) {
		boolean result = false;
		
		for (final Expression element : expression) {
			final Expression newElement = element.accept(this);
			
			newElements.add(newElement);
			
			result |= element != newElement;
		}
		
		return result;
	}
	
	private final Expression tryReplace(final Expression expression) {
		return this.getPattern().equals(expression) ? this.getReplacement() : expression;
	}
	
	private static final long serialVersionUID = -4150340599391663242L;
	
}
