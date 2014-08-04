package jrewrite3.modules;

import static jrewrite3.core.Module.IDENTITY;
import static jrewrite3.core.Module.ROOT;
import static jrewrite3.core.Module.equality;

import jrewrite3.core.Module;
import jrewrite3.core.Module.Symbol;
import net.sourceforge.aprog.tools.IllegalInstantiationException;

/**
 * @author codistmonk (creation 2014-08-04)
 */
public final class Standard {
	
	private Standard() {
		throw new IllegalInstantiationException();
	}
	
	public static final String SYMMETRY_OF_IDENTITY = "symmetry_of_identity";
	
	public static final Module module = new Module(ROOT);
	
	static {
		final Module symmetryOfIdentity = new Module(module);
		final Symbol x = symmetryOfIdentity.parameter("x");
		final Symbol y = symmetryOfIdentity.parameter("y");
		
		symmetryOfIdentity.new Suppose("if x=y", equality(x, y)).execute();
		
		final Module yEqualsX = new Module(symmetryOfIdentity);
		
		yEqualsX.new Bind("as x=x", ROOT, IDENTITY).bind(x).execute();
		yEqualsX.new Rewrite("then y=x", yEqualsX, "as x=x", symmetryOfIdentity, "if x=y").atIndices(0).execute();
		
		symmetryOfIdentity.new Claim("then y=x", equality(y, x), yEqualsX).execute();
		
		module.new Claim(SYMMETRY_OF_IDENTITY, symmetryOfIdentity).execute();
	}
	
}
