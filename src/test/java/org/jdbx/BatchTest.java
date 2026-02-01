package org.jdbx;


import org.jdbx.BatchResult.CountType;
import org.junit.jupiter.api.Test;


public class BatchTest extends JdbxTest
{
	@Test public void testBatch() throws Exception
	{
		try (StaticStmt stmt = new StaticStmt(con()))
		{
			stmt.update("CREATE TABLE btest (id INTEGER IDENTITY PRIMARY KEY, name VARCHAR(30))");

			stmt.batch()
				.add("INVALID")
				.clear();
			stmt.batch()
				.add("INSERT INTO btest (name) VALUES ('A'), ('B')")
				.add("INSERT INTO btest (name) VALUES ('C')")
				.run()
				.requireSize(2)
				.requireCount(0, 2)
				.requireCount(1, 1);
		}
	}


	@Test public void testBatchResultNullCounts()
	{
		// null counts are silently converted
		BatchResult<?> result = new BatchResult<>((int[])null);
		assertEquals(0, result.size());
		assertArrayEquals(new int[0], result.getCounts());
	}


	@Test public void testBatchResultValue()
	{
		BatchResult<String> result = new BatchResult<>("a", 1);
		assertSame("a", result.requireValue());
		assertSame("a", result.value());

		BatchResult<?> result2 = new BatchResult<>(null, 1);
		assertThrows(JdbxException.class, () -> result2.requireValue());
	}


	@Test public void testBatchResultSizeAndCount()
	{
		BatchResult<?> result = new BatchResult<>(1, -2, -3, -4);
		assertEquals(4, result.size());
		result.requireSize(4);
		assertThrows(JdbxException.class, () -> result.requireSize(3));

		assertEquals(1, result.getCount(0));
		result.requireCount(0, 1);
		assertThrows(JdbxException.class, () -> result.requireCount(0, 2));

		assertSame(CountType.SUCCESS, result.getCountType(0));
		assertSame(CountType.SUCCESS_NO_INFO, result.getCountType(1));
		assertSame(CountType.EXECUTE_FAILED, result.getCountType(2));
		assertSame(CountType.INVALID, result.getCountType(3));
		result.requireCountType(0, CountType.SUCCESS);
		assertThrows(JdbxException.class, () -> result.requireCountType(0, CountType.INVALID));
	}
}
