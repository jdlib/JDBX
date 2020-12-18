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


/**
 * Check defines helper methods for argument checking.
 */
class Check
{
	/**
	 * Checks that an object is not null.
	 * @param object the object
	 * @param what describes the object.
	 * @return the object
	 * @exception IllegalArgumentException if the object is null.
	 */
	public static <T> T notNull(T object, String what)
	{
		if (object == null)
			throw new IllegalArgumentException(what + " is null");
		return object;
	}


	/**
	 * Checks that a JdbcEnum is not null and valid.
	 * @exception IllegalArgumentException if the object is null or invalid.
	 */
	public static <V extends JdbcEnum> V valid(Class<V> type, V value)
	{
		if ((value == null) || value.isInvalid())
			throw new IllegalArgumentException("not a valid " + type.getSimpleName() + ": " + value);
		return value;
	}


	/**
	 * Checks that a column or parameter number is >= 1.
	 * @param number the number
	 * @return the number
	 * @exception IllegalArgumentException if the number is < 1.
	 */
	public static int number(int number)
	{
		if (number < 1)
			throw new IllegalArgumentException("number must be >= 1, is " + number);
		return number;
	}


	/**
	 * Checks that a column or parameter name is not null.
	 * @param name the name
	 * @return the name
	 */
	public static String name(String name)
	{
		return notNull(name, "name");
	}
}
