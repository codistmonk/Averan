package averan2.io;

import static averan2.core.SessionTest.CollectParameters.collectParameters;
import static averan2.io.ConsoleOutput.AsString.asString;
import static java.util.Collections.nCopies;
import static net.sourceforge.aprog.tools.Tools.join;

import averan2.core.Composite;
import averan2.core.Equality;
import averan2.core.Expression;
import averan2.core.Expression.Visitor;
import averan2.core.Module;
import averan2.core.Session.Frame;
import averan2.core.Substitution;
import averan2.core.Symbol;
import averan2.core.Variable;
import averan2.io.SessionExporter.Output;

import java.io.PrintStream;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
	public final void beginConditions(final Composite<Expression<?>> conditions) {
		if (0 < conditions.size()) {
			this.out.println(this.indent + "((CONDITIONS))");
		}
	}
	
	@Override
	public final void processCondition(final String name, final Expression<?> condition) {
		this.out.println(this.indent + "	(" + name + ")");
		this.out.println(this.indent + "	" + condition.accept(asString()));
	}

	@Override
	public final void beginFacts(final Composite<Expression<?>> facts) {
		if (0 < facts.size()) {
			this.out.println(this.indent + "((FACTS))");
		}
	}
	
	@Override
	public final void beginFact(final String name, final Expression<?> fact) {
		this.out.println(this.indent + "	(" + name + ")");
		this.out.println(this.indent + "	" + fact.accept(asString()));
	}
	
	@Override
	public final void processGoal(final Expression<?> goal) {
		if (goal != null) {
			this.out.println(this.indent + "((GOAL))");
			this.out.println(this.indent + "	" + goal.accept(asString()));
		} else {
			this.out.println(this.indent + "(())");
		}
	}
	
	@Override
	public final void endFrame() {
		--this.frameLevel;
	}
	
	private static final long serialVersionUID = 3659783931873586881L;
	
	/**
	 * @author codistmonk (creation 2014-12-28)
	 */
	public static final class AsString implements Visitor<String> {
		
		private final Map<Variable, Variable> done = new IdentityHashMap<>();
		
		public final AsString reset() {
			this.done.clear();
			
			return this;
		}
		
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
			return composite.toString();
		}
		
		@Override
		public final String visit(final Module module) {
			final StringBuilder resultBuilder = new StringBuilder();
			
			if (0 < module.getConditions().size()) {
				resultBuilder
					.append(join(" → ", module.getConditions().stream().map(this::visitElementsOf).toArray()))
					.append(" → ");
			}
			
			if (module.getFacts().size() != 1) {
				resultBuilder.append('(');
			}
			
			resultBuilder
				.append(join(" ∧ ", module.getFacts().stream().map(this::visitElementsOf).toArray()));
			
			if (module.getFacts().size() != 1) {
				resultBuilder.append(')');
			}
			
			return resultBuilder.toString();
		}
		
		@Override
		public final String visit(final Substitution substitution) {
			final StringBuilder resultBuilder = new StringBuilder();
			
			resultBuilder.append('{').append(join(",",
					substitution.getBindings().stream().map(this::visitElementsOf).toArray())).append('}');
			resultBuilder.append('[').append(join(",",
					substitution.getIndices().stream().map(this::visitElementsOf).toArray())).append(']');
			
			return this.visitElementsOf(substitution);
		}
		
		@Override
		public final String visit(final Equality equality) {
			return this.visitElementsOf(equality.getLeft()) + " = " + this.visitElementsOf(equality.getRight());
		}
		
		private final String visitElementsOf(final Expression<?> expression) {
			final StringBuilder resultBuilder = new StringBuilder();
			final List<Variable> parameters = expression.accept(collectParameters());
			
			for (final Iterator<Variable> i = parameters.iterator(); i.hasNext();) {
				final Variable parameter = i.next();
				
				if (this.done.containsKey(parameter)) {
					i.remove();
				} else {
					this.done.put(parameter, parameter);
				}
			}
			
			if (!parameters.isEmpty()) {
				resultBuilder.append('∀').append(
						join(",", parameters.stream().map(Variable::getName).toArray())).append(' ');
			}
			
			for (final Expression<?> element : expression) {
				resultBuilder.append(element.accept(this));
			}
			
			return resultBuilder.toString();
		}
		
		private static final long serialVersionUID = 3130405603855469068L;
		
		public static final AsString asString() {
			return new AsString();
		}
		
	}
	
}
