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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

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
					final Variable $x = ((Module) proposition("definition_of_f")).getParameters().get(0);
					
					exportFunction(module(), (Expression<?>) $("f", "_", $x), "f", System.out);
					exportProceduralInduction(module(), (Expression<?>) $("f", "_", $x), $x, "f", System.out);
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
		final Equality initializationEquality = initializationAsModule.getPropositions().last();
		
		final Expression<?> induction = module.findProposition(inductionName);
		final Module inductionAsModule = cast(Module.class, induction);
		final Map<Variable, Class<?>> parameterTypes = getParameterTypes(inductionAsModule);
		final Equality inductionEquality = inductionAsModule.getPropositions().last();
		final Class<?> returnType = inductionEquality.getRight().accept(new GetJavaType(parameterTypes));
//		final String javaCode = equality.getRight().accept(new GetJavaCode());
		
		inductionParameter.reset().equals(ZERO);
		javaOutput.println("	private static " + returnType.getName() + " " + generatedName + "_variable = " + initializationEquality.getRight().accept(Variable.BIND).accept(new GetJavaCode()) + ";");
		javaOutput.print("	public static final " + returnType.getSimpleName() + " " + generatedName + "(");
		javaOutput.print(join(", ", parameterTypes.entrySet().stream().map(entry -> "final " + entry.getValue().getSimpleName() + " " + entry.getKey().getName()).toArray()));
		javaOutput.println(") {");
		javaOutput.println("		return " + generatedName + "_variable = " + inductionEquality.getRight().accept(new GetJavaCode()) + ";");
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
	
	/**
	 * @author codistmonk (creation 2014-12-31)
	 */
	public static final class GetJavaCode implements Visitor<String> {
		
		@Override
		public final String visit(final Symbol<?> symbol) {
			return symbol.toString();
		}
		
		@Override
		public final String visit(final Variable variable) {
			return variable.getName();
		}
		
		@Override
		public final String visit(final Composite<Expression<?>> composite) {
			if (composite.size() == 3) {
				return group(composite.get(0).accept(this) + " " + composite.get(1) + " " + composite.get(2).accept(this));
			}
			
			return null;
		}
		
		@Override
		public final String visit(final Module module) {
			return null;
		}
		
		@Override
		public final String visit(final Substitution substitution) {
			return null;
		}
		
		@Override
		public final String visit(final Equality equality) {
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
		
		private final Map<Variable, Class<?>> parameterTypes;
		
		public GetJavaType(final Map<Variable, Class<?>> parameterTypes) {
			this.parameterTypes = parameterTypes;
		}
		
		@Override
		public final Class<?> visit(final Symbol<?> symbol) {
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
			return this.parameterTypes.get(variable);
		}
		
		@Override
		public final Class<?> visit(final Composite<Expression<?>> composite) {
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
			return null;
		}
		
		@Override
		public final Class<?> visit(final Substitution substitution) {
			return null;
		}
		
		@Override
		public final Class<?> visit(final Equality equality) {
			return null != equality.getLeft().accept(this) && null != equality.getRight().accept(this) ? boolean.class : null;
		}
		
		private static final long serialVersionUID = -5929615464820182753L;
		
	}
	
}
