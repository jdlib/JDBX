/*
 * Copyright (C) 2025 JDBX
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
package org.jdbx.sql;


public class SqlInsert
{
	private final String table_;
	private ClauseBuilder columns_ = new ClauseBuilder(", ");
	private ClauseBuilder values_ = new ClauseBuilder(", ");


	public SqlInsert(String table)
	{
		table_ = table;
	}


	public SqlInsert col(String name)
	{
		columns_.add(name);
		return this;
	}


	public SqlInsert value(String value)
	{
		values_.add(value);
		return this;
	}


	public SqlInsert valParam()
	{
		return value("?");
	}


	public SqlInsert colValue(String name, String value)
	{
		col(name);
		return value(value);
	}


	public SqlInsert colParam(String name)
	{
		return colValue(name, "?");
	}


	@Override public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO ").append(table_);
		columns_.addTo(sb, " (", ")");
		values_.addTo(sb, " VALUES (", ")");
		return sb.toString();
	}
}
