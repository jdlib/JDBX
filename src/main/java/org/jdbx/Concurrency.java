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
 * Concurrency is an Enum for the JDBC constants ResultSet.CONCUR_*.
 * Given a JDBX statement object, you can change the result concurrency as follows:
 * <p><code>stmt.options().resultConcurrency(Concurrency.nnn)</code>
 * @see Stmt#options()
 * @see StmtOptions#setResultConcurrency(Concurrency)
 * @see StmtOptions#getResultConcurrency()
 */
public enum Concurrency implements JdbcEnum
{
	/**
	 * See {@link ResultSet#CONCUR_READ_ONLY}.
	 */
	READ_ONLY(ResultSet.CONCUR_READ_ONLY),

	/**
	 * See {@link ResultSet#CONCUR_UPDATABLE}.
	 */
	CONCUR_UPDATABLE(ResultSet.CONCUR_UPDATABLE),


	INVALID(-1);


	static final JdbcEnumMap<Concurrency> map = new JdbcEnumMap<>(INVALID);


	Concurrency(int code)
	{
		code_ = code;
	}


	/**
	 * Returns the JDBC constant associated with the enum.
	 */
	@Override public int getCode()
	{
		return code_;
	}


	private final int code_;
}
