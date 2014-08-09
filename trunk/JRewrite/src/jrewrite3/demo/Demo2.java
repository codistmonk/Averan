package jrewrite3.demo;

import static java.util.Arrays.copyOfRange;
import static jrewrite3.core.ExpressionTools.*;
import static jrewrite3.demo.Demo2.ExpressionParser.$$;
import static jrewrite3.modules.Standard.IDENTITY;
import static net.sourceforge.aprog.tools.Tools.append;
import static net.sourceforge.aprog.tools.Tools.array;
import static net.sourceforge.aprog.tools.Tools.cast;
import static net.sourceforge.aprog.tools.Tools.ignore;
import static net.sourceforge.aprog.tools.Tools.join;
import static net.sourceforge.aurochs.AurochsTools.input;
import static net.sourceforge.aurochs.LRParserTools.*;
import static net.sourceforge.aurochs.RegularTools.*;

import java.io.Serializable;
import java.util.List;

import jrewrite3.core.Composite;
import jrewrite3.core.Expression;
import jrewrite3.core.Module;
import jrewrite3.core.Module.Symbol;
import jrewrite3.core.Session;
import jrewrite3.modules.Standard;
import net.sourceforge.aprog.tools.IllegalInstantiationException;
import net.sourceforge.aurochs.AbstractLRParser.GeneratedToken;
import net.sourceforge.aurochs.AbstractLRParser.Listener;
import net.sourceforge.aurochs.LRParser;
import net.sourceforge.aurochs.LRParserTools;
import net.sourceforge.aurochs.AbstractLRParser.ReductionEvent;
import net.sourceforge.aurochs.AbstractLRParser.UnexpectedSymbolErrorEvent;
import net.sourceforge.aurochs.LRParserTools.LexerRule;
import net.sourceforge.aurochs.LRParserTools.ParserRule;

/**
 * @author codistmonk (creation 2014-08-04)
 */
public final class Demo2 {
	
	private Demo2() {
		throw new IllegalInstantiationException();
	}
	
	public static final Module MODULE = new Module(Standard.MODULE);
	
	static {
		final Session session = new Session(MODULE);
		
		// TODO define ∈, ᵀ
		session.suppose("definition_of_¬", $$("∀P ¬P = (P→≀false)"));
		session.suppose("definition_of_∃", $$("∀P,x (∃x P x) = ¬(∀y ¬(P y))"));
		session.suppose("definition_of_∩", $$("∀A,B,x (x∈A∩B) = (x∈A ∧ x∈B)"));
//		session.suppose("definition_of_Σ", $$("∀f ((Σ 0) f) = (f 0) ∧ (∀n (0<n) → ((Σ n) f) = (f n)+((Σ (n-1)) f))"));
		session.suppose("definition_of_Σ", $$("∀e,a,b,i ((b<a)→((Σi_a,b e)=0)) ∧ ((a=b)→((Σi_a,b e)=e{i=a})) ∧ ((a<b)→((Σi_a,b e)=(Σi_a,b-1 e)+e{i=b}))"));
		session.suppose("definition_of_matrix_product", $$("∀A,B,C,m,p,n,f (A∈≀M_m,p ∧ B∈≀M_p,n) → (C∈≀M_m,n ∧ ∀i,j (0≤i<m ∧ 0≤j<n ∧ ∀k (0≤k<p) → (((f i) j) k) = A_i,kB_k,j) → C_i,j=(Σ (p-1)) f)"));
		
		session.suppose("definition_of_≀M", $$("∀M,m,n (M∈≀M_m,n → ∀i,j (0≤i<m ∧ 0≤j<n) → M_i,j∈ℝ)"));
		session.suppose("definition_of_≀C", $$("∀M,n M∈≀C_n = ∃m M∈≀M_m,n"));
		session.suppose("definition_of_≀R", $$("∀M,m M∈≀R_m = ∃n M∈≀M_m,n"));
		
		// TODO prove
		session.suppose("columnCount", $$("∀X,m,n X∈≀M_m,n → X∈≀C_n"));
		session.suppose("transposition_of_product", $$("∀X,Y (XY)ᵀ = YᵀXᵀ"));
		session.suppose("transposition_of_subtraction", $$("∀X,Y (X-Y)ᵀ = Xᵀ-Yᵀ"));
		session.suppose("product_of_subtractions", $$("∀A,B,C,D ((A-B)(C-D)) = (((AC)-(AD))-(BC))+(BD)"));
		
//		final Symbol m = session.getCurrentContext().getModule().parameter("m");
//		final Symbol n = session.getCurrentContext().getModule().parameter("n");
		
		session.suppose("definition_of_U", $$("∀n,i,j (0≤i<n ∧ 0≤j<n) → (U n)_i,j=1/n"));
		session.suppose("definition_of_M", $$("∀X,n X∈≀C_n → (M X) = X(U n)"));
		session.suppose("definition_of_V", $$("∀X,m,n X∈≀M_m,n → (V X) = (X-(M X))(X-(M X))ᵀ"));
		
		session.claim("simplified_definition_of_V", $$("∀X,m,n X∈≀M_m,n → (V X) = (XXᵀ)-(M X)(M X)ᵀ"));
		
		{
			session.introduce();
			session.introduce();
			session.introduce();
			session.introduce("type_of_X");
			
			final Symbol x = session.getParameter("X");
			final Symbol m = session.getParameter("m");
			final Symbol n = session.getParameter("n");
			final Expression xt = $(x, "ᵀ");
			final Expression mx = $("M", " ", x);
			final Expression mxt = $(mx, "ᵀ");
			final Expression xmxt = $(x, mxt);
			final Expression mxxt = $(mx, xt);
			final Expression mxmxt = $(mx, mxt);
			
			session.bind("definition_of_V", x, m, n);
			session.apply("#1", "type_of_X");
			session.bind("transposition_of_subtraction", x, mx);
			session.rewrite("#2", "#3");
			session.bind("product_of_subtractions", x, mx, xt, mxt);
			session.rewrite("#4", "#5");
			session.claim($(xmxt, "=", mxmxt));
			{
				session.bind(IDENTITY, xmxt);
				session.bind("definition_of_M", x, n);
				session.bind("columnCount", x, m, n);
				session.apply("#2", "type_of_X");
				session.apply("#1", "#3");
				session.rewrite("#0", "#4", 1);
//				
			}
		}
		
		session.new Exporter(true).exportSession();
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
			
			static final LexerRule[] lexerRules = {
				
				tokenRule(        "VARIABLE", /* -> */ union(range('A', 'Z'), range('a', 'z'))),
				
				tokenRule(        "INTEGER",  /* -> */ oneOrMore(range('0', '9'))),
				
				tokenRule(        "1_",       /* -> */ string("1_")),
		        
		        verbatimTokenRule("→",        /* -> */ '→'),
		        
		        verbatimTokenRule("∧",        /* -> */ '∧'),
		        
				verbatimTokenRule("=",        /* -> */ '='),
				
				verbatimTokenRule("+",        /* -> */ '+'),
				
				verbatimTokenRule("-",        /* -> */ '-'),
				
				verbatimTokenRule("/",        /* -> */ '/'),
				
				verbatimTokenRule("ᵀ",        /* -> */ 'ᵀ'),
				
		        verbatimTokenRule("(",        /* -> */ '('),
		        
		        verbatimTokenRule(")",        /* -> */ ')'),
		        
		        verbatimTokenRule("[",        /* -> */ '['),
		        
		        verbatimTokenRule("]",        /* -> */ ']'),
		        
		        verbatimTokenRule("{",        /* -> */ '{'),
		        
		        verbatimTokenRule("}",        /* -> */ '}'),
		        
		        verbatimTokenRule(" ",        /* -> */ ' '),
		        
		        verbatimTokenRule("_",        /* -> */ '_'),
		        
		        verbatimTokenRule(",",        /* -> */ ','),
		        
		        verbatimTokenRule("≀",        /* -> */ '≀'),
		        
		        verbatimTokenRule("≤",        /* -> */ '≤'),
		        
		        verbatimTokenRule("<",        /* -> */ '<'),
		        
		        verbatimTokenRule("∈",        /* -> */ '∈'),
		        
		        verbatimTokenRule("ℕ",        /* -> */ 'ℕ'),
		        
		        verbatimTokenRule("ℝ",        /* -> */ 'ℝ'),
		        
		        verbatimTokenRule("∃",        /* -> */ '∃'),
		        
		        verbatimTokenRule("∀",        /* -> */ '∀'),
		        
		        verbatimTokenRule("¬",        /* -> */ '¬'),
		        
		        verbatimTokenRule("∩",        /* -> */ '∩'),
		        
		        verbatimTokenRule("Σ",        /* -> */ 'Σ'),

		        nontokenRule(     '.',        /* -> */ zeroOrMore('.')),
		        
			};
			
			static final ParserRule[] parserRules = {
				
				leftAssociative(' ', 5),
				
				leftAssociative('∧', 5),
				
				leftAssociative('→', 5),
				
				leftAssociative(',', 8),
				
				leftAssociative('=', 10),
				
				leftAssociative('∈', 50),
				
				leftAssociative('≤', 50),
				
				leftAssociative('<', 50),
				
				leftAssociative('+', 100),
				
				leftAssociative('-', 100),
				
				leftAssociative('∩', 125),
				
				leftAssociative('(', 150),
				
				leftAssociative('[', 150),
				
				leftAssociative('{', 150),
				
				leftAssociative("VARIABLE", 150),
				
				leftAssociative("INTEGER", 150),
				
				leftAssociative('Σ', 150),
				
				leftAssociative('ℕ', 150),
				
				leftAssociative('ℝ', 150),
				
				leftAssociative("1_", 150),
				
				leftAssociative('/', 155),
				
				leftAssociative('ᵀ', 400),
				
				leftAssociative('_', 400),
				
				leftAssociative('≀', 500),
				
				leftAssociative('∃', 400),
				
				leftAssociative('¬', 400),
				
				leftAssociative('∀', 400),
				
				namedRule("expression",              "ALL",        /* -> */ "EXPRESSION"),
				
				namedRule("expression",              "EXPRESSION", /* -> */ '∃', "VARIABLE"),
				
				namedRule("expression",              "EXPRESSION", /* -> */ '∃', "VARIABLE", ' '),
				
				namedRule("forall",                  "EXPRESSION", /* -> */ '∀', "VARIABLES"),
				
				namedRule("forall",                  "EXPRESSION", /* -> */ '∀', "VARIABLES", ' '),
				
				namedRule("forall",                  "VARIABLES",  /* -> */ "VARIABLE", ',', "VARIABLES"),
				
				namedRule("forall",                  "VARIABLES",  /* -> */ "VARIABLE"),
				
				namedRule("expression",              "EXPRESSION", /* -> */ '¬', "EXPRESSION"),
				
				namedRule("expression",              "EXPRESSION", /* -> */ "EXPRESSION", 'ᵀ'),
				
				namedRule("parenthesizedExpression", "EXPRESSION", /* -> */ '(', "EXPRESSION", ')'),
				
				namedRule("expression",              "EXPRESSION", /* -> */ '[', "EXPRESSION", ']'),
				
				namedRule("expression",              "EXPRESSION", /* -> */ '{', "EXPRESSION", '}'),
				
				namedRule("operatedExpression",      "EXPRESSION", /* -> */ "EXPRESSION", "OPERATION"),
				
				namedRule("operation",               "OPERATION",  /* -> */ " = ", "EXPRESSION"),
				
				namedRule("operation",               "OPERATION",  /* -> */ " → ", "EXPRESSION"),
				
				namedRule("operation",               "OPERATION",  /* -> */ " ∧ ", "EXPRESSION"),
				
				namedRule("operation",               "OPERATION",  /* -> */ '∈', "EXPRESSION"),
				
				namedRule("operation",               "OPERATION",  /* -> */ '≤', "EXPRESSION"),
				
				namedRule("operation",               "OPERATION",  /* -> */ '<', "EXPRESSION"),
				
				namedRule("operation",               "OPERATION",  /* -> */ '+', "EXPRESSION"),
				
				namedRule("operation",               "OPERATION",  /* -> */ '-', "EXPRESSION"),
				
				namedRule("operation",               "OPERATION",  /* -> */ '/', "EXPRESSION"),
				
				namedRule("operation",               "OPERATION",  /* -> */ ' ', "EXPRESSION"),
				
				namedRule("operation",               "OPERATION",  /* -> */ '∩', "EXPRESSION"),
				
				namedRule("operation",               "OPERATION",  /* -> */ ',', "EXPRESSION"),
				
				namedRule("operation",               "OPERATION",  /* -> */ "EXPRESSION"),
				
				namedRule("expression",              "EXPRESSION", /* -> */ "INTEGER"),
				
				namedRule("expression",              "EXPRESSION", /* -> */ "VARIABLE"),
				
				namedRule("operation",               "OPERATION",  /* -> */ '_', "INDEX"),
				
				namedRule("operation",               "OPERATION",  /* -> */ '_', "INDEX", ',', "INDEX"),
				
				namedRule("expression",              "INDEX",      /* -> */ "VARIABLE"),
				
				namedRule("expression",              "INDEX",      /* -> */ "INTEGER"),
				
				namedRule("expression",              "EXPRESSION", /* -> */ 'Σ'),
				
				namedRule("expression",              "EXPRESSION", /* -> */ 'ℕ'),
				
				namedRule("expression",              "EXPRESSION", /* -> */ 'ℝ'),
				
				namedRule("expression",              "EXPRESSION", /* -> */ "1_", "VARIABLE"),
				
				namedRule("expression",              "EXPRESSION", /* -> */ "1_", "INTEGER"),
				
				namedRule("identifier",              "EXPRESSION", /* -> */ '≀', "IDENTIFIER"),
				
				namedRule("identifier",              "IDENTIFIER", /* -> */ "VARIABLE", "IDENTIFIER"),
				
				namedRule("identifier",              "IDENTIFIER", /* -> */ "VARIABLE"),
				
				namedRule("binaryOperation",         " = ",        /* -> */ " +", '=', " +"),
				
				namedRule("binaryOperation",         " = ",        /* -> */ '=', " +"),
				
				namedRule("binaryOperation",         " = ",        /* -> */ " +", '='),
				
				namedRule("binaryOperation",         " = ",        /* -> */ '='),
				
				namedRule("binaryOperation",         " → ",        /* -> */ " +", '→', " +"),
				
				namedRule("binaryOperation",         " → ",        /* -> */ '→', " +"),
				
				namedRule("binaryOperation",         " → ",        /* -> */ " +", '→'),
				
				namedRule("binaryOperation",         " → ",        /* -> */ '→'),
				
				namedRule("binaryOperation",         " ∧ ",        /* -> */ " +", '∧', " +"),
				
				namedRule("binaryOperation",         " ∧ ",        /* -> */ '∧', " +"),
				
				namedRule("binaryOperation",         " ∧ ",        /* -> */ " +", '∧'),
				
				namedRule("binaryOperation",         " ∧ ",        /* -> */ '∧'),
				
				namedRule("spaces",                  " +",         /* -> */ ' ', " +"),
				
				namedRule("spaces",                  " +",         /* -> */ ' '),
				
			};
			
			public static final Object[] trim(final Object[] values) {
				int i = 0;
				int j = values.length;
				
				while (i < j && "".equals(values[i].toString().trim())) {
					++i;
				}
				
				while (i < j && "".equals(values[j - 1].toString().trim())) {
					--j;
				}
				
				return copyOfRange(values, i, j);
			}
			
			final Object expression(final Object[] values) {
				if (1 == values.length && values[0] instanceof List) {
					return values[0];
				}
				
				return $(trim(values));
			}
			
			final Object operatedExpression(final Object[] values) {
				return $(append(array(values[0]), (Object[]) values[1]));
			}
			
			final Object forall(final Object[] values) {
				if (values[0].equals('∀')) {
					return forAll((Object[]) values[1]);
				}
				
				if (3 == values.length && values[1].equals(',')) {
					return append(array(values[0]), (Object[]) values[2]);
				}
				
				return values;
			}
			
			final Object binaryOperation(final Object[] values) {
				return trim(values)[0];
			}
			
			final Object spaces(final Object[] values) {
				return "";
			}
			
			final Object operation(final Object[] values) {
				if (0 < values.length && values[0].equals('→')) {
					values[0] = "->";
				}
				
				if (values.length == 2) {
					if (values[0].equals('≤') || values[0].equals('<')) {
						final Composite values1 = cast(Composite.class, values[1]);
						
						if (values1 != null && 3 <= values1.getChildren().size()) {
							final String rightOperator = values1.getChildren().get(1).toString();
							
							if ("≤".equals(rightOperator) || "<".equals(rightOperator)) {
								return append(array(values[0]), values1.getChildren().toArray());
							}
						}
					}
					
					if (values[0].equals(',')) {
						return array(values[1]);
					}
				}
				
				return values;
			}
			
			final Object parenthesizedExpression(final Object[] values) {
				return values[1] instanceof List ? values[1] : $(values[1]);
			}
			
			final Object identifier(final Object[] values) {
				return join("", values);
			}
			
		}
		
	}
	
}