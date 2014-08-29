package averan.demos;

import static averan.core.ExpressionTools.*;
import static averan.core.SessionTools.*;
import static averan.modules.Standard.*;
import static java.awt.Color.BLACK;
import static java.awt.Color.WHITE;
import static net.sourceforge.aprog.tools.Tools.cast;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.scilab.forge.jlatexmath.TeXFormula;

import averan.core.Expression;
import averan.core.Module;
import averan.core.Module.Symbol;
import averan.core.Pattern;
import averan.core.Rewriter;
import averan.core.Session;
import averan.demos.Demo2.BreakSessionException;
import averan.io.SessionExporter;
import averan.io.TexPrinter;
import averan.modules.Standard;

import net.sourceforge.aprog.tools.IllegalInstantiationException;
import net.sourceforge.aprog.tools.Pair;
import net.sourceforge.aprog.tools.Tools;

/**
 * @author codistmonk (creation 2014-08-28)
 */
public final class Demo4 {
	
	private Demo4() {
		throw new IllegalInstantiationException();
	}
	
	public static final Module MODULE = new Module(Standard.MODULE, Demo4.class.getName());
	
	public static final Expression anyfy(final Module module) {
		final Rewriter rewriter = new Rewriter();
		
		for (final Symbol parameter : module.getParameters()) {
			rewriter.rewrite(parameter, new Pattern.Any(new Pattern.Any.Key(parameter)));
		}
		
		return module.accept(rewriter);
	}
	
	public static final List<Pair<String, Pattern>> findJustificationsIn(final Module context, final Expression expression) {
		final List<Pair<String, Pattern>> result = new ArrayList<>();
		
		for (final Map.Entry<String, Integer> entry : context.getFactIndices().entrySet()) {
			final Expression contextFact = context.getFacts().get(entry.getValue());
			
			{
				final Pattern justificationPattern = new Pattern(contextFact);
				
				if (justificationPattern.equals(expression)) {
					result.add(new Pair<>(entry.getKey(), justificationPattern));
					continue;
				}
			}
			
			{
				Module module = cast(Module.class, contextFact);
				
				if (module != null) {
					module = ((Module) anyfy(module)).canonical();
					
					for (final Expression moduleFact : module.getFacts()) {
						final Pattern pattern = new Pattern(moduleFact);
						
						if (pattern.equals(expression)) {
							final Pattern justificationPattern = new Pattern(module);
							
							justificationPattern.getBindings().putAll(pattern.getBindings());
							
							result.add(new Pair<>(entry.getKey(), justificationPattern));
							
							break;
						}
					}
				}
			}
		}
		
		return result;
	}
	
	public static final List<Pair<String, Pattern>> findJustificationsFor(final Expression expression) {
		return findJustificationsIn(session(), expression);
	}
	
	public static final List<Pair<String, Pattern>> findJustificationsIn(final Session session, final Expression expression) {
		final List<Pair<String, Pattern>> result = new ArrayList<>();
		
		{
			for (Module context = session.getCurrentModule(); context != null; context = context.getParent()) {
				result.addAll(0, findJustificationsIn(context, expression));
			}
			
			for (final Module context : session.getTrustedModules()) {
				result.addAll(0, findJustificationsIn(context, expression));
			}
			
			Tools.debugPrint(result);
		}
		
		return result;
	}
	
	static {
		new SessionScaffold() {
			
			@Override
			public final void run() {
				final Symbol x = parameter("x");
				
				claim($(x, "->", x));
				{
					introduce();
					recall(conditionName(-1));
				}
				
				claim(x);
				{
					Tools.debugPrint(goal());
					
					findJustificationsFor(goal());
				}
			}
			
			/**
			 * {@value}.
			 */
			private static final long serialVersionUID = -2527396009076173030L;
			
		};
	}
	
	/**
	 * @param commandLineArguments
	 * <br>Unused
	 */
	public static void main(final String[] commandLineArguments) {
		// NOP
	}
	
	/**
	 * @author codistmonk (creation 2014-08-28)
	 */
	public static abstract class SessionScaffold implements Serializable {
		
		public SessionScaffold() {
			final Session session = session();
			String sessionBreakPoint = "";
			
			try {
				this.run();
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
		
		public abstract void run();
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = -8607265458958375768L;
		
	}
	
}
