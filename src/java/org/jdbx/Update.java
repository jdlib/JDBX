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
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.util.List;
import org.jdbx.function.GetReturnCols;


/**
 * Update is a builder class to configure and run a DML or DDL command
 * which updates or changes the database.
 */
public abstract class Update extends StmtRunnable
{
	/**
	 * Instructs the Update to retrieve the update count as long value.
	 * The large count can be retrieved from the result by calling {@link UpdateResult#largeCount()}
	 * @see Statement#executeUpdate(String)
	 * @see Statement#executeLargeUpdate(String)
	 * @return this
	 */
	public Update returnLargeCount()
	{
		large_ = true;
		return this;
	}


	/**
	 * Runs the update command.
	 * @return the update result. Its value is null.
	 * @see Statement#executeUpdate(String)
	 * @see PreparedStatement#executeUpdate()
	 */
	public UpdateResult<Void> run() throws JdbxException
	{
		long count = 0L;
		Exception ex = null;
		try
		{
			count = runUpdate(large_);
		}
		catch(Exception e)
		{
			ex = e;
		}
		finally
		{
			cleanup(ex);
		}
		return new UpdateResult<>(count);
	}


	/**
	 * Runs the command and returns the value of the first returned column.
	 * @param colType the type of the column
	 * @param <V> the type of the column
	 * @return an UpdateResult holding the update count and the column value
	 */
	public <V> UpdateResult<V> runGetCol(Class<V> colType) throws JdbxException
	{
		Check.notNull(colType, "colType");
		return runGetCols((c,q) -> q.row().col().get(colType));
	}


	/**
	 * Runs the command and returns a list of auto generated keys.
	 * @param keyType the type of the generated keys
	 * @param <V> the type of the value returned by the AutoKeysReader
	 * @return an UpdateResult holding the update count and the key list
	 */
	public <V> UpdateResult<List<V>> runGetCols(Class<V> colType) throws JdbxException
	{
		Check.notNull(colType, "colType");
		return runGetCols((c,q) ->  q.rows().col().get(colType));
	}


	/**
	 * Runs the command and passes the result-set of the generated keys to the reader.
	 * @param reader a reader functions. It read the keys from the update result-set and returns it as object of type V.
	 * @param <V> the type of the value returned by the AutoKeysReader
	 * @return an UpdateResult holding the update count and the column values
	 */
	public <V> UpdateResult<V> runGetCols(GetReturnCols<V> reader) throws JdbxException
	{
		Check.notNull(reader, "reader");
		UpdateResult<V> result = null;

		Exception ex = null;
		try
		{
			long count = (int)runUpdate(false);
			try (ResultSet rs = getGeneratedKeys()) { 
				V value = reader.read(count, QueryResult.of(rs));
				result  = new UpdateResult<>(count, value);
			}
		}
		catch (Exception e)
		{
			ex = e;
		}
		finally
		{
			cleanup(ex);
		}
		return result;
	}


	protected final long runUpdate(boolean large) throws Exception
	{
		registerRun();
		return runUpdateImpl(large);
	}


	/**
	 * May only be called by {@link #runUpdate()}
	 */
	protected abstract long runUpdateImpl(boolean large) throws Exception;


	/**
	 * May only be called by {@link #runGetCols()}
	 */
	protected abstract ResultSet getGeneratedKeys() throws Exception;


	private void cleanup(Exception e1) throws JdbxException
	{
		Exception e2 = null;
		try
		{
			cleanup();
		}
		catch (Exception e)
		{
			e2 = e;
		}
		if ((e1 != null) || (e2 != null))
			throw JdbxException.combine(e1, e2);
	}


	/**
	 * Release any JDBC resources opened by the implementation.
	 */
	protected abstract void cleanup() throws Exception;


	@Override protected final String getRunnableType()
	{
		return "Update";
	}


	private boolean large_;
}
