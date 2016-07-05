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
 * UpdateResult is returned by various {@link Update} methods.
 * It stores the update count and the value of 
 * automatically generated keys.
 * @param <V> the type of the result value.
 */
public class UpdateResult<V>
{
	/**
	 * Creates a new empty UpdateResult.
	 */
	public UpdateResult()
	{
	}


	/**
	 * Creates a new UpdateResult and initializes the count.
	 * @param count the update count
	 */
	public UpdateResult(int count)
	{
		this.count = count;
	}


	/**
	 * Creates a new UpdateResult and initializes the count and value.
	 * @param count the update count
	 * @param value the value
	 */
	public UpdateResult(int count, V value)
	{
		this.count = count;
		this.value = value;
	}


	/**
	 * Checks if the count stored in the result matches the expected count.
	 * @param expectedCount the expected count
	 * @return this
	 * @throws JdbException if the counts do not match
	 */
	public UpdateResult<V> checkCount(int expectedCount) throws JdbException
	{
		if (this.count != expectedCount)
			throw JdbException.invalidResult("expected update count " + expectedCount + ", but was " + this.count);
		return this;
	}


	/**
	 * Checks that the value is not null
	 * @return the value
	 * @throws JdbException if the value is null
	 */
	public V checkHasValue() throws JdbException
	{
		if (this.value == null)
			throw JdbException.invalidResult("expected non-null value");
		return this.value;
	}


	/**
	 * The number of records that were affected by the SQL command.
	 */
	public int count;

	/**
	 * The value created from the auto generated keys result set.
	 */
	public V value;
}


