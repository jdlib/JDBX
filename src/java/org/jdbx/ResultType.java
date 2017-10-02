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
 * ResultType is an Enum for the JDBC constants which define the ResultSet type.
 */
public enum ResultType implements JdbcEnum
{
    /**
     * The enum indicating the type for a <code>QueryResult</code> object
     * whose cursor may move only forward.
     * @since 1.2
     */
	FORWARD_ONLY(ResultSet.TYPE_FORWARD_ONLY),
	
    /**
     * The enum indicating the type for a <code>QueryResult</code> object
     * that is scrollable but generally not sensitive to changes to the data
     * that underlies the <code>QueryResult</code>.
     * @see ResultSet#TYPE_SCROLL_INSENSITIVE
     */
	SCROLL_INSENSITIVE(ResultSet.TYPE_SCROLL_INSENSITIVE),

	/**
     * The enum indicating the type for a <code>QueryResult</code> object
     * that is scrollable and generally sensitive to changes to the data
     * that underlies the <code>QueryResult</code>.
     * @see ResultSet#TYPE_SCROLL_SENSITIVE
     */
	SCROLL_SENSITIVE(ResultSet.TYPE_SCROLL_SENSITIVE),
	
	INVALID(-1);


	/**
	 * Maps codes to enums.
	 */
	public static final Map<ResultType> MAP = new Map<>(ResultType.class, INVALID);


	ResultType(int code)
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
