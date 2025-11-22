package org.jdbx;


import org.junit.jupiter.api.Test;


public class BatchTest extends JdbxTest
{
	@Test public void testBatch() throws Exception
	{
		try (StaticStmt stmt = new StaticStmt(con()))
		{
			stmt.update("CREATE TABLE btest (id INTEGER IDENTITY PRIMARY KEY, name VARCHAR(30))");

			Batch batch = stmt.batch();
			batch.clear();
		}
	}
}
