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


import java.sql.*;
import org.jdbx.function.CheckedConsumer;
import org.jdbx.function.CheckedFunction;


/**
 * QueryResult is a builder class to run a SQL query and extract values from the JDBC result set.
 * Note that actual query on JDBC level may not be run until
 * a terminal operation of the builder method chain is executed. 
 */
public abstract class QueryResult extends StmtRunnable
{
	/**
	 * Returns a QueryResult object for an existing ResultSet.
	 * @param resultSet a ResultSet
	 * @return the QueryResult
	 */
	public static QueryResult of(ResultSet resultSet)
	{
		return new ResultSetQResult(resultSet);
	}


	/**
	 * Skips the first n rows from the result.
	 * @param n a number of rows
	 * @return this
	 */
	public QueryResult skip(int n)
	{
		skip_ = Math.max(0, n);
		return this;
	}


	int getSkip()
	{
		return skip_;
	}


	boolean applySkip(QueryCursor cursor) throws SQLException
	{
		return (skip_ <= 0) || cursor.skip(skip_);
	}


	/**
	 * Executes the query and returns the result in form of a QueryCursor.
	 * You should actively close the cursor once it is no longer used. 
	 * @return the cursor
	 */
	public QueryCursor cursor() throws JdbxException
	{
		try
		{
			ResultSet resultSet = runQuery();
			return new QueryCursor(resultSet);
		}
		catch (Exception e)
		{
			throw JdbxException.of(e);
		}
	}


	/**
	 * Returns a builder to access the first row of the result set.
	 * @return the builder
	 */
	public QResultOneRow row()
	{
		return new QResultOneRow(this);
	}


	/**
	 * Returns a builder to access all rows of the result set.
	 * @return the builder
	 */
	public QResultRows rows()
	{
		return new QResultRows(this);
	}
	
	
	/**
	 * Executes the query and passes the result cursor to the consumer.
	 * @param consumer a result consumer
	 */
	public void read(CheckedConsumer<QueryCursor> consumer) throws JdbxException
	{
		read(result -> {
			consumer.accept(result);
			return null;
		});
	}


	/**
	 * Executes the query and passes the result cursor to the reader.
	 * @param reader a reader which can return a value from a result cursor
	 * @param <T> the type of the value returned by the reader
	 * @return the value returned by the reader.
	 */
	public <T> T read(CheckedFunction<QueryCursor,T> reader) throws JdbxException
	{
		return read(skip_ > 0, reader);
	}



	/**
	 * Implementation method to read the result using a reader function.
	 * We allow callers of this method to decide if they want to apply skipping themselves:
	 * If skipping is done here, the reader may invoke QueryCursor.next()
	 * after an unsuccessful prior call to this method - unfortunately in this case
	 * a JDBC driver is allowed to throw an exception instead of returning false.
	 */
	<R> R read(boolean applySkip, CheckedFunction<QueryCursor,R> reader) throws JdbxException
	{
		Check.notNull(reader, "reader");

		Exception e1 = null, e2 = null;
		R returnValue = null;

		try (QueryCursor cursor = new QueryCursor(runQuery()))
		{
			if (applySkip)
				applySkip(cursor);
			returnValue = reader.apply(cursor);
		}
		catch (Exception e)
		{
			e1 = e;
		}
		finally
		{
			try
			{
				cleanup();
			}
			catch (Exception e)
			{
				e2 = e;
			}
		}

		if ((e1 != null) || (e2 != null))
			throw JdbxException.combine(e1, e2);
		return returnValue;
	}


	protected final ResultSet runQuery() throws Exception
	{
		registerRun();
		return runQueryImpl();
	}


	/**
	 * Must only be called by {@link #run()}
	 */
	protected abstract ResultSet runQueryImpl() throws Exception;


	protected abstract void cleanup() throws Exception;


	@Override protected final String getRunnableType()
	{
		return "QueryResult";
	}


	private int skip_;
}

