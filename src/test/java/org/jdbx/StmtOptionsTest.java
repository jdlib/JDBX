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


	@Test public void testMisc()
	{
		assertSame(StmtOption.CLOSEONCOMPLETION.name, StmtOption.CLOSEONCOMPLETION.toString());
	}


	@Test public void testWithoutJdbcStmt()
	{
		try (PrepStmt pstmt = new PrepStmt(con()))
		{
			// make sure that there is no underlying JDBC statement
			assertNull(pstmt.jdbcStmt_);

			// no defined default value
			assertEquals(
				"Poolable: default value is implementation dependent. To access it, first initialize the statement",
				assertThrows(JdbxException.class, () -> pstmt.options().isPoolable()).getMessage());

			// with defined default value
			assertSame(FetchDirection.UNKNOWN, pstmt.options().getFetchDirection());

			// set a value which is - since there is no underlying JDBC statement - only stored in the options
			pstmt.options().setFetchDirection(FetchDirection.FORWARD);
			assertSame(FetchDirection.FORWARD, pstmt.options().getFetchDirection());
		}
	}
}
