package averan.modules;

import static averan.core.ExpressionTools.$;
import static averan.core.SessionTools.*;
import static averan.io.ExpressionParser.$$;
import static averan.modules.Reals.real;
import static averan.modules.Standard.justificationFor;

import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;

import averan.core.Composite;
import averan.core.Expression;
import averan.core.Module;
import averan.core.Session;
import averan.core.Module.Symbol;
import averan.io.SessionExporter;
import net.sourceforge.aprog.tools.IllegalInstantiationException;
import net.sourceforge.aprog.tools.Tools;

/**
 * @author codistmonk (creation 2014)
 */
public final class RealMatrices {
	
	private RealMatrices() {
		throw new IllegalInstantiationException();
	}
	
	public static final Module MODULE = new Module(Reals.MODULE, RealMatrices.class.getName());
	
	static {
		pushNewSession(MODULE);
		
		try {
			suppose("definition_of_matrices",
					$$("∀X,m,n (X∈≀M_(m,n) = ('rowCount'_X = m ∧ 'columnCount'_X = n ∧ ∀i,j (X_(i,j)∈ℝ)))"));
			suppose("type_of_matrices",
					$$("∀X ((X∈≀M)=(X∈≀M_(('rowCount'_X),('columnCount'_X))))"));
			suppose("definition_of_matrix_equality",
					$$("∀X,Y ((X∈≀M) → ((Y∈≀M) → ((X=Y) = (∀i,j ((X)_(i,j)=(Y_(i,j)))))))"));
			suppose("definition_of_matrix_scalarization",
					$$("∀X ((X∈≀M_(1,1)) → (⟨X⟩=X_(1,1)))"));
			suppose("definition_of_matrix_addition",
					$$("∀X,Y,i,j (((X+Y)_(i,j) = (X_(i,j))+(Y_(i,j))))"));
			suppose("type_of_matrix_addition",
					$$("∀X,Y ((X∈≀M) → ((Y∈≀M) → ((X+Y)∈≀M)))"));
			suppose("definition_of_matrix_addition_rowCount",
					$$("∀X,Y ('rowCount'_(X+Y) = 'rowCount'_X)"));
			suppose("definition_of_matrix_addition_columnCount",
					$$("∀X,Y ('columnCount'_(X+Y) = 'columnCount'_X)"));
			suppose("definition_of_matrix_subtraction",
					$$("∀X,Y,i,j (((X-Y)_(i,j) = (X_(i,j))-(Y_(i,j))))"));
			suppose("definition_of_matrix_subtraction_rowCount",
					$$("∀X,Y ('rowCount'_(X-Y) = 'rowCount'_X)"));
			suppose("definition_of_matrix_subtraction_columnCount",
					$$("∀X,Y ('columnCount'_(X-Y) = 'columnCount'_X)"));
			suppose("definition_of_matrix_multiplication",
					$$("∀X,Y,i,j,k ((XY)_(i,j)=((Σ_(k=0)^(('columnCount'_X)-1)) ((X_(i,k))(Y_(k,j)))))"));
			suppose("definition_of_matrix_multiplication_rowCount",
					$$("∀X,Y ('rowCount'_(XY) = 'rowCount'_X)"));
			suppose("definition_of_matrix_multiplication_columnCount",
					$$("∀X,Y ('columnCount'_(XY) = 'columnCount'_Y)"));
			suppose("definition_of_transposition",
					$$("∀X (∀i,j (Xᵀ_(i,j)=X_(j,i)))"));
			suppose("definition_of_transposition_rowCount",
					$$("∀X ('rowCount'_(Xᵀ)='columnCount'_X)"));
			suppose("definition_of_transposition_columnCount",
					$$("∀X ('columnCount'_(Xᵀ)='rowCount'_X)"));
			
			claimMatrixFactMirroringRealFact("associativity_of_matrix_addition", "associativity_of_addition");
			
			new SessionExporter(session()).exportSession();
		} finally {
			popSession();
		}
	}
	
	public static final Composite realMatrix(final Expression expression) {
		return $(expression, "∈", "≀M");
	}
	
	public static final void claimMatrixFactMirroringRealFact(final String matrixFactName, final String realFactName) {
		claimMatrixFactMirroringRealFact(session(), matrixFactName, realFactName);
	}
	
	public static final void claimMatrixFactMirroringRealFact(final Session session, final String matrixFactName, final String realFactName) {
		final Session s = new Session(new Module(session.getCurrentModule(), matrixFactName));
		final Module realFact = ((Module) session.getProposition(realFactName)).canonical();
		final Collection<Expression> remainingConditions = new HashSet<>(realFact.getConditions());
		
		lookForRealParameters:
		for (final Symbol parameter : realFact.getParameters()) {
			final Expression typeOfParameter = real(parameter);
			
			for (final Expression condition : realFact.getConditions()) {
				if (typeOfParameter.equals(condition)) {
					s.suppose(realMatrix(s.getCurrentContext().parameter(parameter.toString().toUpperCase(Locale.ENGLISH))));
					remainingConditions.remove(condition);
					continue lookForRealParameters;
				}
			}
			
			s.getCurrentContext().parameter(parameter.toString());
		}
		
		Tools.debugPrint(realFact);
		
		for (final Expression e : s.getCurrentModule().bind(realFact).getFacts()) {
			s.claim(e);
			Tools.debugPrint(e);
			{
				final Composite g = (Composite) s.getCurrentGoal();
				Tools.debugPrint(g);
				s.bind("definition_of_matrix_equality", (Expression) g.get(0), g.get(2));
				Expression lastFact = s.getFact(-1);
				
				while (lastFact instanceof Module) {
					final Module lastModule = (Module) lastFact;
					Reals.proveWithBindAndApply(s, lastModule.getConditions().get(0));
					s.apply(s.getFactName(-2), s.getFactName(-1));
					lastFact = s.getFact(-1);
				}
				
				Tools.debugPrint(justificationFor(s, e));
			}
		}
		
		new SessionExporter(s).exportSession();
	}
	
}
