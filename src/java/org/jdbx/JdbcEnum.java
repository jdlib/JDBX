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


interface JdbcEnum
{
	/**
	 * Returns the value of the corresponding JDBC constant.
	 * @return the constant value
	 */
	public int getCode();


	/**
	 * Returns if this enum is valid.
	 * @return the invalid flag
	 */
	default boolean isInvalid()
	{
		return getCode() < 0;
	}


	/**
	 * Maps between JDBC constants and enum values.
	 */
	public static class Map<E extends Enum<E> & JdbcEnum>
	{
		public Map(Class<E> type, E unknown)
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
}
