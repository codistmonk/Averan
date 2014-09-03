package averan.core;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static net.sourceforge.aprog.tools.Tools.cast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import averan.core.Module.Symbol;
import net.sourceforge.aprog.tools.IllegalInstantiationException;

/**
 * @author codistmonk (creation 2014-08-04)
 */
public final class ExpressionTools {
	
	private ExpressionTools() {
		throw new IllegalInstantiationException();
	}
	
	private static final Module nullModule = new Module(null);
	
	@SuppressWarnings("unchecked")
	public static final <E extends Expression> E $(final Object... objects) {
		final Function<Object, Expression> mapper = object -> object instanceof Expression
				? (Expression) object : nullModule.new Symbol(object.toString());
		
		if (objects.length == 1) {
			return (E) mapper.apply(objects[0]);
		}
		
		if (objects.length == 2 && objects[0] instanceof List) {
			final Module module = cast(Module.class, objects[1]);
			
			if (module != null) {
				final List<Symbol> newParameters = new ArrayList<>((List<Symbol>) objects[0]);
				
				newParameters.addAll(module.getParameters());
				
				return (E) new Module(null, "", new ArrayList<>(),
						newParameters, module.getConditions(), module.getFacts());
			}
			
			return (E) new Module(null, "", new ArrayList<>(),
					(List<Symbol>) objects[0], new ArrayList<>(), new ArrayList<>(Arrays.asList($(objects[1]))));
		}
		
		if (objects.length == 3) {
			switch (objects[1].toString()) {
			case "=":
				return (E) Module.equality($(objects[0]), $(objects[2]));
			case "->":
			case "→":
				return (E) rule($(objects[0]), $(objects[2]));
			}
		}
		
		if ((objects.length & 1) != 0) {
			boolean isConjunction = true;
			
			for (int i = 1; isConjunction && i < objects.length; i += 2) {
				if (!("&".equals(objects[i]) || "∧".equals(objects[i]))) {
					isConjunction = false;
				}
			}
			
			if (isConjunction) {
				final Object[] facts = new Object[objects.length / 2 + 1];
				
				for (int i = 0; i < objects.length; i += 2) {
					facts[i / 2] = objects[i];
				}
				
				return (E) facts(facts);
			}
		}
		
		if (2 <= objects.length) {
			final Composite bracedEqualities = cast(Composite.class, objects[1]);
			
			if (bracedEqualities != null) {
				final List<Expression> children = bracedEqualities.getChildren();
				final int n = children.size();
				
				if (2 <= n && "{".equals(children.get(0).toString()) && "}".equals(children.get(n - 1).toString())) {
					Composite equalities = new Composite(children.subList(1, n - 1));
					
					if (Module.isSequenceOfEqualities(equalities)) {
						return $(objects[0], equalities);
					}
					
					if (equalities.getChildren().size() == 1) {
						equalities = cast(Composite.class, equalities.getChildren().get(0));
						
						if (equalities != null) {
							return $(objects[0], equalities);
						}
					}
				}
			}
		}
		
		return (E) new Composite(stream(objects).map(mapper).collect(toList()));
	}
	
	public static final List<Symbol> forAll(final Object... objects) {
		return Arrays.stream(objects).map(object -> nullModule.new Symbol(object.toString())).collect(toList());
	}
	
	public static final Composite equality(final Object left, final Object right) {
		return Module.equality($(left), $(right));
	}
	
	public static final Composite substitution(final Object expression, final Composite... equalities) {
		return $(expression, composite((Object[]) equalities));
	}
	
	public static final Module rule(final Object condition, final Object fact) {
		final Module result = new Module(null);
		
		result.new Suppose($(condition)).execute();
		result.new Admit($(fact)).execute();
		
		return result;
	}
	
	public static final Module conditions(final Object... conditions) {
		return facts(conditions);
	}
	
	public static final Module facts(final Object... facts) {
		final Module result = new Module(null);
		
		for (final Object fact : facts) {
			result.new Admit($(fact)).execute();
		}
		
		return result;
	}
	
	public static final Composite composite(final Object... children) {
		return new Composite(Arrays.stream(children).map(ExpressionTools::<Expression>$).collect(toList()));
	}
	
}
