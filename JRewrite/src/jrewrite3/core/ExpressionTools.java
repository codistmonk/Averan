package jrewrite3.core;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static net.sourceforge.aprog.tools.Tools.cast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import jrewrite3.core.Module.Symbol;
import net.sourceforge.aprog.tools.IllegalInstantiationException;
import net.sourceforge.aprog.tools.Tools;

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
			final Module rule = cast(Module.class, objects[1]);
			
			if (rule != null) {
				return (E) new Module(null, (List<Symbol>) objects[0], rule.getConditions(), rule.getFacts());
			}
			
			return (E) new Module(null, (List<Symbol>) objects[0], new ArrayList<>(),
					new ArrayList<>(Arrays.asList($(objects[1]))));
		}
		
		if (objects.length == 3) {
			switch (objects[1].toString()) {
			case "=":
				return (E) Module.equality($(objects[0]), $(objects[2]));
			case "->":
				return (E) rule($(objects[0]), $(objects[2]));
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
	
	public static final Module rule(final Object condition, final Object fact) {
		final Module result = new Module(null);
		
		result.new Suppose($(condition)).execute();
		result.new Admit($(fact)).execute();
		
		return result;
	}
	
}
