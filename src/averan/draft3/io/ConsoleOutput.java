package averan.draft3.io;

import static averan.draft3.core.Composite.FORALL;
import static java.lang.Math.max;
import static java.util.Collections.nCopies;
import static multij.tools.Tools.join;
import averan.common.Metadata;
import averan.draft3.core.Composite;
import averan.draft3.core.Expression;
import averan.draft3.core.Proof;
import averan.draft3.core.Symbol;
import averan.draft3.core.Variable;
import averan.draft3.core.Proof.Deduction;
import averan.draft3.core.Proof.Deduction.Inclusion;
import averan.draft3.core.Session.Output;

import java.io.PrintStream;

/**
 * @author codistmonk (creation 2015-01-06)
 */
public final class ConsoleOutput implements Output {
	
	private final PrintStream out;
	
	private int level;
	
	private String indent;
	
	public ConsoleOutput() {
		this(System.out);
	}
	
	public ConsoleOutput(final PrintStream out) {
		this.out = out;
		this.level = -1;
	}
	
	@Override
	public final void beginDeduction(final Deduction deduction) {
		this.indent = join("", nCopies(++this.level, '	'));
		
		this.out.println();
		this.println("Deduce (", deduction.getPropositionName(), ")");
		
		if (deduction.getRootParameters() != null) {
			this.println1(deduction.getRootParameters().accept(TO_STRING));
		}
	}
	
	@Override
	public final void processProof(final Proof proof) {
		if (proof instanceof Inclusion) {
			return;
		}
		
		this.println("(", proof.getPropositionName(), ")");
		this.println1(proof.getProposition().accept(TO_STRING));
		this.println1(proof);
	}
	
	@Override
	public final void endDeduction(Deduction deduction) {
		if (deduction.getGoal() != null) {
			this.println("Goal: ", deduction.getGoal().accept(TO_STRING));
		}
		
		this.println('.');
		
		this.indent = join("", nCopies(max(0, --this.level), '	'));
	}
	
	private final void println(final Object... objects) {
		this.out.println(this.indent + join("", objects));
	}
	
	private final void println1(final Object... objects) {
		this.out.println(this.indent + '	' + join("", objects));
	}
	
	private static final long serialVersionUID = -7906865954633254050L;
	
	public static final ConsoleOutput.ToString TO_STRING = new ToString();
	
	public static final <E extends Expression<?>> E group(final E expression) {
		Metadata.put(expression, "forcedGrouping", true);
		
		return expression;
	}
	
	/**
	 * @author codistmonk (creation 2015-01-05)
	 */
	public static final class ToString implements Expression.Visitor<String> {
		
		@Override
		public final String visit(final Symbol<?> symbol) {
			return symbol.toString();
		}
		
		@Override
		public final String visit(final Variable variable) {
			return Variable.getNumberedName(variable);
		}
		
		@Override
		public final String visit(final Composite<Expression<?>> composite) {
			if (composite.isList()) {
				if (FORALL.implies(composite.get(0))) {
					final StringBuilder resultBuilder = new StringBuilder().append(FORALL);
					final int n = composite.getListSize();
					
					for (int i = 1; i < n; ++i) {
						if (1 < i) {
							resultBuilder.append(',');
						}
						
						resultBuilder.append(composite.getListElement(i).accept(this));
					}
					
					resultBuilder.append(' ');
					
					return resultBuilder.toString();
				}
				
				final StringBuilder resultBuilder = new StringBuilder();
				final int n = composite.getListSize();
				
				resultBuilder.append('[');
				
				for (int i = 0; i < n; ++i) {
					resultBuilder.append(composite.getListElement(i).accept(this));
				}
				
				resultBuilder.append(']');
				
				return resultBuilder.toString();
			}
			
			return "(" + join("", composite.stream().map(e -> e.accept(this)).toArray()) + ")";
		}
		
		private static final long serialVersionUID = 315809084289227049L;
		
	}
	
}