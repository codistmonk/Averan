package averan.io;

import static averan.core.ExpressionTools.*;
import static java.util.Arrays.copyOfRange;
import static net.sourceforge.aprog.tools.Tools.array;
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

import net.sourceforge.aprog.tools.Tools;
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
public final class ExpressionParser2 implements Serializable {
	
	private final Lexer mathLexer;
	
	private final LRParser mathParser;
	
	private transient Object object;
	
	{
		final LexerBuilder lexerBuilder = new LexerBuilder();
		final Union digit = union(range('0', '9'));
		final Union letter = union(union(range('a', 'z')), union(range('A', 'Z'), disjoin("Σℕℝ")));
		final Union groupingOperator = disjoin("(){}[]⟨⟩");
		final Union prefixOperator = disjoin("∀∃¬");
		final Union infixOperator = disjoin(",=+-*/_^<≤∧∈∩→");
		final Union postfixOperator = disjoin("ᵀ");
		final Union operator = merge(groupingOperator, prefixOperator, infixOperator, postfixOperator);
		
		lexerBuilder.generate("natural", oneOrMore(digit));
		lexerBuilder.generate("variable", letter);
		for (final Object symbol : operator.getSymbols()) {
			lexerBuilder.generate(symbol.toString(), symbol);
		}
		lexerBuilder.generate("string", '\'', "characters", '\'');
		lexerBuilder.define("characters", union(digit, letter, operator, ' ', sequence('\\', '\'')), "characters");
		lexerBuilder.define("characters");
		lexerBuilder.skip(oneOrMore(' '));
		
		this.mathLexer = lexerBuilder.newLexer();
		
		final ParserBuilder parserBuilder = new ParserBuilder(this.mathLexer);
		
		parserBuilder.define("()", "Expression").setListener((rule, data) -> {
			this.object = data[0];
			
			return null;
		});
		defineGroupingOperations(parserBuilder, groupingOperator);
		definePrefixOperations(parserBuilder, prefixOperator);
		defineInfixOperations(parserBuilder, infixOperator);
		definePostfixOperations(parserBuilder, postfixOperator);
		parserBuilder.define("Expression", "-", "Expression").setListener((rule, data) -> $(data));
		parserBuilder.define("Expression", "Expression", "Expression").setListener((rule, data) -> $(data));
		parserBuilder.define("Expression", "natural").setListener((rule, data) -> $(data));
		parserBuilder.define("Expression", "variable").setListener((rule, data) -> $(data));
		parserBuilder.define("Expression", "string").setListener((rule, data) -> $(data));
		
		parserBuilder.resolveConflictWith(block("Expression", "Expression"), block("(", "Expression", ")"));
//		parserBuilder.resolveConflictWith(block("-", "Expression"), block("(", "Expression", ")"));
		
		for (final Object[] pair : groupingPairs(groupingOperator)) {
			parserBuilder.resolveConflictWith(pair[0].toString(), block("Expression", "-", "Expression"), pair[1].toString());
		}
		
		for (final Object op1 : prefixOperator.getSymbols()) {
			for (final Object op2 : prefixOperator.getSymbols()) {
				parserBuilder.resolveConflictWith(block(op1.toString(), "Expression"), block(op2.toString(), "Expression"));
			}
		}
		for (final Object op2 : prefixOperator.getSymbols()) {
			parserBuilder.resolveConflictWith(block("-", "Expression"), block(op2.toString(), "Expression"));
		}
		
		for (final Object op1 : infixOperator.getSymbols()) {
			for (final Object op2 : prefixOperator.getSymbols()) {
				parserBuilder.resolveConflictWith("Expression", op1.toString(), block("Expression", block(op2.toString(), "Expression")));
			}
		}
		
		for (final Object op1 : prefixOperator.getSymbols()) {
			for (final Object[] pair : groupingPairs(groupingOperator)) {
				parserBuilder.resolveConflictWith(block(op1.toString(), "Expression"), block(pair[0].toString(), "Expression", pair[1].toString()));
			}
		}
		for (final Object[] pair : groupingPairs(groupingOperator)) {
			parserBuilder.resolveConflictWith(block("-", "Expression"), block(pair[0].toString(), "Expression", pair[1].toString()));
		}
		
		for (final Object op1 : infixOperator.getSymbols()) {
			for (final Object[] pair : groupingPairs(groupingOperator)) {
				parserBuilder.resolveConflictWith("Expression", op1.toString(), block("Expression", block(pair[0].toString(), "Expression", pair[1].toString())));
			}
		}
		
		parserBuilder.setPriority(200, NONE, "-", "Expression");
		for (final Object op : prefixOperator.getSymbols()) {
			parserBuilder.setPriority(200, LEFT, op.toString(), "Expression");
		}
		for (final Object op : infixOperator.getSymbols()) {
			parserBuilder.setPriority(200, LEFT, "Expression", op.toString(), "Expression");
		}
		for (final Object op : postfixOperator.getSymbols()) {
			parserBuilder.setPriority(300, RIGHT, "Expression", op.toString());
		}
		parserBuilder.setPriority(200, LEFT, "Expression", "natural");
		parserBuilder.setPriority(200, LEFT, "Expression", "variable");
		parserBuilder.setPriority(200, LEFT, "Expression", "string");
		parserBuilder.setPriority(200, LEFT, "Expression", "Expression");
		
		this.mathParser = parserBuilder.newParser();
	}
	
	public final <E extends Expression> E parse(final CharSequence input) {
		if (this.mathParser.parse(this.mathLexer.translate(tokens(input)))) {
			return $(this.object);
		}
		
		throw new IllegalArgumentException("Syntax error");
	}
	
	/**
	 * {@value}.
	 */
	private static final long serialVersionUID = -5684274223441131221L;
	
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
	
}
