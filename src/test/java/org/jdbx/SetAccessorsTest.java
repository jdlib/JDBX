package org.jdbx;


import java.sql.SQLException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class SetAccessorsTest extends JdbxTest
{
	@BeforeAll public static void beforeClass()
	{
		Jdbx.update(con(), "CREATE TABLE AccTest (id INTEGER IDENTITY PRIMARY KEY, n INTEGER null, d DOUBLE NULL)");
	}


	@BeforeEach public void before()
	{
		pstmt_ = new PrepStmt(con());
		pstmt_.init("DELETE FROM AccTest").update();
	}


	@AfterEach public void after()
	{
		pstmt_.close();
	}


	@Test public void testPrimitiveAccessors() throws JdbxException, SQLException
	{
		pstmt_.init("INSERT INTO AccTest (n, d) VALUES (?, ?), (?,?)")
			.params(1, 2.2, null, null).update().requireCount(2);

		try (StaticStmt stmt = new StaticStmt(con())) {
			stmt.options().setResultConcurrency(Concurrency.UPDATABLE);
			stmt.query("SELECT id, n, d FROM AccTest").read(c -> {
				assertTrue(c.nextRow());
				c.col(2).setInteger(null);
				c.col(3).setDouble(null);
				c.col("n").setInt(2);
				c.col("d").setDouble(3.3);
			});
		}
	}


	private PrepStmt pstmt_;
}
