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
import java.util.List;
import org.jdbx.function.GetReturnCols;


/**
 * Batch represents a list of batched SQL commands.
 */
public abstract class Batch
{
	/**
	 * Clears the batch.
	 * @return this
	 * @see Statement#clearBatch()
	 */
	public Batch clear() throws JdbxException
	{
		stmt().call(Statement::clearBatch);
		return this;
	}


	/**
	 * Executes the batched SQL commands.
	 * @return a BatchResult
	 * @see Statement#executeBatch()
	 */
	public BatchResult<Void> run() throws JdbxException
	{
		return new BatchResult<>(runImpl());
	}


	/**
	 * Runs the batched SQL commands and returns the auto-generated keys.
	 * We assume that the batched commands contain a single column for which keys are generated.
	 * @param colType class of the column type
	 * @param <V> the column type
	 * @return a BatchResult containing the auto-generated keys
	 */
	public <V> BatchResult<List<V>> runGetCols(Class<V> colType) throws JdbxException
	{
		Check.notNull(colType, "colType");
		return runGetCols((c,q) ->  q.rows().col().get(colType));
	}


	/**
	 * Runs the command and passes the result-set of the auto-generated keys to the reader.
	 * @param reader a reader which receives the result-set, 
	 * 		extracts the generated keys and returns them as object of type V.
	 * @param <V> the type of the value returned by the reader
	 * @return a BatchResult containing the auto-generated keys
	 */
	public <V> BatchResult<V> runGetCols(GetReturnCols<V> reader) throws JdbxException
	{
		Check.notNull(reader, "reader");

		try
		{
			int[] counts = runImpl();
			try (ResultSet rs = stmt().getJdbcStmt().getGeneratedKeys()) 
			{ 
				V value = reader.read(counts != null ? counts.length : 0, QueryResult.of(rs));
				return new BatchResult<>(value, counts);
			}
		}
		catch (Exception e)
		{
			throw JdbxException.of(e);
		}
	}


	private int[] runImpl()
	{
		return stmt().get(Statement::executeBatch);
	}


	protected abstract Stmt stmt();
}
