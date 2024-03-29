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
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.jdbx.BatchResult;
import org.jdbx.JdbxException;
import org.jdbx.PrepStmt;


/**
 * Demonstrates the use pf {@link PrepStmt}
 * to run parameterized SQL commands.
 */
@SuppressWarnings("unused")
public class PrepStmtDemo
{
	/**
	 * How to create a PrepStmt.
	 */
	public void createDemo(Connection con, DataSource ds)
	{
		// create a PrepStmt from a connection
		try (PrepStmt stmt = new PrepStmt(con))
		{
			// do stuff
		}


		// create a PrepStmt from a datasource
		// obtains a connection from the datasource and automatically closes the connection once done
		try (PrepStmt stmt = new PrepStmt(ds))
		{
			// do stuff
		}
	}


	/**
	 * How to set and get options. 
	 */
	public void optionsDemo(PrepStmt stmt)
	{
		stmt.options()
			.setCursorName("temp")
			.setQueryTimeoutSeconds(15);
		
		int queryTimeOut = stmt.options().getQueryTimeoutSeconds();
	}
		
		
	// TODO copy other methods from StaticStmtDemo


	/**
	 * How to run update commands.
	 * For more update demos, see {@link UpdateDemo}
	 * See {@link UpdateDemo} for more details on how to configure the update and extract update results.
	 */
	public void updateDemo(PrepStmt pstmt)
	{
		// 1. SQL command string is passed to PrepStmt.init()
		pstmt.init("UPDATE Colors SET used = 1 WHERE name = ?");

		// simply getting the update count
		long updated = pstmt.params("red").update().count();

		// 2. initialize and instruct the statement to return generated keys.
		pstmt.init().returnCols("id").sql("INSERT INTO User VALUES (DEFAULT, ?, ?)");

	    // updating, and returning generated keys
		Integer newUserId = pstmt
			.params("John", "Doe") // set parameters
			.createUpdate() // we want to run the cmd as updating cmd
			.runGetCol(Integer.class) // we want the generated key as integer
			.requireCount(1) // assert that 1 one record was inserted
			.requireValue(); // assert that an id was generated and return the value
	}


	/**
	 * Shows a typical code snippet how plain JDBC uses PreparedStatement.
	 * Compare this to {@link #jdbxCodeDemo(Connection, List)}
	 */
	public List<Integer> jdbcCodeDemo(Connection con, List<String> names) throws SQLException
	{
		List<Integer> ids = new ArrayList<>();
		try (PreparedStatement pstmt = con.prepareStatement("INSERT INTO Cities (name) VALUES (?)", new String[] { "id "}))
		{
			for (String name : names)
			{
				pstmt.setString(1, name);
				if (pstmt.executeUpdate() != 1)
					throw new IllegalStateException("insert failed");

				try (ResultSet rs = pstmt.getGeneratedKeys())
				{
					if (!rs.next())
						throw new IllegalStateException("no id returned");
					ids.add(rs.getObject(1, Integer.class));
				}
			}
		}
		return ids;
	}


	/**
	 * Shows a typical code snippet how JDBX uses PrepStats.
	 * Compare this to {@link #jdbcCodeDemo(Connection, List)}
	 */
	@SuppressWarnings("resource")
	public List<Integer> jdbxCodeDemo(Connection con, List<String> names) throws JdbxException
	{
		List<Integer> ids = new ArrayList<>();
		try (PrepStmt pstmt = new PrepStmt(con))
		{
			pstmt.init().returnCols("id").sql("INSERT INTO Cities (name) VALUES (?)");
			for (String name : names)
				ids.add(pstmt.param(1, name).createUpdate().runGetCol(Integer.class).requireCount(1).requireValue());
		}
		return ids;
	}


	/**
	 * How to run prepared commands with different parameters in a batch.
	 */
	public void batchDemo(PrepStmt stmt, int... ids) throws JdbxException
	{
		stmt.init("UPDATE Status SET flag = 1 WHERE id = ?");
		for (int id : ids)
		{
			stmt.param(1).setInt(id);
			stmt.batch().add();
		}
		BatchResult<Void> result = stmt.batch().run();
		// ... evaluate result
	}
}
