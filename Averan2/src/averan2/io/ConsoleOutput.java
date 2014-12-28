package averan2.io;

import static java.util.Collections.nCopies;
import static net.sourceforge.aprog.tools.Tools.join;

import averan2.core.Composite;
import averan2.core.Equality;
import averan2.core.Expression;
import averan2.core.Expression.Visitor;
import averan2.core.Module;
import averan2.core.Session;
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
		this.out.println(this.indent + "	" + condition.accept(TO_STRING.reset()));
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
		this.out.println(this.indent + "	" + fact.accept(TO_STRING.reset()));
	}
	
	@Override
	public final void processGoal(final Expression<?> goal) {
		if (goal != null) {
			this.out.println(this.indent + "((GOAL))");
			this.out.println(this.indent + "	" + goal.accept(TO_STRING.reset()));
		} else {
			this.out.println(this.indent + "(())");
		}
	}
	
	@Override
	public final void endFrame() {
		--this.frameLevel;
	}
	
	private static final long serialVersionUID = 3659783931873586881L;
	
	public static final ToString TO_STRING = ToString.INSTANCE;
	
	/**
	 * @author codistmonk (creation 2014-12-28)
	 */
	public static final class ToString implements Visitor<String> {
		
		private final Map<Variable, Variable> done = new IdentityHashMap<>();
		
		public final ToString reset() {
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
			
			for (final Expression<?> condition : module.getConditions()) {
				this.appendTo(resultBuilder, condition).append(" → ");
			}
			
			int factIndex = 0;
			
			for (final Expression<?> fact : module.getFacts()) {
				if (1 < ++factIndex) {
					resultBuilder.append(" ∧ ");
				}
				
				this.appendTo(resultBuilder, fact);
			}
			
			return resultBuilder.toString();
		}
		
		@Override
		public final String visit(final Substitution substitution) {
			return substitution.toString();
		}
		
		@Override
		public final String visit(final Equality equality) {
			return equality.toString();
		}
		
		private final StringBuilder appendTo(final StringBuilder resultBuilder, final Expression<?> proposition) {
			final String factString = proposition.accept(this);
			final List<Variable> variables = Session.getVariables(proposition);
			
			for (final Iterator<Variable> i = variables.iterator(); i.hasNext();) {
				final Variable variable = i.next();
				
				if (this.done.containsKey(variable)) {
					i.remove();
				} else {
					this.done.put(variable, variable);
				}
			}
			
			if (!variables.isEmpty()) {
				resultBuilder.append('∀').append(join(",", variables.stream().map(Variable::getName).toArray())).append(' ');
			}
			
			return resultBuilder.append(factString);
		}
		
		private static final long serialVersionUID = 3130405603855469068L;
		
		public static final ToString INSTANCE = new ToString();
		
	}
	
}
