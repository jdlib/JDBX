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


class StmtBuilder
{
	private final StringBuilder sb_ = new StringBuilder();
	private final String indent_;

	public StmtBuilder(String indent)
	{
		indent_ = indent;
	}


	public StmtBuilder append(String s)
	{
		sb_.append(s);
		return this;
	}


	public StmtBuilder append(char c)
	{
		sb_.append(c);
		return this;
	}


	public StmtBuilder sep()
	{
		if (indent_ == null)
			sb_.append(' ');
		return this;
	}


	public StmtBuilder lnOrSep()
	{
		if (indent_ != null)
			sb_.append(System.lineSeparator());
		else
			sb_.append(' ');
		return this;
	}


	public StmtBuilder ln()
	{
		if (indent_ != null)
			sb_.append(System.lineSeparator());
		return this;
	}


	public StmtBuilder indent()
	{
		if (indent_ != null)
			sb_.append(indent_);
		return this;
	}


	@Override
	public String toString()
	{
		return sb_.toString();
	}
}
