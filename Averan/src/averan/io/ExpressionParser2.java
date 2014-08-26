package averan.io;

import static averan.core.ExpressionTools.*;
import static java.util.Arrays.copyOfRange;
import static net.sourceforge.aprog.tools.Tools.cast;
import static net.sourceforge.aurochs2.core.LexerBuilder.*;
import static net.sourceforge.aurochs2.core.ParserBuilder.block;
import static net.sourceforge.aurochs2.core.ParserBuilder.Priority.Associativity.LEFT;
import static net.sourceforge.aurochs2.core.ParserBuilder.Priority.Associativity.NONE;
import static net.sourceforge.aurochs2.core.ParserBuilder.Priority.Associativity.RIGHT;
import static net.sourceforge.aurochs2.core.TokenSource.tokens;

import averan.core.Expression;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import net.sourceforge.aurochs2.core.Grammar.Rule;
import net.sourceforge.aurochs2.core.LRParser;
import net.sourceforge.aurochs2.core.Lexer;
import net.sourceforge.aurochs2.core.LexerBuilder;
import net.sourceforge.aurochs2.core.ParserBuilder;
import net.sourceforge.aurochs2.core.LexerBuilder.Union;

/**
 * @author codistmonk (creation 2014-08-06)
 */
public final class ExpressionParser2 implements Serializable {
	
	private final Lexer mathLexer = newMathLexer();
	
	private final LRParser mathParser = newMathParser(this.mathLexer);
	
	public final <E extends Expression> E parse(final CharSequence input) {
		final Object[] result = new Object[1];
		
		if (this.mathParser.parse(this.mathLexer.translate(tokens(input)), result)) {
			return $(result[0]);
		}
		
		throw new IllegalArgumentException("Syntax error");
	}
	
	/**
	 * {@value}.
	 */
	private static final long serialVersionUID = -5684274223441131221L;
	
	public static final Union DIGIT = union(range('0', '9'));
	
	public static final Union LETTER = union(union(range('a', 'z')), union(range('A', 'Z'), disjoin("Σℕℝ")));
	
	public static final Union GROUPING_OPERATOR = disjoin("(){}[]⟨⟩");
	
	public static final Union PREFIX_OPERATOR = disjoin("∀∃¬");
	
	public static final Union INFIX_OPERATOR = disjoin(",=+-*/_^<≤∧∈∩→");
	
	public static final Union POSTFIX_OPERATOR = disjoin("ᵀ");
	
	public static final Union OPERATOR = merge(GROUPING_OPERATOR, PREFIX_OPERATOR, INFIX_OPERATOR, POSTFIX_OPERATOR);
	
	public static final ExpressionParser2 instance = new ExpressionParser2();
	
	public static final <E extends Expression> E $$(final CharSequence input) {
		return instance.parse(input);
	}
	
	public static final Union disjoin(final CharSequence characters) {
		final int n = characters.length();
		final Object[] cs = new Character[n];
		
		for (int i = 0; i < n; ++i) {
			cs[i] = characters.charAt(i);
		}
		
		return new Union(cs);
	}
	
	public static final Union merge(final Object... symbolOrUnions) {
		final Collection<Object> protoresult = new ArrayList<>();
		boolean done = true;
		
		for (final Object object : symbolOrUnions) {
			final Union union = cast(Union.class, object);
			
			if (union != null) {
				protoresult.addAll(Arrays.asList(union.getSymbols()));
				done = false;
			} else {
				protoresult.add(object);
			}
		}
		
		return done ? union(protoresult.toArray()) : merge(protoresult.toArray());
	}
	
	public static final void defineGroupingOperations(final ParserBuilder parserBuilder, final Union groupingOperator) {
		for (final Object[] pair : groupingPairs(groupingOperator)) {
			final Object left = pair[0];
			final Object right = pair[1];
			final Rule rule = parserBuilder.define("Expression", left.toString(), "Expression", right.toString());
			
			if (left.equals('(') && right.equals(')')) {
				rule.setListener((r, data) -> {
					return $(data[1]);
				});
			} else {
				rule.setListener((r, data) -> {
					return $(data);
				});
			}
		}
	}
	
	public static final void definePrefixOperations(final ParserBuilder parserBuilder, final Union prefixOperator) {
		for (final Object op : prefixOperator.getSymbols()) {
			parserBuilder.define("Expression", op.toString(), "Expression").setListener((rule, data) -> $(data));
		}
	}
	
	public static final void defineInfixOperations(final ParserBuilder parserBuilder, final Union infixOperator) {
		for (final Object op : infixOperator.getSymbols()) {
			parserBuilder.define("Expression", "Expression", op.toString(), "Expression").setListener((rule, data) -> $(data));
		}
	}
	
	public static final void definePostfixOperations(final ParserBuilder parserBuilder, final Union postfixOperator) {
		for (final Object op : postfixOperator.getSymbols()) {
			parserBuilder.define("Expression", "Expression", op.toString()).setListener((rule, data) -> $(data));
		}
	}
	
	public static final Object[][] groupingPairs(final Union groupingOperator) {
		final Object[] symbols = groupingOperator.getSymbols();
		final int n = symbols.length;
		final Object[][] result = new Object[n / 2][];
		
		for (int i = 0; i < n; i += 2) {
			result[i / 2] = copyOfRange(symbols, i, i + 2);
		}
		
		return result;
	}
	
	public static final Lexer newMathLexer() {
		final LexerBuilder resultBuilder = new LexerBuilder();
		
		resultBuilder.generate("natural", oneOrMore(DIGIT));
		resultBuilder.generate("variable", LETTER);
		for (final Object symbol : OPERATOR.getSymbols()) {
			resultBuilder.generate(symbol.toString(), symbol);
		}
		resultBuilder.generate("string", '\'', "characters", '\'');
		resultBuilder.define("characters", union(DIGIT, LETTER, OPERATOR, ' ', sequence('\\', '\'')), "characters");
		resultBuilder.define("characters");
		resultBuilder.skip(oneOrMore(' '));
		
		return resultBuilder.newLexer();
	}
	
	public static final LRParser newMathParser(final Lexer mathLexer) {
		final ParserBuilder resultBuilder = new ParserBuilder(mathLexer);
		
		resultBuilder.define("()", "Expression").setListener((rule, data) -> data[0]);
		defineGroupingOperations(resultBuilder, GROUPING_OPERATOR);
		definePrefixOperations(resultBuilder, PREFIX_OPERATOR);
		defineInfixOperations(resultBuilder, INFIX_OPERATOR);
		definePostfixOperations(resultBuilder, POSTFIX_OPERATOR);
		resultBuilder.define("Expression", "-", "Expression").setListener((rule, data) -> $(data));
		resultBuilder.define("Expression", "Expression", "Expression").setListener((rule, data) -> $(data));
		resultBuilder.define("Expression", "natural").setListener((rule, data) -> $(data));
		resultBuilder.define("Expression", "variable").setListener((rule, data) -> $(data));
		resultBuilder.define("Expression", "string").setListener((rule, data) -> $(data));
		
		for (final Object[] pair : groupingPairs(GROUPING_OPERATOR)) {
			final String left = pair[0].toString();
			final String right = pair[1].toString();
			
			resultBuilder.resolveConflictWith(block("Expression", "Expression"), block(left, "Expression", right));
			resultBuilder.resolveConflictWith(left, block("Expression", "-", "Expression"), right);
		}
		
		for (final Object op1 : PREFIX_OPERATOR.getSymbols()) {
			for (final Object op2 : PREFIX_OPERATOR.getSymbols()) {
				resultBuilder.resolveConflictWith(block(op1.toString(), "Expression"),
						block(op2.toString(), "Expression"));
			}
		}
		for (final Object op2 : PREFIX_OPERATOR.getSymbols()) {
			resultBuilder.resolveConflictWith(block("-", "Expression"),
					block(op2.toString(), "Expression"));
		}
		
		for (final Object op1 : INFIX_OPERATOR.getSymbols()) {
			for (final Object op2 : PREFIX_OPERATOR.getSymbols()) {
				resultBuilder.resolveConflictWith("Expression", op1.toString(),
						block("Expression", block(op2.toString(), "Expression")));
			}
		}
		
		for (final Object op1 : PREFIX_OPERATOR.getSymbols()) {
			for (final Object[] pair : groupingPairs(GROUPING_OPERATOR)) {
				resultBuilder.resolveConflictWith(block(op1.toString(), "Expression"), block(pair[0].toString(), "Expression", pair[1].toString()));
			}
		}
		for (final Object[] pair : groupingPairs(GROUPING_OPERATOR)) {
			resultBuilder.resolveConflictWith(block("-", "Expression"), block(pair[0].toString(), "Expression", pair[1].toString()));
		}
		
		for (final Object op1 : INFIX_OPERATOR.getSymbols()) {
			for (final Object[] pair : groupingPairs(GROUPING_OPERATOR)) {
				resultBuilder.resolveConflictWith("Expression", op1.toString(), block("Expression", block(pair[0].toString(), "Expression", pair[1].toString())));
			}
		}
		
		for (final Object op : PREFIX_OPERATOR.getSymbols()) {
			resultBuilder.resolveConflictWith(block("Expression", "Expression"), block(op.toString(), "Expression"));
		}
		
		resultBuilder.setPriority(200, NONE, "-", "Expression");
		for (final Object op : PREFIX_OPERATOR.getSymbols()) {
			resultBuilder.setPriority(200, LEFT, op.toString(), "Expression");
		}
		for (final Object op : INFIX_OPERATOR.getSymbols()) {
			resultBuilder.setPriority(200, LEFT, "Expression", op.toString(), "Expression");
		}
		for (final Object op : POSTFIX_OPERATOR.getSymbols()) {
			resultBuilder.setPriority(300, RIGHT, "Expression", op.toString());
		}
		resultBuilder.setPriority(200, LEFT, "Expression", "natural");
		resultBuilder.setPriority(200, LEFT, "Expression", "variable");
		resultBuilder.setPriority(200, LEFT, "Expression", "string");
		resultBuilder.setPriority(200, LEFT, "Expression", "Expression");
		
		return resultBuilder.newParser();
	}
	
}
