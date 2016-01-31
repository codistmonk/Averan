package averan.draft6.proofs;

import static averan.draft6.expressions.Expressions.*;
import static multij.tools.Tools.cast;

import averan.draft6.expressions.Id;

import java.io.Serializable;
import java.util.List;

import multij.tools.Tools;

/**
 * @author codistmonk (creation 2016-01-31)
 */
public final class MetaCheck extends Proof.Abstract {
	
	private final Object proposition;
	
	public MetaCheck(final String provedPropositionName, final List<Object> message, final Object proposition) {
		super(provedPropositionName, message);
		this.proposition = proposition;
	}
	
	@Override
	public final Object getProvedPropositionFor(final Deduction context) {
		if (!isValidMetaProposition(this.proposition)) {
			throw new IllegalArgumentException("Invalid: " + this.proposition);
		}
		
		return this.getProvedProposition();
	}
	
	public final synchronized Object getProvedProposition() {
		return this.proposition;
	}
	
	private static final long serialVersionUID = -3950356322595641455L;
	
	public static final Object I = $new("𝕚");
	
	public static final Object J = $new("𝕛");
	
	public static final Object K = $new("𝕜");
	
	public static final Object L = $new("𝕝");
	
	public static final Object AT = $new("@");
	
	private static final Object MEMBER = $("∈");
	
	private static final Object ID_MEMBERSHIP = $(Any.ANY, MEMBER, I);
	
	private static final Object NONID_ATOM_MEMBERSHIP = $(Any.ANY, MEMBER, J);
	
	private static final Object ATOM_MEMBERSHIP = $(Any.ANY, MEMBER, K);
	
	private static final Object UNSIZED_LIST_MEMBERSHIP = $(Any.ANY, MEMBER, L);
	
	private static final Object SIZED_LIST_MEMBERSHIP = $(Any.ANY, MEMBER,$(L, Any.ANY));
	
	private static final Object ELEMENT_EQUALITY = $(Any.ANY, "=", $(Any.ANY, AT, Any.ANY));
	
	public static final Object operator(final Object binaryOperation) {
		return list(binaryOperation).get(1);
	}
	
	public static final boolean isIdMembership(final Object expression) {
		return ID_MEMBERSHIP.equals(expression);
	}
	
	public static final boolean isNonIdAtomMembership(final Object expression) {
		return NONID_ATOM_MEMBERSHIP.equals(expression);
	}
	
	public static final boolean isAtomMembership(final Object expression) {
		return ATOM_MEMBERSHIP.equals(expression);
	}
	
	public static final boolean isUnsizedListMembership(final Object expression) {
		return UNSIZED_LIST_MEMBERSHIP.equals(expression);
	}
	
	public static final boolean isSizedListMembership(final Object expression) {
		return SIZED_LIST_MEMBERSHIP.equals(expression);
	}
	
	public static final boolean isElementEquality(final Object expression) {
		return ELEMENT_EQUALITY.equals(expression);
	}
	
	public static final boolean isValidIdMembership(final Object expression) {
		if (isIdMembership(expression)) {
			return left(expression) instanceof Id; 
		}
		
		return false;
	}
	
	public static final boolean isValidNonIdAtomMembership(final Object expression) {
		if (isNonIdAtomMembership(expression)) {
			final Object left = left(expression);
			
			return !(left instanceof Id) && !(left instanceof List); 
		}
		
		return false;
	}
	
	public static final boolean isValidAtomMembership(final Object expression) {
		if (isNonIdAtomMembership(expression)) {
			return !(left(expression) instanceof List); 
		}
		
		return false;
	}
	
	public static final boolean isValidUnsizedListMembership(final Object expression) {
		if (isNonIdAtomMembership(expression)) {
			return !(left(expression) instanceof List); 
		}
		
		return false;
	}
	
	public static final boolean isValidSizedListMembership(final Object expression) {
		if (isNonIdAtomMembership(expression)) {
			final List<Object> left = cast(List.class, left(expression));
			
			return left != null && Tools.equals(list(right(expression)).get(1), left.size()); 
		}
		
		return false;
	}
	
	public static final boolean isValidElementEquality(final Object expression) {
		if (isNonIdAtomMembership(expression)) {
			final List<Object> l = cast(List.class, left(right(expression)));
			final Integer i = cast(Integer.class, right(right(expression)));
			
			return l != null && i != null && 0 <= i && i < l.size() && Tools.equals(left(expression), l.get(i)); 
		}
		
		return false;
	}
	
	public static final boolean isMetaProposition(final Object expression) {
		return isIdMembership(expression) || isNonIdAtomMembership(expression) || isAtomMembership(expression)
				|| isUnsizedListMembership(expression) || isSizedListMembership(expression) || isElementEquality(expression);
	}
	
	public static final boolean isValidMetaProposition(final Object expression) {
		return isValidIdMembership(expression) || isValidNonIdAtomMembership(expression) || isValidAtomMembership(expression)
				|| isValidUnsizedListMembership(expression) || isValidSizedListMembership(expression) || isValidElementEquality(expression);
	}
	
	/**
	 * @author codistmonk (creation 2016-01-31)
	 */
	private static final class Any implements Serializable {
		
		@Override
		public final int hashCode() {
			return super.hashCode();
		}
		
		@Override
		public final boolean equals(final Object obj) {
			return true;
		}
		
		private static final long serialVersionUID = 6663169167841120797L;
		
		static final Any ANY = new Any();
		
	}
	
}
