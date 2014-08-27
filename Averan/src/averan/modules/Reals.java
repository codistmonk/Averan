package averan.modules;

import static averan.core.SessionTools.*;
import static averan.io.ExpressionParser.$$;

import averan.core.Module;

import net.sourceforge.aprog.tools.IllegalInstantiationException;

/**
 * @author codistmonk (creation 2014-08-27)
 */
public final class Reals {
	
	private Reals() {
		throw new IllegalInstantiationException();
	}
	
	public static final Module MODULE = new Module(Standard.MODULE);
	
	static {
		suppose("definition_of_subtraction",
				$$("∀x,y ((x∈ℝ) → ((y∈ℝ) → ((x-y)=(x+(-y)))))"));
		suppose("type_of_opposite",
				$$("∀x ((x∈ℝ) → ((-x)∈ℝ))"));
		suppose("type_of_addition",
				$$("∀x,y ((x∈ℝ) → ((y∈ℝ) → ((x+y)∈ℝ)))"));
		suppose("type_of_subtraction",
				$$("∀x,y ((x∈ℝ) → ((y∈ℝ) → ((x-y)∈ℝ)))"));
		suppose("type_of_multiplication",
				$$("∀x,y ((x∈ℝ) → ((y∈ℝ) → ((xy)∈ℝ)))"));
		admit("right_distributivity_of_multiplication_over_addition",
				$$("∀a,b,c ((a∈ℝ) → ((b∈ℝ) → ((c∈ℝ) → (((a+b)c)=((ac)+(bc))))))"));
		admit("associativity_of_addition",
				$$("∀x,y,z ((x∈ℝ) → ((y∈ℝ) → ((z∈ℝ) → ((x+(y+z))=((x+y)+z)))))"));
		admit("associativity_of_multiplication",
				$$("∀x,y,z ((x∈ℝ) → ((y∈ℝ) → ((z∈ℝ) → ((x(yz))=((xy)z)))))"));
		admit("left_distributivity_of_multiplication_over_addition",
				$$("∀a,b,c ((a∈ℝ) → ((b∈ℝ) → ((c∈ℝ) → ((a(b+c))=((ab)+(ac))))))"));
		admit("right_distributivity_of_multiplication_over_subtraction",
				$$("∀a,b,c ((a∈ℝ) → ((b∈ℝ) → ((c∈ℝ) → (((a-b)c)=((ac)-(bc))))))"));
		admit("left_distributivity_of_multiplication_over_subtraction",
				$$("∀a,b,c ((a∈ℝ) → ((b∈ℝ) → ((c∈ℝ) → ((a(b-c))=((ab)-(ac))))))"));
		admit("commutativity_of_multiplication",
				$$("∀x,y ((x∈ℝ) → ((y∈ℝ) → ((xy)=(yx))))"));
		admit("commutativity_of_addition",
				$$("∀x,y ((x∈ℝ) → ((y∈ℝ) → ((x+y)=(y+x))))"));
		admit("ordering_of_terms",
				$$("∀x,y,z ((x∈ℝ) → ((y∈ℝ) → ((z∈ℝ) → (((x+z)+y)=((x+y)+z)))))"));
		admit("ordering_of_factors",
				$$("∀x,y,z ((x∈ℝ) → ((y∈ℝ) → ((z∈ℝ) → (((xz)y)=((xy)z)))))"));
		admit("opposite_of_multiplication",
				$$("∀x,y ((x∈ℝ) → ((y∈ℝ) → (((-x)y)=(-(xy)))))"));
	}
	
}
