package org.jdbx;


import org.junit.jupiter.api.Test;


public class StmtOptionsTest extends JdbxTest
{
	@Test public void test()
	{
		try (StaticStmt stmt = new StaticStmt(con()))
		{
			StmtOptions options = stmt.options();

			options.setCloseOnCompletion();
			options.isCloseOnCompletion(); // no assert, since H2 always returns false??

			options.setCursorName("abc");
			// no getter

			options.setEscapeProcessing(true);
			// no getter

			options.setFetchDirection(FetchDirection.REVERSE);
			assertSame(FetchDirection.REVERSE, options.getFetchDirection());

			options.setFetchRows(5);
			assertEquals(5, options.getFetchRows());

			options.setLargeMaxRows(14L);
			assertEquals(14L, options.getLargeMaxRows());

			options.setMaxFieldBytes(0);
			assertEquals(0, options.getMaxFieldBytes());

			options.setMaxRows(12);
			assertEquals(12, options.getMaxRows());

			options.setPoolable(true);
			assertTrue(options.isPoolable());

			options.setQueryTimeoutSeconds(123);
			assertEquals(123, options.getQueryTimeoutSeconds());

			options.setResultHoldability(Holdability.HOLD_OVER_COMMIT);
			assertSame(Holdability.HOLD_OVER_COMMIT, options.getResultHoldability());
		}
	}
}
