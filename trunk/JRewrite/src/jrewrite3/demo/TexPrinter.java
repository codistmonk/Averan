package jrewrite3.demo;

import static jrewrite3.core.Composite.isBracedComposite;
import static jrewrite3.core.ExpressionTools.$;
import static jrewrite3.demo.TexPrinter.TexStringGenerator.Pattern.any;
import static net.sourceforge.aprog.tools.Tools.join;
import static net.sourceforge.aprog.tools.Tools.list;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import jrewrite3.core.Composite;
import jrewrite3.core.Expression;
import jrewrite3.core.Module;
import jrewrite3.core.Module.Command;
import jrewrite3.core.Module.Symbol;
import jrewrite3.core.Session;
import jrewrite3.core.Visitor;
import net.sourceforge.aprog.tools.Tools;

/**
 * @author codistmonk (creation 2014-08-09)
 */
public final class TexPrinter implements Session.ExporterOutput {
	
	private final PrintStream output;
	
	public TexPrinter() {
		this(System.out);
	}
	
	public TexPrinter(final OutputStream output) {
		this.output = output instanceof PrintStream ? (PrintStream) output : new PrintStream(output);
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
		this.left(pgroup(word(conditionName)));
		this.center(condition.accept(TexStringGenerator.INSTANCE));
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
		this.left(pgroup(word(factName)));
		this.center(fact.accept(TexStringGenerator.INSTANCE));
	}
	
	@Override
	public final void beginModuleFactProof() {
		this.left("\\;");
		this.center(pgroup(pgroup(word("PROOF"))));
	}
	
	@Override
	public final void processModuleFactProof(final Command command) {
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
		this.hline();
		this.center(pgroup(pgroup(word("GOAL"))));
		this.center(currentGoal.accept(TexStringGenerator.INSTANCE));
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
	public static final class TexStringGenerator implements Visitor<String> {
		
		@Override
		public final String beginVisit(final Composite composite) {
			{
				final Pattern summation = newSummationPattern1();
				
				if (summation.equals(composite)) {
					return "\\sum_" + group(summation.get("i=a").accept(this))
							+ "^" + group(summation.get("b").accept(this)) + " "
							+ group(summation.get("e").accept(this));
				}
			}
			
			final List<Expression> children = composite.getChildren();
			final StringBuilder resultBuilder = new StringBuilder();
			final boolean thisIsBraced = isBracedComposite(composite);
			final int n = children.size();
			
			if (!thisIsBraced && Module.isSubstitution(composite)) {
				final Composite equalities = (Composite) children.get(1);
				
				resultBuilder.append(children.get(0)).append(
						cgroup(Tools.join(",", this.transform(equalities.getChildren()))));
				
				if (n == 3) {
					final Composite indices = (Composite) children.get(2);
					
					resultBuilder.append(children.get(0)).append(
							sgroup(Tools.join(",", this.transform(indices.getChildren()))));
				}
				
				return resultBuilder.toString();
			}
			
			if (thisIsBraced) {
				return "\\left" + children.get(0)
						+ join("", Expression.listAcceptor(children.subList(1, n - 1), this).get())
						+ "\\right" + children.get(n - 1);
			}
			
			if (n == 3 && "/".equals(children.get(1).toString())) {
				return group("\\frac" + group(children.get(0).accept(this)) + group(children.get(2).accept(this)));
			}
			
			for (final Expression child : children) {
				if (child instanceof Symbol || isBracedComposite(child)) {
					resultBuilder.append(child.accept(this));
				} else {
					resultBuilder.append(pgroup(child.accept(this)));
				}
			}
			
			return group(resultBuilder.toString());
		}
		
		@Override
		public final String beginVisit(final Module module) {
			return group(module.getParameters().isEmpty() ? "" : "∀" + Tools.join(",", this.transform(module.getParameters())) + "\\;")
					+ (module.getConditions().isEmpty() ? "" : formatConjunction(this.transform(module.getConditions())) + " → ")
					+ formatConjunction(this.transform(module.getFacts()));
		}
		
		@Override
		public final String visit(final Symbol symbol) {
			final String string = symbol.toString();
			
			return string.length() == 1 ? string : word(string);
		}
		
		public final Object[] transform(final Collection<? extends Expression> elements) {
			return elements.stream().map(e -> e.accept(this)).toArray();
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = 3004635190043687534L;
		
		public static final TexStringGenerator INSTANCE = new TexStringGenerator();
		
		public static final String formatConjunction(final List<Expression> propositions) {
			if (propositions.size() == 1) {
				final Expression proposition = propositions.get(0);
				
				if (proposition instanceof Symbol) {
					return proposition.toString();
				}
			}
			
			return pgroup(join(" ∧ ", propositions));
		}
		
		public static final String formatConjunction(final Object... propositions) {
			if (propositions.length == 1) {
				final Object proposition = propositions[0];
				
				if (proposition instanceof Symbol) {
					return proposition.toString();
				}
			}
			
			return pgroup(Tools.join(" ∧ ", propositions));
		}
		
		public static final String join(final String separator, final Iterable<?> elements) {
			return Tools.join(separator, list(elements).toArray());
		}
		
		public static final Pattern newSummationPattern1() {
			return new Pattern($($($("Σ", "_", any("i=a")), "^", any("b")), any("e")));
		}
		
		/**
		 * @author codistmonk (creation 2014-08-09)
		 */
		public static final class Pattern implements Serializable {
			
			private final Map<String, Expression> bindings; 
			
			private final Expression template;
			
			public Pattern(final Expression template) {
				this.bindings = new HashMap<>();
				this.template = template.accept(new SetupAny());
			}
			
			public final Map<String, Expression> getBindings() {
				return this.bindings;
			}
			
			@Override
			public final int hashCode() {
				return 0;
			}
			
			@Override
			public final boolean equals(final Object object) {
				return this.template.equals(object);
			}
			
			@SuppressWarnings("unchecked")
			public final <E extends Expression> E get(final String anyName) {
				return (E) this.getBindings().get(anyName);
			}
			
			/**
			 * @author codistmonk (creation 2014-08-09)
			 */
			final class SetupAny implements Visitor<Expression> {
				
				@Override
				public final Expression endVisit(final Composite composite, final Expression compositeVisit,
						final Supplier<List<Expression>> childVisits) {
					childVisits.get();
					
					return composite;
				}
				
				@Override
				public final Expression endVisit(final Module module, final Expression moduleVisit,
						final Supplier<List<Expression>> parameterVisits,
						final Supplier<List<Expression>> conditionVisits,
						final Supplier<List<Expression>> factVisits) {
					parameterVisits.get();
					conditionVisits.get();
					factVisits.get();
					
					return module;
				}
				
				@Override
				public final Expression visit(final Symbol symbol) {
					return symbol;
				}
				
				public final Map<String, Expression> getBindings() {
					return Pattern.this.getBindings();
				}
				
				/**
				 * {@value}.
				 */
				private static final long serialVersionUID = -5894410726685899719L;
				
			}
			
			/**
			 * {@value}.
			 */
			private static final long serialVersionUID = -8302462150472406630L;
			
			public static final Any any(final String name) {
				return new Any(name);
			}
			
			/**
			 * @author codistmonk (creation 2014-08-09)
			 */
			public static final class Any implements Expression {
				
				private Map<String, Expression> bindings;
				
				private final String name;
				
				public Any(final String name) {
					this.name = name;
				}
				
				@SuppressWarnings("unchecked")
				@Override
				public final <R> R accept(final Visitor<R> visitor) {
					this.bindings = ((SetupAny) visitor).getBindings();
					
					return (R) this;
				}
				
				@Override
				public final boolean equals(final Object object) {
					final Expression alreadyBound = this.bindings.get(this.toString());
					
					if (alreadyBound == null) {
						this.bindings.put(this.toString(), (Expression) object);
						
						return true;
					}
					
					return alreadyBound.equals(object);
				}
				
				@Override
				public final String toString() {
					return this.name;
				}
				
				final void setBindings(final Map<String, Expression> bindings) {
					this.bindings = bindings;
				}
				
				@Override
				public final int hashCode() {
					return 0;
				}
				
				/**
				 * {@value}.
				 */
				private static final long serialVersionUID = -6185178560899095806L;
				
			}
			
		}
		
	}
	
}
