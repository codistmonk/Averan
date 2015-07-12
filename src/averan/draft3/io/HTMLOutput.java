package averan.draft3.io;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import multij.tools.Tools;
import averan.draft3.core.Expression;
import averan.draft3.core.Proof;
import averan.draft3.core.Session;
import averan.draft3.core.Proof.Deduction;
import averan.draft3.core.Proof.Message;
import averan.draft3.core.Proof.Deduction.Inclusion;
import averan.draft3.core.Proof.Message.Composite;
import averan.draft3.core.Proof.Message.Element;
import averan.draft3.core.Proof.Message.Reference;
import averan.draft3.core.Session.Output;

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
		this.included = new HashSet<>();
	}
	
	@Override
	public final void beginSession(final Session session) {
		final String propositionName = session.getDeductions().get(0).getPropositionName();
		
		if (this.out == null) {
			this.out = newPrintStream(propositionName + ".html");
		}
		
		this.out.println("<html><head>");
		this.out.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">");
		this.out.println("<title>" + escape(propositionName) + "</title>");
		this.out.println("</head><body>");
	}
	
	@Override
	public final void beginDeduction(final Deduction deduction) {
		this.out.println("<div>Deduce (" + escape(deduction.getPropositionName()) + ")<br/>");
		this.out.println("<ul>");
		
		if (deduction.getRootParameters() != null) {
			this.out.println("<li>" + escape(deduction.getRootParameters().accept(ConsoleOutput.TO_STRING)) + "</li>");
		}
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
				this.out.println("<span onclick=\"var style=document.getElementById('" + escape(proofContentsId) + "').style; style.display=style.display==''?'none':''\"><u>"
						+ proof.getMessage().accept(MESSAGE_TO_STRING) + "</u></span>");
				
				final Deduction proofAsDeduction = (Deduction) proof;
				
				this.out.println("<ul id='" + escape(proofContentsId) + "' style='display:none'>");
				
				if (proofAsDeduction.getRootParameters() != null) {
					this.out.println("<li>" + escape(proofAsDeduction.getRootParameters().accept(ConsoleOutput.TO_STRING)) + "</li>");
				}
				
				proofAsDeduction.getProofs().forEach(this::processProof);
				
				this.out.println("</ul>");
			} else {
				this.out.println(proof.getMessage().accept(MESSAGE_TO_STRING));
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
	
	public static final Message.Visitor<String> MESSAGE_TO_STRING = new Message.Visitor<String>() {
		
		@Override
		public final String visit(final Element element) {
			if (element.getObject() instanceof Expression[]) {
				return Tools.join(", ", Arrays.stream((Expression<?>[]) element.getObject()).map(e -> e == null ? "_" : e.accept(ConsoleOutput.TO_STRING)).toArray());
			}
			
			return escape(element.accept(Message.TO_STRING));
		}
		
		@Override
		public final String visit(final Reference reference) {
			final Proof proof = reference.getObject().getFirst().findProof(reference.getObject().getSecond());
			Deduction root = reference.getObject().getFirst();
			
			if (proof instanceof Inclusion) {
				root = ((Inclusion) proof).getIncluded().getParent();
			}
			
			while (root.getParent() != null) {
				root = root.getParent();
			}
			
			final String deductionName = escape(root.getPropositionName());
			final String proofName = escape(proof.getPropositionName());
			
			return "<a href=\"" + deductionName + ".html#" + proofName + "\">(" + proofName + ")</a>";
		}
		
		@Override
		public final String visit(final Composite composite) {
			final StringBuilder result = new StringBuilder();
			
			for (final Message<?> message : composite.getObject()) {
				result.append(message.accept(this));
			}
			
			return result.toString();
		}
		
		private static final long serialVersionUID = 1466918098245297850L;
		
	};
	
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
