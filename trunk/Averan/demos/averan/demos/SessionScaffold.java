package averan.demos;

import static averan.core.SessionTools.popSession;
import static averan.core.SessionTools.pushSession;
import static java.awt.Color.BLACK;
import static java.awt.Color.WHITE;

import averan.core.Module;
import averan.core.Session;
import averan.io.SessionExporter;
import averan.io.TexPrinter;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;

import org.scilab.forge.jlatexmath.TeXFormula;

/**
	 * @author codistmonk (creation 2014-08-28)
	 */
	public abstract class SessionScaffold implements Serializable {
		
		protected SessionScaffold(final Module module) {
			this(new Session(module));
		}
		
		protected SessionScaffold(final Session session) {
			pushSession(session);
			
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