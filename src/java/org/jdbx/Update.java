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


/**
 * Update is a builder class to configure and run a DML or DDL command
 * which updates or changes the database.
 */
public abstract class Update extends StmtRunnable
{
	/**
	 * A functional interface for a service which reads the returned column value
	 * when executing an update command.
	 */
	@FunctionalInterface
	public static interface AutoKeysReader<V>
	{
		/**
		 * Reads the returned column values as a value of type V 
		 * @param updateCount the update count
		 * @param result a Query representing the returned column values
		 * @return the value representing the returned column values 
		 * @see Statement#getGeneratedKeys()
		 * @throws Exception if an error occurs
		 */
		public V read(long updateCount, Query result) throws Exception;
	}


	/**
	 * Instructs the Update to retrieve the update count as long value.
	 * The count is always reported as {@link UpdateResult#count() long value} but
	 * by default is on the JDBC level retrieved as int value.  
	 * @see Statement#executeUpdate(String)
	 * @see Statement#executeLargeUpdate(String)
	 * @return this.
	 */
	public Update large()
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
	 * Runs the command and returns a single auto generated key.
	 * @param keyType the type of the generated key
	 * @param <V> the type of the value returned by the AutoKeysReader
	 * @return an UpdateResult holding the update count and the key value
	 */
	public <V> UpdateResult<V> runGetCol(Class<V> keyType) throws JdbxException
	{
		Check.notNull(keyType, "keyType");
		return runGetCols((c,q) -> q.row().col().get(keyType));
	}


	/**
	 * Runs the command and returns a list of auto generated keys.
	 * @param keyType the type of the generated keys
	 * @param <V> the type of the value returned by the AutoKeysReader
	 * @return an UpdateResult holding the update count and the key list
	 */
	public <V> UpdateResult<List<V>> runGetCols(Class<V> keyType) throws JdbxException
	{
		Check.notNull(keyType, "keyType");
		return runGetCols((c,q) ->  q.rows().col().get(keyType));
	}


	/**
	 * Runs the command and passes the result-set of the generated keys to the reader.
	 * @param reader a AutoKeysReader. It should read the keys and place it in appropriate form into the result object.
	 * @param <V> the type of the value returned by the AutoKeysReader
	 * @return an UpdateResult holding the update count and the key list
	 */
	public <V> UpdateResult<V> runGetCols(AutoKeysReader<V> reader) throws JdbxException
	{
		Check.notNull(reader, "reader");
		UpdateResult<V> result = new UpdateResult<>();

		Exception ex = null;
		try
		{
			long count = (int)runUpdate(false);
			V value;
			try (ResultSet rs = getGeneratedKeys()) { 
				value = reader.read(count, Query.of(rs));
			}
			return new UpdateResult<>(count, value);
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


	protected abstract ResultSet getGeneratedKeys() throws Exception;


	protected abstract void cleanup() throws Exception;


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


	@Override protected final String getRunnableType()
	{
		return "Update";
	}


	private boolean large_;
}
