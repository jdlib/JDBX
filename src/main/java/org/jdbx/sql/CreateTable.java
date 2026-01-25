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


import java.util.ArrayList;
import java.util.List;


public class CreateTable
{
	private final String table_;
	private final List<String> columns_ = new ArrayList<>();
	private String indent_; // if not null we are in multiline mode
	private boolean ifNotExists_;
	private String suffix_;


	public CreateTable(String table)
	{
		table_ = table;
	}


	public CreateTable multiLine()
	{
		return multiLine("  ");
	}


	public CreateTable multiLine(String indent)
	{
		indent_ = indent;
		return this;
	}


	public CreateTable ifNotExists()
	{
		ifNotExists_ = true;
		return this;
	}


	public CreateTable col(String col)
	{
		columns_.add(col);
		return this;
	}


	public CreateTable col(String... declParts)
	{
		return col(String.join(" ", declParts));
	}


	public CreateTable suffix(String suffix)
	{
		suffix_ = suffix;
		return this;
	}


	@Override public String toString()
	{
		StmtBuilder sb = new StmtBuilder(indent_);
		sb.append("CREATE TABLE ");
		if (ifNotExists_)
			sb.append("IF NOT EXISTS ");
		sb.append(table_).lnOrSep().append('(');
		for (int i=0; i<columns_.size(); i++)
		{
			if (i > 0)
				sb.append(',').sep();
			sb.ln().indent();
			sb.append(columns_.get(i));
		}
		sb.ln().append(')');
		if (suffix_ != null)
			sb.lnOrSep().append(suffix_);
		return sb.toString();
	}
}

/*
space or ln					create table^(
-     or ln indent
space or ln indent
-     or ln


(
	a int,
	b int
)


ln indent
ln

*/