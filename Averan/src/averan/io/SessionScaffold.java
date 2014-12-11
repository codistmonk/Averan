package averan.io;

import static averan.core.SessionTools.popSession;
import static averan.core.SessionTools.pushSession;
import static java.awt.Color.BLACK;
import static java.awt.Color.WHITE;
import averan.core.Module;
import averan.core.Session;
import averan.core.Session.BreakException;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;

import org.scilab.forge.jlatexmath.TeXFormula;

/**
 * @author codistmonk (creation 2014-08-28)
 */
public abstract class SessionScaffold implements Serializable {
	
	private final int successfulBuildMaximumProofDepth;
	
	protected SessionScaffold(final Module module) {
		this(module, 1);
	}
	
	protected SessionScaffold(final Module module, final int successfulBuildMaximumProofDepth) {
		this(new Session(module), successfulBuildMaximumProofDepth);
	}
	
	protected SessionScaffold(final Session session) {
		this(session, 1);
	}
	
	protected SessionScaffold(final Session session, final int successfulBuildMaximumProofDepth) {
		this.successfulBuildMaximumProofDepth = successfulBuildMaximumProofDepth;
		
		pushSession(session);
		
		String sessionBreakPoint = "";
		
		try {
			this.buildSession();
		} catch (final BreakException exception) {
			sessionBreakPoint = exception.getStackTrace()[1].toString();
		} finally {
			popSession();
			new SessionExporter(session, 0).exportSession();
			
			System.out.println(sessionBreakPoint);
		}
		
		{
			final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			
			new SessionExporter(session, new TexPrinter(buffer)
			, 1 < session.getStack().size() ? 0 : this.successfulBuildMaximumProofDepth).exportSession();
			
//			System.out.println(buffer.toString());
			
			new TeXFormula(buffer.toString()).createPNG(0, 18F, "view.png", WHITE, BLACK);
		}
	}
	
	public abstract void buildSession();
	
	/**
	 * {@value}.
	 */
	private static final long serialVersionUID = -8607265458958375768L;
	
}
