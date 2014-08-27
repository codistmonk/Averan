package averan.demos;

import static averan.core.ExpressionTools.*;
import static averan.core.SessionTools.*;
import static averan.io.ExpressionParser2.$$;
import static averan.modules.Standard.*;
import static java.awt.Color.BLACK;
import static java.awt.Color.WHITE;
import static net.sourceforge.aprog.tools.Tools.ignore;

import averan.core.Composite;
import averan.core.Expression;
import averan.core.Module;
import averan.core.Session;
import averan.core.Module.Symbol;
import averan.demos.Demo2.BreakSessionException;
import averan.io.SessionExporter;
import averan.io.TexPrinter;
import averan.modules.Standard;

import java.io.ByteArrayOutputStream;

import org.scilab.forge.jlatexmath.TeXFormula;

/**
 * @author codistmonk (creation 2014-08-12)
 */
public final class Demo3 {
	
	public static final Module MODULE = new Module(Standard.MODULE);
	
	static {
		final Session session = session();
		String sessionBreakPoint = "";
		
		try {
			suppose("notation", $$("∀P,Q ((P/Q) = (P→Q))"));
			claim("parametrize", $$("∀P,X (P/(∀X P))"));
			{
				final Symbol p = introduce();
				final Symbol x = introduce();
				final Composite goal = goal();
				
				ignore(p);
				ignore(x);
				
				bind("notation", (Expression) goal.get(0), goal.get(2));
				claim(((Composite) fact(-1)).get(2));
				{
					introduce();
					recall(conditionName(-1));
				}
				rewriteRight(factName(-1), factName(-2));
			}
			
			claim("suppose", $$("∀P,Q (Q/(P→Q))"));
			{
				final Symbol p = introduce();
				final Symbol q = introduce();
				final Composite goal = goal();
				
				ignore(p);
				ignore(q);
				
				bind("notation", (Expression) goal.get(0), goal.get(2));
				claim(((Composite) fact(-1)).get(2));
				{
					introduce();
					recall(conditionName(-1));
				}
				rewriteRight(factName(-1), factName(-2));
			}
			
			claim("admit", $$("∀P (' '/P)"));
			{
				final Symbol p = introduce();
				final Composite goal = goal();
				
				bind("notation", (Expression) goal.get(0), goal.get(2));
				claim(((Composite) fact(-1)).get(2));
				{
					introduce();
					admit(p);
				}
				rewriteRight(factName(-1), factName(-2));
			}
			
			claim("claim", $$("∀P,Q ((P ∧(P/Q))/Q)"));
			{
				final Symbol p = introduce();
				final Symbol q = introduce();
				final Composite goal = goal();
				
				bind("notation", (Expression) goal.get(0), goal.get(2));
				claim(((Composite) fact(-1)).get(2));
				{
					introduce();
					bind(conditionName(-1));
					bind("notation", p, q);
					rewrite(factName(-2), factName(-1));
					apply(factName(-1), factName(-4));
				}
				rewriteRight(factName(-1), factName(-2));
			}
			
			claim("apply", $$("∀P,Q ((P ∧ (P→Q))/Q)"));
			{
				final Symbol p = introduce();
				final Symbol q = introduce();
				final Composite goal = goal();
				
				ignore(p);
				ignore(q);
				
				bind("notation", (Expression) goal.get(0), goal.get(2));
				claim(((Composite) fact(-1)).get(2));
				{
					introduce();
					bind(conditionName(-1));
					apply(factName(-1), factName(-2));
				}
				rewriteRight(factName(-1), factName(-2));
			}
			
			claim("bind", $$("∀P,X,Y ((∀X P)/(P{X=Y}))"));
			{
				final Symbol p = introduce();
				final Symbol x = introduce();
				final Symbol y = introduce();
				final Composite goal = goal();
				
				ignore(p);
				ignore(y);
				
				bind("notation", (Expression) goal.get(0), goal.get(2));
				claim(((Composite) fact(-1)).get(2));
				{
					introduce();
					bind(conditionName(-1), x);
					substitute(goal());
					rewriteRight(factName(-2), factName(-1));
				}
				rewriteRight(factName(-1), factName(-2));
			}
			
			claim("rewrite", $$("∀P,X,Y ((P ∧ (X=Y))/(P{X=Y}))"));
			{
				final Symbol p = introduce();
				final Symbol x = introduce();
				final Symbol y = introduce();
				
				bind("notation", (Expression) $(p, "&", $(x, "=", y)), $(p, composite($(x, "=", y))));
				claim(((Composite) fact(-1)).get(2));
				{
					introduce();
					bind(conditionName(-1));
					substitute(goal());
					rewriteRight(factName(-3), factName(-1));
				}
				rewriteRight(factName(-1), factName(-2));
			}
			
			admit("substitute", $$("∀P,X,Y ((P'\\\\og\\\\{\\\\fg'X=Y'\\\\og\\\\}\\\\fg')/((P'\\\\og\\\\{\\\\fg'X=Y'\\\\og\\\\}\\\\fg')=(P{X=Y})))"));
		} catch (final BreakSessionException exception) {
			sessionBreakPoint = exception.getStackTrace()[1].toString();
		} finally {
			new SessionExporter(session, -1).exportSession();
			
			System.out.println(sessionBreakPoint);
			
		}
		
		{
			final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			
			new SessionExporter(session, new TexPrinter(buffer)
			, 1 < session.getStack().size() ? 0 : 1).exportSession();
			
			System.out.println(buffer.toString());
			
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
