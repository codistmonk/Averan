package jrewrite;

import static net.sourceforge.aprog.af.AFTools.setupSystemLookAndFeel;
import static net.sourceforge.aprog.swing.SwingTools.packAndCenter;
import static net.sourceforge.aprog.swing.SwingTools.scrollable;
import static net.sourceforge.aprog.swing.SwingTools.verticalBox;
import static net.sourceforge.aprog.tools.Tools.debugPrint;
import static net.sourceforge.aprog.tools.Tools.getCallerClass;
import static net.sourceforge.aprog.tools.Tools.unchecked;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.scilab.forge.jlatexmath.Atom;
import org.scilab.forge.jlatexmath.RowAtom;
import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;

import net.sourceforge.aprog.tools.IllegalInstantiationException;
import net.sourceforge.aprog.tools.Tools;

/**
 * @author codistmonk (creation 2014-02-28)
 */
public final class GUI {
	
	private GUI() {
		throw new IllegalInstantiationException();
	}
	
	/**
	 * @param commandLineArguments
	 * <br>Unused
	 */
	public static final void main(final String[] commandLineArguments) {
		setupSystemLookAndFeel("JRewriteGUI");
		newMainFrame().setVisible(true);
	}
	
	public static final JFrame newMainFrame() {
		final JFrame[] result = { null };
		final String title = getCallerClass().getName();
		
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				
				@Override
				public final void run() {
					final JFrame frame = new JFrame(title);
					frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
					frame.setLayout(new BorderLayout());
					frame.add(new Editor(), BorderLayout.CENTER);
					
					result[0] = packAndCenter(frame);
				}
				
			});
		} catch (final Exception exception) {
			throw unchecked(exception);
		}
		
		return result[0];
	}
	
	/**
	 * @author codistmonk (creation 2014-02-28)
	 */
	public static final class Editor extends JPanel {
		
		private final AtomicBoolean changed;
		
		private final JTextArea formulaEdit;
		
		private final JLabel formulaView;
		
		private final Timer timer;
		
		public Editor() {
			super(new BorderLayout());
			this.changed = new AtomicBoolean(true);
			this.formulaEdit = new JTextArea("\\frac{1}{2}\\times2=1");
			this.formulaView = new JLabel();
			this.timer = new Timer(1000, new ActionListener() {
				
				@Override
				public final void actionPerformed(final ActionEvent event) {
					Editor.this.renderLatex();
				}
				
			});
			
			this.formulaEdit.getDocument().addDocumentListener(new DocumentListener() {
				
				@Override
				public final void removeUpdate(final DocumentEvent event) {
					Editor.this.setChanged();
				}
				
				@Override
				public final void insertUpdate(final DocumentEvent event) {
					Editor.this.setChanged();
				}
				
				@Override
				public final void changedUpdate(final DocumentEvent event) {
					Editor.this.setChanged();
				}
				
			});
			
			this.add(scrollable(verticalBox(this.formulaEdit, this.formulaView)), BorderLayout.CENTER);
			
			this.renderLatex();
			this.timer.start();
		}
		
		public final void setChanged() {
			this.changed.set(true);
		}
		
		public final void renderLatex() {
			if (!this.changed.getAndSet(false)) {
				return;
			}
			
			try {
				final TeXFormula formula = new TeXFormula(this.formulaEdit.getText());
				
				{
					final RowAtom rowAtom = Tools.cast(RowAtom.class, formula.root);
					
					if (rowAtom != null) {
						debugPrint(getElements(rowAtom));
					}
				}
				
				final TeXIcon icon = formula.new TeXIconBuilder().setStyle(TeXConstants.STYLE_DISPLAY)
						.setSize(16)
						.setWidth(TeXConstants.UNIT_PIXEL, 256f, TeXConstants.ALIGN_CENTER)
						.setIsMaxWidth(true).setInterLineSpacing(TeXConstants.UNIT_PIXEL, 20f)
						.build();
				this.formulaView.setIcon(icon);
			} catch (final Exception exception) {
				exception.printStackTrace();
				this.formulaView.setIcon(null);
				this.formulaView.setText(exception.getMessage());
			}
			
			this.invalidate();
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = 1879183829782522150L;
		
		public static final List<Atom> getElements(final RowAtom rowAtom) {
			try {
				final Field field = rowAtom.getClass().getDeclaredField("elements");
				field.setAccessible(true);
				return (List<Atom>) field.get(rowAtom);
			} catch (final Exception exception) {
				throw unchecked(exception);
			}
		}
		
	}
	
}
