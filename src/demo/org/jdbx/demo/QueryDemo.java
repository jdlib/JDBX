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


import java.math.BigDecimal;
import java.sql.*;
import java.util.List;
import org.jdbx.JdbxException;
import org.jdbx.PrepStmt;
import org.jdbx.StaticStmt;


@SuppressWarnings({"unused", "boxing"})
public class QueryDemo
{
	public static void start()
	{
		String sql				= "SELECT * FROM Orders WHERE foo = 1 and bar = 2";
		String psql				= "SELECT * FROM Orders WHERE foo = ? and bar = ?";
		Connection con 			= null; // ...
		Statement stmt 			= null; // ...
		PreparedStatement pstmt	= null; // ...
		ResultSet result		= null; // ...

//		FDBC.query(sql, con);			// ... from sql + connection
//		FDBC.query(result);				// ... from result
//		FDBC.query(pstmt);				// ... prepared statement
//		FDBC.query(pstmt, "a", 2);		// ... prepared statement + parameter
//		FDBC.query(result);				// ... invoke on result
	}


	public static void handleErrors(StaticStmt stmt)
	{														// this happens in case of an error:
//		stmt.query("SELECT count(*) FROM Orders")		    // throws SQLException
//			.onErrorThrow(IOException::new)					// throws IOException
//			.onErrorThrow(IllegalStateException::new)		// throws IllegalStateException
//			.onErrorLog(Throwable::printStackTrace)			// throws nothing, print stacktrace to System.err, returns null
//			.onErrorLog(QueryDemo::logError, 0)				// throws nothing, calls logError, returns 0
//			.onErrorReturn(-1)								// throws nothing, returns -1
//			.onError(e-> { throw new Error(e); }, null);	// executes the lambda, i.e. throws an Error
	}


	private static void logError(Exception e)
	{
		System.err.println(e.getMessage());
	}


	public static void stmt(StaticStmt stmt) throws JdbxException
	{
		String sql1 	= "SELECT count(*) FROM Orders";
		int count  		= stmt.query(sql1).row().col().getInt();

		String sql2		= "SELECT amount FROM Orders WHERE id = 1";
		double amount 	= stmt.query(sql2).row().col().getDouble(0.0);
	}


	public static void run(PrepStmt pstmt) throws Exception
	{
		BigDecimal amount 	= pstmt.params(4235).query().row().col().getBigDecimal();

		pstmt.param(1, "a");
		List<Double> list 	= pstmt.query().rows().col().getDouble();

		List<Date> dates 	= pstmt.query().skip(2).rows().max(5).col().getSqlDate();
	}


	public static void queryFirst(PreparedStatement pstmt) throws Exception
	{
	}
}
