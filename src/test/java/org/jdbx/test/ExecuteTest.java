/*
 * Copyright (C) 2016 JDBX
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


import org.jdbx.ExecuteResult;
import org.jdbx.JdbxException;
import org.jdbx.StaticStmt;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;


public class ExecuteTest extends JdbxTest
{
	@BeforeAll public static void beforeClass() throws JdbxException
	{
		try (StaticStmt stmt = new StaticStmt(con()))
		{
			stmt.update("CREATE TABLE ExecTest (id INTEGER IDENTITY PRIMARY KEY, name VARCHAR(30))");
		}
	}


	@Test public void test()
	{
		try (StaticStmt stmt = new StaticStmt(con()))
		{
			ExecuteResult result = stmt.createExecute("INSERT INTO ExecTest VALUES (DEFAULT, 'a'); SELECT * FROM ExecTest").run();
			
//			unforunately hsqldb only returns the last result!			
//			assertTrue(result.next());
//			assertTrue(result.isUpdateResult());
			
			assertTrue(result.next());
			assertTrue(result.isQueryResult());
			assertFalse(result.next());
		}
	}
}
