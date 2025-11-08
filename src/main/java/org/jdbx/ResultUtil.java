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
import java.util.HashMap;
import java.util.Map;


/**
 * Provides helper methods to read values from a JDBC ResultSet.
 */
class ResultUtil
{
	public static Object[] readValues(QueryResult result) throws SQLException
	{
		return readValues(result.getJdbcResult());
	}

	
	public static Object[] readValues(ResultSet resultSet) throws SQLException
	{
		Object[] values = new Object[resultSet.getMetaData().getColumnCount()];
		for (int i=0; i<values.length; i++)
			values[i] = resultSet.getObject(i+1);
		return values;
	}


	public static Object[] readValues(ResultSet resultSet, int... colNrs) throws SQLException
	{
		Object[] values = new Object[colNrs.length];
		for (int i=0; i<colNrs.length; i++)
			values[i] = resultSet.getObject(colNrs[i]);
		return values;
	}


	public static Object[] readValues(ResultSet rs, String... colNames) throws SQLException
	{
		Check.notNull(colNames, "column names");
		Object[] values = new Object[colNames.length];
		for (int i=0; i<colNames.length; i++)
			values[i] = rs.getObject(colNames[i]);
		return values;
	}


	public static Map<String,Object> readMap(QueryResult result) throws SQLException
	{
		return readMap(result.getJdbcResult());
	}
	
	
	public static Map<String,Object> readMap(ResultSet rs) throws SQLException
	{
		HashMap<String,Object> map = new HashMap<>();
		ResultSetMetaData md = rs.getMetaData();
		int count = md.getColumnCount();
		for (int i=1; i<=count; i++)
		{
			String name = md.getColumnLabel(i);  // if as clause is specified
			if ((name == null) || (name.length() == 0))
				name = md.getColumnName(i);
			map.put(name, rs.getObject(i));
		}
		return map;
	}


	@SuppressWarnings("unchecked")
	public static <T> Map<String,T> readMap(ResultSet rs, String... colNames) throws SQLException
	{
		Check.notNull(colNames, "column names");
		HashMap<String,T> map = new HashMap<>();
		for (String colName : colNames)
			map.put(colName, (T)rs.getObject(colName));
		return map;
	}
}
