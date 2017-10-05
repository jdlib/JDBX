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


import org.jdbx.JdbxException;
import org.jdbx.Jdbx;
import org.jdbx.PrepStmt;
import org.jdbx.QueryCursor;
import org.jdbx.ResultIterator;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


public class PrepStmtTest extends JdbxTest
{
	@BeforeClass public static void beforeClass() throws JdbxException
	{
		Jdbx.update(con(), "CREATE TABLE PTests(id INTEGER IDENTITY PRIMARY KEY, name VARCHAR(30), type INTEGER NOT NULL)");
	}


	@Before public void before() throws JdbxException
	{
		Jdbx.update(con(), "DELETE FROM PTests");
		pstmt_ = new PrepStmt(con());
	}


	@After public void after() throws JdbxException
	{
		pstmt_.close();
	}


	@SuppressWarnings("boxing")
	@Test public void test() throws JdbxException
	{
		assertFalse(pstmt_.isInitialized());

		// insert a single record and remember the generated id
		pstmt_.init().returnCols(1).cmd("INSERT INTO PTests VALUES (DEFAULT, ?, ?)");
		assertTrue(pstmt_.isInitialized());
		pstmt_.params("a", 1);
		Integer idA = pstmt_.createUpdate().runGetAutoKey(Integer.class)
			.checkCount(1)
			.checkHasValue();

		// insert a single record and remember it: use named parameters
		pstmt_.init().namedParams().cmd("INSERT INTO PTests VALUES (DEFAULT, :name, :type)");
		pstmt_.param("name").setString("b");
		pstmt_.param("type").setInt(15);
		assertEquals(1, pstmt_.update());
		
		// insert two records using a batch
		pstmt_.param(1, "c").param(2, 2).batch().add();
		pstmt_.param(1, "d");
		pstmt_.param(2).setInt(3);
		pstmt_.batch().add();
		int updateCounts[] = pstmt_.batch().run();
		assertEquals(2, updateCounts.length);
		assertEquals(1, updateCounts[0]);
		assertEquals(1, updateCounts[1]);

		// read a row by id
		pstmt_.init("SELECT * FROM PTests WHERE id = ?");
		Dao dao = pstmt_.params(idA).createQuery().row().read(Dao::read);
		assertNotNull(dao);
		assertEquals(idA, 	dao.id);
		assertEquals("a", 	dao.name);
		assertEquals(1, 	dao.type);
	}

	
	public static class Dao
	{
		public static Dao read(QueryCursor cursor) throws JdbxException
		{
			ResultIterator it 	= ResultIterator.of(cursor);
			Dao dao 			= new Dao();
			dao.id   			= it.getInteger();
			dao.name			= it.getString();
			dao.type			= it.getInt();
			return dao;
		}


		public Integer id;
		public String name;
		public int type;
	}


	private PrepStmt pstmt_;
}
