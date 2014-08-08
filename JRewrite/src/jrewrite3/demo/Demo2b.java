package jrewrite3.demo;

import static java.awt.Color.BLACK;
import static java.awt.Color.WHITE;
import static java.util.Arrays.copyOfRange;
import static jrewrite3.core.ExpressionTools.$;
import static jrewrite3.core.ExpressionTools.facts;
import static jrewrite3.demo.Demo2b.ExpressionParser.$$;
import static net.sourceforge.aprog.tools.Tools.append;
import static net.sourceforge.aprog.tools.Tools.array;
import static net.sourceforge.aprog.tools.Tools.cast;
import static net.sourceforge.aprog.tools.Tools.ignore;
import static net.sourceforge.aprog.tools.Tools.join;
import static net.sourceforge.aurochs.AurochsTools.input;
import static net.sourceforge.aurochs.LRParserTools.*;
import static net.sourceforge.aurochs.RegularTools.*;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import org.scilab.forge.jlatexmath.TeXFormula;

import jrewrite3.core.Composite;
import jrewrite3.core.Expression;
import jrewrite3.core.Module;
import jrewrite3.core.Module.Command;
import jrewrite3.core.Module.Symbol;
import jrewrite3.core.Session;
import jrewrite3.core.Visitor;
import jrewrite3.modules.Standard;
import net.sourceforge.aprog.tools.IllegalInstantiationException;
import net.sourceforge.aprog.tools.Tools;
import net.sourceforge.aurochs.LRParser;
import net.sourceforge.aurochs.LRParserTools;
import net.sourceforge.aurochs.AbstractLRParser.GeneratedToken;
import net.sourceforge.aurochs.AbstractLRParser.Listener;
import net.sourceforge.aurochs.AbstractLRParser.ReductionEvent;
import net.sourceforge.aurochs.AbstractLRParser.UnexpectedSymbolErrorEvent;
import net.sourceforge.aurochs.LRParserTools.LexerRule;
import net.sourceforge.aurochs.LRParserTools.ParserRule;

/**
 * @author codistmonk (creation 2014-08-08)
 */
public final class Demo2b {
	
	private Demo2b() {
		throw new IllegalInstantiationException();
	}
	
	public static final Module MODULE = new Module(Standard.MODULE);
	
	static {
		final Session session = new Session(MODULE);
		
		session.suppose("definition_of_¬", $$("∀P (¬P = (P→`false))"));
		session.suppose("definition_of_∃", $$("∀P,x (∃x (P x)) = ¬(∀y ¬(P y))"));
		session.suppose("definition_of_∩", $$("∀A,B,x (x∈A∩B) = (x∈A ∧ x∈B)"));
		session.suppose("definition_of_Σ", $$("∀i,a,b,e,s (s=((Σ_(i=a)^b) e)) → (((b<a) → (s=0)) ∧ ((a≤b) → (s=s{b=b-1}+e{i=b})))"));
		session.suppose("definition_of_≀M", $$("∀X,m,n (X∈≀M_(m,n) = ∀i,j (0≤i<m ∧ 0≤j<n) → X_(i,j)∈ℝ)"));
		session.suppose("definition_of_≀C", $$("∀X,n (X∈≀C_n) = ∃m (X∈≀M_(m,n))"));
		session.suppose("definition_of_≀R", $$("∀X,m (X∈≀R_m) = ∃n (X∈≀M_(m,n))"));
		session.suppose("definition_of_matrix_product", $$("∀X,Y,n ((X∈≀C_n) ∧ (Y∈≀R_n)) → (∀i,j,k (XY)_(i,j)=((Σ_(k=0)^(n-1)) (X_(i,k))(Y_(k,j))))"));
		session.suppose("definition_of_ᵀ", $$("∀X (∀i,j (Xᵀ_(i,j)=X_(j,i)))"));
		
		session.new Exporter(true).exportSession();
		
		{
			final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			
//			session.new Exporter(new Session.Printer(new TexPrintStream(buffer)), true).exportSession();
			session.new Exporter(new TexPrinter(), true).exportSession();
			
//			final String s = $$("∀P (¬P = (P→`false))").accept(new TexExporter()) + "\\\\2+2";
			final String s = buffer.toString();
			
			final TeXFormula formula = new TeXFormula(s);
			formula.createPNG(0, 16F, "view.png", WHITE, BLACK);
		}
	}
	
	/**
	 * @param commandLineArguments
	 * <br>Unused
	 */
	public static final void main(final String[] commandLineArguments) {
		// NOP
	}
	
	/**
	 * @author codistmonk (creation 2014-08-06)
	 */
	public static final class ExpressionParser implements Serializable {
		
		private final LRParser mathParser;
		
		private transient Object result;
		
		{
			this.mathParser = LRParserTools.newParser(MathParser.class);
			
			this.mathParser.addListener(new Listener() {

				@Override
				public final void unexpectedSymbolErrorOccured(final UnexpectedSymbolErrorEvent event) {
					ignore(event);
				}
				
				@Override
				public final void reductionOccured(final ReductionEvent event) {
					final GeneratedToken generatedToken = event.getGeneratedToken();
					
					if ("ALL".equals(generatedToken.getSymbol())) {
						ExpressionParser.this.setResult(generatedToken.getValue());
					}
				}
				
				/**
				 * {@value}.
				 */
				private static final long serialVersionUID = 7194229918744361926L;
				
			});
		}
		
		public final <E extends Expression> E parse(final CharSequence charSequence) {
			this.setResult(null);
			
			if (!this.mathParser.parse(input(charSequence))) {
				throw new IllegalArgumentException("Syntax error");
			}
			
			return $(this.result);
		}
		
		final void setResult(final Object result) {
			this.result = result;
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = -2690081831029013658L;
		
		public static final ExpressionParser instance = new ExpressionParser();
		
		@SuppressWarnings("unchecked")
		public static final <E extends Expression> E $$(final CharSequence... charSequences) {
			final int n = charSequences.length;
			
			if (n == 1) {
				return instance.parse(charSequences[0]);
			}
			
			final Object[] facts = new Object[n];
			
			for (int i = 0; i < n; ++i) {
				facts[i] = instance.parse(charSequences[i]);
			}
			
			return (E) facts(facts);
		}
		
		/**
		 * @author codistmonk (creation 2014-08-05)
		 */
		public static final class MathParser implements Serializable {
			
			/**
			 * {@value}.
			 */
			private static final long serialVersionUID = 2607977220438106247L;
			
			static final LexerRule[] lexerRules = appendVerbatims(array(
					tokenRule("VARIABLE", /* -> */ union(range('A', 'Z'), range('a', 'z'))),
					tokenRule("NATURAL",  /* -> */ oneOrMore(range('0', '9'))),
					nontokenRule(" *",     /* -> */ zeroOrMore(' '))
			), "+", "-", "/", "=", "(", ")", "{", "}", "[", "]", ",", "∀", "∃", "¬", "→", "`", "≀", "∧", "∈", "∩", "<", "≤", "Σ", "_", "^", "ℕ", "ℝ", "ᵀ");
			
			static final ParserRule[] parserRules = {
				leftAssociative("∧", 5),
				leftAssociative("→", 5),
				leftAssociative(",", 8),
				leftAssociative("=", 10),
				leftAssociative("∈", 50),
				leftAssociative("<", 50),
				leftAssociative("≤", 50),
				leftAssociative("(", 100),
				leftAssociative("{", 100),
				leftAssociative("[", 100),
				leftAssociative("+", 100),
				leftAssociative("-", 100),
				leftAssociative("∩", 125),
				leftAssociative("/", 200),
				leftAssociative("ᵀ", 300),
				leftAssociative("¬", 300),
				leftAssociative("∀", 300),
				leftAssociative("∃", 300),
				leftAssociative("_", 330),
				leftAssociative("^", 330),
				leftAssociative("`", 340),
				leftAssociative("≀", 340),
				leftAssociative("VARIABLE", 350),
				leftAssociative("NATURAL", 350),
				leftAssociative("Σ", 350),
				leftAssociative("ℕ", 350),
				leftAssociative("ℝ", 350),
				
		        namedRule("expression",        "ALL",        /* -> */  "EXPRESSION"),
		        namedRule("expression",        "EXPRESSION", /* -> */  "∀", "PARAMETERS", "EXPRESSION"),
		        namedRule("expression",        "EXPRESSION", /* -> */  "∃", "VARIABLE", "EXPRESSION"),
		        namedRule("expression",        "EXPRESSION", /* -> */  "¬", "EXPRESSION"),
		        namedRule("expression",        "EXPRESSION", /* -> */  "EXPRESSION", "ᵀ"),
		        namedRule("operation",         "EXPRESSION", /* -> */  "EXPRESSION", "OPERATION"),
		        namedRule("verbatim",          "OPERATION",  /* -> */  "+", "EXPRESSION"),
		        namedRule("verbatim",          "OPERATION",  /* -> */  "-", "EXPRESSION"),
		        namedRule("verbatim",          "OPERATION",  /* -> */  "/", "EXPRESSION"),
		        namedRule("verbatim",          "OPERATION",  /* -> */  "=", "EXPRESSION"),
		        namedRule("verbatim",          "OPERATION",  /* -> */  "→", "EXPRESSION"),
		        namedRule("verbatim",          "OPERATION",  /* -> */  "∧", "EXPRESSION"),
		        namedRule("verbatim",          "OPERATION",  /* -> */  "∈", "EXPRESSION"),
		        namedRule("verbatim",          "OPERATION",  /* -> */  "∩", "EXPRESSION"),
		        namedRule("verbatim",          "OPERATION",  /* -> */  "<", "EXPRESSION"),
		        namedRule("verbatim",          "OPERATION",  /* -> */  "≤", "EXPRESSION"),
		        namedRule("verbatim",          "OPERATION",  /* -> */  "_", "EXPRESSION"),
		        namedRule("verbatim",          "OPERATION",  /* -> */  "^", "EXPRESSION"),
		        namedRule("verbatim",          "OPERATION",  /* -> */  ",", "EXPRESSION"),
		        namedRule("verbatim",          "OPERATION",  /* -> */  "EXPRESSION"),
		        namedRule("grouping",          "EXPRESSION", /* -> */  "(", "EXPRESSION", ")"),
		        namedRule("expression",        "EXPRESSION", /* -> */  "{", "EXPRESSION", "}"),
		        namedRule("expression",        "EXPRESSION", /* -> */  "[", "EXPRESSION", "]"),
		        namedRule("expression",        "EXPRESSION", /* -> */  "NATURAL"),
		        namedRule("expression",        "EXPRESSION", /* -> */  "IDENTIFIER"),
		        namedRule("expression",        "EXPRESSION", /* -> */  "Σ"),
		        namedRule("expression",        "EXPRESSION", /* -> */  "ℕ"),
		        namedRule("expression",        "EXPRESSION", /* -> */  "ℝ"),
		        namedRule("concatenation",     "IDENTIFIER", /* -> */  "≀", "WORD"),
		        namedRule("concatenation",     "IDENTIFIER", /* -> */  "≀", "`", "WORD"),
		        namedRule("concatenation",     "IDENTIFIER", /* -> */  "`", "WORD"),
		        namedRule("concatenation",     "IDENTIFIER", /* -> */  "VARIABLE"),
		        namedRule("concatenation",     "WORD",       /* -> */  "WORD", "VARIABLE"),
		        namedRule("concatenation",     "WORD",       /* -> */  "VARIABLE"),
		        namedRule("list",              "PARAMETERS", /* -> */ "PARAMETERS", ",", "VARIABLE"),
		        namedRule("list",              "PARAMETERS", /* -> */ "VARIABLE"),
				
			};
			
		    final Object expression(final Object[] values) {
		    	if ("∀".equals(values[0].toString())) {
		    		return $(copyOfRange(values, 1, values.length));
		    	}
		    	
		        return $(values);
		    }
			
		    final Object operation(final Object[] values) {
		    	final Object[] right = cast(Object[].class, values[1]);
		    	
		    	if (right != null) {
		    		final Composite left = cast(Composite.class, values[0]);
		    		
		    		if (left != null && ("<".equals(right[0].toString()) || "≤".equals(right[0].toString()))) {
		    			return $(append(left.getChildren().toArray(), right));
		    		}
		    		
		    		return $(append(array(values[0]), right));
		    	}
		    	
		    	return $(append(array(values[0]), values[1]));
		    }
		    
		    final Object verbatim(final Object[] values) {
		    	return values;
		    }
		    
		    final Object concatenation(final Object[] values) {
		    	return join("", values);
		    }
		    
		    final Object grouping(final Object[] values) {
		    	return values[1];
		    }
		    
		    final Object list(final Object[] values) {
		    	List<?> result = cast(List.class, values[0]);
		    	
		    	if (result == null) {
		    		result = new ArrayList<>();
		    	}
		    	
		    	result.add($(values[values.length - 1]));
		    	
		    	return result;
		    }
		    
		}
		
	}
	
	public static final LexerRule[] appendVerbatims(final LexerRule[] lexerRules, final Object... verbatims) {
		final int m = lexerRules.length;
		final int n = m + verbatims.length;
		final LexerRule[] result = Arrays.copyOf(lexerRules, n);
		
		for (int i = m; i < n; ++i) {
			final Object verbatim = verbatims[i - m];
			result[i] = verbatimTokenRule(verbatim, string(verbatim.toString()));
		}
		
		return result;
	}
	
	/**
	 * @author codistmonk (creation 2014-08-09)
	 */
	public static final class TexPrinter implements Session.ExporterOutput {
		
		private final OutputStream output;
		
		public TexPrinter() {
			this(System.out);
		}
		
		public TexPrinter(final OutputStream output) {
			this.output = output;
		}
		
		@Override
		public final void subcontext(final String name) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void processModuleParameters(Module module) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void beginModuleConditions(Module module) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void processModuleCondition(String conditionName,
				Expression condition) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void endModuleConditions(Module module) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void beginModuleFacts(Module module) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void processModuleFact(String factName, Expression fact) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void beginModuleFactProof() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void processModuleFactProof(Command command) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void endModuleFactProof() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void endModuleFacts(Module module) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void processCurrentGoal(Expression currentGoal) {
			// TODO Auto-generated method stub
			
		}
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 2589185560566140739L;
		
		/**
		 * @author codistmonk (creation 2014-08-09)
		 */
		public static final class TexStringGenerator implements Visitor<String> {

			@Override
			public final String beginVisit(final Composite composite) {
				return composite.toString();
			}
			
			@Override
			public final String beginVisit(final Module module) {
				return module.toString();
			}
			
			@Override
			public final String visit(final Symbol symbol) {
				return symbol.toString();
			}
			
			/**
			 * {@value}.
			 */
			private static final long serialVersionUID = 3004635190043687534L;
			
			public static final TexStringGenerator INSTANCE = new TexStringGenerator();
			
		}
		
	}
	
}
