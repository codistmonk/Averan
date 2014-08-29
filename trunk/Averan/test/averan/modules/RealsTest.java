package averan.modules;

import static averan.core.SessionTools.*;
import static averan.io.ExpressionParser.$$;
import static averan.modules.Reals.*;
import static net.sourceforge.aprog.tools.Tools.append;
import static org.junit.Assert.assertEquals;

import averan.core.Module;
import averan.modules.Reals;
import averan.modules.Reals.RewriteHint;

import org.junit.Test;

/**
 * @author codistmonk (creation 2014-08-28)
 */
public final class RealsTest {
	
	@Test
	public final void test() {
		pushNewSession(new Module(Reals.MODULE));
		
		try {
			final RewriteHint[] additionHints = hints.get("addition");
			final RewriteHint[] multiplicationHints = hints.get("multiplication");
			final RewriteHint[] additionAndMultiplicationHints = append(additionHints, multiplicationHints);
			final RewriteHint[] subtractionHints = hints.get("subtraction");
			final RewriteHint[] arithmeticHints = hints.get("arithmetic");
			
			claim("Demo4.test1", $$("∀x,y (x → ((x→y) → y))"));
			{
				introduce();
				introduce();
				introduce();
				introduce();
				proveWithBindAndApply(goal());
			}
			
			claim("Demo4.test2", $$("∀x,y,z ((x∈ℝ) → ((y∈ℝ) → ((z∈ℝ) → ((x+z+y)=(z+y+x)))))"));
			{
				introduce();
				introduce();
				introduce();
				introduce();
				introduce();
				introduce();
				
				proveEquality(goal(), additionHints);
			}
			
			claim("Demo4.test3", $$("∀x,y,z ((x∈ℝ) → ((y∈ℝ) → ((z∈ℝ) → ((xzy)=(zyx)))))"));
			{
				introduce();
				introduce();
				introduce();
				introduce();
				introduce();
				introduce();
				
				proveEquality(goal(), multiplicationHints);
			}
			
			claim("Demo4.test4", $$("∀a,b,c,d ((a∈ℝ) → ((b∈ℝ) → ((c∈ℝ) → ((d∈ℝ) → ((dc+ba)=(ab+cd))))))"));
			{
				introduce();
				introduce();
				introduce();
				introduce();
				introduce();
				introduce();
				introduce();
				introduce();
				
				proveEquality(goal(), additionAndMultiplicationHints);
			}
			
			claim("Demo4.test5", $$("∀a,b,c,d ((a∈ℝ) → ((b∈ℝ) → ((c∈ℝ) → ((d∈ℝ) → ((dc+(a-ba))=(cd-ab+a))))))"));
			{
				introduce();
				introduce();
				introduce();
				introduce();
				introduce();
				introduce();
				introduce();
				introduce();
				
				proveEquality(goal(), append(additionAndMultiplicationHints, subtractionHints));
			}
			
			claim("Demo4.test6", $$("∀a,b,c,d ((a∈ℝ) → ((b∈ℝ) → ((c∈ℝ) → ((d∈ℝ) → ((c+(a-ba)d)=(c-adb+da))))))"));
			{
				introduce();
				introduce();
				introduce();
				introduce();
				introduce();
				introduce();
				introduce();
				introduce();
				
				proveEquality(goal(), arithmeticHints);
			}
			
			assertEquals(1L, session().getStack().size());
		} finally {
			popSession();
		}
	}
	
}
