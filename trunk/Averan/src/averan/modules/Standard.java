package averan.modules;

import static averan.core.Module.ROOT;
import static averan.core.Module.equality;
import averan.core.Module;
import averan.core.Module.Symbol;
import net.sourceforge.aprog.tools.IllegalInstantiationException;

/**
 * @author codistmonk (creation 2014-08-04)
 */
public final class Standard {
	
	private Standard() {
		throw new IllegalInstantiationException();
	}
	
	/**
	 * {@value}.
	 */
	public static final String IDENTITY = "identity";
	
	/**
	 * {@value}.
	 */
	public static final String SYMMETRY_OF_EQUALITY = "symmetry_of_equality";

	/**
	 * {@value}.
	 */
	public static final String TRUTHNESS_OF_TRUE = "truthness_of_true";
	
	/**
	 * {@value}.
	 */
	public static final String ELIMINATION_OF_FALSE = "elimination_of_false";
	
	public static final Module MODULE = new Module(ROOT);
	
	public static final Symbol TRUE = MODULE.new Symbol("true");
	
	public static final Symbol FALSE = MODULE.new Symbol("false");
	
	static {
		{
			final Module identity = new Module(MODULE);
			final Symbol x = identity.new Parametrize("x").executeAndGet();
			
			identity.new Admit(equality(x, x)).execute();
			
			MODULE.new Admit(IDENTITY, identity).execute();
		}
		
		{
			MODULE.new Admit(TRUTHNESS_OF_TRUE, TRUE).execute();
		}
		
		{
			final Module eliminationOfFalse = new Module(MODULE);
			
			eliminationOfFalse.new Suppose(FALSE);
			
			final Module anythingIsTrue = new Module(eliminationOfFalse);
			final Symbol p = anythingIsTrue.new Parametrize("P").executeAndGet();
			
			anythingIsTrue.new Admit("truthness_of_P", p).execute();
			eliminationOfFalse.new Claim("anything_is_true", anythingIsTrue).execute();
			
			MODULE.new Claim(ELIMINATION_OF_FALSE, eliminationOfFalse).execute();
		}
		
		{
			final Module symmetryOfIdentity = new Module(MODULE);
			final Symbol x = symmetryOfIdentity.new Parametrize("x").executeAndGet();
			final Symbol y = symmetryOfIdentity.new Parametrize("y").executeAndGet();
			
			symmetryOfIdentity.new Suppose("if x=y", equality(x, y)).execute();
			
			final Module yEqualsX = new Module(symmetryOfIdentity);
			
			yEqualsX.new Bind("as x=x", MODULE, IDENTITY).bind(x).execute();
			yEqualsX.new Rewrite("then y=x", yEqualsX, "as x=x", symmetryOfIdentity, "if x=y").atIndices(0).execute();
			
			symmetryOfIdentity.new Claim("then y=x", equality(y, x), yEqualsX).execute();
			
			MODULE.new Claim(SYMMETRY_OF_EQUALITY, symmetryOfIdentity).execute();
		}
	}
	
}
