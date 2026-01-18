package org.jdbx;


import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;


public class QueryTest extends JdbxTest
{
	@BeforeAll public static void beforeAll()
	{
		stmt_ = new StaticStmt(con());
		stmt_.update("CREATE TABLE querytest (id INTEGER PRIMARY KEY, name VARCHAR(30))");
		stmt_.update("INSERT INTO querytest (id, name) VALUES (0, 'A'), (1, 'B'), (2, 'C'), (3, 'D')");
	}


	@AfterAll public static void afterAll()
	{
		if (stmt_ != null)
		{
			stmt_.close();
			stmt_ = null;
		}
	}


	@Test public void testRowsForEach()
	{
		AtomicInteger count = new AtomicInteger();
		query().rows().forEach(result -> count.addAndGet(result.col().getInt()));
		assertEquals(6, count.get());
	}


	@Test public void testRowsRead()
	{
		List<String> names = query().skip(1).rows().read(result -> result.col(2).getString());
		assertEquals(List.of("B", "C", "D"), names);
	}


	@Test public void testRowsCols()
	{
		List<Map<String,Object>> mapList = query().rows().cols().toMap();
		assertEquals(4, mapList.size());
		Map<String,Object> row3 = mapList.get(3);
		assertEquals(2, row3.size());
		assertEquals(3, row3.get("ID")); // h2 converts unquoted column names to upper case
		assertEquals("D", row3.get("NAME"));

		List<List<Object>> listList = query().rows().cols(2, 1).toList();
		assertEquals(4, listList.size());
		List<Object> row2 = listList.get(2);
		assertEquals(2, row2.size());
		assertEquals("C", row2.get(0));
		assertEquals(2, row2.get(1));
	}


	private Query query()
	{
		return stmt_.query("SELECT id, name FROM querytest ORDER BY id");
	}


	private static StaticStmt stmt_;
}
