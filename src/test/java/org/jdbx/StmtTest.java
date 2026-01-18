package org.jdbx;


import java.sql.Connection;
import java.sql.SQLWarning;
import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.jupiter.api.Test;


public class StmtTest extends JdbxTest
{
	@Test public void testDatasourceCtors()
	{
		JDBCDataSource ds = new JDBCDataSource();
		ds.setURL("jdbc:hsqldb:mem:dstest");
		ds.setUser("sa");
		ds.setPassword(""); // H2 default
		try (StaticStmt stmt = new StaticStmt(ds);
			 PrepStmt pstmt = new PrepStmt(ds);
			 CallStmt cstmt = new CallStmt(ds))
		{
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
