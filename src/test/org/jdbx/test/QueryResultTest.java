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
import org.jdbx.QueryResult;
import org.jdbx.ResultConcurrency;
import org.jdbx.ResultType;
import org.jdbx.StaticStmt;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


public class QueryResultTest extends JdbxTest
{
	@BeforeClass public static void beforeClass() throws JdbxException
	{
		Jdbx.update(con(), "CREATE TABLE STest (id INTEGER IDENTITY PRIMARY KEY, name VARCHAR(30))");
		Jdbx.update(con(), "INSERT INTO STest (name) VALUES ('A'), ('B'), ('C'), ('D')");
	}


	@Before public void before() throws JdbxException
	{
		stmt_ = new StaticStmt(con());
	}
	
	
	@Test public void testAdvanced()
	{
		stmt_.init().resultType(ResultType.SCROLL_INSENSITIVE).resultConcurrency(ResultConcurrency.CONCUR_UPDATABLE);
		try (QueryResult result = stmt_.createQuery("SELECT name FROM STest ORDER BY name").result())
		{
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


	private StaticStmt stmt_;
}