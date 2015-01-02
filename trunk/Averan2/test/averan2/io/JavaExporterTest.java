package averan2.io;

import static averan2.core.Equality.equality;
import static averan2.core.Session.*;
import static averan2.core.Session.Stack.*;
import static averan2.core.Symbol.symbol;
import static averan2.core.Variable.variable;
import static averan2.modules.Reals.*;
import static averan2.modules.Standard.*;
import static java.util.Collections.emptyMap;
import static net.sourceforge.aprog.tools.Tools.array;
import static net.sourceforge.aprog.tools.Tools.cast;
import static net.sourceforge.aprog.tools.Tools.getThisMethodName;
import static net.sourceforge.aprog.tools.Tools.ignore;
import static net.sourceforge.aprog.tools.Tools.join;
import averan2.core.Composite;
import averan2.core.Equality;
import averan2.core.Expression;
import averan2.core.Expression.Visitor;
import averan2.core.Module;
import averan2.core.Substitution;
import averan2.core.Symbol;
import averan2.core.Variable;
import averan2.modules.Reals;

import java.io.PrintStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import net.sourceforge.aprog.tools.Tools;

import org.junit.Test;

/**
 * @author codistmonk (creation 2014-12-31)
 */
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
							$(forAll($x), $(real($x), "->", equality($("f", "_", $x), addition($x, $x)))));
				}
				
				{
					final Variable $n = variable("n");
					
					suppose("definition_of_u_0",
							equality($("u", "_", ZERO), ZERO));
					
					suppose("definition_of_u_n",
							$(forAll($n), $(natural($n), "->", equality($("u", "_", $n), addition($("u", "_", subtraction($n, ONE)), $n)))));
				}
				
				{
					final Variable $x = ((Module) proposition("definition_of_f")).getParameters().get(0);
					
					exportFunction(module(), (Expression<?>) $("f", "_", $x), "f", System.out);
				}
				
				{
					final Variable $n = ((Module) proposition("definition_of_u_n")).getParameters().get(0);
					
					exportProceduralInduction(module(), (Expression<?>) $("u", "_", $n), $n, "u", System.out);
				}
			}
			
		});
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
	
	public static final void exportProceduralInduction(final Module module, final Expression<?> leftSideOfEquality, final Variable inductionParameter,
			final String generatedName, final PrintStream javaOutput) {
		final Variable rightSide = new Variable("computableExpression");
		inductionParameter.reset().equals(ZERO);
		final String initializationName = justificationsFor(equality(leftSideOfEquality.accept(Variable.BIND), rightSide.reset())).get(0).getFirst();
		final String inductionName = justificationsFor(equality(leftSideOfEquality, rightSide.reset())).get(0).getFirst();
		
		Tools.debugPrint(initializationName, inductionName);
		
		final Expression<?> initialization = module.findProposition(initializationName);
		final Module initializationAsModule = cast(Module.class, initialization);
		final Equality initializationEquality = initializationAsModule == null ? (Equality) initialization : initializationAsModule.getPropositions().last();
		final Expression<?> induction = module.findProposition(inductionName);
		final Module inductionAsModule = cast(Module.class, induction);
		final Map<Variable, Class<?>> parameterTypes = getParameterTypes(inductionAsModule);
		final Map<Expression<?>, Class<?>> allTypes = new LinkedHashMap<>(parameterTypes);
		final Equality inductionEquality = inductionAsModule.getPropositions().last();
		allTypes.put($("u", "_", subtraction(inductionParameter, ONE)), int.class);
		final Class<?> returnType = inductionEquality.getRight().accept(new GetJavaType(allTypes));
		final Map<Expression<?>, Function<Expression<?>, String>> specialCodes = new LinkedHashMap<>();
		specialCodes.put($("u", "_", subtraction(inductionParameter, ONE)), e -> "u(n-1)");
//		final String javaCode = equality.getRight().accept(new GetJavaCode());
		
		inductionParameter.reset().equals(ZERO);
		javaOutput.println("	private static " + returnType.getName() + " " + generatedName + "_variable = " + initializationEquality.getRight().accept(Variable.BIND).accept(new GetJavaCode()) + ";");
		javaOutput.print("	public static final " + returnType.getSimpleName() + " " + generatedName + "(");
		javaOutput.print(join(", ", parameterTypes.entrySet().stream().map(entry -> "final " + entry.getValue().getSimpleName() + " " + entry.getKey().getName()).toArray()));
		javaOutput.println(") {");
		javaOutput.println("		return " + generatedName + "_variable = " + inductionEquality.getRight().accept(new GetJavaCode(specialCodes)) + ";");
		javaOutput.println("	}");
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
		
		Tools.debugPrint(proposition);
		
		final Module propositionAsModule = cast(Module.class, proposition);
		
		if (propositionAsModule != null) {
			final Map<Variable, Class<?>> parameterTypes = getParameterTypes(propositionAsModule);
			final Equality equality = propositionAsModule.getPropositions().last();
			final Class<?> returnType = equality.getRight().accept(new GetJavaType(parameterTypes));
			final String javaCode = equality.getRight().accept(new GetJavaCode());
			
			javaOutput.print("	public static final " + returnType.getSimpleName() + " " + generatedName + "(");
			javaOutput.print(join(", ", parameterTypes.entrySet().stream().map(entry -> "final " + entry.getValue().getSimpleName() + " " + entry.getKey().getName()).toArray()));
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
	
	public static final Map<Variable, Class<?>> getParameterTypes(final Module module) {
		final Map<Variable, Class<?>> result = new LinkedHashMap<>();
		
		for (final Variable parameter : module.getParameters()) {
			for (final Expression<?> p : module.getPropositions()) {
				final Composite<?> c = cast(Composite.class, p);
				
				if (c != null && c.size() == 3 && c.get(0) == parameter && symbol("∈").equals(c.get(1))) {
					final Expression<?> type = c.get(2);
					
					if (!knownTypes.containsKey(type) || result.containsKey(parameter)) {
						throw new IllegalArgumentException();
					}
					
					result.put(parameter, knownTypes.get(type));
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
