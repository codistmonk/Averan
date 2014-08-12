package averan.io;

import static averan.core.Composite.isBracedComposite;
import static averan.core.Pattern.any;
import static averan.tactics.ExpressionTools.$;
import static net.sourceforge.aprog.tools.Tools.cast;
import static net.sourceforge.aprog.tools.Tools.join;
import static net.sourceforge.aprog.tools.Tools.list;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import averan.core.Composite;
import averan.core.Expression;
import averan.core.Module;
import averan.core.Module.Statement;
import averan.core.Module.Symbol;
import averan.core.Pattern;
import averan.core.Visitor;

import net.sourceforge.aprog.tools.Pair;
import net.sourceforge.aprog.tools.Tools;

/**
 * @author codistmonk (creation 2014-08-09)
 */
public final class TexPrinter implements SessionExporter.Output {
	
	private final PrintStream output;
	
	private final TexStringGenerator texStringGenerator;
	
	public TexPrinter() {
		this(System.out);
	}
	
	public TexPrinter(final OutputStream output) {
		this.output = output instanceof PrintStream ? (PrintStream) output : new PrintStream(output);
		this.texStringGenerator = new TexStringGenerator();
	}
	
	public final TexPrinter hint(final Object object, final DisplayHint hint) {
		this.texStringGenerator.getDisplayHints().put(object, hint);
		
		return this;
	}
	
	public final void left(final Object object) {
		this.output.println(object + "\\cr");
	}
	
	public final void center(final Object object) {
		this.left("\\multicolumn{1}{|c|}" + group(object));
	}
	
	public final void hline() {
		this.output.println("\\hline");
	}
	
	@Override
	public final void beginSession() {
		this.output.println("\\begin{array}{l}");
	}
	
	@Override
	public final void subcontext(final String name) {
		this.left("\\;");
		this.hline();
		this.hline();
		this.center(pgroup(pgroup(word("MODULE " + name))));
	}
	
	@Override
	public final void processModuleParameters(final Module module) {
		this.center("\\forall" + join(",", module.getParameters().stream().map(s -> word(s.toString())).toArray()));
	}
	
	@Override
	public final void beginModuleConditions(final Module module) {
		this.left("\\;");
		this.center(pgroup(pgroup(word("CONDITIONS"))));
	}
	
	@Override
	public final void processModuleCondition(final String conditionName, final Expression condition) {
		this.left("\\;");
		this.left(pgroup(word(conditionName)));
		this.center(condition.accept(this.texStringGenerator).getFirst());
	}
	
	@Override
	public final void endModuleConditions(final Module module) {
		// NOP
	}
	
	@Override
	public final void beginModuleFacts(final Module module) {
		this.left("\\;");
		this.center(pgroup(pgroup(word("FACTS"))));
	}
	
	@Override
	public final void processModuleFact(final String factName, final Expression fact) {
		this.left("\\;");
		this.left(pgroup(word(factName)));
		this.center(fact.accept(this.texStringGenerator).getFirst());
	}
	
	@Override
	public final void beginModuleFactProof() {
		this.left("\\;");
		this.center(pgroup(pgroup(word("PROOF"))));
	}
	
	@Override
	public final void processModuleFactProof(final Statement command) {
		this.center(word(command));
	}
	
	@Override
	public final void endModuleFactProof() {
		// NOP
	}
	
	@Override
	public final void endModuleFacts(final Module module) {
		// NOP
	}
	
	@Override
	public final void processCurrentGoal(final Expression currentGoal) {
		this.left("\\;");
		this.hline();
		this.center(pgroup(pgroup(word("GOAL"))));
		this.center(currentGoal.accept(this.texStringGenerator).getFirst());
	}
	
	@Override
	public final void endSession() {
		this.output.println("\\end{array}");
		this.output.flush();
	}
	
	/**
	 * {@value}.
	 */
	private static final long serialVersionUID = 2589185560566140739L;
	
	public static final String group(final Object object) {
		return "{" + object + "}";
	}
	
	public static final String pgroup(final Object object) {
		return "\\left(" + object + "\\right)";
	}
	
	public static final String cgroup(final Object object) {
		return "\\left\\{" + object + "\\right\\}";
	}
	
	public static final String sgroup(final Object object) {
		return "\\left[" + object + "\\right]";
	}
	
	public static final String word(final Object object) {
		String string = object.toString();
		String fontType = "text";
		
		if (object.toString().startsWith("≀")) {
			string = string.substring(1);
			fontType = "mathcal";
		}
		
		return "\\" + fontType + group(string);
	}
	
	/**
	 * @author codistmonk (creation 2014-08-09)
	 */
	public static final class TexStringGenerator implements Visitor<Pair<String, DisplayHint>> {
		
		private final Map<Object, DisplayHint> displayHints = new HashMap<>();
		
		public final Map<Object, DisplayHint> getDisplayHints() {
			return this.displayHints;
		}
		
		@Override
		public final Pair<String, DisplayHint> visit(final Composite composite) {
			{
				final Pattern summation = newSummationPattern1();
				
				if (summation.equals(composite)) {
					return DisplayHint.DEFAULT.hint("\\sum_" + group(summation.get("i=a").accept(this).getFirst())
							+ "^" + group(summation.get("b").accept(this).getFirst()) + " "
							+ group(summation.get("e").accept(this).getFirst()));
				}
			}
			
			final List<Expression> children = composite.getChildren();
			final StringBuilder resultBuilder = new StringBuilder();
			final boolean thisIsBraced = isBracedComposite(composite);
			final int n = children.size();
			
			if (!thisIsBraced && Module.isSubstitution(composite)) {
				final Composite equalities = (Composite) children.get(1);
				
				resultBuilder.append(children.get(0)).append(
						cgroup(Tools.join(",", Arrays.stream(this.transform(equalities.getChildren())).map(p -> p.getFirst()).toArray())));
				
				if (n == 3) {
					final Composite indices = (Composite) children.get(2);
					
					resultBuilder.append(children.get(0)).append(
							sgroup(Tools.join(",", Arrays.stream(this.transform(indices.getChildren())).map(p -> p.getFirst()).toArray())));
				}
				
				return DisplayHint.DEFAULT.hint(resultBuilder.toString());
			}
			
			if (thisIsBraced) {
				return DisplayHint.DEFAULT.hint("\\left" + children.get(0)
						+ join("", Expression.listAcceptor(children.subList(1, n - 1), this).get().stream().map(p -> p.getFirst()).collect(Collectors.toList()))
						+ "\\right" + children.get(n - 1));
			}
			
			if (n == 3 && "/".equals(children.get(1).toString())) {
				return DisplayHint.DEFAULT.hint(group("\\frac"
						+ group(children.get(0).accept(this).getFirst())
						+ group(children.get(2).accept(this).getFirst())));
			}
			
			if (isSubscripted(composite)) {
				final Expression child0 = children.get(0);
				final Pair<String, DisplayHint> pair1 = child0.accept(this);
				final Pair<String, DisplayHint> pair2 = children.get(2).accept(this);
				
				if (pair1.getSecond().getPriority() < DisplayHint.GROUP.getPriority()
						|| (child0 instanceof Composite && !isBracedComposite(child0))) {
					resultBuilder.append(pgroup(pair1.getFirst()));
				} else {
					resultBuilder.append(group(pair1.getFirst()));
				}
				
				resultBuilder.append('_').append(group(pair2.getFirst()));
				
				return DisplayHint.GROUP.hint(group(resultBuilder));
			}
			
			final DisplayHint resultHint = n == 2 ? DisplayHint.GROUP
					: n == 3 ? this.getHintOrDefault(children.get(1)) : DisplayHint.DEFAULT;
			
			for (final Expression child : children) {
				final Pair<String, DisplayHint> childPair = child.accept(this);
				
				if (child instanceof Symbol || isBracedComposite(child)) {
					resultBuilder.append(childPair.getFirst());
				} else {
					if (childPair.getSecond().getPriority() < resultHint.getPriority()) {
						resultBuilder.append(pgroup(childPair.getFirst()));
					} else {
						resultBuilder.append(group(childPair.getFirst()));
					}
				}
			}
			
			return resultHint.hint(group(resultBuilder.toString()));
		}
		
		public static final boolean isSubscripted(final Expression expression) {
			final Composite composite = cast(Composite.class, expression);
			
			if (composite == null) {
				return false;
			}
			
			final List<Expression> children = composite.getChildren();
			
			return (children.size() == 3 && "_".equals(children.get(1).toString()));
		}
		
		@Override
		public final Pair<String, DisplayHint> visit(final Module module) {
			final StringBuilder resultBuilder = new StringBuilder();
			
			if (!module.getParameters().isEmpty()) {
				resultBuilder.append("∀").append(Tools.join(",",
						Arrays.stream(this.transform(module.getParameters())).map(p -> p.getFirst()).toArray()))
						.append("\\;");
			}
			
			if (!module.getConditions().isEmpty()) {
				final Pair<String, DisplayHint> conjunction = formatConjunction(this.transform(module.getConditions()));
				
				if (conjunction.getSecond().getPriority() < DisplayHint.IMPLICATION.getPriority()) {
					resultBuilder.append(pgroup(conjunction.getFirst()));
				} else {
					resultBuilder.append(group(conjunction.getFirst()));
				}
				
				resultBuilder.append(" → ");
			}
			
			if (!module.getFacts().isEmpty()) {
				final Pair<String, DisplayHint> conjunction = formatConjunction(this.transform(module.getFacts()));
				
				if (conjunction.getSecond().getPriority() < DisplayHint.IMPLICATION.getPriority()) {
					resultBuilder.append(pgroup(conjunction.getFirst()));
				} else {
					resultBuilder.append(group(conjunction.getFirst()));
				}
			}
			
			return DisplayHint.DEFAULT.hint(resultBuilder.toString());
		}
		
		@Override
		public final Pair<String, DisplayHint> visit(final Symbol symbol) {
			final String string = symbol.toString();
			
			return getHintOrGroup(symbol).hint(string.length() == 1 ? string : word(string));
		}
		
		@SuppressWarnings("unchecked")
		public final Pair<String, DisplayHint>[] transform(final Collection<? extends Expression> elements) {
			return elements.stream().map(e -> e.accept(this)).toArray(Pair[]::new);
		}
		
		public final DisplayHint getHintOrDefault(final Object object) {
			final DisplayHint result = this.getDisplayHints().get(object);
			
			return result != null ? result : DisplayHint.DEFAULT;
		}
		
		public final DisplayHint getHintOrGroup(final Object object) {
			final DisplayHint result = this.getDisplayHints().get(object);
			
			return result != null ? result : DisplayHint.GROUP;
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = 3004635190043687534L;
		
		public static final Pair<String, DisplayHint> formatConjunction(
				final Pair<String, DisplayHint>... propositions) {
			if (propositions.length == 1) {
				return propositions[0];
			}
			
			return DisplayHint.GROUP.hint(pgroup(Tools.join(" ∧ ",
					Arrays.stream(propositions).map(p -> p.getFirst()).toArray())));
		}
		
		public static final String join(final String separator, final Iterable<?> elements) {
			return Tools.join(separator, list(elements).toArray());
		}
		
		public static final Pattern newSummationPattern1() {
			return new Pattern($($($("Σ", "_", any("i=a")), "^", any("b")), any("e")));
		}
		
	}
	
	/**
	 * @author codistmonk (creation 2014-08-11)
	 */
	public static final class DisplayHint implements Serializable {
		
		private final int priority;
		
		private final String prefix;
		
		private final String postfix;
		
		private final int application;
		
		public DisplayHint() {
			this(0, "", "", 0);
		}
		
		public DisplayHint(final int priority, final String prefix, final String postfix, final int application) {
			this.priority = priority;
			this.prefix = prefix;
			this.postfix = postfix;
			this.application = application;
		}
		
		public final String getPrefix() {
			return this.prefix;
		}
		
		public final String getPostfix() {
			return this.postfix;
		}
		
		public final int getPriority() {
			return this.priority;
		}
		
		public final int getApplication() {
			return this.application;
		}
		
		public final Pair<String, DisplayHint> hint(final String string) {
			final DisplayHint newHint = this.getApplication() == 0 ? this : new DisplayHint(
					this.getPriority(), this.getPrefix(), this.getPostfix(), this.getApplication() - 1);
			
			return new Pair<>(this.getPrefix() + string + this.getPostfix(), newHint);
		}
		
		public static final DisplayHint DEFAULT = new DisplayHint();
		
		public static final DisplayHint IMPLICATION = new DisplayHint(5, "", "", 0);
		
		public static final DisplayHint GROUP = new DisplayHint(1000, "", "", 0);
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = 8176839660157109283L;
		
	}
	
}
