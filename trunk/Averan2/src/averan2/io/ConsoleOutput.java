package averan2.io;

import static java.util.Collections.nCopies;
import static net.sourceforge.aprog.tools.Tools.join;

import averan2.core.Composite;
import averan2.core.Equality;
import averan2.core.Expression;
import averan2.core.Expression.Visitor;
import averan2.core.Module;
import averan2.core.Module.Proof;
import averan2.core.Session.Frame;
import averan2.core.Substitution;
import averan2.core.Symbol;
import averan2.core.Variable;
import averan2.io.SessionExporter.Output;

import java.io.PrintStream;
import java.util.List;

/**
 * @author codistmonk (creation 2014-12-21)
 */
public final class ConsoleOutput implements Output {
	
	private final PrintStream out;
	
	private int frameLevel;
	
	private String indent;
	
	public ConsoleOutput() {
		this(System.out);
	}
	
	public ConsoleOutput(final PrintStream out) {
		this.out = out;
		this.frameLevel = -1;
		this.indent = "";
	}
	
	@Override
	public final void beginFrame(final Frame frame) {
		++this.frameLevel;
		this.indent = join("", nCopies(this.frameLevel, '	').toArray());
		this.out.println(this.indent + "((MODULE " + frame.getName() + "))");
		
		if (!frame.getIntroducedBindings().isEmpty()) {
			this.out.println(this.indent + "	∀" + join(",", frame.getIntroducedBindings().stream().map(Equality::getLeft).toArray()));
		}
	}
	
	@Override
	public final void beginConditions(final List<String> conditionNames) {
		if (0 < conditionNames.size()) {
			this.out.println(this.indent + "((CONDITIONS))");
		}
	}
	
	@Override
	public final void processCondition(final String name, final Expression<?> condition) {
		this.out.println(this.indent + "	(" + name + ")");
		this.out.println(this.indent + "	" + this.asString(condition));
	}

	@Override
	public final void beginFacts(final List<String> factNames) {
		if (0 < factNames.size()) {
			this.out.println(this.indent + "((FACTS))");
		}
	}
	
	@Override
	public final void beginFact(final String name, final Expression<?> fact) {
		this.out.println(this.indent + "	(" + name + ")");
		this.out.println(this.indent + "	" + this.asString(fact));
	}
	
	@Override
	public final void beginProof(final Proof factProof) {
		this.out.println(this.indent + "		(" + factProof + ")");
	}
	
	@Override
	public final void processGoal(final Expression<?> goal) {
		if (goal != null) {
			this.out.println(this.indent + "((GOAL))");
			this.out.println(this.indent + "	" + goal.accept(new AsString()));
		} else {
			this.out.println(this.indent + "(())");
		}
	}
	
	@Override
	public final void endFrame() {
		--this.frameLevel;
	}
	
	public final String asString(final Expression<?> expression) {
		return expression.accept(Variable.RESET).accept(new AsString());
	}
	
	private static final long serialVersionUID = 3659783931873586881L;
	
	/**
	 * @author codistmonk (creation 2014-12-28)
	 */
	public static final class AsString implements Visitor<String> {
		
		@Override
		public final String visit(final Symbol<?> symbol) {
			return symbol.toString();
		}
		
		@Override
		public final String visit(final Variable variable) {
			return variable.getName();
		}
		
		@Override
		public final String visit(final Composite<Expression<?>> composite) {
			final StringBuilder resultBuilder = new StringBuilder();
			
			for (final Expression<?> element : composite) {
				resultBuilder.append(element.accept(this));
			}
			
			return resultBuilder.toString();
		}
		
		@Override
		public final String visit(final Module module) {
			final StringBuilder resultBuilder = new StringBuilder();
			
			{
				boolean first = true;
				
				for (final Variable parameter : module.getParameters()) {
					resultBuilder.append(first ? '∀' : ',').append(parameter.getName());
					first = false;
				}
				
				if (!first) {
					resultBuilder.append(' ');
				}
			}
			
			if (!module.getParameters().isEmpty()) {
				resultBuilder.append('(');
			}
			
			{
				boolean first = true;
				
				for (final Expression<?> proposition : module.getPropositions()) {
					if (!first) {
						resultBuilder.append(" → ");
					} else {
						first = false;
					}
					
					if (1 < module.getPropositions().size() && (proposition instanceof Module
							|| proposition instanceof Composite<?> || proposition instanceof Equality)) {
						resultBuilder.append('(');
					}
					
					resultBuilder.append(proposition.accept(this));
					
					if (1 < module.getPropositions().size() && (proposition instanceof Module ||
							proposition instanceof Composite<?> || proposition instanceof Equality)) {
						resultBuilder.append(')');
					}
				}
			}
			
			if (!module.getParameters().isEmpty()) {
				resultBuilder.append(')');
			}
			
			return resultBuilder.toString();
		}
		
		@Override
		public final String visit(final Substitution substitution) {
			final StringBuilder resultBuilder = new StringBuilder();
			
			resultBuilder.append('{').append(join(",",
					substitution.getBindings().stream().map(e -> e.accept(this)).toArray())).append('}');
			resultBuilder.append('[').append(join(",",
					substitution.getIndices().stream().map(e -> e.accept(this)).toArray())).append(']');
			
			return resultBuilder.toString();
		}
		
		@Override
		public final String visit(final Equality equality) {
			return equality.getLeft().accept(this) + " = " + equality.getRight().accept(this);
		}
		
		private static final long serialVersionUID = 3130405603855469068L;
		
	}
	
}
