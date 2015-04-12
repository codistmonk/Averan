package averan4.core;

import static averan4.core.AveranTools.*;
import static averan4.deductions.Standard.*;
import static averan4.io.Simple.print;
import static java.util.Collections.emptyList;
import static net.sourceforge.aprog.tools.Tools.*;

import net.sourceforge.aprog.tools.IllegalInstantiationException;

/**
 * @author codistmonk (creation 2015-04-11)
 */
public final class Demo {
	
	private Demo() {
		throw new IllegalInstantiationException();
	}
	
	/**
	 * @param commandLineArguments
	 * <br>Unused
	 */
	public static final void main(final String[] commandLineArguments) {
		{
			debugPrint();
			
			final Deduction deduction = new Deduction(null);
			
			deduction.conclude(new Substitution("p", $("x"), map($("x"), $("y")), emptyList()));
			
			print(deduction, 1);
		}
		
		{
			debugPrint();
			
			push();
			
			supposeRewriteLeft();
			deduceIdentity();
			
			print(pop(), 1);
		}
		
		{
			debugPrint();
			
			push();
			
			supposeRewriteLeft();
			deduceIdentity();
			
			{
				final Goal goal = Goal.deduce($equality("a", "a"));
				
				bind("identity", $("a"));
				
				goal.conclude();
			}
			
			print(pop(), 2);
		}
	}
	
}
