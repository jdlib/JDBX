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
package org.jdbx;


import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * Provides helper methods to read values from a JDBC ResultSet.
 */
class ResultUtil
{
	public static Object[] readValues(QueryResult result) throws SQLException
	{
		return toArray(result.getJdbcResult());
	}


	public static Object[] toArray(ResultSet resultSet) throws SQLException
	{
		Object[] values = new Object[resultSet.getMetaData().getColumnCount()];
		for (int i=0; i<values.length; i++)
			values[i] = resultSet.getObject(i+1);
		return values;
	}


	public static Object[] toArray(ResultSet resultSet, int... colNumbers) throws SQLException
	{
		Object[] values = new Object[colNumbers.length];
		for (int i=0; i<colNumbers.length; i++)
			values[i] = resultSet.getObject(colNumbers[i]);
		return values;
	}


	public static Object[] toArray(ResultSet rs, String... colNames) throws SQLException
	{
		Check.notNull(colNames, "column names");
		Object[] values = new Object[colNames.length];
		for (int i=0; i<colNames.length; i++)
			values[i] = rs.getObject(colNames[i]);
		return values;
	}


	public static Map<String,Object> readMap(QueryResult result) throws SQLException
	{
		return toMap(result.getJdbcResult());
	}


	public static Map<String,Object> toMap(ResultSet rs) throws SQLException
	{
		Map<String,Object> map = new LinkedHashMap<>(); // obeys insertion order
		ResultSetMetaData md = rs.getMetaData();
		int count = md.getColumnCount();
		for (int colNumber=1; colNumber<=count; colNumber++)
		{
			String name = getName(md, colNumber);
			map.put(name, rs.getObject(colNumber));
		}
		return map;
	}


	public static Map<String,Object> toMap(ResultSet rs, String... colNames) throws SQLException
	{
		Check.notNull(colNames, "column names");
		Map<String,Object> map = new LinkedHashMap<>(); // obeys insertion order
		for (String colName : colNames)
			map.put(colName, rs.getObject(colName));
		return map;
	}


	public static Map<String,Object> toMap(ResultSet rs, int... colNumbers) throws SQLException
	{
		Map<String,Object> map = new LinkedHashMap<>(); // obeys insertion order
		ResultSetMetaData md = rs.getMetaData();
		for (int colNumber : colNumbers)
		{
			String name = getName(md, colNumber);
			map.put(name, rs.getObject(colNumber));
		}
		return map;
	}


	public static String getName(ResultSetMetaData md, int colNumber) throws SQLException
	{
		String name = md.getColumnLabel(colNumber);  // "as" clause specified
		if (isEmpty(name))
			name = md.getColumnName(colNumber);
		if (isEmpty(name))
			name = String.valueOf(colNumber);
		return name;
	}


	private static boolean isEmpty(String s)
	{
		return s == null || s.isEmpty();
	}
}
