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


import org.jdbx.JdbcEnum.Map;


/**
 * Maps between JDBC constants to enum values.
 */
class JdbcEnumMap<E extends Enum<E> & JdbcEnum>
{
	/**
	 * A JdbcEnumMap for Concurrency.
	 */
	public static final JdbcEnumMap<Concurrency> CONCURRENCY = new JdbcEnumMap<>(Concurrency.class, Concurrency.INVALID);


	/**
	 * A JdbcEnumMap for FetchDirection.
	 */
	public static final Map<FetchDirection> FETCH_DIRECTION = new Map<>(FetchDirection.class, FetchDirection.INVALID);


	/**
	 * A JdbcEnumMap for Holdability.
	 */
	public static final Map<Holdability> HOLDABILITY = new Map<>(Holdability.class, Holdability.INVALID);


	/**
	 * A JdbcEnumMap for ResultType.
	 */
	public static final Map<ResultType> RESULT_TYPE = new Map<>(ResultType.class, ResultType.INVALID);
	

	public JdbcEnumMap(Class<E> type, E unknown)
	{
		values_ 	= type.getEnumConstants();
		unknown_ 	= unknown;
	}


	public E forCode(int code)
	{
		for (E e : values_)
		{
			if (e.getCode() == code)
				return e;
		}
		return unknown_;
	}


	public E forCode(Integer code)
	{
		return code != null ? forCode(code.intValue()) : unknown_;
	}


	private final E[] values_;
	private final E unknown_;
}
