/*
 * Copyright (C) 2018 JDBX
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
import org.jdbx.CallStmt;
import org.jdbx.JdbxException;


/**
 * Demonstrates the use of {@link CallStmt}
 * to call SQL stored procedures.
 */
@SuppressWarnings("unused")
public class CallStmtDemo
{
	/**
	 * How to create a PrepStmt.
	 */
	public void createDemo(Connection con, DataSource ds)
	{
		// create a CallStmt from a connection
		try (CallStmt stmt = new CallStmt(con))
		{
			// do stuff
		}


		// create a CallStmt from a datasource
		// obtains a connection from the datasource and automatically closes the connection once done
		try (CallStmt stmt = new CallStmt(ds))
		{
			// do stuff
		}
	}
	
	
	// TODO query, execute, return out param
	
	
	/**
	 * How to run prepared commands with different parameters in a batch.
	 */
	public void batchDemo(CallStmt stmt, String... cityNames) throws JdbxException
	{
		stmt.init("{call CreateCity(?)}");
		for (String cityName : cityNames)
		{
			stmt.param("name").set(cityName);
			stmt.batch().add();
		}
		
		BatchResult<Void> result = stmt.batch().run();
		// ... evaluate result
	}
}
