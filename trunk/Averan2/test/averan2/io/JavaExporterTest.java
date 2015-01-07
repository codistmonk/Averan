package averan2.io;

import static averan2.core.Equality.equality;
import static averan2.core.Session.*;
import static averan2.core.Session.Stack.*;
import static averan2.core.Symbol.symbol;
import static averan2.core.Variable.variable;
import static averan2.io.ConsoleOutput.group;
import static averan2.modules.Reals.*;
import static averan2.modules.Standard.*;
import static java.util.Collections.emptyMap;
import static net.sourceforge.aprog.tools.Tools.array;
import static net.sourceforge.aprog.tools.Tools.cast;
import static net.sourceforge.aprog.tools.Tools.getThisMethodName;
import static net.sourceforge.aprog.tools.Tools.ignore;
import static net.sourceforge.aprog.tools.Tools.join;
import static net.sourceforge.aprog.tools.Tools.unchecked;
import averan2.core.Composite;
import averan2.core.Equality;
import averan2.core.Expression;
import averan2.core.Expression.Visitor;
import averan2.core.Module;
import averan2.core.Session;
import averan2.core.Substitution;
import averan2.core.Symbol;
import averan2.core.Variable;
import averan2.modules.Reals;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import net.sourceforge.aprog.tools.Pair;
import net.sourceforge.aprog.tools.Tee;

import org.junit.Test;

/**
 * @author codistmonk (creation 2014-12-31)
 */
@SuppressWarnings("unchecked")
public final class JavaExporterTest {
	
	@Test
	public final void test() {
		final Module module = build(getThisMethodName(), new Runnable() {
			
			@Override
			public final void run() {
				include(Reals.MODULE);
				
				{
					final Variable $x = variable("x");
					
					suppose("definition_of_f",
							$(forAll($x), $(real($x), "->", equality($("f", "_", $x), $x))));
				}
				
				{
					final Variable $x = variable("x");
					final Variable $y = variable("y");
					
					suppose("definition_of_g",
							$(forAll($x, $y), $(natural($x), "->", real($y), "->", equality($("g", "_", group($($x, ",", $y))), addition($x, $y)))));
				}
				
				{
					final Variable $n = variable("n");
					
					suppose("definition_of_u_0",
							equality($("u", "_", ZERO), ZERO));
					
					suppose("definition_of_u_n",
							$(forAll($n), $(nonzeroNatural($n), "->", equality($("u", "_", $n), addition($("u", "_", group(subtraction($n, ONE))), $n)))));
				}
				
				{
					{
						final Variable $i = variable("i");
						final Variable $n = variable("n");
						
						suppose("definition_of_s",
								$(forAll($i, $n), $(natural($n), "->", equality($("s", "_", $n), sum($i, $n, $i)))));
					}
					
					deduce("definition_of_s_0");
					{
						final Variable $i = new Variable("i");
						
						bind("definition_of_s", $i, ZERO);
						apply(name(-1), "type_of_0");
						bind("definition_of_sum_0", $i, $i);
						rewrite(name(-2), name(-1));
						final Expression<?> unsubsituted = ((Equality) proposition(-1)).getRight();
						substitute((Expression<?>) unsubsituted.get(0), ((Substitution) unsubsituted.get(1)).getBindings().toArray());
						rewrite(name(-2), name(-1));
						conclude();
					}
					
					deduce("definition_of_s_n");
					{
						final Variable $i = new Variable("i");
						final Symbol<String> i = parametrize($i);
						final Variable $n = new Variable("n");
						final Symbol<String> n = parametrize($n);
						
						suppose(nonzeroNatural(n));
//						autoDeduce(natural(n));
						apply("nonzero_naturals_are_naturals", name(-1));
						bind("definition_of_s", i, n);
						apply(name(-1), name(-2));
						bind("definition_of_sum_n", i, n, i);
						rewrite(name(-2), name(-1));
						substitute(i, equality(i, n));
						rewrite(name(-2), name(-1));
						conclude();
//						stop();
					}
				}
			}
			
		});
		
		pushNewSessionToWorkWithModule(module);
		
		try (final PrintStream javaOutput = beginClass("test", "averan2.generated.Demo", System.out)) {
			{
				final String definitionName = "definition_of_f";
				final Module definition = proposition(definitionName);
				
				exportFunction(module(), getLeftSideOfTerminalEquality(definition), "f", javaOutput);
			}
			
			{
				final String definitionName = "definition_of_g";
				final Module definition = proposition(definitionName);
				
				exportFunction(module(), getLeftSideOfTerminalEquality(definition), "g", javaOutput);
			}
			
			{
				final String definitionName = "definition_of_u_n";
				final Module definition = proposition(definitionName);
				final Variable $n = definition.getParameters().get(0);
				
				exportProceduralInduction(module(), getLeftSideOfTerminalEquality(definition), $n, "u", javaOutput);
			}
			
			endClass(javaOutput);
		} finally {
			popSession();
		}
	}
	
	public static final PrintStream beginClass(final String rootPath, final String fullyQualifiedName) {
		return beginClass(rootPath, fullyQualifiedName, null);
	}
	
	public static final PrintStream beginClass(final String rootPath, final String fullyQualifiedName, final OutputStream secondaryOutput) {
		final int lastDotIndex = fullyQualifiedName.lastIndexOf('.');
		
		if (lastDotIndex == 0) {
			throw new IllegalArgumentException();
		}
		
		final File file = new File(rootPath, fullyQualifiedName.replaceAll("\\.", "/") + ".java");
		
		file.getParentFile().mkdirs();
		
		try {
			final PrintStream result = secondaryOutput == null ? new PrintStream(file) :
				new PrintStream(new Tee(new FileOutputStream(file), secondaryOutput));
			
			if (0 < lastDotIndex) {
				result.println("package " + fullyQualifiedName.substring(0, lastDotIndex) + ";");
			}
			
			result.println("public final class " + fullyQualifiedName.substring(lastDotIndex + 1) + " {");
			
			return result;
		} catch (final FileNotFoundException exception) {
			throw new UncheckedIOException(exception);
		}
	}
	
	public static final void endClass(final PrintStream javaOutput) {
		javaOutput.println("}");
	}
	
	static final Map<Expression<?>, Class<?>> knownTypes = new HashMap<>();
	
	static final Map<Expression<?>, Map<Class<?>, Map<Class<?>, Class<?>>>> binaryOperationTypes = new HashMap<>();
	
	static {
		knownTypes.put(BOOLEANS, boolean.class);
		knownTypes.put(NATURALS, int.class);
		knownTypes.put(nonzero(NATURALS), int.class);
		knownTypes.put(REALS, double.class);
		knownTypes.put(nonzero(REALS), double.class);
		
		for (final Expression<?> operator : array(ADDITION_OPERATOR, SUBTRACTION_OPERATOR, MULTIPLICATION_OPERATOR, DIVISION_OPERATOR)) {
			final Map<Class<?>, Map<Class<?>, Class<?>>> operationTypes = binaryOperationTypes.computeIfAbsent(operator, op -> new HashMap<>());
			
			operationTypes.computeIfAbsent(int.class, leftType -> new HashMap<>()).put(int.class, int.class);
			operationTypes.computeIfAbsent(int.class, leftType -> new HashMap<>()).put(double.class, double.class);
			operationTypes.computeIfAbsent(double.class, leftType -> new HashMap<>()).put(int.class, double.class);
			operationTypes.computeIfAbsent(double.class, leftType -> new HashMap<>()).put(double.class, double.class);
		}
	}
	
	public static final void pushNewSessionToWorkWithModule(final Module module) {
		pushSession(new Session());
		
		try {
			include(module);
			
			deduce("");
		} catch (final Exception exception) {
			popSession();
			
			throw unchecked(exception);
		}
	}
	
	public static final Pair<String, Expression<?>> findMostSpecificDefinition(final List<Pair<String, Expression<?>>> justifications) {
		Pair<String, Expression<?>> result = null;
		Expression<?> resultLHS = null;
		
		for (final Pair<String, Expression<?>> justification : justifications) {
			final Expression<?> leftSide = getLeftSideOfTerminalEquality(proposition(justification.getFirst()));
			
			if (result == null) {
				result = justification;
				resultLHS = leftSide;
			} else if (resultLHS.implies(leftSide)) {
				result = justification;
				resultLHS = leftSide;
			}
		}
		
		return result;
	}
	
	public static final Expression<?> getLeftSideOfTerminalEquality(final Expression<?> expression) {
		final Module module = cast(Module.class, expression);
		
		if (module != null) {
			return getLeftSideOfTerminalEquality(module.getPropositions().last());
		}
		
		return ((Equality) expression).getLeft();
	}
	
	public static final void exportProceduralInduction(final Module module, final Expression<?> leftSideOfEquality, final Variable inductionParameter,
			final String generatedName, final PrintStream javaOutput) {
		final Variable rightSide = new Variable("computableExpression");
		inductionParameter.reset().equals(ZERO);
		final String initializationName = findMostSpecificDefinition(justificationsFor(equality(leftSideOfEquality.accept(Variable.BIND), rightSide.reset()))).getFirst();
		final String inductionName = justificationsFor(equality(leftSideOfEquality, rightSide.reset())).get(0).getFirst();
		final Expression<?> initialization = module.findProposition(initializationName);
		final Module initializationAsModule = cast(Module.class, initialization);
		final Equality initializationEquality = initializationAsModule == null ? (Equality) initialization : initializationAsModule.getPropositions().last();
		final Expression<?> induction = module.findProposition(inductionName);
		final Module inductionAsModule = cast(Module.class, induction);
		final Map<Symbol<String>, Class<?>> parameterTypes = getParameterTypes(inductionAsModule);
		final Map<Expression<?>, Class<?>> allTypes = new LinkedHashMap<>(parameterTypes);
		final Equality inductionEquality = inductionAsModule.getPropositions().last();
		final Expression<?> lhs = getLeftSideOfTerminalEquality(inductionEquality);
		
		inductionParameter.reset().equals(subtraction(inductionParameter, ONE));
		allTypes.put(lhs.accept(Variable.BIND), int.class);
		
		final Class<?> returnType = inductionEquality.getRight().accept(new GetJavaType(allTypes));
		
		{
			javaOutput.println("	public static final " + returnType.getName() + " " + generatedName + "_initialize() {");
			inductionParameter.reset().equals(ZERO);
			javaOutput.println("		return " + initializationEquality.getRight().accept(Variable.BIND).accept(new GetJavaCode()) + ";");
			javaOutput.println("	}");
		}
		
		final Map<Expression<?>, Function<Expression<?>, String>> specialCodes = new LinkedHashMap<>();
		final Symbol<String> inductionParameterAsSymbol = symbol(inductionParameter.getName());
		final Map<Symbol<String>, Class<?>> extendedParameterTypes = new LinkedHashMap<>(parameterTypes);
		
		extendedParameterTypes.put(symbol(generatedName + "_current"), returnType);
		
		{
			inductionParameter.reset().equals(subtraction(inductionParameterAsSymbol, ONE));
			specialCodes.put(lhs.accept(Variable.BIND), e -> generatedName + "_current");
			
			javaOutput.print("	public static final " + returnType.getSimpleName() + " " + generatedName + "_next(");
			javaOutput.print(join(", ", extendedParameterTypes.entrySet().stream().map(entry -> "final " + entry.getValue().getSimpleName() + " " + entry.getKey().toString()).toArray()));
			javaOutput.println(") {");
			
			inductionParameter.reset().equals(inductionParameterAsSymbol);
			javaOutput.println("		return " + inductionEquality.getRight().accept(new GetJavaCode(specialCodes)) + ";");
			
			javaOutput.println("	}");
		}
		
		{
			javaOutput.print("	public static final " + returnType.getSimpleName() + " " + generatedName + "(");
			javaOutput.print(join(", ", parameterTypes.entrySet().stream().map(entry -> "final " + entry.getValue().getSimpleName() + " " + entry.getKey().toString()).toArray()));
			javaOutput.println(") {");
			
			javaOutput.println("		" + returnType.getName() + " " + generatedName + "_current = " + generatedName + "_initialize();");
			
			// TODO handle possible name conflict if a parameter is named "i"
			javaOutput.println("		for (int i = 0; i <= " + inductionParameterAsSymbol + "; ++i) {");
			javaOutput.print("			" + generatedName + "_current = " + generatedName + "_next(");
			javaOutput.print(join(", ", extendedParameterTypes.keySet().stream().map(k -> k.equals(inductionParameterAsSymbol) ? "i" : k.toString()).toArray()));
			javaOutput.println(");");
			
			javaOutput.println("		}");
			
			javaOutput.println("		return " + generatedName + "_current;");
			
			javaOutput.println("	}");
		}
	}
	
	public static final void exportFunction(final Module module, final Expression<?> leftSideOfEquality,
			final String generatedName, final PrintStream javaOutput) {
		final Variable rightSide = new Variable("computableExpression");
		final String propositionName = justificationsFor(equality(leftSideOfEquality, rightSide)).get(0).getFirst();
		
		exportFunction(module, propositionName, generatedName, javaOutput);
	}
	
	public static final void exportFunction(final Module module, final String propositionName,
			final String generatedName, final PrintStream javaOutput) {
		final Expression<?> proposition = module.findProposition(propositionName);
		final Module propositionAsModule = cast(Module.class, proposition);
		
		if (propositionAsModule != null) {
			final Map<Symbol<String>, Class<?>> parameterTypes = getParameterTypes(propositionAsModule);
			final Equality equality = propositionAsModule.getPropositions().last();
			propositionAsModule.getParameters().forEach(p -> p.reset().equals(symbol(p.getName())));
			final Class<?> returnType = equality.getRight().accept(Variable.BIND).accept(new GetJavaType(parameterTypes));
			final String javaCode = equality.getRight().accept(new GetJavaCode());
			
			javaOutput.print("	public static final " + returnType.getSimpleName() + " " + generatedName + "(");
			javaOutput.print(join(", ", parameterTypes.entrySet().stream().map(entry -> "final " + entry.getValue().getSimpleName() + " " + entry.getKey().toString()).toArray()));
			javaOutput.println(") {");
			javaOutput.println("		return " + javaCode + ";");
			javaOutput.println("	}");
			
			return;
		}
		
		final Equality propositionAsEquality = cast(Equality.class, proposition);
		
		if (propositionAsEquality != null) {
			// TODO
			
			return;
		}
		
		throw new IllegalArgumentException();
	}
	
	public static final Map<Symbol<String>, Class<?>> getParameterTypes(final Module module) {
		final Map<Symbol<String>, Class<?>> result = new LinkedHashMap<>();
		
		for (final Variable parameter : module.getParameters()) {
			for (final Expression<?> p : module.getPropositions()) {
				final Composite<?> c = cast(Composite.class, p);
				
				if (c != null && c.size() == 3 && c.get(0) == parameter && symbol("∈").equals(c.get(1))) {
					final Expression<?> type = c.get(2);
					
					if (!knownTypes.containsKey(type) || result.containsKey(parameter)) {
						throw new IllegalArgumentException();
					}
					
					result.put(symbol(parameter.getName()), knownTypes.get(type));
				}
			}
		}
		
		return result;
	}
	
	public static final <V> V find(final Map<? extends Expression<?>, V> haystack, final Expression<?> needle) {
		for (final Map.Entry<? extends Expression<?>, V> entry : haystack.entrySet()) {
			if (entry.getKey().accept(Variable.RESET).equals(needle)) {
				return entry.getValue();
			}
		}
		
		return null;
	}
	
	/**
	 * @author codistmonk (creation 2014-12-31)
	 */
	public static final class GetJavaCode implements Visitor<String> {
		
		private final Map<? extends Expression<?>, Function<Expression<?>, String>> specialCodes;
		
		public GetJavaCode() {
			this(Collections.emptyMap());
		}
		
		public GetJavaCode(Map<? extends Expression<?>, Function<Expression<?>, String>> specialCodes) {
			this.specialCodes = specialCodes;
		}
		
		@Override
		public final String visit(final Symbol<?> symbol) {
			final Function<Expression<?>, String> protoresult = find(this.specialCodes, symbol);
			
			if (protoresult != null) {
				return protoresult.apply(symbol);
			}
			
			return symbol.toString();
		}
		
		@Override
		public final String visit(final Variable variable) {
			final Function<Expression<?>, String> protoresult = find(this.specialCodes, variable);
			
			if (protoresult != null) {
				return protoresult.apply(variable);
			}
			
			return variable.getName();
		}
		
		@Override
		public final String visit(final Composite<Expression<?>> composite) {
			final Function<Expression<?>, String> protoresult = find(this.specialCodes, composite);
			
			if (protoresult != null) {
				return protoresult.apply(composite);
			}
			
			if (composite.size() == 3) {
				return group(composite.get(0).accept(this) + " " + composite.get(1) + " " + composite.get(2).accept(this));
			}
			
			return null;
		}
		
		@Override
		public final String visit(final Module module) {
			final Function<Expression<?>, String> protoresult = find(this.specialCodes, module);
			
			if (protoresult != null) {
				return protoresult.apply(module);
			}
			
			return null;
		}
		
		@Override
		public final String visit(final Substitution substitution) {
			final Function<Expression<?>, String> protoresult = find(this.specialCodes, substitution);
			
			if (protoresult != null) {
				return protoresult.apply(substitution);
			}
			
			return null;
		}
		
		@Override
		public final String visit(final Equality equality) {
			final Function<Expression<?>, String> protoresult = find(this.specialCodes, equality);
			
			if (protoresult != null) {
				return protoresult.apply(equality);
			}
			
			return group(equality.getLeft().accept(this) + " == " + equality.getRight().accept(this));
		}
		
		private static final long serialVersionUID = -169952762455825101L;
		
		public static final String group(final String string) {
			return "(" + string + ")";
		}
		
	}
	
	/**
	 * @author codistmonk (creation 2014-12-31)
	 */
	public static final class GetJavaType implements Visitor<Class<?>> {
		
		private final Map<? extends Expression<?>, Class<?>> types;
		
		public GetJavaType(final Map<? extends Expression<?>, Class<?>> types) {
			this.types = types;
		}
		
		@Override
		public final Class<?> visit(final Symbol<?> symbol) {
			final Class<?> candidate = find(this.types, symbol);
			
			if (candidate != null) {
				return candidate;
			}
			
			try {
				Integer.parseInt(symbol.toString());
				
				return int.class;
			} catch (final NumberFormatException exception) {
				ignore(exception);
			}
			
			try {
				Double.parseDouble(symbol.toString());
				
				return double.class;
			} catch (final NumberFormatException exception) {
				ignore(exception);
			}
			
			return null;
		}
		
		@Override
		public final Class<?> visit(final Variable variable) {
			return find(this.types, variable);
		}
		
		@Override
		public final Class<?> visit(final Composite<Expression<?>> composite) {
			final Class<?> candidate = find(this.types, composite);
			
			if (candidate != null) {
				return candidate;
			}
			
			if (composite.size() == 3) {
				final Class<?> leftType = composite.get(0).accept(this);
				final Class<?> rightType = composite.get(2).accept(this);
				
				return binaryOperationTypes
						.getOrDefault(composite.get(1), emptyMap())
						.getOrDefault(leftType, emptyMap())
						.get(rightType);
			}
			
			return null;
		}
		
		@Override
		public final Class<?> visit(final Module module) {
			return find(this.types, module);
		}
		
		@Override
		public final Class<?> visit(final Substitution substitution) {
			return find(this.types, substitution);
		}
		
		@Override
		public final Class<?> visit(final Equality equality) {
			final Class<?> candidate = find(this.types, equality);
			
			if (candidate != null) {
				return candidate;
			}
			
			return null != equality.getLeft().accept(this) && null != equality.getRight().accept(this) ? boolean.class : null;
		}
		
		private static final long serialVersionUID = -5929615464820182753L;
		
	}
	
}
