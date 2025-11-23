/*
 * Copyright (C) 2017 JDBX
 *
 * https://github.com/jdlib/JDBX
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jdbx;


import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class QueryResultTest extends JdbxTest
{
	@BeforeAll public static void beforeClass() throws JdbxException
	{
		Jdbx.update(con(), "CREATE TABLE qrtest (id INTEGER PRIMARY KEY, name VARCHAR(30))");
	}


	@BeforeEach public void before() throws JdbxException
	{
		stmt_ = new StaticStmt(con());
		stmt_.update("DELETE FROM qrtest");
		stmt_.update("INSERT INTO qrtest (id, name) VALUES (0, 'A'), (1, 'B'), (2, 'C'), (3, 'D')");
	}


	@Test public void testScroll()
	{
		stmt_.options().setResultType(ResultType.SCROLL_INSENSITIVE);
		try (QueryResult result = stmt_.query("SELECT name FROM qrtest ORDER BY name").result())
		{
			assertSame(ResultType.SCROLL_INSENSITIVE, result.getType());
			assertTrue(result.position().isBeforeFirst());
			assertTrue(result.move().absolute(2));
			assertEquals("B", result.col().getString());
			assertTrue(result.move().relative(2));
			assertEquals("D", result.col().getString());
			assertTrue(result.position().isLast());
			result.move().afterLast();
			assertTrue(result.position().isAfterLast());
		}
	}


	@Test public void testUpdate() throws Exception
	{
		stmt_.options().setResultType(ResultType.SCROLL_INSENSITIVE).setResultConcurrency(Concurrency.CONCUR_UPDATABLE);
		try (QueryResult result = stmt_.query("SELECT name FROM qrtest").result())
		{
			assertSame(Concurrency.CONCUR_UPDATABLE, result.getConcurrency());

			assertTrue(result.nextRow());
			result.col().setString("Z");
			assertTrue(result.row().isUpdated());
			result.row().update();
		}
	}


	@Test public void testCols()
	{
		try (QueryResult result = stmt_.query("SELECT id, name FROM qrtest WHERE id = 0").result())
		{
			assertTrue(result.nextRow());

			Object[] row0 = result.cols().toArray();
			assertArrayEquals(new Object[] { 0, "A"}, row0);

			List<Object> row1 = result.cols(2, 1).toList();
			assertEquals(List.of("A", 0), row1);

			Map<String, Object> row2 = result.cols("id", "name").toMap();
			assertEquals(Map.of("id", 0, "name", "A"), row2);
		}
	}


	@Test public void testCol()
	{
		try (QueryResult result = stmt_.query("SELECT id, name FROM qrtest WHERE id = 0").result())
		{
			assertTrue(result.nextRow());
			assertEquals(0, result.col().getInt());
			assertEquals("A", result.col(2).getString());
			assertEquals("A", result.col(2).getObject(String.class));
			assertEquals("A", result.col("name").getString());
			assertEquals("A", result.col("name").getObject(String.class));
		}
	}


	@Test public void testNextCol()
	{
		try (QueryResult result = stmt_.query("SELECT id, name, id, name FROM qrtest WHERE id = 0").result())
		{
			assertTrue(result.nextRow());
			assertEquals(1, result.getNextColNumber());
			assertEquals(0, result.nextCol().getInt());
			assertEquals(2, result.getNextColNumber());
			result.setNextColNumber(4);
			assertEquals("A", result.nextCol().getString());
		}
	}


	private StaticStmt stmt_;
}
