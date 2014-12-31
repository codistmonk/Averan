package averan2.io;

import static averan2.core.Equality.equality;
import static averan2.core.Session.*;
import static averan2.core.Session.Stack.*;
import static averan2.core.Symbol.symbol;
import static averan2.core.Variable.variable;
import static averan2.io.SessionExporter.getConditionNames;
import static averan2.modules.Reals.real;
import static averan2.modules.Standard.*;
import static net.sourceforge.aprog.tools.Tools.cast;
import static net.sourceforge.aprog.tools.Tools.getThisMethodName;
import static org.junit.Assert.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.aprog.tools.Tools;

import org.junit.Test;

import averan2.core.Composite;
import averan2.core.Equality;
import averan2.core.Expression;
import averan2.core.Module;
import averan2.core.Symbol;
import averan2.core.Variable;
import averan2.modules.Reals;

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
							$(forAll($x), $(real($x), "->", equality($("f", "_", $x), $x))));
				}
			}
			
		});
		
		export(module, "definition_of_f");
	}
	
	public static final void export(final Module module, final String propositionName) {
		final Expression<?> proposition = module.findProposition(propositionName);
		
		Tools.debugPrint(proposition);
		
		final Module propositionAsModule = cast(Module.class, proposition);
		
		if (propositionAsModule != null) {
			final Map<Variable, Symbol<String>> types = new LinkedHashMap<>();
			
			for (final Variable parameter : propositionAsModule.getParameters()) {
				for (final Expression<?> p : propositionAsModule.getPropositions()) {
					final Composite<?> c = cast(Composite.class, p);
					
					if (c != null && c.size() == 3 && c.get(0) == parameter && symbol("∈").equals(c.get(1))) {
						if (null != types.put(parameter, c.get(2))) {
							throw new IllegalArgumentException();
						}
					}
				}
			}
			
			Tools.debugPrint(types);
			
			return;
		}
		
		final Equality propositionAsEquality = cast(Equality.class, proposition);
		
		if (propositionAsEquality != null) {
			
			return;
		}
		
		throw new IllegalArgumentException();
	}
	
}
