package averan2.io;

import static averan2.core.Expression.CollectParameters.collectParameters;
import static java.util.Collections.nCopies;
import static net.sourceforge.aprog.tools.Tools.join;
import averan2.core.Composite;
import averan2.core.Equality;
import averan2.core.Expression;
import averan2.core.Expression.GatherParameters;
import averan2.core.Expression.GatherParameters.Key;
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

import net.sourceforge.aprog.tools.Tools;

/**
 * @author codistmonk (creation 2014-12-21)
 */
public final class ConsoleOutput implements Output {
	
	private final PrintStream out;
	
	private Frame frame;
	
	private int frameLevel;
	
	private String indent;
	
	private GatherParameters parameters;
	
	public ConsoleOutput() {
		this(System.out);
	}
	
	public ConsoleOutput(final PrintStream out) {
		this.out = out;
		this.frameLevel = -1;
		this.indent = "";
	}
	
	@Override
	public final void beginSession(final Session session) {
		this.parameters = session.getParameters();
	}
	
	@Override
	public final void beginFrame(final Frame frame) {
		this.frame = frame;
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
		this.out.println(this.indent + "	" + this.asString(condition));
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
		this.out.println(this.indent + "	" + this.asString(fact));
	}
	
	@Override
	public final void processGoal(final Expression<?> goal) {
		if (goal != null) {
			this.out.println(this.indent + "((GOAL))");
			final GatherParameters goalParameters = new GatherParameters();
			goalParameters.getModuleContexts().putAll(this.parameters.getModuleContexts());
			goalParameters.getVariableContexts().putAll(this.parameters.getVariableContexts());
			if (goal instanceof Module) {
				goalParameters.getModuleContexts().put((Module) goal, this.frame.getModule());
			}
			goal.accept(goalParameters);
			
			this.out.println(this.indent + "	" + goal.accept(new AsString(goalParameters)));
		} else {
			this.out.println(this.indent + "(())");
		}
	}
	
	@Override
	public final void endFrame() {
		--this.frameLevel;
	}
	
	public final String asString(final Expression<?> expression) {
		return expression.accept(Variable.RESET).accept(new AsString(this.parameters));
	}
	
	private static final long serialVersionUID = 3659783931873586881L;
	
	/**
	 * @author codistmonk (creation 2014-12-28)
	 */
	public static final class AsString implements Visitor<String> {
		
		private final GatherParameters parameters;
		
		private final Map<Variable, Variable> done = new IdentityHashMap<>();
		
		private int level;
		
		public AsString(final GatherParameters parameters) {
			this.parameters = parameters;
		}
		
		@Override
		public final String visit(final Symbol<?> symbol) {
			try {
				if (++this.level == 1) {
//					return this.visitProposition(symbol);
				}
				
				return symbol.toString();
			} finally {
				--this.level;
			}
		}
		
		@Override
		public final String visit(final Variable variable) {
			try {
				if (++this.level == 1) {
//					return this.visitProposition(variable);
				}
				
				return variable.getName();
			} finally {
				--this.level;
			}
		}
		
		@Override
		public final String visit(final Composite<Expression<?>> composite) {
			try {
				if (++this.level == 1) {
//					return this.visitProposition(composite);
				}
				
				final StringBuilder resultBuilder = new StringBuilder();
				
				for (final Expression<?> element : composite) {
					resultBuilder.append(element.accept(this));
				}
				
				return resultBuilder.toString();
			} finally {
				--this.level;
			}
		}
		
		@Override
		public final String visit(final Module module) {
			final StringBuilder resultBuilder = new StringBuilder();
			
			{
				Tools.debugPrint(this.parameters.getVariableContexts().size(), module);
				boolean first = true;
				
				for (final Map.Entry<Key<Variable>, Module> entry : this.parameters.getVariableContexts().entrySet()) {
					Tools.debugPrint(entry);
					if (module == entry.getValue()) {
						Tools.debugPrint(entry.getKey());
						resultBuilder.append(first ? '∀' : ',').append(entry.getKey().getObject().getName());
						first = false;
					}
				}
				
				if (!first) {
					resultBuilder.append(' ');
				}
			}
			
//			if (0 < module.getConditions().size()) {
//				resultBuilder.
////					append(join(" → ", module.getConditions().stream().map(this::visitProposition).toArray())).
//					append(join(" → ", module.getConditions().stream().map(e -> e.accept(this)).toArray())).
//					append(" → ");
//			}
			
			if (module.getFacts().size() != 1) {
				resultBuilder.append('(');
			}
			
//			resultBuilder.append(this.visitFacts(module.getFacts()));
			resultBuilder.
				append(join(" → ", module.getFacts().stream().map(e -> e.accept(this)).toArray()));
			
			if (module.getFacts().size() != 1) {
				resultBuilder.append(')');
			}
			
			return resultBuilder.toString();
		}
		
		@Override
		public final String visit(final Substitution substitution) {
			try {
				if (++this.level == 1) {
//					return this.visitProposition(substitution);
				}
				
				final StringBuilder resultBuilder = new StringBuilder();
				
				resultBuilder.append('{').append(join(",",
						substitution.getBindings().stream().map(e -> e.accept(this)).toArray())).append('}');
				resultBuilder.append('[').append(join(",",
						substitution.getIndices().stream().map(e -> e.accept(this)).toArray())).append(']');
				
				return resultBuilder.toString();
			} finally {
				--this.level;
			}
		}
		
		@Override
		public final String visit(final Equality equality) {
			try {
				if (++this.level == 1) {
//					return this.visitProposition(equality);
				}
				
				return equality.getLeft().accept(this) + " = " + equality.getRight().accept(this);
			} finally {
				--this.level;
			}
		}
		
		private final String visitProposition(final Expression<?> proposition) {
			final StringBuilder resultBuilder = new StringBuilder();
			final List<Variable> parameters = proposition.accept(collectParameters());
			
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
			
			resultBuilder.append(proposition.accept(this));
			
			return resultBuilder.toString();
		}
		
		private final String visitFacts(final Composite<Expression<?>> facts) {
			final StringBuilder resultBuilder = new StringBuilder();
			final List<Variable> parameters = facts.accept(collectParameters());
			
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
			
			resultBuilder.append(join(" ∧ ", facts.stream().map(e -> e.accept(this)).toArray()));
			
			return resultBuilder.toString();
		}
		
		private static final long serialVersionUID = 3130405603855469068L;
		
	}
	
}
