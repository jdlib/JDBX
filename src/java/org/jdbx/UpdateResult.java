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


import java.util.Objects;


/**
 * UpdateResult is returned by various {@link Update} methods.
 * It stores the update count and a value representing returned columns.
 * @param <V> the type of the result value.
 */
public class UpdateResult<V>
{
	/**
	 * Creates a new empty UpdateResult.
	 */
	public UpdateResult()
	{
		this(0);
	}


	/**
	 * Creates a new UpdateResult and initializes the count.
	 * @param count the update count
	 */
	public UpdateResult(long count)
	{
		this(count, null);
	}


	/**
	 * Creates a new UpdateResult and initializes the count and value.
	 * @param count the update count
	 * @param value the value
	 */
	public UpdateResult(long count, V value)
	{
		count_ = count;
		value_ = value;
	}


	/**
	 * Returns the number of affected records.
	 */
	public int count()
	{
		return (int)count_;
	}
	
	
	/**
	 * Returns the number of affected records as long.
	 * Unless you actively activate large updates, the returned value will also 
	 * be in the int range.
	 * @see Update#large()
	 */
	public long largeCount()
	{
		return (int)count_;
	}

	
	/**
	 * Checks if the count stored in the result matches the expected count.
	 * @param expected the expected count
	 * @return this
	 * @throws JdbxException if the counts do not match
	 */
	public UpdateResult<V> requireCount(long expected) throws JdbxException
	{
		if (count_ != expected)
			throw JdbxException.invalidResult("expected update count " + expected + ", but was " + count_);
		return this;
	}


	/**
	 * Checks if the count stored in the result falls in the expected count interval.
	 * @param minExpected the minimum expected count
	 * @param maxExpected the maximum expected count
	 * @return this
	 * @throws JdbxException if the counts do not match
	 */
	public UpdateResult<V> requireCount(int minExpected, int maxExpected) throws JdbxException
	{
		if ((count_ < minExpected) || (count_ > maxExpected))
			throw JdbxException.invalidResult("expected update count in [" + minExpected + ',' + maxExpected + "], but was " + count_);
		return this;
	}

	
	/**
	 * Returns the value of the update result.
	 */
	public V value()
	{
		return value_;
	}
	
	
	/**
	 * Checks that the value is not null
	 * @return the {@link #value value} of the update result
	 * @throws JdbxException if the value is null
	 */
	public V requireValue() throws JdbxException
	{
		if (value_ == null)
			throw JdbxException.invalidResult("expected non-null value");
		return value_;
	}


	/**
	 * Checks that the actual result value equals the expected value.
	 * @throws JdbxException if the actual and expected value are not equal
	 */
	public UpdateResult<V> requireValue(V expected) throws JdbxException
	{
		if (!Objects.equals(expected, value_))
			throw JdbxException.invalidResult("expected value '" + expected + "' but was '" + value_ + "'");
		return this;
	}

	
	/**
	 * The number of records that were affected by the SQL command.
	 */
	private final long count_;

	/**
	 * The value created from the returned column values.
	 */
	private final V value_;
}
