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
import javax.sql.DataSource;
import org.jdbx.BatchResult;
import org.jdbx.Concurrency;
import org.jdbx.ExecuteResult;
import org.jdbx.FetchDirection;
import org.jdbx.QueryResult;
import org.jdbx.StaticStmt;
import org.jdbx.UpdateResult;


/**
 * Demonstrates the use of {@link StaticStmt} to
 * execute parameterless SQL or DDL commands.
 */
@SuppressWarnings("unused")
public class StaticStmtDemo
{
	/**
	 * How to create a StaticStmt.
	 */
	public void createDemo(Connection con, DataSource ds)
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
	 * How to set and get options. 
	 */
	public void optionsDemo(StaticStmt stmt)
	{
		stmt.options()
			.setCloseOnCompletion()
			.setFetchDirection(FetchDirection.REVERSE)
			.setQueryTimeoutSeconds(15)
			.setResultConcurrency(Concurrency.CONCUR_UPDATABLE);
		
		int queryTimeOut = stmt.options().getQueryTimeoutSeconds();
	}
		
		
	/**
	 * How to run a SQL query.
	 * For more query demos, see {@link QueryDemo}
	 * See {@link QueryDemo} for more details on how to configure the query and extract query results.
	 */
	public void queryDemo(StaticStmt stmt)
	{
		// SQL query string is passed to StaticStmt.query()
		int userCount = stmt.query("SELECT count(*) FROM Users").row().col().getInt();
	}


	/**
	 * How to run an updating SQL or DDL command.
	 * For more update demos, see {@link UpdateDemo}
	 * See {@link UpdateDemo} for more details on how to configure the update and extract update results.
	 */
	public void updateDemo(StaticStmt stmt)
	{
		// SQL command string is passed to StaticStmt.update or StaticStmt.createUpdate

		// simply getting the update count
		int updated = stmt.update("INSERT INTO User VALUES (DEFAULT, 'John', 'Doe')").count();

	    // updating, and returning generated keys
		Integer newUserId = stmt
			.createUpdate("INSERT INTO User VALUES (DEFAULT, 'John', 'Doe')")	// the cmd
			.returnCols("id") 			// contains the auto generated key
			.runGetCol(Integer.class) 	// and we want it as integer
			.requireCount(1) 			// assert that 1 one record was inserted
			.requireValue(); 			// assert that an id was generated and return the value
	}


	/**
	 * How to run a SQL command which may return multiple result sets.
	 * For more update demos, see {@link ExecuteDemo}
	 * See {@link ExecuteDemo} for more details on how to configure the execute and extract execute results.
	 */
	public void executeDemo(StaticStmt stmt, String sql)
	{
		// SQL command string is passed to StaticStmt.createExecute or StaticStmt.execute.
		// The command may return multiple result or we don't know what it returns

		ExecuteResult result = stmt.createExecute(sql).run();
		while (result.next())
		{
			if (result.isUpdateResult())
			{
				UpdateResult<Void> ur = result.getUpdateResult();
				// ... evaluate the update result
			}
			else
			{
				QueryResult qr = result.getQueryResult();
				// ... read values from query result 
			}
		}
	}


	/**
	 * How to run static commands in a batch.
	 * See {@link BatchDemo} for more details on how to configure the batch and extract batch results.
	 */
	public void batchDemo(StaticStmt stmt)
	{
		BatchResult<Void> result = stmt.batch()
			.add("UPDATE Status1 SET flag1 = 1")
			.add("UPDATE Status2 SET flag2 = 2")
			.add("UPDATE Status3 SET flag3 = 3")
			.run();
		// ... evaluate result
	}
}
