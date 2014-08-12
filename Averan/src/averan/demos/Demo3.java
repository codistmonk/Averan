package averan.demos;

import static averan.io.ExpressionParser.$$;
import static averan.tactics.ExpressionTools.*;
import static averan.tactics.SessionTools.*;
import static averan.tactics.StandardTools.rewriteRight;
import static java.awt.Color.BLACK;
import static java.awt.Color.WHITE;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import org.scilab.forge.jlatexmath.TeXFormula;

import averan.core.Composite;
import averan.core.Expression;
import averan.core.Module;
import averan.core.Module.Symbol;
import averan.demos.Demo2.BreakSessionException;
import averan.io.SessionExporter;
import averan.io.TexPrinter;
import averan.modules.Standard;
import averan.tactics.StandardTools;

/**
 * @author codistmonk (creation 2014-08-12)
 */
public final class Demo3 {
	
	public static final Module MODULE = new Module(Standard.MODULE);
	
	static {
		String sessionBreakPoint = "";
		
		try {
			admit("notation", $$("∀P,Q ((P/Q) = (P→Q))"));
			admit("parametrize", $$("∀P,X (P/(∀X P))"));
			admit("suppose", $$("∀P,Q (Q/(P→Q))"));
			admit("recall", $$("∀P (P/(P→P))"));
			admit("admit", $$("∀P (' '/P)"));
			admit("claim", $$("∀P,Q ((P ∧(P/Q))/Q)"));
			admit("apply", $$("∀P,Q ((P ∧ (P→Q))/Q)"));
			admit("bind", $$("∀P,X,Y ((∀X P)/(P{X=Y}))"));
			claim("rewrite", $$("∀P,X,Y ((P ∧ (X=Y))/(P{X=Y}))"));
			{
				final Symbol p = introduce();
				final Symbol x = introduce();
				final Symbol y = introduce();
				
				bind("notation", (Expression) $(p, "&", $(x, "=", y)), $(p, new Composite(Arrays.asList($(x, "=", y)))));
				claim(((Composite) fact(-1)).get(2));
				{
					introduce();
					bind(conditionName(-1));
					substitute(goal());
					rewriteRight(factName(-3), factName(-1));
				}
				rewriteRight(factName(-1), factName(-2));
			}
//			admit("substitute", $$("∀P,X,Y ((P'\\og\\{\\fg'X=Y'\\og\\}\\fg')/((P'\\og\\{\\fg'X=Y'\\og\\}\\fg')=(P{X=Y})))"));
		} catch (final BreakSessionException exception) {
			sessionBreakPoint = exception.getStackTrace()[1].toString();
		} finally {
			new SessionExporter(session(), -1).exportSession();
			
			System.out.println(sessionBreakPoint);
			
		}
		
		{
			final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			
			new SessionExporter(session(), new TexPrinter(buffer)
			, -1).exportSession();
			
			new TeXFormula(buffer.toString()).createPNG(0, 18F, "view.png", WHITE, BLACK);
		}
	}
	
	/**
	 * @param commandLineArguments
	 * <br>Unused
	 */
	public static final void main(final String[] commandLineArguments) {
		// NOP
	}
	
}
