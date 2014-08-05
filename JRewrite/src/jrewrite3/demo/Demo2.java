package jrewrite3.demo;

import static jrewrite3.core.ExpressionTools.*;
import static jrewrite3.demo.Demo2.ExpressionParser.$$;
import static net.sourceforge.aprog.tools.Tools.ignore;
import static net.sourceforge.aurochs.AurochsTools.input;
import static net.sourceforge.aurochs.LRParserTools.*;
import static net.sourceforge.aurochs.RegularTools.*;

import java.io.Serializable;

import jrewrite3.core.Expression;
import jrewrite3.core.Module;
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
		
		session.suppose("definition_of_M", $(forAll("X", "n"), $(
				$("n", "=", $("columnCount", " ", "X")),
				"->",
				$$("M X=1/nX(1_n)(1_nᵀ)"))));
		session.suppose("definition_of_V", $(forAll("X"), $$("V X=(X-(M X))(X-(M X))ᵀ")));
	}
	
	/**
	 * @param commandLineArguments
	 * <br>Unused
	 */
	public static final void main(final String[] commandLineArguments) {
		Session.printModule(MODULE, System.out, true, "");
	}
	
	/**
	 * @author codistmonk (creation 2014-08-06)
	 */
	public static final class ExpressionParser implements Serializable {
		
		private final LRParser mathParser;
		
		private Object result;
		
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
		
		public static final <E extends Expression> E $$(final CharSequence charSequence) {
			return instance.parse(charSequence);
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
				
				verbatimTokenRule("+",        /* -> */ '+'),
				
				verbatimTokenRule("=",        /* -> */ '='),
				
				verbatimTokenRule("-",        /* -> */ '-'),
				
				verbatimTokenRule("/",        /* -> */ '/'),
				
				verbatimTokenRule("ᵀ",        /* -> */ 'ᵀ'),
				
		        verbatimTokenRule("(",        /* -> */ '('),

		        verbatimTokenRule(")",        /* -> */ ')'),
		        
		        verbatimTokenRule(" ",        /* -> */ ' '),
		        
			};
			
			static final ParserRule[] parserRules = {
				
				leftAssociative('=', 10),
				
				leftAssociative('+', 100),
				
				leftAssociative('-', 100),
				
				leftAssociative('(', 150),
				
				leftAssociative("VARIABLE", 150),
				
				leftAssociative("INTEGER", 150),
				
				leftAssociative("EXPRESSION", 150),
				
				leftAssociative('/', 200),
				
				leftAssociative(' ', 300),
				
				leftAssociative('ᵀ', 400),
				
				namedRule("expression",              "ALL",         /* -> */ "EXPRESSION"),
				
				namedRule("expression",              "EXPRESSION",  /* -> */ "PEXPRESSION"),
				
				namedRule("parenthesizedExpression", "PEXPRESSION", /* -> */ '(', "EXPRESSION", ')'),
				
				namedRule("expression",              "EXPRESSION",  /* -> */ "EXPRESSION", '+', "EXPRESSION"),
				
				namedRule("expression",              "EXPRESSION",  /* -> */ "EXPRESSION", '-', "EXPRESSION"),
				
				namedRule("expression",              "EXPRESSION",  /* -> */ "EXPRESSION", "VARIABLE"),
				
				namedRule("expression",              "EXPRESSION",  /* -> */ "EXPRESSION", "INTEGER"),
				
				namedRule("expression",              "EXPRESSION",  /* -> */ "INTEGER", "EXPRESSION"),
				
				namedRule("expression",              "EXPRESSION",  /* -> */ "EXPRESSION", "PEXPRESSION"),
				
				namedRule("expression",              "EXPRESSION",  /* -> */ "PEXPRESSION", "EXPRESSION"),
				
				namedRule("expression",              "EXPRESSION",  /* -> */ "EXPRESSION", '/', "EXPRESSION"),
				
				namedRule("expression",              "EXPRESSION",  /* -> */ "EXPRESSION", 'ᵀ'),
				
				namedRule("expression",              "EXPRESSION",  /* -> */ "INTEGER"),
				
				namedRule("expression",              "EXPRESSION",  /* -> */ "VARIABLE"),
				
				namedRule("expression",              "EXPRESSION",  /* -> */ "1_", "VARIABLE"),
				
				namedRule("expression",              "EXPRESSION",  /* -> */ "1_", "INTEGER"),
				
				namedRule("expression",              "EXPRESSION",  /* -> */ "EXPRESSION", ' ', "EXPRESSION"),
				
				namedRule("expression",              "EXPRESSION",  /* -> */ "EXPRESSION", '=', "EXPRESSION"),
				
			};
			
			final Object expression(final Object[] values) {
				return $(values);
			}
			
			final Object parenthesizedExpression(final Object[] values) {
				return $(values[1]);
			}
			
		}
		
	}
	
}
