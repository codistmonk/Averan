package averan.demos;

import static averan.core.SessionTools.*;
import static averan.io.ExpressionParser.$$;
import static averan.modules.Reals.*;
import static java.awt.Color.BLACK;
import static java.awt.Color.WHITE;
import static net.sourceforge.aprog.tools.Tools.append;

import averan.core.Module;
import averan.core.Session;
import averan.demos.Demo2.BreakSessionException;
import averan.io.SessionExporter;
import averan.io.TexPrinter;
import averan.modules.Reals;
import averan.modules.Reals.RewriteHint;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;

import org.scilab.forge.jlatexmath.TeXFormula;

import net.sourceforge.aprog.tools.IllegalInstantiationException;

/**
 * @author codistmonk (creation 2014-08-28)
 */
public final class Demo4 {
	
	private Demo4() {
		throw new IllegalInstantiationException();
	}
	
	public static final Module MODULE = new Module(Reals.MODULE, Demo4.class.getName());
	
	static {
		new SessionScaffold() {
			
			@Override
			public final void run() {
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
			}
			
			/**
			 * {@value}.
			 */
			private static final long serialVersionUID = -2527396009076173030L;
			
		};
	}
	
	/**
	 * @param commandLineArguments
	 * <br>Unused
	 */
	public static void main(final String[] commandLineArguments) {
		// NOP
	}
	
	/**
	 * @author codistmonk (creation 2014-08-28)
	 */
	public static abstract class SessionScaffold implements Serializable {
		
		public SessionScaffold() {
			final Session session = pushSession(new Session(MODULE));
			String sessionBreakPoint = "";
			
			try {
				this.run();
			} catch (final BreakSessionException exception) {
				sessionBreakPoint = exception.getStackTrace()[1].toString();
			} finally {
				popSession();
				new SessionExporter(session, 0).exportSession();
				
				System.out.println(sessionBreakPoint);
			}
			
			{
				final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
				
				new SessionExporter(session, new TexPrinter(buffer)
				, 1 < session.getStack().size() ? 0 : 1).exportSession();
				
//				System.out.println(buffer.toString());
				
				new TeXFormula(buffer.toString()).createPNG(0, 18F, "view.png", WHITE, BLACK);
			}
		}
		
		public abstract void run();
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = -8607265458958375768L;
		
	}
	
}
