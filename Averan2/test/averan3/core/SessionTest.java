package averan3.core;

import static averan3.core.Session.*;
import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author codistmonk (creation 2015-01-06)
 */
public final class SessionTest {
	
	@Test
	public final void test1() {
		begin();
		
		try {
			deduce("averan.deductions.Standard");
			{
				
			}
		} finally {
			export(end(), new ConsoleOutput());
		}
	}
	
}
