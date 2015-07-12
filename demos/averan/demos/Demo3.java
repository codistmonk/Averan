package averan.demos;

import static averan.core.ExpressionTools.*;
import static averan.core.SessionTools.*;
import static averan.io.ExpressionParser.$$;
import static averan.modules.Standard.*;
import static multij.tools.Tools.ignore;
import averan.core.Composite;
import averan.core.Expression;
import averan.core.Module;
import averan.core.Module.Symbol;
import averan.io.SessionScaffold;
import averan.modules.Standard;

/**
 * @author codistmonk (creation 2014-08-12)
 */
public final class Demo3 {
	
	public static final Module MODULE = new Module(Standard.MODULE);
	
	static {
		new SessionScaffold(MODULE) {
			
			@Override
			public final void buildSession() {
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
			}
			
			/**
			 * {@value}.
			 */
			private static final long serialVersionUID = 8758246876585919708L;
			
		};
	}
	
	/**
	 * @param commandLineArguments
	 * <br>Unused
	 */
	public static final void main(final String[] commandLineArguments) {
		// NOP
	}
	
}
