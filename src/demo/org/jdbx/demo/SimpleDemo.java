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
import java.util.List;
import org.jdbx.Jdbx;
import org.jdbx.JdbxException;


/**
 * Code snippets how to use Fdbc for very simple queries and updates.
 * The Fdbc class provides static methods to issue simple queries and updates.
 * All you need is an open JDBC connection.
 * Statements, PreparedStatements and ResultSet are transparently opened as needed.
 * and are guaranteed to be closed.
 */
@SuppressWarnings({"unused"})
public class SimpleDemo
{
	public void run(Connection con) throws JdbxException
	{
		String sql;

		// query without params - read a single int value
		sql = "SELECT COUNT(*) FROM City";
		int count =	Jdbx.createQuery(con, sql)
			.row()				// only read first row
			.col()					// get first column
			.getInt();				// and return as int


		// query a list of string values
		sql = "SELECT size, name FROM City ORDER BY size DESC";
		List<String> names = Jdbx.createQuery(con, sql)
			.rows()				// read all rows
			.col("name")			// get column "name"
			.getString();			// and return as string


		// query a list of complex values using parameters
		sql = "SELECT * FROM City WHERE country = ?";
		List<City> cities = Jdbx.createQuery(con, sql, "fr")
			.rows()				// read all rows
			.read(City::read);		// and create a city object for each row


		// run a parameterless update
		count = Jdbx.update(con, "UPDATE Status SET flag = 'T' WHERE flag = 'F'");


		// run a parameterized insert
		count = Jdbx.update(con, "INSERT INTO Status (flag) VALUES (?)", "F");
	}
}
