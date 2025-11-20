package org.jdbx;


import java.sql.SQLException;
import org.jdbx.JdbxException.Reason;
import org.jdbx.JdbxException.SqlExType;
import org.junit.jupiter.api.Test;


public class JdbxExceptionTest extends JdbxTest
{
	@Test public void testFromSqlException()
	{
		SQLException se = new SQLException("test");
		JdbxException e = JdbxException.of(se);
		assertSame(e, JdbxException.of(e));
		assertSame(Reason.JDBC, e.getReason());
		assertTrue(e.hasSqlExCause());
		assertSame(se, e.getSqlExCause());
		assertSame(SqlExType.GENERAL, e.getSqlExType());
	}


	@Test public void testFromOtherException()
	{
		IllegalArgumentException iae = new IllegalArgumentException("test");
		JdbxException e = JdbxException.of(iae);
		assertSame(e, JdbxException.of(e));
		assertSame(Reason.PROCESS, e.getReason());
		assertFalse(e.hasSqlExCause());
		assertNull(e.getSqlExCause());
		assertNull(e.getSqlExType());
	}
}
