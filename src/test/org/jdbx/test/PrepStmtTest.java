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


import org.jdbx.JdbException;
import org.jdbx.Jdbx;
import org.jdbx.PrepStmt;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


public class PrepStmtTest extends JdbxTest
{
	@BeforeClass public static void beforeClass() throws JdbException
	{
		Jdbx.update(con(), "CREATE TABLE ParamUser (id INTEGER IDENTITY PRIMARY KEY, name VARCHAR(30), type INTEGER NOT NULL)");
	}


	@Before public void before() throws JdbException
	{
		Jdbx.update(con(), "DELETE FROM ParamUser");
		stmt_ = new PrepStmt(con());
	}


	@After public void after() throws JdbException
	{
		stmt_.close();
	}


	@SuppressWarnings("boxing")
	@Test public void test() throws JdbException
	{
		String sql;

		// insert a single record and remember it
		sql = "INSERT INTO ParamUser VALUES (DEFAULT, ?, ?)";
		assertFalse(stmt_.isInitialized());
		stmt_.init().reportAutoKeys(1).cmd(sql);
		assertTrue(stmt_.isInitialized());
		Integer idA = stmt_.params("A", 1).createUpdate().runGetAutoKey(Integer.class)
			.checkCount(1)
			.checkHasValue();

		// insert a single record and remember it
		sql = "INSERT INTO ParamUser VALUES (DEFAULT, :name, :type)";
		stmt_.init().named().cmd(sql);
		stmt_.param("name").set("X");
		stmt_.param("type").setInt(15);
		assertEquals(1, stmt_.update());

		// insert two records using a batch
		stmt_.param(1, "B").param(2, 2).batch().add();
		stmt_.param(1).set("C");
		stmt_.param(2).setInt(3);
		stmt_.batch().add();
		int updateCounts[] = stmt_.batch().run();
		assertEquals(2, updateCounts.length);
		assertEquals(1, updateCounts[0]);
		assertEquals(1, updateCounts[1]);


		stmt_.init("SELECT * FROM ParamUser WHERE id = ?");
		ParamUser user = stmt_.params(idA).createQuery().row().value(ParamUser::read);
		assertNotNull(user);
		assertEquals(idA, user.id);
		assertEquals("A", user.name);
		assertEquals(1, user.type);
	}


	private PrepStmt stmt_;
}
