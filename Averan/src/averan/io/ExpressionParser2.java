package averan.io;

import static averan.core.ExpressionTools.*;
import static java.util.Arrays.copyOfRange;
import static net.sourceforge.aprog.tools.Tools.append;
import static net.sourceforge.aprog.tools.Tools.array;
import static net.sourceforge.aprog.tools.Tools.cast;
import static net.sourceforge.aprog.tools.Tools.join;
import static net.sourceforge.aurochs2.core.LexerBuilder.*;
import static net.sourceforge.aurochs2.core.ParserBuilder.bloc;
import static net.sourceforge.aurochs2.core.ParserBuilder.Priority.Associativity.LEFT;
import static net.sourceforge.aurochs2.core.ParserBuilder.Priority.Associativity.NONE;
import static net.sourceforge.aurochs2.core.TokenSource.tokens;
import averan.core.Composite;
import averan.core.Expression;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.sourceforge.aprog.tools.Tools;
import net.sourceforge.aurochs2.core.LRParser;
import net.sourceforge.aurochs2.core.Lexer;
import net.sourceforge.aurochs2.core.LexerBuilder;
import net.sourceforge.aurochs2.core.ParserBuilder;
import net.sourceforge.aurochs2.core.LexerBuilder.Union;
import net.sourceforge.aurochs2.core.TokenSource;

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
		final Union letter = union(union(range('a', 'z')), union(range('A', 'Z')));
		final Union other = disjoin("=()+-,_^");
		
		lexerBuilder.generate("natural", oneOrMore(digit));
		lexerBuilder.generate("variable", letter);
		for (final Object symbol : other.getSymbols()) {
			lexerBuilder.generate(symbol.toString(), symbol);
		}
		lexerBuilder.generate("string", '\'', "characters", '\'');
		lexerBuilder.define("characters", union(digit, letter, other, ' ', sequence('\\', '\'')), "characters");
		lexerBuilder.define("characters");
		lexerBuilder.skip(oneOrMore(' '));
		
		this.mathLexer = lexerBuilder.newLexer();
		
		final ParserBuilder parserBuilder = new ParserBuilder(this.mathLexer);
		
		parserBuilder.define("()", "Expression").setListener((rule, data) -> {
			this.object = data[0];
			
			return null;
		});
		parserBuilder.define("Expression", "(", "Expression", ")").setListener((rule, data) -> {
			return data[1];
		});
		parserBuilder.define("Expression", "Expression", "+", "Expression").setListener((rule, data) -> $(data));
		parserBuilder.define("Expression", "Expression", "-", "Expression").setListener((rule, data) -> $(data));
		parserBuilder.define("Expression", "-", "Expression").setListener((rule, data) -> $(data));
		parserBuilder.define("Expression", "Expression", "Expression").setListener((rule, data) -> $(data));
		parserBuilder.define("Expression", "natural").setListener((rule, data) -> $(data));
		parserBuilder.define("Expression", "variable").setListener((rule, data) -> $(data));
		parserBuilder.define("Expression", "string").setListener((rule, data) -> $(data));
		
		parserBuilder.resolveConflictWith(bloc("Expression", "Expression"), bloc("(", "Expression", ")"));
		parserBuilder.resolveConflictWith(bloc("-", "Expression"), bloc("(", "Expression", ")"));
		parserBuilder.resolveConflictWith("Expression", "+", bloc("Expression", bloc("(", "Expression", ")")));
		parserBuilder.resolveConflictWith("Expression", "-", bloc("Expression", bloc("(", "Expression", ")")));
		parserBuilder.resolveConflictWith("(", bloc("Expression", "-", "Expression"), ")");
		
		parserBuilder.setPriority(200, NONE, "-", "Expression");
		parserBuilder.setPriority(200, LEFT, "Expression", "natural");
		parserBuilder.setPriority(200, LEFT, "Expression", "variable");
		parserBuilder.setPriority(200, LEFT, "Expression", "string");
		parserBuilder.setPriority(200, LEFT, "Expression", "Expression");
		parserBuilder.setPriority(100, LEFT, "Expression", "+", "Expression");
		parserBuilder.setPriority(100, LEFT, "Expression", "-", "Expression");
		
		this.mathParser = parserBuilder.newParser();
	}
	
	public final <E extends Expression> E parse(final CharSequence input) {
		if (this.mathParser.parse(this.mathLexer.translate(tokens(input)))) {
			return $(this.object);
		}
		
		throw new IllegalArgumentException("Syntax error");
	}
	
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
	
}
