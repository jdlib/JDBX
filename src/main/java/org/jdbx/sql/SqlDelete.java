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

import java.util.function.Consumer;

public class SqlDelete
{
	private final String table_;
	private SqlExpr where_ = new SqlExpr();


	public SqlDelete(String table)
	{
		table_ = table;
	}


	public SqlDelete where(String item)
	{
		where().add(item);
		return this;
	}


	SqlDelete where(Consumer<SqlExpr> consumer)
	{
		consumer.accept(where());
		return this;
	}


	public SqlExpr where()
	{
		return where_;
	}


	@Override public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("DELETE FROM ").append(table_);
		where_.builder().addTo(sb, " WHERE ");
		return sb.toString();
	}
}
