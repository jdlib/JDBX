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


import java.sql.SQLException;
import org.jdbx.ExecuteResult;
import org.jdbx.StaticStmt;


/**
 * ExecuteDemo shows how to use SQL batch processing with JDBX.
 */
public class ExecuteDemo
{
	public void run(StaticStmt stmt) throws SQLException
	{
		@SuppressWarnings("unused")
		String sql = "SELECT * FROM Orders WHERE foo = 1 and bar = 2";

		// start with sql + connection
//		@SuppressWarnings("unused")
//		String s = stmt.(sql, con).run(this::computeResult);

		// start with sql + statement
//		FDBC.execute(sql, stmt).run(this::acceptResult);
	}


	@SuppressWarnings("unused")
	private void acceptResult(ExecuteResult result)
	{
	}


	@SuppressWarnings("unused")
	private String computeResult(ExecuteResult result)
	{
		return "theResult";
	}
}
