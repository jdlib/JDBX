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
 * Update is a builder class to configure and run a SQL or DDL command
 * which updates or changes the database.
 */
public abstract class Update extends StmtRunnable
{
	/**
	 * A functional interface for a service which reads auto generated keys
	 * when executing an update command.
	 */
	@FunctionalInterface
	public static interface AutoKeysReader<V>
	{
		/**
		 * Reads the auto generated keys and stores the result
		 * into the value of the UpdateResult.
		 * @param generatedKeys a result-set containing the auto generated keys
		 * @param result the UpdateResult storing the update count and the value describing the auto generated keys.
		 * @see Statement#getGeneratedKeys()
		 * @throws Exception if an error occurs
		 */
		public void read(ResultSet generatedKeys, UpdateResult<V> result) throws Exception;
	}


	/**
	 * Runs the update command.
	 * @return the number of affected records
	 * @see Statement#executeUpdate(String)
	 * @see PreparedStatement#executeUpdate()
	 */
	public int run() throws JdbxException
	{
		return (int)run(false);
	}


	/**
	 * Runs the command.
	 * @return the number of affected records as long value
	 * @see Statement#executeLargeUpdate(String)
	 * @see PreparedStatement#executeLargeUpdate()
	 */
	public long runLarge() throws JdbxException
	{
		return run(true);
	}


	/**
	 * Common implementation for run() and runLarge().
	 */
	private long run(boolean large) throws JdbxException
	{
		long count = 0L;
		Exception ex = null;
		try
		{
			count = runUpdate(large);
		}
		catch(Exception e)
		{
			ex = e;
		}
		finally
		{
			cleanup(ex);
		}
		return count;
	}


	/**
	 * Runs the command and returns a single auto generated key.
	 * @param keyType the type of the generated key
	 * @param <V> the type of the value returned by the AutoKeysReader
	 * @return an UpdateResult holding the update count and the key value
	 */
	public <V> UpdateResult<V> runGetAutoKey(Class<V> keyType) throws JdbxException
	{
		Check.notNull(keyType, "keyType");
		return runGetAutoKeys((rs,r) -> {
			if (rs.next())
				r.value = rs.getObject(1, keyType);
		});
	}


	/**
	 * Runs the command and returns a list of auto generated keys.
	 * @param keyType the type of the generated keys
	 * @param <V> the type of the value returned by the AutoKeysReader
	 * @return an UpdateResult holding the update count and the key list
	 */
	public <V> UpdateResult<List<V>> runGetAutoKeys(Class<V> keyType) throws JdbxException
	{
		Check.notNull(keyType, "keyType");
		return runGetAutoKeys((rs,r) -> {
			r.value = QueryResult.of(rs).rows().col().get(keyType);
		});
	}


	/**
	 * Runs the command and passes the result-set of the generated keys to the reader.
	 * @param reader a AutoKeysReader. It should read the keys and place it in appropriate form into the result object.
	 * @param <V> the type of the value returned by the AutoKeysReader
	 * @return an UpdateResult holding the update count and the key list
	 */
	public <V> UpdateResult<V> runGetAutoKeys(AutoKeysReader<V> reader) throws JdbxException
	{
		Check.notNull(reader, "reader");
		UpdateResult<V> result = new UpdateResult<>();

		Exception ex = null;
		try
		{
			result.count = (int)runUpdate(false);
			reader.read(getGeneratedKeys(), result);
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
}

