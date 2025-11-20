package org.jdbx;


import org.junit.jupiter.api.Test;


public class CheckTest extends JdbxTest
{
	@Test public void testValid()
	{
		assertThrows(IllegalArgumentException.class, () -> Check.valid(FetchDirection.class, FetchDirection.INVALID));
	}
}
