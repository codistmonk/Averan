package averan.io;

import static averan.core.ExpressionTools.*;
import static java.util.Arrays.copyOfRange;
import static net.sourceforge.aprog.tools.Tools.cast;
import static net.sourceforge.aprog.tools.Tools.ignore;
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
import java.util.HashSet;
import java.util.List;

import net.sourceforge.aurochs2.core.Grammar.Rule;
import net.sourceforge.aurochs2.core.LRParser;
import net.sourceforge.aurochs2.core.Lexer;
import net.sourceforge.aurochs2.core.LexerBuilder;
import net.sourceforge.aurochs2.core.ParserBuilder;
import net.sourceforge.aurochs2.core.LexerBuilder.Union;
import net.sourceforge.aurochs2.core.ParserBuilder.Priority.Associativity;

/**
 * @author codistmonk (creation 2014-08-06)
 */
public final class ExpressionParser implements Serializable {
	
	private final Lexer mathLexer = newMathLexerBuilder().newLexer();
	
	private final LRParser mathParser = newMathParserBuilder(this.mathLexer).newParser();
	
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
	
	public static final Union LETTER = union(union(range('a', 'z')), union(range('A', 'Z')), disjoin("Σℕℝ"));
	
	public static final Union GROUPING_OPERATOR = disjoin("(){}[]⟨⟩");
	
	public static final Union SPECIAL_OPERATOR = disjoin("∀");
	
	public static final Union PREFIX_OPERATOR = disjoin("∃¬");
	
	public static final Union INFIX_OPERATOR = disjoin(",=+-*/_^<≤∧∈∩→");
	
	public static final Union POSTFIX_OPERATOR = disjoin("ᵀ");
	
	public static final Union OPERATOR = merge(GROUPING_OPERATOR, SPECIAL_OPERATOR, PREFIX_OPERATOR, INFIX_OPERATOR, POSTFIX_OPERATOR);
	
	public static final ExpressionParser instance = new ExpressionParser();
	
	public static final <E extends Expression> E $$(final CharSequence input) {
		return instance.parse(input);
	}
	
	public static final Sequence string(final CharSequence characters) {
		final int n = characters.length();
		final Object[] symbols = new Object[n];
		
		for (int i = 0; i < n; ++i) {
			symbols[i] = characters.charAt(i);
		}
		
		return sequence(symbols);
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
				rule.setAction((r, data) -> {
					return $(data[1]);
				});
			} else {
				rule.setAction((r, data) -> {
					return $(data);
				});
			}
		}
	}
	
	public static final void definePrefixOperations(final ParserBuilder parserBuilder, final Union prefixOperator) {
		for (final Object op : prefixOperator.getSymbols()) {
			parserBuilder.define("Expression", op.toString(), "Expression").setAction((rule, data) -> $(data));
		}
	}
	
	public static final void defineInfixOperations(final ParserBuilder parserBuilder, final Union infixOperator) {
		for (final Object op : infixOperator.getSymbols()) {
			parserBuilder.define("Expression", "Expression", op.toString(), "Expression").setAction((rule, data) -> $(data));
		}
	}
	
	public static final void definePostfixOperations(final ParserBuilder parserBuilder, final Union postfixOperator) {
		for (final Object op : postfixOperator.getSymbols()) {
			parserBuilder.define("Expression", "Expression", op.toString()).setAction((rule, data) -> $(data));
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
	
	public static final LexerBuilder newMathLexerBuilder() {
		final LexerBuilder result = new LexerBuilder();
		
		result.generate("natural", oneOrMore(DIGIT));
		result.generate("variable", LETTER);
		result.generate("variable", '≀', LETTER);
		for (final Object symbol : OPERATOR.getSymbols()) {
			result.generate(symbol.toString(), symbol);
		}
		result.generate("string", '\'', "characters", '\'');
		result.define("characters", union(DIGIT, LETTER, OPERATOR, ' ', "escaped_character"), "characters");
		result.define("characters");
		result.define("escaped_character", union(string("\\'"), string("\\\\")))
				.setAction((rule, data) -> data[0].toString().charAt(1));
		result.skip(oneOrMore(' '));
		
		return result;
	}
	
	public static final ParserBuilder newMathParserBuilder(final Lexer mathLexer) {
		final ParserBuilder result = new ParserBuilder(mathLexer);
		
		result.define("()", "Expression").setAction((rule, data) -> data[0]);
		result.define("Expression", "∀", "Identifiers", "Expression").setAction((rule, data) -> $(data[1], data[2]));
		defineGroupingOperations(result, GROUPING_OPERATOR);
		definePrefixOperations(result, PREFIX_OPERATOR);
		defineInfixOperations(result, INFIX_OPERATOR);
		definePostfixOperations(result, POSTFIX_OPERATOR);
		result.define("Expression", "-", "Expression").setAction((rule, data) -> $(data));
		result.define("Expression", "Expression", "Expression").setAction((rule, data) -> $(data));
		result.define("Expression", "natural").setAction((rule, data) -> $(data));
		result.define("Expression", "variable").setAction((rule, data) -> $(data));
		result.define("Expression", "string").setAction(ExpressionParser::unquote);
		result.define("Identifier", "string").setAction(ExpressionParser::unquote);
		result.define("Identifier", "variable").setAction((rule, data) -> $(data));
		result.define("Identifiers", "Identifiers", ",", "Identifier").setAction(ExpressionParser::flatten);
		result.define("Identifiers", "Identifier").setAction(ExpressionParser::flatten);
		
		resolveConflicts(result);
		
		return result;
	}
	
	private static final void resolveConflicts(final ParserBuilder resultBuilder) {
		for (final Object op : INFIX_OPERATOR.getSymbols()) {
			resultBuilder.resolveConflictWith("∀", "Identifiers", block("Expression", op.toString(), "Expression"));
			resultBuilder.resolveConflictWith("Expression", op.toString(), block("Expression", block("∀", "Identifiers", "Expression")));
		}
		
		final List<Object> complexTokens = Arrays.asList("string", "natural", "variable");
		
		for (final Object complexToken : complexTokens) {
			resultBuilder.resolveConflictWith("∀", "Identifiers", block("Expression", complexToken));
		}
		
		for (final Object[] pair : groupingPairs(GROUPING_OPERATOR)) {
			final String left = pair[0].toString();
			final String right = pair[1].toString();
			
			resultBuilder.resolveConflictWith(block("Expression", "Expression"), block(left, "Expression", right));
			resultBuilder.resolveConflictWith(left, block("Expression", "-", "Expression"), right);
			resultBuilder.resolveConflictWith("∀", "Identifiers", block("Expression", block(left, "Expression", right)));
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
		
		final Collection<Object> done = new HashSet<>();
		
		resultBuilder.setPriority(200, NONE, "-", "Expression");
		setBinaryOperatorPriority("=", 150, NONE, resultBuilder, done);
		setBinaryOperatorPriority("∧", 100, LEFT, resultBuilder, done);
		setBinaryOperatorPriority("_", 250, RIGHT, resultBuilder, done);
		for (final Object op : PREFIX_OPERATOR.getSymbols()) {
			resultBuilder.setPriority(200, LEFT, op.toString(), "Expression");
		}
		for (final Object op : INFIX_OPERATOR.getSymbols()) {
			setBinaryOperatorPriority(op, 200, LEFT, resultBuilder, done);
		}
		for (final Object op : POSTFIX_OPERATOR.getSymbols()) {
			resultBuilder.setPriority(300, RIGHT, "Expression", op.toString());
		}
		resultBuilder.setPriority(250, LEFT, "Expression", "natural");
		resultBuilder.setPriority(250, LEFT, "Expression", "variable");
		resultBuilder.setPriority(250, LEFT, "Expression", "string");
		resultBuilder.setPriority(250, LEFT, "Expression", "Expression");
	}
	
	public static final void setBinaryOperatorPriority(final Object operator,
			final int priority, final Associativity associativity,
			final ParserBuilder parserBuilder, final Collection<Object> done) {
		final String opToken = operator.toString();
		
		if (done.add(opToken)) {
			parserBuilder.setPriority(priority, associativity, "Expression", opToken, "Expression");
		}
	}
	
	public static final Object unquote(final Rule rule, final Object[] data) {
		ignore(rule);
		
		final String string = data[0].toString();
		
		return string.substring(1, string.length() - 1);
	}
	
	public static final Object flatten(final Rule rule, final Object[] data) {
		ignore(rule);
		
		if (data.length == 1) {
			final List<Object> result = new ArrayList<>();
			
			result.add($(data[0]));
			
			return result;
		}
		
		@SuppressWarnings("unchecked")
		final List<Object> result = (List<Object>) data[0];
		
		result.add($(data[2]));
		
		return result;
	}
	
}
