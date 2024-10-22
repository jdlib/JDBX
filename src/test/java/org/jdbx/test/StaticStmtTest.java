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


import java.util.List;
import java.util.Map;
import org.jdbx.JdbxException;
import org.jdbx.QueryCursor;
import org.jdbx.BatchResult;
import org.jdbx.Jdbx;
import org.jdbx.Concurrency;
import org.jdbx.QResultIterator;
import org.jdbx.StaticStmt;
import org.jdbx.UpdateResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class StaticStmtTest extends JdbxTest
{
	@BeforeAll public static void beforeClass() throws JdbxException
	{
		Jdbx.update(con(), "CREATE TABLE STest (id INTEGER IDENTITY PRIMARY KEY, name VARCHAR(30))");
	}


	@BeforeEach public void before() throws JdbxException
	{
		stmt_ = new StaticStmt(con());
		stmt_.update("DELETE FROM STest");
	}


	@AfterEach public void after() throws JdbxException
	{
		stmt_.close();
	}


	@Test public void testQuery() throws JdbxException
	{
		String sql;
		long count;

		sql   = "SELECT COUNT(*) FROM STest";
		count = stmt_.query(sql).row ().col().getInt();
		assertEquals(0, count);

		sql   = "INSERT INTO STest (name) VALUES ('A'), ('B'), ('C'), ('D')";
		count = stmt_.update(sql).count();
		assertEquals(4, count);

		sql   = "SELECT count(*) FROM STest";
		count = stmt_.query(sql).row().col().getInt();
		assertEquals(4, count);

		sql   = "SELECT * FROM STest ORDER BY name DESC";
		List<String> names = stmt_.query(sql).rows().col("NAME").getString();
		assertEquals(4, names.size());
		assertEquals("D", names.get(0));
		assertEquals("C", names.get(1));
		assertEquals("B", names.get(2));
		assertEquals("A", names.get(3));

		names = stmt_.query(sql).skip(1).rows().max(2).col(2 /*=name*/).getString();
		assertEquals(2, names.size());
		assertEquals("C", names.get(0));
		assertEquals("B", names.get(1));

		Map<String,Object> map = stmt_.query(sql).row().map();
		assertEquals(2, map.size());
		assertEquals("D", map.get("NAME"));
		assertTrue(map.get("ID") instanceof Integer);

		Object[] array = stmt_.query(sql).row().cols();
		assertEquals(2, array.length);
		assertTrue(array[0] instanceof Integer);
		assertEquals("D", array[1]);

		List<Dao> users = stmt_.query(sql).rows().read(Dao::new);
		assertEquals(4, users.size());
		Dao userD = users.get(0);
		assertNotNull(userD.id);
		assertEquals(userD.name, "D");
	}


	@Test public void testUpdate() throws JdbxException
	{
		String sql;
		long count;

		sql = "INSERT INTO STest (name) VALUES ('A'), ('B')";
		UpdateResult<List<Integer>> result = stmt_.createUpdate(sql)
			.returnCols("ID")
			.runGetCols(Integer.class);
		assertEquals(2, result.count());
		assertEquals(2, result.value().size());
		int idA = result.value().get(0).intValue();
		int idB = result.value().get(1).intValue();
		assertEquals(idA + 1, idB);

		sql 	= "UPDATE STest SET name = 'BB' WHERE name = 'B'";
		count	= stmt_.update(sql).count();
		assertEquals(1, count);
	}


	@Test public void testExecute() throws JdbxException
	{
		stmt_.createExecute("INSERT INTO STest (name) VALUES ('A')").returnAutoKeyCols().run(r -> {
			assertTrue(r.next());
			assertTrue(r.isUpdateResult());
			UpdateResult<Integer> ur = r.getUpdateResult(Integer.class);
			assertEquals(1, ur.count());
			assertNotNull(ur.value());
			assertFalse(r.next());
		});
	}


	@Test public void testOptions() throws JdbxException
	{
		assertTrue(stmt_.isInitialized());
		assertEquals(Concurrency.READ_ONLY, stmt_.options().getResultConcurrency());
		stmt_.options().setResultConcurrency(Concurrency.CONCUR_UPDATABLE);
		assertEquals(Concurrency.CONCUR_UPDATABLE, stmt_.options().getResultConcurrency());
		stmt_.update("INSERT INTO STest (name) VALUES ('A'), ('B')");
	}


	@Test public void testBatch() throws JdbxException
	{
		stmt_.batch().add("INSERT INTO STest (name) VALUES ('A'), ('B')");
		stmt_.batch().add("INSERT INTO STest (name) VALUES ('C'), ('D')");
		BatchResult<Void> result = stmt_.batch().run();
		result.requireSize(2);
		result.requireCount(0, 2);
		result.requireCount(1, 2);
	}


	public static class Dao
	{
		public Dao(QueryCursor cursor) throws JdbxException
		{
			QResultIterator it = QResultIterator.of(cursor);
			id		= it.getInteger();
			name	= it.getString();
		}


		public Integer id;
		public String name;
	}

	
	private StaticStmt stmt_;
}
