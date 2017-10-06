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
package org.jdbx.test;


import org.jdbx.Jdbx;
import org.jdbx.JdbxException;
import org.jdbx.QueryCursor;
import org.jdbx.ResultConcurrency;
import org.jdbx.ResultType;
import org.jdbx.StaticStmt;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


public class QueryCursorTest extends JdbxTest
{
	@BeforeClass public static void beforeClass() throws JdbxException
	{
		Jdbx.update(con(), "CREATE TABLE QRTest (id INTEGER IDENTITY PRIMARY KEY, name VARCHAR(30))");
	}


	@Before public void before() throws JdbxException
	{
		stmt_ = new StaticStmt(con());
		stmt_.update("DELETE FROM QRTest");
		stmt_.update("INSERT INTO QRTest (name) VALUES ('A'), ('B'), ('C'), ('D')");
	}
	
	
	@Test public void testScroll()
	{
		stmt_.init().resultType(ResultType.SCROLL_INSENSITIVE);
		try (QueryCursor cursor = stmt_.query("SELECT name FROM QRTest ORDER BY name").cursor())
		{
			assertSame(ResultType.SCROLL_INSENSITIVE, cursor.getType());
			assertTrue(cursor.position().isBeforeFirst());
			assertTrue(cursor.move().absolute(2));
			assertEquals("B", cursor.col().getString());
			assertTrue(cursor.move().relative(2));
			assertEquals("D", cursor.col().getString());
			assertTrue(cursor.position().isLast());
			cursor.move().afterLast();
			assertTrue(cursor.position().isAfterLast());
		}
	}


	@Test public void testUpdate() throws Exception
	{
		stmt_.init().resultType(ResultType.SCROLL_INSENSITIVE).resultConcurrency(ResultConcurrency.CONCUR_UPDATABLE);
		try (QueryCursor cursor = stmt_.query("SELECT name FROM QRTest").cursor())
		{
			assertSame(ResultConcurrency.CONCUR_UPDATABLE, cursor.getConcurrency());
			
			assertTrue(cursor.next());
			cursor.col().setString("Z");
			assertTrue(cursor.row().isUpdated());
			cursor.row().update();
		}
	}

	
	private StaticStmt stmt_;
}
