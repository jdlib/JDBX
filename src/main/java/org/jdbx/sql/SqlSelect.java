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


public class SqlSelect
{
	private final ClauseBuilder out_ = new ClauseBuilder(", ");
	private final SqlFrom from_ = new SqlFrom();
	private final SqlExpr where_ = new SqlExpr();
	private ClauseBuilder groupBy_;
	private SqlExpr having_;
	private ClauseBuilder orderBy_;


	public SqlSelect out(String item)
	{
		out_.add(item);
		return this;
	}


	public SqlSelect from(String item)
	{
		from_.add(item);
		return this;
	}


	public SqlSelect from(String... items)
	{
		from_.add(items);
		return this;
	}


	public SqlSelect from(Consumer<SqlFrom> from)
	{
		if (from != null)
			from.accept(from_);
		return this;
	}


	public SqlSelect where(String item)
	{
		where().add(item);
		return this;
	}


	public SqlSelect where(Consumer<SqlExpr> consumer)
	{
		consumer.accept(where());
		return this;
	}


	public SqlExpr where()
	{
		return where_;
	}


	public SqlSelect groupBy(String item)
	{
		if (groupBy_ == null)
			groupBy_ = new ClauseBuilder(", ");
		groupBy_.add(item);
		return this;
	}


	public SqlSelect having(String item)
	{
		having().add(item);
		return this;
	}


	public SqlSelect having(Consumer<SqlExpr> consumer)
	{
		consumer.accept(having());
		return this;
	}


	public SqlExpr having()
	{
		if (having_ == null)
			having_ = new SqlExpr();
		return having_;
	}


	public SqlSelect orderBy(String item)
	{
		if (orderBy_ == null)
			orderBy_ = new ClauseBuilder(", ");
		orderBy_.add(item);
		return this;
	}


	@Override public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT");
		out_.addTo(sb, " ");
		from_.builder().addTo(sb, " FROM ");
		where_.builder().addTo(sb, " WHERE ");
		if (groupBy_ != null)
			groupBy_.addTo(sb, " GROUP BY ");
		if (having_ != null)
			having_.builder().addTo(sb, " HAVING ");
		if (orderBy_ != null)
			orderBy_.addTo(sb, " ORDER BY ");
		return sb.toString();
	}
}
