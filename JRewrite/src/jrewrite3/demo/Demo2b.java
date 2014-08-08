package jrewrite3.demo;

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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jrewrite3.core.Expression;
import jrewrite3.core.Module;
import jrewrite3.core.Session;
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
		
		session.suppose("definition_of_¬", $$("∀P ¬P = (P→`false)"));
		session.suppose("definition_of_∃", $$("∀P,x (∃x (P x)) = ¬(∀y ¬(P y))"));
		
		session.printTo(System.out, true);
	}
	
	/**
	 * @param commandLineArguments
	 * <br>Unused
	 */
	public static final void main(final String[] commandLineArguments) {
//		Tools.debugPrint(LRParserTools.newParser(MathParser.class).parse(input("1  +1")));
		Tools.debugPrint(ExpressionParser.instance.parse("1 +2"));
		Tools.debugPrint(ExpressionParser.instance.parse("A+B+C"));
		Tools.debugPrint(ExpressionParser.instance.parse("(AB)+(CD)"));
		Tools.debugPrint($$("¬P = (P → `false)"));
		Tools.debugPrint($$("¬(∀y ¬(P y))"));
		Tools.debugPrint($$("∀P (∃x (P x)) = ¬(∀y ¬(P y))"));
//		Tools.debugPrint($$("P=(P→≀false)"));
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
					nontokenRule('_',     /* -> */ zeroOrMore(' '))
			), "+", "-", "/", "=", "(", ")", "{", "}", "[", "]", ",", "∀", "∃", "¬", "→", "`", "≀"/*, "∧", "∈", "Σ", "∩", "≤", "<", "_", "^", "ᵀ"*/);
			
			static final ParserRule[] parserRules = {
//				leftAssociative("∧", 5),
				leftAssociative("→", 5),
				leftAssociative(",", 8),
				leftAssociative("=", 10),
//				leftAssociative("∈", 50),
				leftAssociative("(", 100),
				leftAssociative("{", 100),
				leftAssociative("[", 100),
				leftAssociative("+", 100),
				leftAssociative("-", 100),
				leftAssociative("/", 200),
				leftAssociative("¬", 300),
				leftAssociative("∀", 300),
				leftAssociative("∃", 300),
				leftAssociative("`", 300),
				leftAssociative("≀", 300),
				leftAssociative("VARIABLE", 350),
				leftAssociative("NATURAL", 350),
				
		        namedRule("expression",        "ALL",        /* -> */  "EXPRESSION"),
		        namedRule("expression",        "EXPRESSION", /* -> */  "∀", "PARAMETERS", "EXPRESSION"),
		        namedRule("expression",        "EXPRESSION", /* -> */  "∃", "VARIABLE", "EXPRESSION"),
		        namedRule("expression",        "EXPRESSION", /* -> */  "¬", "EXPRESSION"),
		        namedRule("operation",         "EXPRESSION", /* -> */  "EXPRESSION", "OPERATION"),
		        namedRule("verbatim",          "OPERATION",  /* -> */  "+", "EXPRESSION"),
		        namedRule("verbatim",          "OPERATION",  /* -> */  "-", "EXPRESSION"),
		        namedRule("verbatim",          "OPERATION",  /* -> */  "/", "EXPRESSION"),
		        namedRule("verbatim",          "OPERATION",  /* -> */  "=", "EXPRESSION"),
		        namedRule("verbatim",          "OPERATION",  /* -> */  "→", "EXPRESSION"),
//		        namedRule("verbatim",          "OPERATION",  /* -> */  "∧", "EXPRESSION"),
//		        namedRule("verbatim",          "OPERATION",  /* -> */  "∈", "EXPRESSION"),
//		        namedRule("verbatim",          "OPERATION",  /* -> */  ",", "EXPRESSION"),
		        namedRule("verbatim",          "OPERATION",  /* -> */  "EXPRESSION"),
		        namedRule("grouping",          "EXPRESSION", /* -> */  "(", "EXPRESSION", ")"),
		        namedRule("expression",        "EXPRESSION", /* -> */  "{", "EXPRESSION", "}"),
		        namedRule("expression",        "EXPRESSION", /* -> */  "[", "EXPRESSION", "]"),
		        namedRule("expression",        "EXPRESSION", /* -> */  "NATURAL"),
		        namedRule("expression",        "EXPRESSION", /* -> */  "IDENTIFIER"),
		        namedRule("concatenation",     "IDENTIFIER", /* -> */  "≀", "WORD"),
		        namedRule("concatenation",     "IDENTIFIER", /* -> */  "≀", "`", "WORD"),
		        namedRule("concatenation",     "IDENTIFIER", /* -> */  "`", "WORD"),
		        namedRule("concatenation",     "IDENTIFIER", /* -> */  "VARIABLE"),
		        namedRule("concatenation",     "WORD",       /* -> */  "WORD", "VARIABLE"),
		        namedRule("concatenation",     "WORD",       /* -> */  "VARIABLE"),
		        namedRule("list",              "PARAMETERS",       /* -> */ "PARAMETERS", ",", "VARIABLE"),
		        namedRule("list",              "PARAMETERS",       /* -> */ "VARIABLE"),
				
			};
			
		    final Object expression(final Object[] values) {
		    	if ("∀".equals(values[0].toString())) {
		    		return $(copyOfRange(values, 1, values.length));
		    	}
		    	
		        return $(values);
		    }
			
		    final Object operation(final Object[] values) {
		    	if (values[1] instanceof Object[]) {
		    		return $(append(array(values[0]), (Object[])values[1]));
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
	
}
