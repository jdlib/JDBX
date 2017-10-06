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


/**
 * A QueryResult based on a JDBC ResultSet.
 */
class ResultSetQuery extends QueryResult
{
	public ResultSetQuery(ResultSet result)
	{
		result_ = Check.notNull(result, "result");
	}


	@Override protected ResultSet runQueryImpl() throws Exception
	{
		return result_;
	}


	@Override protected void cleanup() throws Exception
	{
	}


	@Override protected String toDescription()
	{
		return result_.toString();
	}


	private ResultSet result_;
}