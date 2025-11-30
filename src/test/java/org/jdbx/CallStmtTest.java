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
package org.jdbx;


import java.sql.JDBCType;
import java.sql.ParameterMetaData;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class CallStmtTest extends JdbxTest
{
	private static Integer id_;


	@BeforeAll public static void beforeClass() throws JdbxException
	{
		try (StaticStmt stmt = new StaticStmt(con()))
		{
			stmt.update("CREATE TABLE CallUser (id INT IDENTITY PRIMARY KEY, firstname VARCHAR(50), lastname VARCHAR(50), salary DOUBLE)");

			id_ = stmt.createUpdate("INSERT INTO CallUser VALUES (DEFAULT, 'Paul', 'Smith', 1.2)")
				.returnAutoKeyCols()
				.runGetCol(Integer.class)
				.requireCount(1)
				.requireValue();

			stmt.update(
				"CREATE PROCEDURE CreateUser(IN firstname VARCHAR(50), IN lastname VARCHAR(50), IN salary DOUBLE)" +
			    "  MODIFIES SQL DATA" +
			    "  BEGIN ATOMIC" +
			    "     INSERT INTO CallUser VALUES (DEFAULT, firstname, lastname, salary);" +
			    "  END");

			stmt.update(
				"CREATE PROCEDURE GetUserName(IN theId INT, OUT firstname VARCHAR(50), OUT lastname VARCHAR(50))" +
			    "  READS SQL DATA " +
			    "  BEGIN ATOMIC" +
			    "     SELECT firstName, lastName INTO firstname, lastname FROM CallUser WHERE id = theId;" +
			    "  END");

			stmt.update(
				"CREATE PROCEDURE GetUserAsResult(IN theId INT)" +
			    "  READS SQL DATA DYNAMIC RESULT SETS 1" +
			    "  BEGIN ATOMIC" +
			    "     DECLARE result CURSOR FOR SELECT * FROM CallUser WHERE id = theId;" +
			    "     OPEN result;" +
			    "  END");
		}
	}


	@BeforeEach public void before() throws JdbxException
	{
		cstmt_ = new CallStmt(con());
	}


	@AfterEach public void after() throws JdbxException
	{
		cstmt_.close();
	}


	/**
	 * Calls a stored procedure which returns a result set.
	 * The result set is accessed via {@link CallStmt#query()}.
	 */
	@Test public void testQueryReturnResultSet() throws JdbxException
	{
		cstmt_.options().setMaxRows(2000); // increase option coverage
		cstmt_.init("{call GetUserAsResult(?)}");
		cstmt_.param(1).setInteger(id_);
		Object[] data = cstmt_.query().row().cols().toArray();
		assertNotNull(data);
		assertEquals(4, data.length);
		assertEquals(id_, data[0]);
	}


	/**
	 * Calls a stored procedure which returns an update count and a generated key.
	 * The result set is accessed via {@link CallStmt#createExecute()}.
	 */
	@Test public void testExecuteReturnGenKey() throws Exception
	{
		cstmt_.init("{call CreateUser(?,?,?)}");
		cstmt_.param("firstname").set("Alpha");
		cstmt_.param("lastname").set("Beta");
		cstmt_.param("salary").set(Double.valueOf(2.3));
		cstmt_.createExecute().run(r -> {
			assertTrue(r.next());
			assertTrue(r.isUpdateResult());
			return null;
		});
	}


	/**
	 * Calls a stored procedure which returns a result set.
	 * The result is accessed via {@link CallStmt#execute()}.
	 */
	@Test public void testExecuteReturnResultSet() throws JdbxException
	{
		cstmt_.init("{call GetUserAsResult(?)}");
		cstmt_.param(1).setInteger(id_);
		List<Object> data = cstmt_.createExecute().run(r -> {
			assertTrue(r.nextQueryResult());
			List<Object> result = r.getQueryResult().row().required().cols().toList();
			assertFalse(r.next());
			return result;
		});
		assertNotNull(data);
		assertEquals(4, data.size());
		assertEquals(id_, data.get(0));
	}


	@Test public void testReturnOutParam() throws JdbxException
	{
		cstmt_.init("{call GetUserName(?,?,?)}");
		cstmt_.param(1).setDouble(1.1);
		cstmt_.param(2).out(java.sql.Types.VARCHAR);
		cstmt_.param(3).out(JDBCType.VARCHAR);
		cstmt_.clearParams();
		cstmt_.param(1, id_);
		cstmt_.execute();
		assertEquals("Paul",  cstmt_.param(2).getString());
		assertEquals("Smith", cstmt_.param(3).getString());
	}


	@Test public void testParamMetaData() throws Exception
	{
		assertFalse(cstmt_.isInitialized());
		cstmt_.init("{call GetUserName(?,?,?)}");
		assertTrue(cstmt_.isInitialized());
		assertTrue(cstmt_.toString().endsWith("[{call GetUserName(?,?,?)}]"));

		ParameterMetaData md = cstmt_.getParamMetaData();
		assertEquals(3, md.getParameterCount());

		cstmt_.init("{call GetUserAsResult(?)}");
		assertEquals(1, cstmt_.getParamMetaData().getParameterCount());
	}


	@Test public void testOptions() throws Exception
	{
		assertSame(ResultType.FORWARD_ONLY, cstmt_.options().getResultType());

		cstmt_.options().setResultType(ResultType.SCROLL_SENSITIVE);
		assertSame(ResultType.SCROLL_SENSITIVE, cstmt_.options().getResultType());
	}


	@Test public void testBatch() throws Exception
	{
		// TODO
	}


	private CallStmt cstmt_;
}
