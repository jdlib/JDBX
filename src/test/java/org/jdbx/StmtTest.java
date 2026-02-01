package org.jdbx;


import java.sql.Connection;
import java.sql.SQLWarning;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;


public class StmtTest extends JdbxTest
{
	@Test public void testDatasourceCtors()
	{
		DataSource ds = dataSource("dstest");
		try (StaticStmt stmt = new StaticStmt(ds);
			PrepStmt pstmt = new PrepStmt(ds);
			CallStmt cstmt = new CallStmt(ds);
			MultiStmt mstmt = new MultiStmt(ds))
		{
			assertNotNull(mstmt.getConnection());
		}

		try (StaticStmt stmt = new StaticStmt(ds::getConnection, false);
			PrepStmt pstmt = new PrepStmt(ds::getConnection, false);
			CallStmt cstmt = new CallStmt(ds::getConnection, false);
			MultiStmt mstmt = new MultiStmt(ds::getConnection, false))
		{
			assertNotNull(mstmt.getConnection());
		}
	}


	@Test public void testAccessors()
	{
		Connection con = con();
		try (StaticStmt stmt = new StaticStmt(con))
		{
			SQLWarning warning = stmt.getWarnings();
			assertSame(con, stmt.getConnection());
			assertNull(warning);
			stmt.clearWarnings();

			stmt.cancel();
		}
	}
}
