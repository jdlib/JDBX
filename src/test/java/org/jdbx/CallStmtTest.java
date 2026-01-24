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
import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class CallStmtTest extends JdbxTest
{
	private static Integer id_;


	@BeforeAll public static void beforeAll() throws JdbxException
	{
		try (StaticStmt stmt = new StaticStmt(con()))
		{
			stmt.update("CREATE TABLE cstmtest (id INT IDENTITY PRIMARY KEY, firstname VARCHAR(50), lastname VARCHAR(50), salary DOUBLE)");

			id_ = stmt.createUpdate("INSERT INTO cstmtest VALUES (DEFAULT, 'Paul', 'Smith', 1.2)")
				.returnAutoKeyCols()
				.runGetCol(Integer.class)
				.requireCount(1)
				.requireValue();

			stmt.update(
				"CREATE PROCEDURE CreateUser(IN firstname VARCHAR(50), IN lastname VARCHAR(50), IN salary DOUBLE)" +
			    "  MODIFIES SQL DATA" +
			    "  BEGIN ATOMIC" +
			    "     INSERT INTO cstmtest VALUES (DEFAULT, firstname, lastname, salary);" +
			    "  END");

			stmt.update(
				"CREATE PROCEDURE GetUserName(IN theId INT, OUT firstname VARCHAR(50), OUT lastname VARCHAR(50))" +
			    "  READS SQL DATA " +
			    "  BEGIN ATOMIC" +
			    "     SELECT firstName, lastName INTO firstname, lastname FROM cstmtest WHERE id = theId;" +
			    "  END");

			stmt.update(
				"CREATE PROCEDURE GetUserAsResult(IN theId INT)" +
			    "  READS SQL DATA DYNAMIC RESULT SETS 1" +
			    "  BEGIN ATOMIC" +
			    "     DECLARE result CURSOR FOR SELECT * FROM cstmtest WHERE id = theId;" +
			    "     OPEN result;" +
			    "  END");

			stmt.update("CREATE TYPE int_array AS INTEGER ARRAY");
			stmt.update(
				"CREATE PROCEDURE MathOps(IN v DECIMAL(10,2), OUT plus DECIMAL(10,2), OUT mult DECIMAL(10,2), OUT bounds int_array)" +
			    "  READS SQL DATA" +
			    "  BEGIN ATOMIC" +
			    "     SET plus = v + v;" +
			    "     SET mult = v * v;" +
		        "     SET bounds = ARRAY[CAST(FLOOR(v) AS INTEGER), CAST(CEILING(v) AS INTEGER)];" +
			    "  END");
		}
	}


	@BeforeEach public void beforeEach()
	{
		cstmt_ = new CallStmt(con());
	}


	@AfterEach public void afterEach()
	{
		cstmt_.close();
	}


	@Test public void testInitError()
	{
		String message = assertThrows(JdbxException.class, () -> cstmt_.init("x")).getMessage();
		assertEquals("unexpected token: X in statement [x]", message);
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
		assertEquals("Paul", data[1]);
		assertEquals("Smith", data[2]);
		assertEquals(1.2, data[3]);
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
		//cstmt_.param("newId").out(JDBCType.INTEGER).get
		cstmt_.createExecute().run(r -> {
			assertTrue(r.next());
			assertTrue(r.isUpdateResult());
			// TODO how to ge the generated keys
			//cstmt_.param("newId").out(JDBCType.INTEGER).get
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


	@Test public void testReturnOutParam() throws JdbxException, SQLException
	{
		cstmt_.init("{call GetUserName(?,?,?)}")
			.registerOutParam(2).as(java.sql.Types.VARCHAR) // by number
			.registerOutParam("lastname").as(JDBCType.VARCHAR); // by name
		cstmt_.param(1).setDouble(1.1);
		cstmt_.clearParams(); // coverage
		cstmt_.param(1, id_);
		cstmt_.execute();
		CallStmt.NumberedParam firstname = cstmt_.param(2);
		assertEquals("Paul",  firstname.getString());
		CallStmt.NamedParam lastname = cstmt_.param("lastname");
		assertEquals("Smith", lastname.getString());
		assertEquals("Smith", lastname.getObject(String.class));

		cstmt_.init("{call MathOps(?,?,?,?)}")
			.registerOutParam(2).as(java.sql.Types.DECIMAL, 2)
			.registerOutParam(3).as(JDBCType.DECIMAL, 2)
			.registerOutParam(4).as(JDBCType.ARRAY, "INT_ARRAY");
		cstmt_.param("v").set(Double.valueOf(2.25), JDBCType.DECIMAL);
		cstmt_.execute();
		assertEquals(4.5,  cstmt_.param(2).getDouble());
		assertEquals(5.06, cstmt_.param(3).getObject(Double.class));
		Integer[] bounds = cstmt_.param(4).getArray(Integer.class);
		assertArrayEquals(new Integer[] {2, 3}, bounds);
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