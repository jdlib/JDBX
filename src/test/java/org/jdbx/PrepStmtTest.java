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


import java.sql.ParameterMetaData;
import java.sql.ResultSetMetaData;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class PrepStmtTest extends JdbxTest
{
	@BeforeAll public static void beforeAll() throws JdbxException
	{
		Jdbx.update(con(), "CREATE TABLE ptests (id INTEGER IDENTITY PRIMARY KEY, name VARCHAR(30), type INTEGER NOT NULL)");
	}


	@BeforeEach public void beforeEach() throws JdbxException
	{
		Jdbx.update(con(), "DELETE FROM ptests");
		pstmt_ = new PrepStmt(con());
	}


	@AfterEach public void afterEach() throws JdbxException
	{
		if (pstmt_ != null)
			pstmt_.close();
	}


	@Test public void testMisc() throws Exception
	{
		assertFalse(pstmt_.isInitialized());
		assertTrue(pstmt_.toString().startsWith(PrepStmt.class.getName()));

		// insert a single record and remember the generated id
		final String insert = "INSERT INTO ptests VALUES (DEFAULT, ?, ?)";
		pstmt_.init().returnCols(1).sql(insert);
		assertTrue(pstmt_.isInitialized());
		assertTrue(pstmt_.toString().endsWith('[' + insert + ']'));

		pstmt_.clearParams(); // coverage
		pstmt_.params("a", 1);
		Integer idA = pstmt_.createUpdate().runGetCol(Integer.class)
			.requireCount(1)
			.requireValue();

		// insert two records using a batch
		pstmt_.param(1, "c").param(2, 2).batch().add();
		pstmt_.param(1, "d");
		pstmt_.param(2).setInt(3);
		pstmt_.batch().add();
		BatchResult<List<Integer>> result = pstmt_.batch().runGetCols(Integer.class);
		assertEquals(2, result.size());
		result.requireCount(0, 1);
		result.requireCount(1, 1);
		assertEquals(List.of(idA +1, idA + 2), result.value());

		// read a row by id
		pstmt_.init("SELECT * FROM ptests WHERE id = ?");
		Dao dao = pstmt_.params(idA).query().row().read(Dao::read);
		assertNotNull(dao);
		assertEquals(idA, 	dao.id);
		assertEquals("a", 	dao.name);
		assertEquals(1, 	dao.type);
	}


	@Test public void testNamedParams()
	{
		final String sql = "INSERT INTO ptests VALUES (DEFAULT, :name, :type)";
		// insert a single record using named parameters
		pstmt_.init().namedParams().sql(sql);
		pstmt_.param("name").setString("b");
		pstmt_.param("type").setInt(15);
		assertEquals("sql command does not contain parameter 'unknown'",
			assertThrows(IllegalArgumentException.class, () -> pstmt_.param("unknown")).getMessage());
		assertEquals(1, pstmt_.update().count());

		NamedParamCmd npcmd = new NamedParamCmd(sql);
		pstmt_.init().sql(npcmd);
		pstmt_.param("name").setString("c");
		pstmt_.param("type").setInt(16);
		assertEquals(1, pstmt_.update().count());

		pstmt_.init().sql("INSERT INTO ptests VALUES (DEFAULT, ?, ?)");
		assertEquals("statement is not named: use init().named().cmd(sql) to create a named statement",
			assertThrows(IllegalArgumentException.class, () -> pstmt_.param("name")).getMessage());
	}


	@Test public void testMetaData() throws Exception
	{
		pstmt_.init("SELECT name FROM ptests WHERE id = ?");
		ResultSetMetaData rsmd = pstmt_.getMetaData();
		assertNotNull(rsmd);
		assertEquals(1, rsmd.getColumnCount());
		assertEquals("NAME", rsmd.getColumnName(1));

		ParameterMetaData pmd = pstmt_.getParamMetaData();
		assertNotNull(pmd);
		assertEquals(1, pmd.getParameterCount());
		assertEquals(java.sql.Types.INTEGER, pmd.getParameterType(1));
	}


	@Test public void testInitVariants() throws Exception
	{
		final String sql = "INSERT INTO ptests VALUES (DEFAULT, ?, ?)";

		pstmt_.options().setPoolable(true); // coverage, apply options to newly created JDBC statement

		pstmt_.init().returnCol("id").sql(sql);
		pstmt_.params("x", 5).createUpdate().runGetCol(Integer.class)
			.requireCount(1)
			.requireValue();

		pstmt_.init().returnCol(1).sql(sql);
		pstmt_.params("x", 5).createUpdate().runGetCol(Integer.class)
			.requireCount(1)
			.requireValue();

		pstmt_.init().returnAutoKeyCols().sql(sql);
		pstmt_.params("x", 5).createUpdate().runGetCol(Integer.class)
			.requireCount(1)
			.requireValue();
	}


	public static class Dao
	{
		public static Dao read(QueryResult result) throws JdbxException
		{
			Dao dao 	 = new Dao();
			dao.id   = result.nextCol().getInteger();
			dao.name	 = result.nextCol().getString();
			dao.type	 = result.nextCol().getInt();
			return dao;
		}


		public Integer id;
		public String name;
		public int type;
	}


	private PrepStmt pstmt_;
}
