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
import java.util.Map;
import org.jdbx.function.CheckedRunnable;


/**
 * ResultIterator is a lightweight wrapper around a ResultSet.
 * It allows easy reading of subsequent columns - it maintains
 * a column index, and at each get operation it reads the value
 * from the ResultSet and then increments the index.
 */
public class ResultIterator extends GetResult implements AutoCloseable
{
	/**
	 * Returns a ResultIterator for the result
	 * which does not close the Result when it is closed.
	 * Use this factory method to avoid compiler warnings
	 * for unclosed resources when you don't want to close the ResultSet.
	 * @param result the result which is iterated
	 * @return the new ResultIterator
	 */
	@SuppressWarnings("resource")
	public static ResultIterator of(QueryResult result)
	{
		return new ResultIterator(result).setCloseResult(false);
	}


	/**
	 * Returns a ResultIterator for the ResultSet
	 * which does not close the ResultSet when it is closed.
	 * Use this factory method when you want to avoid compiler warnings
	 * for unclosed resources.
	 * @param resultSet the resultSet which is iterated
	 * @return the new ResultIterator
	 */
	@SuppressWarnings("resource")
	public static ResultIterator of(ResultSet resultSet)
	{
		return new ResultIterator(resultSet).setCloseResult(false);
	}


	/**
	 * Creates a ResultIterator for a QueryResult.
	 * @param result the result
	 */
	public ResultIterator(QueryResult result)
	{
		resultSet_ = Check.notNull(result, "result").getJdbcResult();
	}


	/**
	 * Creates a ResultIterator for a ResultSet.
	 * @param resultSet the ResultSet
	 */
	public ResultIterator(ResultSet resultSet)
	{
		resultSet_ = Check.notNull(resultSet, "resultSet");
	}


	/**
	 * Returns the internal ResultSet.
	 * @return the result set
	 */
	public ResultSet getResultSet()
	{
		return resultSet_;
	}


	/**
	 * Advances the result set to the next row.
	 * @return true if the new current row is valid
	 */
	public boolean nextRow() throws JdbException
	{
		resetIndex();
		try
		{
			return resultSet_.next();
		}
		catch (Exception e)
		{
			throw JdbException.of(e);
		}
	}


	/**
	 * Returns the current column index.
	 * @return the index
	 */
	public int getIndex()
	{
		return colIndex_;
	}


	/**
	 * Sets the current column index.
	 * @param index the index
	 */
	public void setIndex(int index)
	{
		colIndex_ = Check.index(index);
	}


	/**
	 * Reset the current column index to 1.
	 */
	public void resetIndex()
	{
		colIndex_ = 1;
	}


	/**
	 * Increases the column index by 1.
	 */
	public void skipCol()
	{
		colIndex_++;
	}


	/**
	 * Increases the column index by the given delta.
	 * @param delta the delta
	 */
	public void skipCols(int delta)
	{
		setIndex(colIndex_ + delta);
	}


	/**
	 * Returns the value as object of the given type.
	 */
	@Override public <T> T get(Class<T> type) throws JdbException
	{
		Check.notNull(type, "type");
		try
		{
			return resultSet_.getObject(colIndex_++, type);
		}
		catch (Exception e)
		{
			throw JdbException.of(e);
		}
	}


	/**
	 * Returns the value as object.
	 * @param map a map that contains the mapping from SQL type names to Java classes
	 */
	@Override public Object get(Map<String,Class<?>> map) throws JdbException
	{
		Check.notNull(map, "map");
		try
		{
			return resultSet_.getObject(colIndex_++, map);
		}
		catch (Exception e)
		{
			throw JdbException.of(e);
		}
	}


	@Override <T> T get(GetAccessors<T> accessors) throws JdbException
	{
		try
		{
			return accessors.resultForIndex.get(resultSet_, colIndex_++);
		}
		catch (Exception e)
		{
			throw JdbException.of(e);
		}
	}


	/**
	 * Returns if this Iterator closes the ResultSet when itself is closed.
	 * @return the flag
	 */
	public boolean isCloseResult()
	{
		return closeResult_;
	}


	/**
	 * Sets if this Iterator closes the ResultSet when itself is closed.
	 * @param flag the close flag
	 * @return this
	 */
	public ResultIterator setCloseResult(boolean flag)
	{
		closeResult_ = flag;
		return this;
	}


	/**
	 * Closes the ResultIterator.
	 * Depending on the {@link #isCloseResult() close flag} it also closes the ResultSet.
	 */
	@Override public void close() throws JdbException
	{
		if (closeResult_)
			CheckedRunnable.unchecked(resultSet_::close);
	}


	private int colIndex_ = 1;
	private ResultSet resultSet_;
	private boolean closeResult_;
}
