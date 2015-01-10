package averan3.io;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.HashSet;

import averan3.core.Proof;
import averan3.core.Proof.Deduction;
import averan3.core.Proof.Deduction.Inclusion;
import averan3.core.Session;
import averan3.core.Session.Output;

/**
 * @author codistmonk (creation 2015-01-10)
 */
public final class HTMLOutput implements Output {
	
	private PrintStream out;
	
	private final Collection<String> included;
	
	public HTMLOutput() {
		this(null);
	}
	
	public HTMLOutput(final PrintStream out) {
		this.out = out;
		this.included = new HashSet<String>();
	}
	
	@Override
	public final void beginSession(final Session session) {
		final String propositionName = session.getDeductions().get(0).getPropositionName();
		
		if (this.out == null) {
			this.out = newPrintStream(propositionName + ".html");
		}
		
		this.out.println("<html><head><title>" + escape(propositionName) + "</title></head><body>");
	}
	
	@Override
	public final void beginDeduction(final Deduction deduction) {
		this.out.println("<div>Deduce (" + escape(deduction.getPropositionName()) + ")<br/>");
		this.out.println("<ul>");
	}
	
	@Override
	public final void processProof(final Proof proof) {
		if (proof instanceof Inclusion) {
			final String includedParentName = ((Inclusion) proof).getIncluded().getParent().getPropositionName();
			
			if (this.included.add(includedParentName)) {
				this.out.println("<li>Include <a href='" + includedParentName + ".html'>" + escape(includedParentName) + "</a></li>");
			}
		} else {
			this.out.println("<li><div><a id='" + escape(proof.getPropositionName()) + "'>"
					+ "(" + escape(proof.getPropositionName()) + ")</a><br/>"
					+ "<div style='margin-left:2em'>" + escape(proof.getProposition().accept(ConsoleOutput.TO_STRING)) + "<br/>");
			
			
			if (proof instanceof Deduction) {
				final String proofContentsId = proof.getPropositionName() + "_contents";
				this.out.println("<span onclick=\"var style=document.getElementById('"+ proofContentsId + "').style; style.display=style.display==''?'none':''\"><u>" + escape(proof.toString()) + "</u></span>");
				
				final Deduction proofAsDeduction = (Deduction) proof;
				
				this.out.println("<ul id='" + proofContentsId + "' style='display:none'>");
				
				if (proofAsDeduction.getRootParameters() != null) {
					this.out.println("<li>" + escape(proofAsDeduction.getRootParameters().accept(ConsoleOutput.TO_STRING)) + "</li>");
				}
				
				proofAsDeduction.getProofs().forEach(this::processProof);
				
				this.out.println("</ul>");
			} else {
				this.out.println(escape(proof.toString()));
			}
			
			this.out.println("</div></div></li>");
		}
	}
	
	@Override
	public final void endDeduction(final Deduction deduction) {
		this.out.println("</ul>");
		
		if (deduction.getGoal() != null) {
			this.out.println("<div>Goal: " + escape(deduction.getGoal().accept(ConsoleOutput.TO_STRING)) + "</div>");
		}
		
		this.out.println("</div>");
	}
	
	@Override
	public final void endSession(final Session session) {
		this.out.println("</body></html>");
	}

	private static final long serialVersionUID = 8823222879002739370L;
	
	public static final PrintStream newPrintStream(final String filePath) {
		try {
			return new PrintStream(filePath);
		} catch (final FileNotFoundException exception) {
			throw new UncheckedIOException(exception);
		}
	}
	
	public static final String escape(final String string) {
		return string.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
	}
	
}
