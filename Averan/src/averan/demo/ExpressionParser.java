package averan.demo;

import static averan.core.ExpressionTools.$;
import static averan.core.ExpressionTools.facts;
import static averan.core.ExpressionTools.rule;
import static java.util.Arrays.copyOfRange;
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

import averan.core.Composite;
import averan.core.Expression;
import net.sourceforge.aurochs.LRParser;
import net.sourceforge.aurochs.LRParserTools;
import net.sourceforge.aurochs.AbstractLRParser.GeneratedToken;
import net.sourceforge.aurochs.AbstractLRParser.Listener;
import net.sourceforge.aurochs.AbstractLRParser.ReductionEvent;
import net.sourceforge.aurochs.AbstractLRParser.UnexpectedSymbolErrorEvent;
import net.sourceforge.aurochs.LRParserTools.LexerRule;
import net.sourceforge.aurochs.LRParserTools.ParserRule;

/**
 * @author codistmonk (creation 2014-08-06)
 */
public final class ExpressionParser implements Serializable {
	
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
		
		static final Object[] verbatims = {
			"+", "-", "/", "=", "(", ")", "{", "}", "[", "]",
			",", "∀", "∃", "¬", "→", "`", "≀", "∧", "∈", "∩",
			"<", "≤", "Σ", "_", "^", "ℕ", "ℝ", "ᵀ", "⟨", "⟩",
		};
		
		static final LexerRule[] lexerRules = appendVerbatims(array(
				tokenRule("VARIABLE", /* -> */ union(range('A', 'Z'), range('a', 'z'), range('Α', 'Ω'), range('α', 'ω'))),
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
			leftAssociative("⟨", 100),
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
	        namedRule("expression",        "EXPRESSION", /* -> */  "∃", "IDENTIFIER", "EXPRESSION"),
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
	        namedRule("expression",        "EXPRESSION", /* -> */  "⟨", "EXPRESSION", "⟩"),
	        namedRule("expression",        "EXPRESSION", /* -> */  "NATURAL"),
	        namedRule("expression",        "EXPRESSION", /* -> */  "IDENTIFIER"),
	        namedRule("expression",        "EXPRESSION", /* -> */  "Σ"),
	        namedRule("expression",        "EXPRESSION", /* -> */  "ℕ"),
	        namedRule("expression",        "EXPRESSION", /* -> */  "ℝ"),
	        namedRule("list",              "PARAMETERS", /* -> */ "PARAMETERS", ",", "IDENTIFIER"),
	        namedRule("list",              "PARAMETERS", /* -> */ "IDENTIFIER"),
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
