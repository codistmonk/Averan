package averan3.core;

import static averan3.core.Composite.FORALL;
import static java.lang.Math.max;
import static java.util.Collections.nCopies;
import static net.sourceforge.aprog.tools.Tools.join;

import averan3.core.Proof.Deduction;
import averan3.core.Session.Output;

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
		this.indent = join("", nCopies(++this.level, ' '));
		
		this.println("Deduce (", deduction.getPropositionName(), ")");
		
		if (deduction.getRootParameters() != null) {
			this.println1(deduction.getRootParameters().accept(TO_STRING));
		}
	}
	
	@Override
	public final void processProof(final Proof proof) {
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
		
		this.indent = join("", nCopies(max(0, --this.level), ' '));
	}
	
	private final void println(final Object... objects) {
		this.out.println(this.indent + join("", objects));
	}
	
	private final void println1(final Object... objects) {
		this.out.println(this.indent + '	' + join("", objects));
	}
	
	private static final long serialVersionUID = -7906865954633254050L;
	
	public static final ConsoleOutput.ToString TO_STRING = new ToString();
	
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
			return variable.getName();
		}
		
		@Override
		public final String visit(final Composite<Expression<?>> composite) {
			if (1 < composite.size() && FORALL.implies(composite.get(0))) {
				final StringBuilder resultBuilder = new StringBuilder().append(FORALL);
				final int n = composite.size();
				
				for (int i = 1; i < n; ++i) {
					if (1 < i) {
						resultBuilder.append(',');
					}
					
					resultBuilder.append(composite.get(i).accept(this));
				}
				
				resultBuilder.append(' ');
				
				return resultBuilder.toString();
			}
			
			return "(" + join("", composite.stream().map(e -> e.accept(this)).toArray()) + ")";
		}
		
		private static final long serialVersionUID = 315809084289227049L;
		
	}
	
}