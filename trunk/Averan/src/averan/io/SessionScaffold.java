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
	
	private final String latexPNGOutputPath;
	
	protected SessionScaffold(final Module module) {
		this(module, 1, DEFAULT_LATEX_PNG_OUTPUT_PATH);
	}
	
	protected SessionScaffold(final Module module, final int successfulBuildMaximumProofDepth, final String latexPNGOutputPath) {
		this(new Session(module), successfulBuildMaximumProofDepth, latexPNGOutputPath);
	}
	
	protected SessionScaffold(final Session session) {
		this(session, 1, DEFAULT_LATEX_PNG_OUTPUT_PATH);
	}
	
	protected SessionScaffold(final Session session, final int successfulBuildMaximumProofDepth, final String latexPNGOutputPath) {
		this.successfulBuildMaximumProofDepth = successfulBuildMaximumProofDepth;
		this.latexPNGOutputPath = latexPNGOutputPath;
		
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
		
		if (this.latexPNGOutputPath != null) {
			final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			
			new SessionExporter(session, new TexPrinter(buffer)
			, 1 < session.getStack().size() ? 0 : this.successfulBuildMaximumProofDepth).exportSession();
			
			new TeXFormula(buffer.toString()).createPNG(0, 18F, this.latexPNGOutputPath, WHITE, BLACK);
		}
	}
	
	public abstract void buildSession();
	
	private static final long serialVersionUID = -8607265458958375768L;
	
	/**
	 * {@value}.
	 */
	public static final String DEFAULT_LATEX_PNG_OUTPUT_PATH = "view.png";
	
}
