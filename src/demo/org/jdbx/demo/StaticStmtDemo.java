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
import java.sql.ResultSet;
import javax.sql.DataSource;
import org.jdbx.ExecuteResult;
import org.jdbx.StaticStmt;


/**
 * Demonstrates {@link StaticStmt}.
 */
@SuppressWarnings("unused")
public class StaticStmtDemo
{
	/**
	 * How to create a StaticStmt.
	 */
	public void create(Connection con, DataSource ds)
	{
		// create a StaticStmt from a connection
		try (StaticStmt stmt = new StaticStmt(con))
		{
			// do stuff
		}


		// create a StaticStmt from a datasource
		// - obtains a connection and automatically closes the connection
		try (StaticStmt stmt = new StaticStmt(ds))
		{
			// do stuff
		}
	}


	/**
	 * How to run a SQL query.
	 * For more query demos, see {@link QueryDemo}
	 */
	private void query(StaticStmt stmt)
	{
		// SQL query string is passed to StaticStmt.createQuery
		int userCount = stmt.createQuery("SELECT count(* FROM Users").row().col().getInt();
	}


	/**
	 * How to run an updating SQL or DDL command.
	 * For more update demos, see {@link UpdateDemo}
	 */
	private void update(StaticStmt stmt)
	{
		// SQL command string is passed to StaticStmt.update or StaticStmt.createUpdate

		// simply getting the update count
		int updated = stmt.update("INSERT INTO User VALUES (DEFAULT, 'John', 'Doe')");

	    // updating, and returning generated keys
		Integer newUserId = stmt
			.createUpdate("INSERT INTO User VALUES (DEFAULT, 'John', 'Doe')")	// the cmd
			.returnCols("id") // contains the auto generated key
			.runGetAutoKey(Integer.class) // and we want it as integer
			.checkCount(1) // assert that 1 one record was inserted
			.checkHasValue(); // assert that an id was generated and return the value
	}


	/**
	 * How to run a SQL command which may return multiple result sets.
	 * For more update demos, see {@link ExecuteDemo}
	 */
	private void execute(StaticStmt stmt)
	{
		// SQL command string is passed to StaticStmt.createExecute or StaticStmt.createUpdate

		String sql = null; // assume command which is not know what it returns

		ExecuteResult result = stmt.createExecute(sql).run();
		while (result.next())
		{
			if (result.isUpdateCount())
			{
				int updateCount = result.getUpdateCount();
				// evaluate updateCount
			}
			else
			{
				ResultSet rs = result.getResultSet();
				// evaluate result set
			}
		}
	}


	/**
	 * How to run commands in a batch.
	 * For more batch demos, see {@link BatchDemo}
	 */
	private void batch(StaticStmt stmt)
	{
		// SQL command string is passed to StaticStmt.Batch.add

		int[] updateCounts = stmt.batch()
			.add("UPDATE Status1 SET flag1 = 1")
			.add("UPDATE Status2 SET flag2 = 2")
			.add("UPDATE Status3 SET flag3 = 3")
			.run();
	}
}
