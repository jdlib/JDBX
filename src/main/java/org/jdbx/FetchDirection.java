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
 * An enum for the ResultSet.FETCH_* constants.
 */
public enum FetchDirection implements JdbcEnum
{
	FORWARD(ResultSet.FETCH_FORWARD),
	REVERSE(ResultSet.FETCH_REVERSE),
	UNKNOWN(ResultSet.FETCH_UNKNOWN),
	INVALID(-1);

	
	FetchDirection(int code)
	{
		code_ = code;
	}


	@Override public int getCode()
	{
		return code_;
	}


	private final int code_;
}
