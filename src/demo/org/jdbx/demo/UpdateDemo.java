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


import org.jdbx.JdbException;
import org.jdbx.StaticStmt;


public class UpdateDemo
{
	@SuppressWarnings("unused")
	public static void stmt(StaticStmt stmt) throws JdbException
	{
		String sql1 	= "INSERT INTO X VALUES('a', 'b')";
		int count  		= stmt.createUpdate(sql1).reportAutoKeys().run(); // todo receivce autokeys

		String sql2		= "SELECT amount FROM Orders WHERE id = 1";
		double amount 	= stmt.createQuery(sql2).row().col().getDouble();
	}


	public void run()
	{
		/*
		PreparedStatement pstmt = ...
		FDBC.update(pstmt).getCount();
		FDBC.update(pstmt, ResultConsumer autoKeys) -> int / long

		FDBC.update(stmt, sql, resultconsumer) ->
		FDBC.update(pstmt)


		FDBC.planUpdate(pstmt).run();
		FDBC.planUpdate(pstmt).runLarge();
		FDBC.planExecute()

		 */


	}
}
