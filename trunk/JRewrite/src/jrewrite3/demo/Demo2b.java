package jrewrite3.demo;

import static java.awt.Color.BLACK;
import static java.awt.Color.WHITE;
import static java.util.Arrays.copyOfRange;
import static jrewrite3.core.Composite.isBracedComposite;
import static jrewrite3.core.ExpressionTools.$;
import static jrewrite3.core.ExpressionTools.facts;
import static jrewrite3.core.ExpressionTools.rule;
import static jrewrite3.demo.Demo2b.ExpressionParser.$$;
import static jrewrite3.demo.Demo2b.TexPrinter.TexStringGenerator.Pattern.any;
import static net.sourceforge.aprog.tools.Tools.append;
import static net.sourceforge.aprog.tools.Tools.array;
import static net.sourceforge.aprog.tools.Tools.cast;
import static net.sourceforge.aprog.tools.Tools.ignore;
import static net.sourceforge.aprog.tools.Tools.join;
import static net.sourceforge.aprog.tools.Tools.list;
import static net.sourceforge.aurochs.AurochsTools.input;
import static net.sourceforge.aurochs.LRParserTools.*;
import static net.sourceforge.aurochs.RegularTools.*;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.scilab.forge.jlatexmath.TeXFormula;

import jrewrite3.core.Composite;
import jrewrite3.core.Expression;
import jrewrite3.core.Module;
import jrewrite3.core.Module.Command;
import jrewrite3.core.Module.Symbol;
import jrewrite3.core.Rewriter;
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
		
		try {
			session.suppose("definition_of_conjunction",
					$$("∀P,Q (P → (Q → (P ∧ Q)))"));
			session.suppose("definition_of_proposition_equality",
					$$("∀P,Q ((P=Q) = ((P→Q) ∧ (Q→P)))"));
			session.suppose("definition_of_negation",
					$$("∀P (¬P = (P→`false))"));
			session.suppose("definition_of_existence",
					$$("∀P,x (∃x (P x)) = ¬(∀y ¬(P y))"));
			session.suppose("definition_of_intersection",
					$$("∀A,B,x (x∈A∩B) = (x∈A ∧ x∈B)"));
			session.suppose("definition_of_summation",
					$$("∀i,a,b,e,s ((s=((Σ_(i=a)^b) e)) → (((b<a) → (s=0)) ∧ ((a≤b) → (s=(s{b=(b-1)})+(e{i=b})))))"));
			session.suppose("definition_of_matrices",
					$$("∀X,m,n (X∈≀M_(m,n) = (`rowCount_X = m ∧ `columnCount_X = n ∧ ∀i,j (0≤i<m ∧ 0≤j<n) → X_(i,j)∈ℝ))"));
			session.suppose("definition_of_matrix_size_equality",
					$$("∀X,Y ((`size_X=`size_Y) = ((`columnCount_X = `columnCount_Y) ∧ (`rowCount_X = `rowCount_Y)))"));
			session.suppose("definition_of_matrix_equality",
					$$("∀X,Y ((X=Y) = (∀i,j ((X)_(i,j)=(Y_(i,j)))))"));
			session.suppose("definition_of_matrix_addition",
					$$("∀X,Y ((`size_X=`size_Y) → (∀i,j ((X+Y)_(i,j)=(X_(i,j))+(Y_(i,j)))))"));
			session.suppose("definition_of_matrix_subtraction",
					$$("∀X,Y ((`size_X=`size_Y) → (∀i,j ((X-Y)_(i,j)=(X_(i,j))-(Y_(i,j)))))"));
			session.suppose("definition_of_matrix_multiplication",
					$$("∀X,Y,n ((`columnCount_X = n) ∧ (`rowCount_Y = n)) → (∀i,j,k (XY)_(i,j)=((Σ_(k=0)^(n-1)) (X_(i,k))(Y_(k,j))))"));
			session.suppose("definition_of_transposition",
					$$("∀X (∀i,j (Xᵀ_(i,j)=X_(j,i)))"));
			session.suppose("definition_of_transposition_rowCount",
					$$("∀X (`rowCount_(Xᵀ)=`columnCount_X)"));
			session.suppose("definition_of_transposition_columnCount",
					$$("∀X (`columnCount_(Xᵀ)=`rowCount_X)"));
			
			claimCommutativityOfConjunction(session);
			claimTranspositionOfAddition(session);
		} finally {
			session.new Exporter(0).exportSession();
		}
		
		{
			final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			
			session.new Exporter(new TexPrinter(buffer), 0).exportSession();
			
			new TeXFormula(buffer.toString()).createPNG(0, 16F, "view.png", WHITE, BLACK);
		}
	}
	
	public static final void claimTranspositionOfAddition(final Session session) {
		session.claim("transposition_of_addition", $$("∀X,Y ((`size_X=`size_Y) → ((X+Y)ᵀ=Xᵀ+Yᵀ))"));
		
		{
			session.introduce();
			session.introduce();
			session.introduce();
			
			final Symbol x = session.getParameter("X");
			final Symbol y = session.getParameter("Y");
			final Expression xt = $(x, "ᵀ");
			final Expression yt = $(y, "ᵀ");
			
			session.bind("definition_of_transposition", (Expression) $(x, "+", y));
			session.bind("definition_of_matrix_addition", x, y);
			session.apply("transposition_of_addition#2", "transposition_of_addition#0");
			
			{
				final Module m = (Module) session.getCurrentContext().getModule().getProposition("transposition_of_addition#1");
				final Symbol i = m.getParameter("i");
				final Symbol j = m.getParameter("j");
				
				session.bind("transposition_of_addition#3", j, i);
				
				{
					session.bind("definition_of_transposition", x, i, j);
					rewriteRight(session, "transposition_of_addition#4", "transposition_of_addition#5");
				}
				
				{
					session.bind("definition_of_transposition", y, i, j);
					rewriteRight(session, "transposition_of_addition#6", "transposition_of_addition#7");
				}
				
				session.rewrite("transposition_of_addition#1", "transposition_of_addition#8");
			}
			
			session.bind("definition_of_matrix_addition", xt, yt);
			
			session.claim($($("size", "_", xt), "=", $("size", "_", yt)));
			
			{
				session.bind("definition_of_matrix_size_equality", xt, yt);
				session.bind("definition_of_matrix_size_equality", x, y);
				session.rewrite("transposition_of_addition#0", "transposition_of_addition#11#1");
				session.bind("definition_of_transposition_rowCount", x);
				session.bind("definition_of_transposition_columnCount", x);
				session.bind("definition_of_transposition_rowCount", y);
				session.bind("definition_of_transposition_columnCount", y);
				rewriteRight(session, "transposition_of_addition#11#2", "transposition_of_addition#11#3");
				rewriteRight(session, "transposition_of_addition#11#7", "transposition_of_addition#11#4");
				rewriteRight(session, "transposition_of_addition#11#8", "transposition_of_addition#11#5");
				rewriteRight(session, "transposition_of_addition#11#9", "transposition_of_addition#11#6");
				
				final Module conjunction1110 = session.getProposition("transposition_of_addition#11#10");
				
				session.bind("commutativity_of_conjunction", conjunction1110.getFacts().get(0), conjunction1110.getFacts().get(1));
				session.rewrite("transposition_of_addition#11#10", "transposition_of_addition#11#11");
				rewriteRight(session, "transposition_of_addition#11#12", "transposition_of_addition#11#0");
			}
			
			session.apply("transposition_of_addition#10", "transposition_of_addition#11");
			
			{
				final Module m = (Module) session.getCurrentContext().getModule().getProposition("transposition_of_addition#9");
				final Symbol i = m.getParameter("i");
				final Symbol j = m.getParameter("j");
				
				session.bind("transposition_of_addition#12", i, j);
				rewriteRight(session, "transposition_of_addition#9", "transposition_of_addition#13");
			}
			
			session.bind("definition_of_matrix_equality", (Expression) $($(x, "+", y), "ᵀ"), $(xt, "+", yt));
			rewriteRight(session, "transposition_of_addition#14", "transposition_of_addition#15");
		}
	}
	
	public static final void claimCommutativityOfConjunction(final Session session) {
		session.claim("commutativity_of_conjunction",
				$$("∀P,Q ((P ∧ Q) = (Q ∧ P))"));
		
		{
			session.introduce();
			session.introduce();
			
			final Symbol p = session.getParameter("P");
			final Symbol q = session.getParameter("Q");
			final Expression pq = $(p, "&", q);
			final Expression qp = $(q, "&", p);
			final Expression pq2qp = $(pq, "->", qp);
			final Expression qp2pq = $(qp, "->", pq);
			
			session.bind("definition_of_proposition_equality", pq, qp);
			
			session.claim(pq2qp);
			{
				session.introduce();
				session.bind("commutativity_of_conjunction#1#0");
//				session.recall("commutativity_of_conjunction#1#1/#1");
//				session.recall("commutativity_of_conjunction#1#1/#0");
//				if (true) throw new RuntimeException();
				session.bind("definition_of_conjunction", q, p);
				session.apply("commutativity_of_conjunction#1#3", "commutativity_of_conjunction#1#1/#1");
				session.apply("commutativity_of_conjunction#1#4", "commutativity_of_conjunction#1#1/#0");
			}
			
			session.claim(qp2pq);
			
			{
				session.introduce();
				session.bind("commutativity_of_conjunction#2#0");
				session.bind("definition_of_conjunction", p, q);
				session.apply("commutativity_of_conjunction#2#3", "commutativity_of_conjunction#2#1/#1");
				session.apply("commutativity_of_conjunction#2#4", "commutativity_of_conjunction#2#1/#0");
			}
			
			session.claim($(pq2qp, "&", qp2pq));
			
			{
				session.bind("definition_of_conjunction", pq2qp, qp2pq);
				session.apply("commutativity_of_conjunction#3#0", "commutativity_of_conjunction#1");
				session.apply("commutativity_of_conjunction#3#1", "commutativity_of_conjunction#2");
			}
			
			rewriteRight(session, "commutativity_of_conjunction#3", "commutativity_of_conjunction#0");
		}
	}
	
	public static final void rewriteRight(final Session session, final String sourceName, final String equalityName) {
		final Composite equality = session.getProposition(equalityName);
		
		session.claim(session.getProposition(sourceName).accept(new Rewriter().rewrite(equality.get(2), equality.get(0))));
		
		{
			final String ruleName = session.getCurrentContext().getModule().newPropositionName();
			
			session.bind(ruleName, Standard.SYMMETRY_OF_EQUALITY, (Expression) equality.get(0), equality.get(2));
			
			final String reversedEqualityName = session.getCurrentContext().getModule().newPropositionName();
			
			session.apply(reversedEqualityName, ruleName, equalityName);
			
			session.rewrite(sourceName, reversedEqualityName);
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
			
			static final Object[] verbatims = { "+", "-", "/", "=", "(", ")", "{", "}", "[", "]", ",", "∀", "∃", "¬", "→", "`", "≀", "∧", "∈", "∩", "<", "≤", "Σ", "_", "^", "ℕ", "ℝ", "ᵀ" };
			
			static final LexerRule[] lexerRules = appendVerbatims(array(
					tokenRule("VARIABLE", /* -> */ union(range('A', 'Z'), range('a', 'z'))),
					tokenRule("NATURAL",  /* -> */ oneOrMore(range('0', '9'))),
					nontokenRule(" *",     /* -> */ zeroOrMore(' '))
			), verbatims);
			
			static final ParserRule[] parserRules = append(array(
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
		        namedRule("list",              "PARAMETERS", /* -> */ "PARAMETERS", ",", "VARIABLE"),
		        namedRule("list",              "PARAMETERS", /* -> */ "VARIABLE"),
		        namedRule("identifier",        "IDENTIFIER", /* -> */  "≀", "WORD"),
		        namedRule("identifier",        "IDENTIFIER", /* -> */  "≀", "VARIABLE"),
		        namedRule("identifier",        "IDENTIFIER", /* -> */  "WORD"),
		        namedRule("identifier",        "IDENTIFIER", /* -> */  "VARIABLE"),
		        namedRule("concatenation",     "WORD",       /* -> */  "WORD", "VARIABLE"),
		        namedRule("concatenation",     "WORD",       /* -> */  "`", "VARIABLE")
			), verbatimWordRules());
			
			static final ParserRule[] verbatimWordRules() {
				return Arrays.stream(verbatims).map(v -> namedRule("concatenation", "WORD", "`", v)).toArray(ParserRule[]::new);
			}
			
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
		    		
		    		if (left != null) {
		    			if (("<".equals(right[0].toString()) || "≤".equals(right[0].toString()))) {
		    				return $(append(left.getChildren().toArray(), right));
		    			}
		    		}
		    		
		    		if ("→".equals(right[0].toString())) {
		    			return rule(values[0], right[1]);
		    		}
		    		
		    		if ("∧".equals(right[0].toString())) {
		    			return $(values[0], "&", right[1]);
		    		}
		    		
		    		return $(append(array(values[0]), right));
		    	}
		    	
		    	return $(append(array(values[0]), values[1]));
		    }
		    
		    final Object verbatim(final Object[] values) {
		    	return values;
		    }
		    
		    final Object identifier(final Object[] values) {
		    	String result = join("", values);
		    	
		    	if (result.startsWith("`")) {
		    		return result.substring(1);
		    	} else if (2 <= result.length() && result.charAt(1) == '`') {
		    		return result.charAt(0) + result.substring(2);
		    	}
		    	
		    	return result;
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
				
				if (!thisIsBraced && Module.isSubstitution(composite)) {
					final Composite equalities = (Composite) children.get(1);
					
					resultBuilder.append(children.get(0)).append(
							cgroup(Tools.join(",", this.transform(equalities.getChildren()))));
					
					if (children.size() == 3) {
						final Composite indices = (Composite) children.get(2);
						
						resultBuilder.append(children.get(0)).append(
								sgroup(Tools.join(",", this.transform(indices.getChildren()))));
					}
					
					return resultBuilder.toString();
				}
				
				for (final Expression child : children) {
					if (thisIsBraced || child instanceof Symbol || isBracedComposite(child)) {
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
	
}
