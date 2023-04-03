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
package org.jdbx.demo;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.jdbx.JdbxException;
import org.jdbx.Jdbx;
import org.jdbx.PrepStmt;


/**
 * Contains the examples from README.md
 */
public class TeaserDemo
{
	/**
	 * Using JDBC:
	 * - perform a parameterized insert
	 * - return the auto generated primary key
     * - convert SQLException in a runtime exception
	 */
	public Integer oldJdbcInsertWithAutoKeys(Connection con, String firstName, String lastName)
	{
		try (PreparedStatement pstmt = con.prepareStatement("INSERT INTO Users VALUES (DEFAULT, ?, ?)",
			new String[] { "id" }))
		{
			pstmt.setString(1, firstName);
			pstmt.setString(2, lastName);
			if (pstmt.executeUpdate() != 1)
				throw new IllegalStateException("insert failed");
			Integer id = null;
			ResultSet result = pstmt.getGeneratedKeys();
			if (result.next())
				id = result.getObject(1, Integer.class);
			if (id == null)
				throw new IllegalStateException("id not returned");
			return id;
		}
		catch (SQLException e)
		{
			throw new IllegalStateException("sql error", e);
		}
	}


	/**
	 * JDBX version of {@link #oldJdbcInsertWithAutoKeys(Connection, String, String)}:
	 */
	public Integer newJdbxInsertWithAutoKeys(Connection con, String firstName, String lastName) throws JdbxException
	{
		try (PrepStmt pstmt = new PrepStmt(con))
		{
			pstmt.init().returnCols("id").sql("INSERT INTO Users VALUES (DEFAULT, ?, ?)");
			pstmt.params(firstName, lastName);
			return pstmt.createUpdate().runGetCol(Integer.class).requireCount(1).requireValue();
		}
	}


	/**
	 * Using JDBC:
	 * - perform a SQL select
	 * - create a Bean from every result row
     * - return all beans in a list.
	 */
	public List<City> oldJdbcQueryBeanList(Connection con) throws SQLException
	{
		try (Statement stmt = con.createStatement())
		{
			List<City> list = new ArrayList<>();
			ResultSet result = stmt.executeQuery("SELECT * FROM Cities ORDER BY name");
			while (result.next())
				list.add(City.read(result));
			return list;
		}
	}


	/**
	 * JDBX version of {@link #oldJdbcQueryBeanList(Connection)}:
	 */
	public List<City> newJdbxQueryBeanList(Connection con) throws JdbxException
	{
		return Jdbx.query(con, "SELECT * FROM Cities ORDER BY name").rows().read(City::read);
	}
}
