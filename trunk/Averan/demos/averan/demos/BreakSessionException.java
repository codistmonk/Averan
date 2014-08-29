package averan.demos;

/**
 * @author codistmonk (creation 2014-08-11)
 */
public final class BreakSessionException extends RuntimeException {
	
	/**
	 * {@value}.
	 */
	private static final long serialVersionUID = -6958775377557504848L;
	
	public static final void breakSession() {
		throw new BreakSessionException();
	}
	
}