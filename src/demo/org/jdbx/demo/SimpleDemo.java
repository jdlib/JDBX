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
 * Code snippets how to use Jdbx for very simple queries and updates.
 * The {@link Jdbx} class provides static methods to issue simple queries and updates.
 * All you need is an open JDBC connection.
 */
@SuppressWarnings({"unused"})
public class SimpleDemo
{
	public void run(Connection con) throws JdbxException
	{
		String sql;

		// query without params - read a single int value
		sql = "SELECT COUNT(*) FROM Cities";
		int nrOfCities = Jdbx.query(con, sql)
			.row()				// only read first row
			.col()				// get first column
			.getInt();			// and return as int


		// query a list of string values
		sql = "SELECT size, name FROM Cities ORDER BY size DESC";
		List<String> names = Jdbx.query(con, sql)
			.rows()				// read all rows
			.col("name")		// get column "name"
			.getString();		// and return as string


		// query a list of complex values using parameters
		sql = "SELECT * FROM Cities WHERE country = ?";
		List<City> cities = Jdbx.query(con, sql, "fr")
			.rows()				// read all rows
			.read(City::read);	// and create a city object for each row


		long count;
		// run a parameterless update
		count = Jdbx.update(con, "UPDATE Status SET flag = 'T' WHERE flag = 'F'").count();

		// run a parameterized insert
		count = Jdbx.update(con, "INSERT INTO Status (flag) VALUES (?)", "F").count();
	}
}
