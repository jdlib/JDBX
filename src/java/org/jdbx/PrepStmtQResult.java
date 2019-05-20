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


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.jdbx.function.CheckedSupplier;


/**
 * A QueryResult implementation which is based on a PreparedStatement.
 */
class PrepStmtQResult extends QueryResult
{
	public PrepStmtQResult(CheckedSupplier<PreparedStatement> supplier)
	{
		supplier_ = supplier;
	}


	@Override protected ResultSet runQueryImpl() throws Exception
	{
		return supplier_.get().executeQuery();
	}


	@Override protected void cleanup() throws Exception
	{
	}


	@Override protected String toDescription()
	{
		return supplier_.toString();
	}


	private final CheckedSupplier<PreparedStatement> supplier_;
}
