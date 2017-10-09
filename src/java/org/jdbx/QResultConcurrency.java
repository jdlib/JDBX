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
 * QResultConcurrency is an Enum for the JDBC constants ResultSet.CONCUR_*.
 * Given a JDBX statement object, you can change the result concurrency as follows:
 * <p><code>stmt.options().resultConcurrency(QResultConcurrency.nnn)</code>
 *  
 * @see InitBase#resultConcurrency(QResultConcurrency)
 * @see Stmt#options() 
 */
public enum QResultConcurrency implements JdbcEnum
{
	READ_ONLY(ResultSet.CONCUR_READ_ONLY),
	CONCUR_UPDATABLE(ResultSet.CONCUR_UPDATABLE),
	INVALID(-1);


	/**
	 * Maps codes to enums.
	 */
	public static final Map<QResultConcurrency> MAP = new Map<>(QResultConcurrency.class, INVALID);


	QResultConcurrency(int code)
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


	private int code_;
}
