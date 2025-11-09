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


import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class QueryResultTest extends JdbxTest
{
	@BeforeAll public static void beforeClass() throws JdbxException
	{
		Jdbx.update(con(), "CREATE TABLE qrtest (id INTEGER IDENTITY PRIMARY KEY, name VARCHAR(30))");
	}


	@BeforeEach public void before() throws JdbxException
	{
		stmt_ = new StaticStmt(con());
		stmt_.update("DELETE FROM qrtest");
		stmt_.update("INSERT INTO qrtest (name) VALUES ('A'), ('B'), ('C'), ('D')");
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


	private StaticStmt stmt_;
}
