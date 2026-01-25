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


public class SqlFrom
{
	private ClauseBuilder cb_ = new ClauseBuilder(" ");


	ClauseBuilder builder()
	{
		return cb_;
	}


	public SqlFrom add(String item)
	{
		cb_.add(item);
		return this;
	}


	public SqlFrom add(String... items)
	{
		for (String item : items)
			cb_.add(item);
		return this;
	}


	public SqlFrom comma()
	{
		if (!cb_.isEmpty())
			cb_.addDirect(",");
		return this;
	}


	public boolean isEmpty()
	{
		return cb_.isEmpty();
	}


	@Override public String toString()
	{
		return cb_.toString();
	}
}
