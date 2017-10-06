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


import java.sql.ParameterMetaData;
import org.jdbx.CallStmt;
import org.jdbx.JdbxException;
import org.jdbx.QResultType;
import org.jdbx.StaticStmt;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


public class CallStmtTest extends JdbxTest
{
	private static Integer id_;
	@BeforeClass public static void beforeClass() throws JdbxException
	{
		try (StaticStmt stmt = new StaticStmt(con()))
		{
			stmt.update("CREATE TABLE CallUser (id INT IDENTITY PRIMARY KEY, firstname VARCHAR(50), lastname VARCHAR(50))");

			id_ = stmt.createUpdate("INSERT INTO CallUser VALUES (DEFAULT, 'Paul', 'Smith')")
				.returnAutoKeyCols()
				.runGetCol(Integer.class)
				.requireCount(1)
				.requireValue();

			stmt.update(
				"CREATE PROCEDURE CreateUser(IN firstname VARCHAR(50), IN lastname VARCHAR(50))" +
			    "  MODIFIES SQL DATA" +
			    "  BEGIN ATOMIC" +
			    "     INSERT INTO CallUser VALUES (DEFAULT, firstname, lastname);" +
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


	@Before public void before() throws JdbxException
	{
		stmt_ = new CallStmt(con());
	}


	@After public void after() throws JdbxException
	{
		stmt_.close();
	}


	/**
	 * Calls a stored procedure which returns a result set.
	 * The result set is accessed via {@link CallStmt#query()}.
	 */
	@Test public void testQueryReturnResultSet() throws JdbxException
	{
		stmt_.init("call GetUserAsResult(?)");
		stmt_.param(1).setInteger(id_);
		Object[] data = stmt_.query().row().cols();
		assertNotNull(data);
		assertEquals(3, data.length);
		assertEquals(id_, data[0]);
		assertEquals(id_, data[0]);
	}


	/**
	 * Calls a stored procedure which returns an update count and a generated key.
	 * The result set is accessed via {@link CallStmt#createUpdate()}.
	 */
	@Test public void testExecuteReturnGenKey() throws Exception
	{
		stmt_.init("call CreateUser(?,?)");
		stmt_.param("firstname").set("Alpha");
		stmt_.param("lastname").set("Beta");
		stmt_.createExecute().run(r -> {
			assertTrue(r.next());
			assertTrue(r.isUpdate());
			return null;
		});
	}


	/**
	 * Calls a stored procedure which returns a result set.
	 * The result is accessed via {@link CallStmt#execute()}.
	 */
	@Test public void testExecuteReturnResultSet() throws JdbxException
	{
		stmt_.init("call GetUserAsResult(?)");
		stmt_.param(1).setInteger(id_);
		Object[] data = stmt_.createExecute().run(r -> {
			assertTrue(r.nextQuery());
			Object[] result = r.getQuery().row().required().cols();
			assertFalse(r.next());
			return result;
		});
		assertNotNull(data);
		assertEquals(3, data.length);
		assertEquals(id_, data[0]);
		assertEquals(id_, data[0]);
	}


	@Test public void testReturnOutParam() throws JdbxException
	{
		stmt_.init("call GetUserName(?,?,?)");
		stmt_.param(1).setDouble(1.1);
		stmt_.clearParams();
		stmt_.param(1, id_);
		stmt_.execute();
		assertEquals("Paul",  stmt_.param(2).getString());
		assertEquals("Smith", stmt_.param(3).getString());
	}


	@Test public void testParamMetaData() throws Exception
	{
		assertFalse(stmt_.isInitialized());
		stmt_.init("call GetUserName(?,?,?)");
		assertTrue(stmt_.isInitialized());

		ParameterMetaData md = stmt_.getParamMetaData();
		assertEquals(3, md.getParameterCount());

		stmt_.init("call GetUserAsResult(?)");
		assertEquals(1, stmt_.getParamMetaData().getParameterCount());
	}


	@Test public void testOptions() throws Exception
	{
		assertSame(QResultType.FORWARD_ONLY, stmt_.options().getResultType());
		stmt_.init().resultType(QResultType.SCROLL_SENSITIVE).cmd("call GetUserAsResult(?)");
		assertSame(QResultType.SCROLL_SENSITIVE, stmt_.options().getResultType());
	}


	private CallStmt stmt_;
}
