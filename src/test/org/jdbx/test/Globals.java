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


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.jdbx.JdbxException;


public class Globals
{
	public static synchronized Connection con() throws JdbxException
	{
		if (con_ == null)
			init();
		return con_;
	}


	private static void init() throws JdbxException
	{
		try
		{
			Connection con = DriverManager.getConnection("jdbc:hsqldb:mem:mymemdb", "sa", "");
			con_ = con;
		}
		catch (SQLException e)
		{
			throw JdbxException.of(e);
		}
	}


	private static Connection con_;
}
